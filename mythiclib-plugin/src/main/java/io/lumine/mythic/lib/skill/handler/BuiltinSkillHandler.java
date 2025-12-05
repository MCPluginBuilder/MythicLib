package io.lumine.mythic.lib.skill.handler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BuiltinSkillHandler {

    public String[] mods() default {};

    public boolean triggerable() default true;
}
