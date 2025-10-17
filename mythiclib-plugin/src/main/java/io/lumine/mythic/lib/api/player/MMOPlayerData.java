package io.lumine.mythic.lib.api.player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.comp.profile.ProfileMode;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.listener.PlayerListener;
import io.lumine.mythic.lib.message.actionbar.ActionBarHandler;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;
import io.lumine.mythic.lib.player.cooldown.CooldownType;
import io.lumine.mythic.lib.player.particle.ParticleEffectMap;
import io.lumine.mythic.lib.player.permission.PermissionMap;
import io.lumine.mythic.lib.player.potion.PermanentPotionEffectMap;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.player.skill.PassiveSkillMap;
import io.lumine.mythic.lib.player.skillmod.SkillModifierMap;
import io.lumine.mythic.lib.profile.ProfileSession;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class MMOPlayerData {

    /**
     * This fixes a very specific issue with recent versions of Spigot (1.19+).
     * Bukkit interact events are called when a player drops an item. In specific
     * scenarios, dropping an item triggers left-click abilities, which sucks.
     */
    public long lastDrop;

    // Information shared across all sessions
    private final ActionBarHandler actionBar = new ActionBarHandler(this);
    private final List<TemporaryHandler> tempHandlers = new ArrayList<>();
    private final Map<String, Object> externalData = new HashMap<>();
    private final VariableList variableList = new VariableList(VariableScope.PLAYER);

    /**
     * @param player Player logging in. Original UUID is taken from that player
     */
    private MMOPlayerData(@NotNull Player player) {
        this.lookup = false;
        this.entityId = Objects.requireNonNull(player, "Player cannot be null").getUniqueId();
        this.officialId = player.getUniqueId();
    }

    /**
     * MMOPlayerData object which can be used to lookup data in any plugin.
     * This object will have no effect whatsoever on databases.
     *
     * @param uniqueId Unique ID to lookup
     * @see #isLookup()
     */
    public MMOPlayerData(@NotNull UUID uniqueId) {
        this.lookup = true;
        this.entityId = Objects.requireNonNull(uniqueId, "UUID cannot be null");
        this.officialId = uniqueId;
    }

    /**
     * For backwards compatibility, this returns the same value as getPlayer().getUniqueId().
     * <p>
     * Developers are encouraged to use this method over other getters.
     *
     * @return The Player entity unique ID. This may differ from the current player's
     *         profile ID depending on the profile provider being used on the server.
     * @see #getProfileId()
     * @see #getOfficialId()
     * @see SynchronizedDataHolder#getEffectiveId()
     */
    @NotNull
    public UUID getUniqueId() {
        return entityId;
    }

    //region Player session

    /**
     * UUID of the Player entity. It has to be one of the
     * two among the profile and official player ID.
     */
    @NotNull
    private final UUID entityId;

    @Nullable
    private Player player;
    @Nullable
    private String lastPlayerName;

    private final boolean lookup;

    /**
     * Last time the player either logged in or logged out.
     */
    private long lastLogActivity;

    public boolean isLookup() {
        return lookup;
    }

    /**
     * @return The last time, in millis, the player logged in or out
     */
    public long getLastLogActivity() {
        return lastLogActivity;
    }

    @NotNull
    public String getPlayerName() {
        return Objects.requireNonNull(lastPlayerName, "Player object never provided");
    }

    public boolean isTimedOut() {
        this.savedProfileSessions.values().removeIf(ProfileSession::isTimedOut);
        return !isOnline() && this.savedProfileSessions.isEmpty();
    }

    /**
     * This method simply checks if the cached Player instance is null
     * because MythicLib uncaches it when the player leaves for memory purposes.
     *
     * @return If the player is currently online.
     */
    public boolean isOnline() {
        return player != null;
    }

    /**
     * Throws an exception if the player is currently not online
     * OR if the Player object has not been provided yet.
     * <p>
     * MythicLib updates the Player instance on event priority LOW
     * using {@link PlayerJoinEvent} in class {@link PlayerListener}
     *
     * @return Returns the corresponding Player instance.
     */
    @NotNull
    public Player getPlayer() {
        return Objects.requireNonNull(player, "Player is offline");
    }

    /**
     * Caches a new Player instance and refreshes the last log activity.
     * Provided player can be null if the player is disconnecting
     *
     * @param player Player instance to cache (null if logging off)
     */
    public void updatePlayer(@Nullable Player player) {
        this.player = player;
        if (player != null) {
            this.lastPlayerName = player.getName();
            if (MythicLib.plugin.getProfileMode() == ProfileMode.NONE) chooseProfile(null);
        }
        this.lastLogActivity = System.currentTimeMillis();
    }

    //endregion

    //region Profile session

    @NotNull
    private UUID officialId;

    @Nullable
    private ProfileSession profileSession;

    /**
     * Watch out for the `null` key for when no profile is chosen!
     */
    private final Map<UUID, ProfileSession> savedProfileSessions = new HashMap<>();

    /**
     * This method will throw an error if the player hasn't chosen a profile
     * yet. It is also guaranteed to throw an error if proxy-based profiles
     * are not enabled.
     * <p>
     * Developers are discouraged from using this method.
     *
     * @return The official Mojang/Microsoft account UUID
     */
    @NotNull
    public UUID getOfficialId() {
        return officialId;
    }

    public void setOfficialId(@NotNull UUID officialId) {
        Validate.isTrue(MythicLib.plugin.getProfileMode() == ProfileMode.PROXY, "Player official IDs can only change in proxy profile mode");
        this.officialId = Objects.requireNonNull(officialId, "Official ID cannot be null");
    }

    /**
     * If support for the Profile API is enabled, this returns the current
     * player's profile ID. This method will throw an error if they have
     * not chosen a profile yet.
     *
     * @return The UUID of the current player's profile
     */
    @NotNull
    public UUID getProfileId() {
        return Objects.requireNonNull(profileSession, "No profile chosen").getProfileId();
    }

    /**
     * Saves the current profile session temporarily. If the player leaves this
     * session for too long, it will eventually be discarded.
     */
    public void saveCurrentProfileSession() {
        Validate.notNull(this.profileSession, "No profile session to save");
        Validate.isTrue(this.profileSession.isDead(), "Current profile session is still alive");

        var mapKey = this.profileSession.hasProfile() ? this.profileSession.getProfileId() : null;
        this.savedProfileSessions.put(mapKey, this.profileSession);
        this.profileSession = null;
    }

    public boolean hasProfile() {
        return profileSession != null && profileSession.hasProfile();
    }

    public boolean hasProfileSession() {
        return profileSession != null;
    }

    /**
     * Looks for previous profile sessions with the same profile ID. If one
     * is found, restore the previous session with all its data. If none is
     * found, create a new profile session and open it.
     *
     * @param profileId ID of profile opened, or null if no profile
     */
    public void chooseProfile(@Nullable UUID profileId) {
        Validate.isTrue(!lookup, "Cannot choose a profile in lookup mode");
        Validate.isTrue(this.profileSession == null, "Previous profile session is not dead");

        // Restore previous session
        final ProfileSession restored = savedProfileSessions.remove(profileId);
        if (restored != null) {
            this.profileSession = restored;
            this.profileSession.reset();
            return;
        }

        // Create new session
        this.profileSession = new ProfileSession(this, profileId);
    }

    @NotNull
    public ProfileSession getProfileSession() {
        return Objects.requireNonNull(profileSession, "No profile chosen");
    }

    public void addTemporaryHandler(@NotNull TemporaryHandler handler) {
        Validate.notNull(handler, "Handler cannot be null");
        tempHandlers.add(handler);
    }

    public void removeTemporaryHandler(@NotNull TemporaryHandler handler) {
        Validate.notNull(tempHandlers.remove(handler), "Handler is not registered");
    }

    public void clearTemporaryHandlers() {
        tempHandlers.forEach(handler -> handler.closeNow(true));
        tempHandlers.clear();
    }

    public boolean isPlaying() {
        return profileSession != null && profileSession.isReady();
    }

    //endregion

    //region Session data getters

    private final ProfileSession fallbackProfileSession = new ProfileSession(this, UUID.randomUUID());

    @NotNull
    protected ProfileSession safePlayerSession() {
        return Objects.requireNonNullElse(profileSession, fallbackProfileSession);
    }

    /**
     * @return The player's stat map which can be used by any other plugins to
     *         apply stat modifiers to ANY MMOItems/MMOCore/external stats,
     *         calculate stat values, etc.
     */
    @NotNull
    public StatMap getStatMap() {
        return safePlayerSession().getStatMap();
    }

    /**
     * Cooldown maps centralize cooldowns in MythicLib for easier use.
     * Can be used for item cooldows, item abilities, MMOCore player
     * skills or any other external plugin
     *
     * @return The main player's cooldown map
     */
    @NotNull
    public CooldownMap getCooldownMap() {
        return safePlayerSession().getCooldownMap();
    }

    /**
     * @return The player's skill modifier map. This map applies modifications
     *         to numerical skill parameters (damage, cooldown...)
     */
    @NotNull
    public SkillModifierMap getSkillModifierMap() {
        return safePlayerSession().getSkillModifierMap();
    }

    @NotNull
    public PermanentPotionEffectMap getPermanentEffectMap() {
        return safePlayerSession().getPermanentEffectMap();
    }

    @NotNull
    public ParticleEffectMap getParticleEffectMap() {
        return safePlayerSession().getParticleEffectMap();
    }

    /**
     * @return All currently registered player passive skills
     */
    @NotNull
    public PassiveSkillMap getPassiveSkillMap() {
        return safePlayerSession().getPassiveSkillMap();
    }

    @NotNull
    public PermissionMap getPermissionMap() {
        return safePlayerSession().getPermissionMap();
    }

    //endregion

    @NotNull
    public VariableList getVariableList() {
        return variableList;
    }

    @NotNull
    public ActionBarHandler getActionBar() {
        return actionBar;
    }

    @NotNull
    public Collection<PassiveSkill> isolateSkills(@NotNull TriggerMetadata triggerMetadata) {
        return triggerMetadata.getTriggerType().isActionHandSpecific() ? getPassiveSkillMap().isolateModifiers(triggerMetadata.getActionHand()) : getPassiveSkillMap().getModifiers();
    }

    public void triggerSkills(@NotNull TriggerMetadata triggerMetadata) {
        triggerSkills(triggerMetadata, isolateSkills(triggerMetadata));
    }

    /**
     * Trigger a specific set of skills, with a specific context.
     *
     * @param triggerMetadata Context in which skills were triggered
     * @param skills          The list of skills being potentially triggered
     */
    public void triggerSkills(@NotNull TriggerMetadata triggerMetadata, @NotNull Iterable<PassiveSkill> skills) {
        if (getPlayer().getGameMode() == GameMode.SPECTATOR || !MythicLib.plugin.getFlags().isFlagAllowed(getPlayer(), CustomFlag.MMO_ABILITIES))
            return;

        for (PassiveSkill skill : skills) {
            final SkillHandler<?> handler = skill.getTriggeredSkill().getHandler();
            if (handler.isTriggerable() && skill.getTrigger().equals(triggerMetadata.getTriggerType()))
                skill.getTriggeredSkill().cast(triggerMetadata);
        }
    }

    /**
     * Ticks every second in-game for every online player
     */
    public void tickOnline() {

        // Apply permanent potion effects
        getPermanentEffectMap().applyPermanentPotionEffects();
    }

    @Nullable
    public <T> T getExternalData(String key, Class<T> objectType) {
        final @Nullable Object found = externalData.get(key);
        return found == null ? null : (T) found;
    }

    public void setExternalData(String key, Object obj) {
        externalData.put(key, obj);
    }

    public boolean hasExternalData(String key) {
        return externalData.containsKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MMOPlayerData)) return false;

        MMOPlayerData that = (MMOPlayerData) o;
        return getUniqueId().equals(that.getUniqueId());
    }

    @Override
    public int hashCode() {
        return getUniqueId().hashCode();
    }

    //region Static methods

    private static final Map<UUID, MMOPlayerData> PLAYER_DATA = new WeakHashMap<>();

    /**
     * Called everytime a player enters the server. If the
     * resource data is not initialized yet, initializes it.
     * <p>
     * This is called async using {@link AsyncPlayerPreLoginEvent} which does
     * not provide a Player instance, meaning the cached Player instance is NOT
     * loaded yet. It is only loaded when the player logs in using {@link PlayerJoinEvent}
     *
     * @param player Player whose data should be initialized
     */
    public static MMOPlayerData setup(@NotNull Player player) {
        final MMOPlayerData found = PLAYER_DATA.computeIfAbsent(player.getUniqueId(), uuid -> new MMOPlayerData(player));
        found.updatePlayer(player);
        return found;
    }

    @NotNull
    public static MMOPlayerData get(@NotNull OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    @NotNull
    public static MMOPlayerData get(@NotNull UUID uuid) {
        return Objects.requireNonNull(PLAYER_DATA.get(uuid), "Player data not loaded");
    }

    @Nullable
    public static MMOPlayerData online(@NotNull Player player) {
        if (!player.isOnline()) return null;
        final MMOPlayerData found = PLAYER_DATA.get(player.getUniqueId());
        return found != null && found.isOnline() ? found : null;
    }

    /**
     * Use it at your own risk! Player data might not be loaded
     */
    @Nullable
    public static MMOPlayerData getOrNull(@NotNull Entity player) {
        return player instanceof Player ? getOrNull(player.getUniqueId()) : null;
    }

    /**
     * Use it at your own risk! Player data might not be loaded
     */
    @Nullable
    public static MMOPlayerData getOrNull(@NotNull UUID uuid) {
        return PLAYER_DATA.get(uuid);
    }

    /**
     * This is being used to easily check if an online player corresponds to
     * a real player or a Citizens NPC. Citizens NPCs do not have any player
     * data associated to them
     *
     * @return Checks if player data is loaded for a specific player
     */
    public static boolean has(@NotNull OfflinePlayer player) {
        return has(player.getUniqueId());
    }

    /**
     * This is being used to easily check if an online player corresponds to
     * a real player/profile or a Citizens NPC. Citizens NPCs do not have any player
     * data associated to them
     *
     * @return Checks if player data is loaded for a specific profile UUID
     */
    public static boolean has(@NotNull UUID uuid) {
        return PLAYER_DATA.containsKey(uuid);
    }

    /**
     * @return Currently loaded MMOPlayerData instances. This can be used to
     *         apply things like resource regeneration or other runnable based
     *         tasks instead of looping through online players and having to
     *         resort to a map-lookup-based get(Player) call
     */
    @NotNull
    public static Collection<MMOPlayerData> getLoaded() {
        return PLAYER_DATA.values();
    }

    /**
     * Calls some method for every player that has started playing
     * i.e that has chosen a profile and loaded his data.
     */
    public static void forEachPlaying(@NotNull Consumer<MMOPlayerData> action) {
        for (var registered : PLAYER_DATA.values())
            if (registered.isPlaying()) action.accept(registered);
    }

    /**
     * Unloads all timed-out temporary player data. This should be
     * checked once an hour to make sure not to cause memory leaks.
     */
    public static void flushOfflinePlayerData() {
        PLAYER_DATA.values().removeIf(MMOPlayerData::isTimedOut);
    }

    //endregion

    //region Deprecated

    /**
     * @see #forEachPlaying(Consumer)
     * @deprecated
     */
    @Deprecated
    public static void forEachOnline(@NotNull Consumer<MMOPlayerData> action) {
        for (MMOPlayerData registered : PLAYER_DATA.values())
            if (registered.isOnline()) action.accept(registered);
    }

    @Deprecated
    public boolean hasStartedPlaying() {
        return isPlaying();
    }

    @Deprecated
    public boolean hasOfficialId() {
        return true;
    }

    @Deprecated
    public void setProfileId(@Nullable UUID profileId) {
        throw new IllegalStateException("Cannot change profile ID");
    }

    @Deprecated
    public boolean hasFullySynchronized() {
        return isPlaying();
    }

    @Deprecated
    public long getLastLogin() {
        return getLastLogActivity();
    }

    @Deprecated
    public static boolean isLoaded(UUID uuid) {
        return has(uuid);
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable Entity target) {
        Validate.isTrue(!triggerType.isActionHandSpecific(), "You must provide an action hand");
        triggerSkills(new TriggerMetadata(this, triggerType, target));
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull EquipmentSlot actionHand, @Nullable Entity target) {
        Validate.notNull(actionHand, "Action hand cannot be null");
        triggerSkills(new TriggerMetadata(this, triggerType, actionHand, null, target, null, null, null));
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable PlayerMetadata caster, @Nullable AttackMetadata attackMetadata, @Nullable Entity target) {
        final TriggerMetadata meta = new TriggerMetadata(this, triggerType, null, null, target, null, attackMetadata, caster);
        triggerSkills(meta);
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable PlayerMetadata caster, @Nullable Entity target, @Nullable AttackMetadata attackMetadata) {
        final Iterable<PassiveSkill> candidates = triggerType.isActionHandSpecific() ? getPassiveSkillMap().isolateModifiers(caster == null ? EquipmentSlot.MAIN_HAND : caster.getActionHand()) : getPassiveSkillMap().getModifiers();
        final TriggerMetadata meta = new TriggerMetadata(this, triggerType, null, null, target, null, attackMetadata, caster);
        triggerSkills(meta, candidates);
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable PlayerMetadata caster, @Nullable Entity target) {
        final Iterable<PassiveSkill> candidates = triggerType.isActionHandSpecific() ? getPassiveSkillMap().isolateModifiers(caster == null ? EquipmentSlot.MAIN_HAND : caster.getActionHand()) : getPassiveSkillMap().getModifiers();
        final TriggerMetadata meta = new TriggerMetadata(this, triggerType, null, null, target, null, null, caster);
        triggerSkills(meta, candidates);
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable PlayerMetadata caster, @NotNull Iterable<PassiveSkill> skills, @Nullable Entity target) {
        final TriggerMetadata meta = new TriggerMetadata(this, triggerType, null, null, target, null, null, caster);
        triggerSkills(meta, skills);
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable PlayerMetadata caster, @NotNull Iterable<PassiveSkill> skills, @Nullable Entity target, @Nullable AttackMetadata attack) {
        final TriggerMetadata meta = new TriggerMetadata(this, triggerType, caster == null ? EquipmentSlot.MAIN_HAND : caster.getActionHand(), null, target, null, attack, caster);
        triggerSkills(meta, skills);
    }

    /**
     * @deprecated CooldownType now extends CooldownObject
     */
    @Deprecated
    public void applyCooldown(CooldownType cd, double value) {
        getCooldownMap().applyCooldown(cd.name(), value);
    }

    /**
     * @deprecated CooldownType now extends CooldownObject
     */
    @Deprecated
    public boolean isOnCooldown(CooldownType cd) {
        return getCooldownMap().isOnCooldown(cd.name());
    }

    //endregion
}

