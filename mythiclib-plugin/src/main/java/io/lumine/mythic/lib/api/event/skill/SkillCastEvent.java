package io.lumine.mythic.lib.api.event.skill;

import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.SkillResult;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkillCastEvent extends PlayerSkillEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Called after a player has succesfuly cast a skill.
     *
     * @param skill     Skill being cast
     * @param skillMeta Info of the skill that has been cast
     * @param result    Skill result
     */
    public SkillCastEvent(Skill skill, SkillMetadata skillMeta, SkillResult result) {
        super(skill, skillMeta, result);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
