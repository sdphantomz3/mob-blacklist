package com.drypted.mobblacklist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlacklistConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    public boolean isEnabled = true;
    public boolean allowCreativeSpawning = false;
    public Map<String, Integer> preventedCounts = new HashMap<>();
    public Set<String> blacklistedMobs = new HashSet<>();

    public static BlacklistConfig INSTANCE = new BlacklistConfig();

    public static void load(MinecraftServer server) {
        File worldDir = server.getWorldPath(LevelResource.ROOT).toFile();
        configFile = new File(worldDir, "mob_blacklist.json");

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                INSTANCE = GSON.fromJson(reader, BlacklistConfig.class);
                if (INSTANCE.preventedCounts == null) INSTANCE.preventedCounts = new HashMap<>();
                if (INSTANCE.blacklistedMobs == null) INSTANCE.blacklistedMobs = new HashSet<>();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            INSTANCE = new BlacklistConfig();
            save();
        }
    }

    public static void save() {
        if (configFile == null) return;
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}