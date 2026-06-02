package com.drypted.mobblacklist.event;

import com.drypted.mobblacklist.config.BlacklistConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ServerLifecycleHandler {

    public static void register() {
        // Triggered when a singleplayer world finishes loading OR a dedicated server starts up
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            BlacklistConfig.load(server);
        });

        // Ensures everything saves when exiting the world or stopping the server
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            BlacklistConfig.save();
        });
    }
}