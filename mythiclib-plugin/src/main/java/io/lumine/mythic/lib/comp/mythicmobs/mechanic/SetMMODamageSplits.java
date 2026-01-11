package io.lumine.mythic.lib.comp.mythicmobs.mechanic;


import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.util.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@MythicMechanic(author = "Indyuce", name = "setmmodamagesplits", description = "Sets default damage types dealt by this mob")
public class SetMMODamageSplits implements ITargetedEntitySkill {
    private final List<Entry> splits;

    public static final String METADATA_KEY = "MMODefaultDamageTypes";

    public SetMMODamageSplits(MythicLineConfig mlc) {
        final var rawSplits = mlc.parseStringList(mlc.getString(new String[]{"splits", "s"}));
        Validate.isTrue(!rawSplits.isEmpty(), "At least one split is required");

        this.splits = rawSplits.stream().map(s -> Entry.from(mlc, s)).collect(Collectors.toList());

        // Normalize
        double totalWeights = 0;
        for (var entry : splits) totalWeights += entry.percent;
        Validate.isTrue(totalWeights > 0, "Null total split weight");
        final var normalizationCoef = 1 / totalWeights;
        splits.replaceAll(entry -> entry.normalize(normalizationCoef));
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        abstractEntity.setMetadata(METADATA_KEY, splits);
        return SkillResult.SUCCESS;
    }

    public static class Entry {
        public final double percent;
        public final List<DamageType> types;
        public final Element element;

        Entry(List<DamageType> types, double percent, Element element) {
            this.types = types;
            this.percent = percent;
            this.element = element;

            Validate.isTrue(percent > 0, "Split weight must be positive");
        }

        Entry normalize(double c) {
            return new Entry(types, percent * c, element);
        }

        public static Entry from(MythicLineConfig proxy, String input) {
            var split = input.split(":");

            //////////
            // Parse power
            //////////
            // Default to 1
            var power = split.length > 1 ? Double.parseDouble(split[1]) : 1;

            //////////
            // Parse damage types
            //////////
            var typeStringSplit = split[0];
            if (typeStringSplit.startsWith("["))
                typeStringSplit = typeStringSplit.substring(1, typeStringSplit.length() - 1);
            List<DamageType> types = new ArrayList<>();
            Element element = null;

            // into list of strings
            var els = proxy.parseStringList(typeStringSplit);
            if (els.isEmpty()) els = List.of(typeStringSplit);

            // populate list and element
            for (var candidate : els)
                try {
                    element = MythicLib.plugin.getElements().get(candidate.toUpperCase());
                    // element already set
                } catch (Exception exception) {
                    try {
                        var dt = DamageType.valueOf(UtilityMethods.enumName(candidate));
                        types.add(dt); // add to list
                    } catch (Exception exception1) {
                        throw new IllegalArgumentException("Invalid damage type or element: " + candidate);
                    }
                }

            return new Entry(types, power, element);
        }
    }
}