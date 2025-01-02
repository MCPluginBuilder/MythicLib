package io.lumine.mythic.lib.player.potion;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.potion.PotionEffectType;

/**
 * @deprecated Not implemented yet
 */
@Deprecated
public class PermanentPotionEffect extends PlayerModifier {
    private final PotionEffectType effect;
    private final int amplifier;

    public PermanentPotionEffect(String key, PotionEffectType effect, int amplifier) {
        super(key, EquipmentSlot.OTHER, ModifierSource.OTHER);

        Validate.isTrue(amplifier >= 0, "Amplifier must be bat least zero");

        this.effect = effect;
        this.amplifier = amplifier;
    }

    public PermanentPotionEffect(ConfigObject obj) {
        super(obj.getString("key"), EquipmentSlot.OTHER, ModifierSource.OTHER);

        this.effect = PotionEffectType.getByName(obj.getString("effect"));
        this.amplifier = obj.getInt("level") - 1;
    }

    public PotionEffectType getEffect() {
        return effect;
    }

    public int getAmplifier() {
        return amplifier;
    }

    @Override
    public void register(MMOPlayerData playerData) {
        playerData.getPermanentEffectMap().addModifier(this);
    }

    @Override
    public void unregister(MMOPlayerData playerData) {
        playerData.getPermanentEffectMap().removeModifier(getUniqueId());
    }
}
