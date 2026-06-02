package com.drypted.mobblacklist.command;

import com.drypted.mobblacklist.config.BlacklistConfig;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import java.util.ArrayList;
import java.util.List;

public class BlacklistCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("mob_blacklist")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))

                .then(Commands.literal("add")
                    .then(Commands.argument("mob", IdentifierArgument.id())
                        .executes(BlacklistCommands::executeAddMob)))

                .then(Commands.literal("remove")
                    .then(Commands.argument("mob", IdentifierArgument.id())
                        .executes(BlacklistCommands::executeRemoveMob)))

                .then(Commands.literal("list")
                    .executes(BlacklistCommands::executeListMobs))

                .then(Commands.literal("toggle")
                    .executes(BlacklistCommands::executeToggleBlacklist))

                // /mob_blacklist logs OR /mob_blacklist logs reset
                .then(Commands.literal("logs")
                    .executes(BlacklistCommands::executeShowLogs)
                    .then(Commands.literal("reset")
                        .executes(BlacklistCommands::executeResetLogs)))

                // /mob_blacklist clear_world OR /mob_blacklist clear_world <mob_id>
                .then(Commands.literal("clear_world")
                    .executes(BlacklistCommands::executeClearAllBlacklisted)
                    .then(Commands.argument("mob", IdentifierArgument.id())
                        .executes(BlacklistCommands::executeClearSpecificMob)))

                .then(Commands.literal("settings")
                    .then(Commands.literal("allow_spawning_from_creative")
                        .then(Commands.literal("true").executes(BlacklistCommands::executeSetCreativeTrue))
                        .then(Commands.literal("false").executes(BlacklistCommands::executeSetCreativeFalse))))
            );
        });
    }

    private static int executeAddMob(CommandContext<CommandSourceStack> context) {
        Identifier mobId = IdentifierArgument.getId(context, "mob");
        String idString = mobId.toString();

        if (BlacklistConfig.INSTANCE.blacklistedMobs.add(idString)) {
            BlacklistConfig.save();
            context.getSource().sendSuccess(() -> Component.literal("§aAdded " + idString + " to the blacklist."), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("§c" + idString + " is already blacklisted."), false);
        }
        return 1;
    }

    private static int executeRemoveMob(CommandContext<CommandSourceStack> context) {
        Identifier mobId = IdentifierArgument.getId(context, "mob");
        String idString = mobId.toString();

        if (BlacklistConfig.INSTANCE.blacklistedMobs.remove(idString)) {
            BlacklistConfig.save();
            context.getSource().sendSuccess(() -> Component.literal("§eRemoved " + idString + " from the blacklist."), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("§c" + idString + " is not in the blacklist."), false);
        }
        return 1;
    }

    private static int executeListMobs(CommandContext<CommandSourceStack> context) {
        var mobs = BlacklistConfig.INSTANCE.blacklistedMobs;
        if (mobs.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("§eThe blacklist is currently empty."), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("§6Blacklisted Mobs: §f" + String.join(", ", mobs)), false);
        }
        return 1;
    }

    private static int executeToggleBlacklist(CommandContext<CommandSourceStack> context) {
        BlacklistConfig.INSTANCE.isEnabled = !BlacklistConfig.INSTANCE.isEnabled;
        BlacklistConfig.save();
        String state = BlacklistConfig.INSTANCE.isEnabled ? "§aENABLED" : "§cDISABLED";
        context.getSource().sendSuccess(() -> Component.literal("Mob blacklist is now " + state), false);
        return 1;
    }

    private static int executeShowLogs(CommandContext<CommandSourceStack> context) {
        var counts = BlacklistConfig.INSTANCE.preventedCounts;
        if (counts.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("§eNo spawn prevention logs recorded yet."), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("§6--- Spawn Prevention Logs ---"), false);
            counts.forEach((mobId, count) -> {
                context.getSource().sendSuccess(() -> Component.literal("§e  • " + mobId + ": §f" + count + " blocked"), false);
            });
        }
        return 1;
    }

    private static int executeResetLogs(CommandContext<CommandSourceStack> context) {
        BlacklistConfig.INSTANCE.preventedCounts.clear();
        BlacklistConfig.save();
        context.getSource().sendSuccess(() -> Component.literal("§aSpawn prevention logs have been completely reset."), false);
        return 1;
    }

    private static int executeClearAllBlacklisted(CommandContext<CommandSourceStack> context) {
        int count = 0;
        // Search loaded chunks across ALL dimensions (Overworld, Nether, End, etc.)
        for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
            List<Entity> toRemove = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                String id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                if (BlacklistConfig.INSTANCE.blacklistedMobs.contains(id)) {
                    toRemove.add(entity);
                }
            }
            count += toRemove.size();
            toRemove.forEach(Entity::discard);
        }
        int finalCount = count;
        context.getSource().sendSuccess(() -> Component.literal("§aKilled " + finalCount + " blacklisted mobs across all dimensions."), false);
        return 1;
    }

    private static int executeClearSpecificMob(CommandContext<CommandSourceStack> context) {
        Identifier mobId = IdentifierArgument.getId(context, "mob");
        String idString = mobId.toString();

        if (!BlacklistConfig.INSTANCE.blacklistedMobs.contains(idString)) {
            context.getSource().sendSuccess(() -> Component.literal("§c" + idString + " is not in your blacklist. Cannot purge."), false);
            return 0;
        }

        int count = 0;
        for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
            List<Entity> toRemove = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                String id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                if (id.equals(idString)) {
                    toRemove.add(entity);
                }
            }
            count += toRemove.size();
            toRemove.forEach(Entity::discard);
        }
        int finalCount = count;
        context.getSource().sendSuccess(() -> Component.literal("§aKilled " + finalCount + " instances of " + idString + " across all dimensions."), false);
        return 1;
    }

    private static int executeSetCreativeTrue(CommandContext<CommandSourceStack> context) {
        BlacklistConfig.INSTANCE.allowCreativeSpawning = true;
        BlacklistConfig.save();
        context.getSource().sendSuccess(() -> Component.literal("Creative spawning for blacklisted mobs is now §aALLOWED"), false);
        return 1;
    }

    private static int executeSetCreativeFalse(CommandContext<CommandSourceStack> context) {
        BlacklistConfig.INSTANCE.allowCreativeSpawning = false;
        BlacklistConfig.save();
        context.getSource().sendSuccess(() -> Component.literal("Creative spawning for blacklisted mobs is now §cBLOCKED"), false);
        return 1;
    }
}