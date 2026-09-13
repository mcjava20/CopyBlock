package com.mcjava20.copyblock;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    // ==========修改这一行！！==========
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CopyBlock.MODID);

    public static final DeferredItem<BlockItem> GREEN_COPY_BLOCK =
            ITEMS.register("green_copy_block", () -> new BlockItem(ModBlocks.GREEN_COPY_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> RED_COPY_BLOCK =
            ITEMS.register("red_copy_block", () -> new BlockItem(ModBlocks.RED_COPY_BLOCK.get(), new Item.Properties()));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CopyBlock.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COPYBLOCK_TAB =
            CREATIVE_TABS.register("copyblock_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.copyblock"))
                    .icon(() -> new ItemStack(GREEN_COPY_BLOCK.get()))
                    .displayItems((params, output) -> {
                        output.accept(GREEN_COPY_BLOCK.get());
                        output.accept(RED_COPY_BLOCK.get());
                    })
                    .build());
}
