package io.lumine.mythic.lib.damage.packet;

import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @deprecated Not used yet
 */
@Deprecated
public class ProjectileDamagePacket extends DamagePacket {
    private final Projectile projectile;

    @Deprecated
    public ProjectileDamagePacket(double value, @NotNull Projectile projectile, @NotNull DamageType... types) {
        super(value, Arrays.asList(types));

        this.projectile = projectile;
    }

    /**
     * @return Projectile which hit the entity
     */
    public Projectile getProjectile() {
        return projectile;
    }
}
