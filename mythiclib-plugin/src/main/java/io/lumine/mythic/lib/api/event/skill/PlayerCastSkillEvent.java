package io.lumine.mythic.lib.api.event.skill;

import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.SkillResult;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerCastSkillEvent extends PlayerSkillEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled;

    /**
     * Called after checking that a skill can be cast by a player
     * right before actually applying its effects
     *
     * @param skill     Skill being cast
     * @param skillMeta Info of the skill being cast
     * @param result    Skill result
     */
    public PlayerCastSkillEvent(Skill skill, SkillMetadata skillMeta, SkillResult result) {
        super(skill, skillMeta, result);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
