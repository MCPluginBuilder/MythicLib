package io.lumine.mythic.lib.version;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class OreDrops {
    private final Material material;
    private final int min, max;

    private static final Random RANDOM = new Random();

    public OreDrops(Material material) {
        this(material, 1, 1);
    }

    public OreDrops(Material material, int min, int max) {
        Validate.notNull(material, "Material cannot be null");
        Validate.isTrue(min > 0, "Min amount must be positive");
        Validate.isTrue(max >= min, "Max amount must be higher than min amount");

        this.material = material;
        this.min = min;
        this.max = max;
    }

    @NotNull
    public ItemStack generate(int fortuneLevel) {
        Validate.isTrue(fortuneLevel >= 0, "Fortune level must be positive");

        final int rolled = min == max ? min : RANDOM.nextInt(min, max + 1);
        return new ItemStack(material, rolled * rollFortuneCoefficient(fortuneLevel));
    }

    /**
     * Let's note fortune level n > 0
     * - dropping x1 is twice as likely as anything else
     * - cannot drop more than x(n + 1)
     * - dropping xm for m in [1, n] is as likely for all values of m
     *
     * @param fortuneLevel
     * @return
     */
    private int rollFortuneCoefficient(int fortuneLevel) {
        if (fortuneLevel == 0) return 1;
        if (RANDOM.nextDouble() < 2d / (2d + fortuneLevel)) return 1;
        return RANDOM.nextInt(2, fortuneLevel + 2);
    }
}
