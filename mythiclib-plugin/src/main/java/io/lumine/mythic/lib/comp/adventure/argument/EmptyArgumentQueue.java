package io.lumine.mythic.lib.comp.adventure.argument;

import java.util.Collections;

/**
 * mythiclib
 * 22/12/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class EmptyArgumentQueue extends AdventureArgumentQueue{

    public EmptyArgumentQueue() {
        super(Collections.emptyList());
    }
}
