package dev.doublekekse.scriboodle;

import dev.doublekekse.scriboodle.component.ScribbleStyle;
import dev.doublekekse.scriboodle.data.PaginatedScribbleData;
import dev.doublekekse.scriboodle.data.ScribbleData;
import dev.doublekekse.scriboodle.data.ScribbleManager;
import dev.doublekekse.scriboodle.duck.MinecraftServerDuck;
import dev.doublekekse.scriboodle.packet.PaginatedScribblePacket;
import dev.doublekekse.scriboodle.packet.ScribblePacket;
import dev.doublekekse.scriboodle.registry.ScriboodleComponents;
import dev.doublekekse.scriboodle.registry.ScriboodleSoundEvents;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static net.minecraft.commands.Commands.literal;

public class Scriboodle implements ModInitializer {
    public static final String MOD_ID = "scriboodle";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
//        SharedConstants.IS_RUNNING_IN_IDE = true;
        ScriboodleComponents.register();
        ScriboodleSoundEvents.register();

        PayloadTypeRegistry.serverboundPlay().register(ScribblePacket.TYPE, ScribblePacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PaginatedScribblePacket.TYPE, PaginatedScribblePacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ScribblePacket.TYPE, ScribblePacket::handleServer);

        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandBuildContext, commandSelection) -> {
            commandDispatcher.register(literal(MOD_ID).executes(ctx -> {
                var player = ctx.getSource().getPlayer();
                if (player == null) {
                    return 0;
                }
                var stack = player.getMainHandItem();

                if (!stack.has(ScriboodleComponents.SCRIBBLE_STYLE)) {
                    ctx.getSource().sendFailure(Component.translatable("scriboodle.command.scriboodle.failure.item", stack.getDisplayName()));
                    return 0;
                }

                stack.remove(DataComponents.FOOD);
                stack.remove(DataComponents.CONSUMABLE);

                stack.set(ScriboodleComponents.SCRIBBLEABLE, Unit.INSTANCE);

                ctx.getSource().sendSuccess(() -> Component.literal("Scriboodle"), false);
                return 1;
            }));

            if (SharedConstants.IS_RUNNING_IN_IDE) {
                commandDispatcher.register(literal(MOD_ID + "_stresstest").executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    var scribbleManager = ((MinecraftServerDuck) ctx.getSource().getServer()).scriboodle$getScribbleManager();

                    if(player == null) {
                        return 0;
                    }


                    var style = ScribbleStyle.BOOK;
                    var data = new PaginatedScribbleData(style.foregroundWidth(), style.foregroundHeight(), Collections.emptyList()).clone();

                    for (int j = 0; j < style.maxPageCount(); j++) {
                        ScribbleData scribbleData = data.get(j);

                        for (int x = 0; x < scribbleData.width; x++) {
                            for (int y = 0; y < scribbleData.height; y++) {
                                scribbleData = scribbleData.set(x, y, ColorUtils.randomColor());
                            }
                        }

                        data.set(j, scribbleData);
                    }

                    for (int i = 0; i < 36; i++) {
                        var iStack = new ItemStack(Items.BOOK, 1);
                        var ref = scribbleManager.reserve();
                        scribbleManager.setAll(ref, data, player.getUUID());
                        iStack.set(ScriboodleComponents.SCRIBBLE_REFERENCE, ref);
                        player.addItem(iStack);
                    }
                    return 1;
                }));
            }
        }));

        DefaultItemComponentEvents.MODIFY.register(ctx -> {
            ctx.modify(Items.BOOK, i -> i.set(ScriboodleComponents.SCRIBBLE_STYLE, ScribbleStyle.BOOK));
            ctx.modify(Items.PAPER, i -> i.set(ScriboodleComponents.SCRIBBLE_STYLE, ScribbleStyle.PAPER));
        });

        ItemEvents.USE.register((level, player, hand) -> {
            if (!level.isClientSide()) {
                int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : Inventory.SLOT_OFFHAND;


                var stack = player.getInventory().getItem(slot);
                var style = stack.get(ScriboodleComponents.SCRIBBLE_STYLE);

                if (style == null) {
                    return null;
                }

                var result = InteractionResult.SUCCESS;

                if (stack.has(ScriboodleComponents.SCRIBBLEABLE)) {
                    var newStack = stack.copyWithCount(1);
                    newStack.remove(ScriboodleComponents.SCRIBBLEABLE);

                    var server = (MinecraftServerDuck) level.getServer();
                    assert server != null;

                    var scribbleManager = server.scriboodle$getScribbleManager();

                    setupItem(scribbleManager, newStack);

                    stack.shrink(1);

                    if (stack.isEmpty()) {
                        stack = newStack;
                        result = InteractionResult.SUCCESS.heldItemTransformedTo(newStack);
                    } else {
                        addOrDrop(player, newStack);
                    }
                }

                var ref = stack.get(ScriboodleComponents.SCRIBBLE_REFERENCE);

                if (ref == null) {
                    return null;
                }

                var server = (MinecraftServerDuck) level.getServer();
                assert server != null;

                var scribbleManager = server.scriboodle$getScribbleManager();
                var scribble = scribbleManager.get(ref, style);

                if(scribble == null) {
                    return null;
                }

                ServerPlayNetworking.send((ServerPlayer) player, new PaginatedScribblePacket(scribble, slot));

                return result;
            }

            return null;
        });
    }

    private static void setupItem(ScribbleManager scribbleManager, ItemStack stack) {
        var reference = scribbleManager.reserve();
        stack.set(ScriboodleComponents.SCRIBBLE_REFERENCE, reference);
    }

    private static void addOrDrop(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}