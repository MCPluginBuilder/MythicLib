package io.lumine.mythic.lib.command.argument;

import io.lumine.mythic.lib.command.CommandException;

public class Arguments {

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new CommandException(message);
        }
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new CommandException(message);
        }
    }

    public static void isInstanceOf(Class<?> clazz, Object instance, String message) {
        if (instance == null || !clazz.isAssignableFrom(instance.getClass())) {
            throw new CommandException(message);
        }
    }
}
