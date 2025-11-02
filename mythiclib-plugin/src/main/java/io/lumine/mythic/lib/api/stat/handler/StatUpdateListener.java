package io.lumine.mythic.lib.api.stat.handler;

import io.lumine.mythic.lib.api.stat.StatInstance;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface StatUpdateListener {

    public void onUpdate(@NotNull StatInstance instance);
}
