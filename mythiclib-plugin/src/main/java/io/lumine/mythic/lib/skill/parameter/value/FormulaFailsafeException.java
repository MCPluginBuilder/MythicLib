package io.lumine.mythic.lib.skill.parameter.value;

import io.lumine.mythic.lib.MythicLib;

import java.util.logging.Level;

// TODO merge with NumericalExpression
public class FormulaFailsafeException extends RuntimeException {
    private final double failsafe;

    public FormulaFailsafeException(Exception thrown, double failsafe) {
        super(thrown);

        this.failsafe = failsafe;
    }

    public double getFailsafe() {
        return failsafe;
    }

    public void log(String format, Object... params) {
        MythicLib.plugin.getLogger().log(Level.WARNING, String.format(format, params) + ": " + getCause().getMessage());
    }
}
