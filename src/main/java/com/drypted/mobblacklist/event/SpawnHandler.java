package com.drypted.mobblacklist.event;

import com.drypted.mobblacklist.config.BlacklistConfig;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public class SpawnHandler {

    private static long lastAllowedCreativeTime = 0;
    private static EntityType<?> lastAllowedCreativeMob = null;

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            ItemStack itemStack = player.getItemInHand(hand);
            if (itemStack.getItem() instanceof SpawnEggItem eggItem) {
                EntityType<?> entityType = eggItem.getType(itemStack);
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();

                if (BlacklistConfig.INSTANCE.isEnabled && BlacklistConfig.INSTANCE.blacklistedMobs.contains(id)) {
                    if (player.isCreative() && BlacklistConfig.INSTANCE.allowCreativeSpawning) {
                        lastAllowedCreativeTime = System.currentTimeMillis();
                        lastAllowedCreativeMob = entityType;
                        return InteractionResult.PASS;
                    } else {
                        player.sendOverlayMessage(Component.literal("§cThat mob is disabled from spawning!"));
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (world.isClientSide() || !(entity instanceof LivingEntity)) return;

            Identifier loc = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            String id = loc.toString();

            if (BlacklistConfig.INSTANCE.isEnabled && BlacklistConfig.INSTANCE.blacklistedMobs.contains(id)) {
                if (BlacklistConfig.INSTANCE.allowCreativeSpawning 
                        && entity.getType() == lastAllowedCreativeMob 
                        && (System.currentTimeMillis() - lastAllowedCreativeTime) < 60) {
                    return;
                }

                entity.discard();
                
                // Track counts maps on an individual basis
                int currentCount = BlacklistConfig.INSTANCE.preventedCounts.getOrDefault(id, 0) + 1;
                BlacklistConfig.INSTANCE.preventedCounts.put(id, currentCount);

                // Auto-save every 10 blocks for this specific mob type to save CPU performance
                if (currentCount % 10 == 0) {
                    BlacklistConfig.save();
                }
            }
        });
    }
}