package io.lumine.mythic.lib.damage.packet;

import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @deprecated Not used yet
 */
@Deprecated
public class MeleeDamagePacket extends DamagePacket {
    @Deprecated
    public MeleeDamagePacket(double value, @NotNull DamageType... types) {
        super(value, Arrays.asList(types));
    }
}
