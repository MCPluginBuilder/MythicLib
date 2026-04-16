package io.lumine.mythic.lib.rpg.compat;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.user.SkillsUser;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import io.lumine.mythic.lib.rpg.LevelModule;
import io.lumine.mythic.lib.rpg.ManaModule;
import org.jetbrains.annotations.NotNull;

// No stamina, redirected to food level.
public class AuraSkills implements LevelModule, ManaModule {
    private final AuraSkillsApi aSkills;

    public AuraSkills() {
        aSkills = AuraSkillsApi.get();
    }

    private SkillsUser playerData(MMOPlayerData playerData) {
        return this.aSkills.getUser(playerData.getUniqueId());
    }

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        return playerData(player).getPowerLevel();
    }

    @Override
    public double getMana(@NotNull MMOPlayerData playerData) {
        return this.playerData(playerData).getMana();
    }

    @Override
    public double getStamina(@NotNull MMOPlayerData player) {
        return player.getPlayer().getFoodLevel();
    }

    @Override
    public boolean setMana(@NotNull MMOPlayerData player, double newValue, @NotNull ResourceUpdateReason reason) {
        var playerData = this.playerData(player);
        var currentMana = playerData.getMana();

        // [BUGFIX] AuraSkill has a "Sorcery" skill, which by default can be leveled
        // up by simply using mana (mana_ability_use xp source). MMOItems's mana usage
        // does not trigger this XP gain.
        // Source: https://gitlab.com/phoenix-dvpmt/mmoitems/-/issues/1807
        if (newValue < currentMana) {
            return playerData.consumeMana(currentMana - newValue);
        }

        // Normal mana update
        else {
            playerData.setMana(newValue);
            return true;
        }
    }

    @Override
    public boolean setStamina(@NotNull MMOPlayerData player, double newValue, @NotNull ResourceUpdateReason reason) {
        player.getPlayer().setFoodLevel((int) newValue);
        return true;
    }
}