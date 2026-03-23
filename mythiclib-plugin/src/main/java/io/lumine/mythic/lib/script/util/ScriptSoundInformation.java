package io.lumine.mythic.lib.script.util;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Sound;

public class ScriptSoundInformation {
    public final Sound bukkitSound;
    public final String assetId;
    public final NumericExpression vol, pitch;

    public ScriptSoundInformation(ConfigObject config) {
        assetId = config.string("sound", "snd", "s", "name", "n");

        Sound bukkitSound;
        try {
            bukkitSound = Sounds.fromName(UtilityMethods.enumName(assetId));
        } catch (Exception exception) {
            bukkitSound = null;
        }
        this.bukkitSound = bukkitSound;
        vol = config.numericExpr(NumericExpression.ONE, "volume", "vol", "v");
        pitch = config.numericExpr(NumericExpression.ONE, "pitch", "p", "level", "lvl", "l");
    }
}