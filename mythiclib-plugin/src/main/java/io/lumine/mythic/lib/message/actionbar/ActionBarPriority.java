package io.lumine.mythic.lib.message.actionbar;

/**
 * @see ActionBarHandler
 */
public class ActionBarPriority {

    public static final int LOWEST = 10;

    /**
     * Used by MMOCore permanent health/armor action bar.
     */
    public static final int LOW = 20;

    /**
     * Any normal informative action bar message
     */
    public static final int NORMAL = 30;

    /**
     * Used by skill casting and other priority
     */
    public static final int HIGH = 40;
    public static final int HIGHEST = 50;
}
