package com.mcjava20.copyblock;

import com.mcjava20.copyblock.block.GreenCopyBlock;
import com.mcjava20.copyblock.block.RedCopyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CopyBlock.MODID);

    public static final DeferredBlock<GreenCopyBlock> GREEN_COPY_BLOCK =
            BLOCKS.registerBlock("green_copy_block", GreenCopyBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .strength(3.0F, 6.0F)
                            .lightLevel(state -> state.getValue(GreenCopyBlock.LIT) ? 15 : 0));

    public static final DeferredBlock<RedCopyBlock> RED_COPY_BLOCK =
            BLOCKS.registerBlock("red_copy_block", RedCopyBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .strength(3.0F, 6.0F));
}