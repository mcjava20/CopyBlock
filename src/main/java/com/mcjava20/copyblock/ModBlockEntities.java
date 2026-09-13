package com.mcjava20.copyblock;

import com.mcjava20.copyblock.blockentity.GreenCopyBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CopyBlock.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreenCopyBlockEntity>> GREEN_COPY_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("green_copy_block", () ->
                    BlockEntityType.Builder.of(GreenCopyBlockEntity::new, ModBlocks.GREEN_COPY_BLOCK.get()).build(null));
}
