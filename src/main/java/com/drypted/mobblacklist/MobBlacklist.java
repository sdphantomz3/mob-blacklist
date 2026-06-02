package com.drypted.mobblacklist;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drypted.mobblacklist.command.BlacklistCommands;
import com.drypted.mobblacklist.event.ServerLifecycleHandler;
import com.drypted.mobblacklist.event.SpawnHandler;

public class MobBlacklist implements ModInitializer {
	public static final String MOD_ID = "mob-blacklist";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerLifecycleHandler.register();

		SpawnHandler.register();
		BlacklistCommands.register();
		
		LOGGER.info("MobBlacklist Initalized!");
	}
}