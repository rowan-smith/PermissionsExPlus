package com.zachsthings.netevents;

import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

/** Compile-only stand-in for the NetEvents plugin, whose snapshot is no longer published. */
public class NetEventsPlugin extends JavaPlugin {
    public UUID getServerUUID() {
        throw new UnsupportedOperationException();
    }

    public void callEvent(Event event) {
        throw new UnsupportedOperationException();
    }
}
