package com.mcjava20.copyblock.block;

import com.mcjava20.copyblock.blockentity.GreenCopyBlockEntity;
import com.mcjava20.copyblock.data.PlayerCopyData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;
import java.util.UUID;

public class GreenCopyBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public GreenCopyBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GreenCopyBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide() && placer instanceof Player player) {
            GreenCopyBlockEntity entity = (GreenCopyBlockEntity) level.getBlockEntity(pos);
            if (entity != null) {
                entity.setPlayerUUID(player.getUUID());
                // 记录 p1 上方方块作为模板
                entity.captureTemplate(pos);
                boolean has = entity.hasTemplate() && !entity.getTemplateState().isAir();
                level.setBlock(pos, state.setValue(LIT, has), Block.UPDATE_CLIENTS);
            }
            PlayerCopyData data = PlayerCopyData.get((ServerLevel) level);
            data.setP1(player.getUUID(), pos);
        }
    }

    // 邻居变化时重新读取模板（模板方块被改动时同步）
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide()) return;

        GreenCopyBlockEntity entity = (GreenCopyBlockEntity) level.getBlockEntity(pos);
        if (entity == null) return;

        UUID uuid = entity.getPlayerUUID();
        if (uuid == null) return;

        entity.captureTemplate(pos);
        boolean has = entity.hasTemplate() && !entity.getTemplateState().isAir();
        if (state.getValue(LIT) != has) {
            level.setBlock(pos, state.setValue(LIT, has), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            GreenCopyBlockEntity entity = (GreenCopyBlockEntity) level.getBlockEntity(pos);
            if (entity != null) {
                UUID uuid = entity.getPlayerUUID();
                if (uuid != null) {
                    PlayerCopyData.get((ServerLevel) level).removeP1(uuid);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}