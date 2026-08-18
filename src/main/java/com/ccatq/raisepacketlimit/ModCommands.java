package com.ccatq.raisepacketlimit;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

/**
 * Admin-only test command: {@code /testbigpacket [megabytes]}.
 * <p>
 * Sends a large <b>vanilla</b> {@code container_set_content} packet (for the
 * player's own inventory, containerId 0) to the executing player. Vanilla
 * packets are never split by NeoForge's {@code GenericPacketSplitter}, so this
 * exercises the real 21-bit frame / compression limits that caused the AE2
 * wireless-terminal kicks. With this mod installed the packet arrives fine;
 * without it the player is kicked with an EncoderException.
 * <p>
 * The payload is spread across multiple items (each item carries a 1 MiB NBT
 * tag, safely below the 2 MiB per-tag NBT quota), then the real inventory
 * contents are re-sent immediately so the client's view self-corrects.
 * <p>
 * Registered on the game event bus from {@link RaisePacketLimit}.
 */
public final class ModCommands {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Per-item NBT payload; must stay below the 2 MiB per-tag NBT read quota. */
    private static final int BYTES_PER_ITEM = 1024 * 1024;
    /** Slots in the player inventory menu (containerId 0). */
    private static final int INVENTORY_SLOTS = 46;
    /** First main-inventory slot index (after result/crafting/armor slots 0-8). */
    private static final int FIRST_MAIN_SLOT = 9;
    /** Maximum test size in MiB (limited by available inventory slots x 1 MiB). */
    private static final int MAX_MIB = 37;

    private ModCommands() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("testbigpacket")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> sendBigPacket(ctx, 4))
                .then(Commands.argument("megabytes", IntegerArgumentType.integer(3, MAX_MIB))
                        .executes(ctx -> sendBigPacket(ctx, IntegerArgumentType.getInteger(ctx, "megabytes")))));
    }

    private static int sendBigPacket(CommandContext<CommandSourceStack> ctx, int megabytes) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        int requestedBytes = megabytes * 1024 * 1024;
        int itemCount = Math.max(1, Math.min((requestedBytes + BYTES_PER_ITEM - 1) / BYTES_PER_ITEM, INVENTORY_SLOTS - FIRST_MAIN_SLOT));
        int actualBytes = itemCount * BYTES_PER_ITEM;

        NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < itemCount; i++) {
            CompoundTag tag = new CompoundTag();
            tag.putByteArray("raisepacketlimit_payload", new byte[BYTES_PER_ITEM]);
            ItemStack stack = new ItemStack(Items.STONE);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            items.set(FIRST_MAIN_SLOT + i, stack);
        }

        // Send the oversized vanilla packet (containerId 0 = player inventory menu).
        player.connection.send(new ClientboundContainerSetContentPacket(0, 0, items, ItemStack.EMPTY));
        // Immediately restore the real inventory contents on the client.
        player.connection.send(new ClientboundContainerSetContentPacket(
                0,
                player.inventoryMenu.incrementStateId(),
                player.inventoryMenu.getItems(),
                player.inventoryMenu.getCarried()));

        ctx.getSource().sendSuccess(
                () -> Component.literal("Sent a ~" + actualBytes + " byte ("
                        + megabytes + " MiB requested) container_set_content packet to "
                        + player.getName().getString()
                        + ". Still connected = packet-size limit is raised correctly."),
                true);
        LOGGER.info("Sent {}-byte test packet to {}", actualBytes, player.getName().getString());
        return 0;
    }
}