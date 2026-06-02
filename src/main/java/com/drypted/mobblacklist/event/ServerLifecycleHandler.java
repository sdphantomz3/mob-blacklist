package com.drypted.mobblacklist.event;

import com.drypted.mobblacklist.config.BlacklistConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ServerLifecycleHandler {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            BlacklistConfig.load(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            BlacklistConfig.save();
        });
    }
}