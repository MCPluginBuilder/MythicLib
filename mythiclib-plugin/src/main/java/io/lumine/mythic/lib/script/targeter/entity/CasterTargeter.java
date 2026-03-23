package io.lumine.mythic.lib.script.targeter.entity;

import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.List;

public class CasterTargeter implements EntityTargeter {

    @Override
    public List<Entity> findTargets(SkillMetadata meta) {
        return Collections.singletonList(meta.getCaster().getPlayer());
    }
}
