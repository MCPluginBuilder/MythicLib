package io.lumine.mythic.lib.script.variable.def;

import io.lumine.mythic.lib.script.variable.SimpleVariableRegistry;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.VariableMetadata;
import io.lumine.mythic.lib.script.variable.VariableRegistry;
import org.bukkit.event.Event;

@VariableMetadata(name = "event")
public class EventVariable extends Variable<Event> {
    public static final SimpleVariableRegistry<Event> VARIABLE_REGISTRY = new SimpleVariableRegistry<>();

    static {
        VARIABLE_REGISTRY.registerVariable("name", var -> new StringVariable("temp", var.getEventName()));
    }

    public EventVariable(String name, Event event) {
        super(name, event);
    }

    @Override
    public VariableRegistry<Variable<Event>> getVariableRegistry() {
        return VARIABLE_REGISTRY;
    }

    @Override
    public String toString() {
        return getStored() == null ? "None" : getStored().getEventName();
    }
}