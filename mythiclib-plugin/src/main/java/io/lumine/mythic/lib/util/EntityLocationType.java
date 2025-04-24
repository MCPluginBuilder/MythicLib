package io.lumine.mythic.lib.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public enum EntityLocationType {

    /**
     * By the entity's feet
     */
    FEET(0),

    /**
     * At the middle of the body
     */
    BODY(.5),

    /**
     * At the top of the entity's head
     */
    TOP(1),

    /**
     * The position of the eyes
     */
    EYES(1.6 / 1.8);

    private final double heightPercentage;

    EntityLocationType(double heightPercentage) {
        this.heightPercentage = heightPercentage;
    }

    public Location getLocation(Entity entity) {
        Location loc = entity.getLocation();
        loc.add(0, entity.getHeight() * heightPercentage, 0);
        if (entity instanceof LivingEntity) loc.setDirection(((LivingEntity) entity).getEyeLocation().getDirection());
        return loc;
    }
}
