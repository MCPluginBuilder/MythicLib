package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

import java.util.ArrayList;
import java.util.List;

@MechanicMetadata
public class MarkCritMechanic extends Mechanic {
    private final List<String> tags;

    public MarkCritMechanic(ConfigObject config) {
        var split = config.string("damage_types", "damage_type", "dtypes", "dtype", "dt").split(",");
        tags = new ArrayList<>(split.length);
        for (var s : split) tags.add(s.toLowerCase());
    }

    @Override
    public void cast(SkillMetadata meta) {
        meta.getAttackSource().getDamage().registerCrits(tags);
    }
}
