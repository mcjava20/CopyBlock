package com.mcjava20.copyblock.block;

import com.mcjava20.copyblock.ModBlocks;
import com.mcjava20.copyblock.blockentity.GreenCopyBlockEntity;
import com.mcjava20.copyblock.data.PlayerCopyData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class RedCopyBlock extends Block {

    public RedCopyBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide() && placer instanceof Player player) {
            ServerLevel serverLevel = (ServerLevel) level;
            PlayerCopyData data = PlayerCopyData.get(serverLevel);
            UUID uuid = player.getUUID();

            data.setP2(uuid, pos);

            BlockPos greenPos = data.getP1(uuid);
            BlockPos[] box = data.getBox(uuid);

            if (greenPos == null) {
                player.sendSystemMessage(Component.literal("§c错误：请先放置【一号复制方块(绿色)】！"));
                data.removeP2(uuid);
                return;
            }
            if (box == null) {
                player.sendSystemMessage(Component.literal("§c错误：需要同时存在一号和二号复制方块！"));
                return;
            }

            level.getChunkAt(greenPos);
            BlockState greenState = level.getBlockState(greenPos);
            if (!greenState.is(ModBlocks.GREEN_COPY_BLOCK.get())) {
                data.removeP1(uuid);
                data.removeP2(uuid);
                player.sendSystemMessage(Component.literal("§e一号复制方块已被破坏！"));
                return;
            }

            BlockEntity entity = level.getBlockEntity(greenPos);
            if (!(entity instanceof GreenCopyBlockEntity greenEntity)) {
                return;
            }

            // 读取模板：p1 上方那一格
            greenEntity.captureTemplate(greenPos);
            BlockState templateState = greenEntity.getTemplateState();
            CompoundTag templateBeTag = greenEntity.getTemplateBlockEntityTag();

            // 必须先在绿方块上方放过方块（装填）才能复制；
            // 装填后即使上方被挖成空气，也允许复制空气来移除方块
            if (templateState == null || !greenEntity.isArmed()) {
                player.sendSystemMessage(Component.literal("§e错误：请先在绿方块上方放置方块作为模板！"));
                data.removeP1(uuid);
                data.removeP2(uuid);
                return;
            }
            boolean isAir = templateState.isAir();

            // ========== 读取游戏规则上限（关键改动） ==========
            // 根据你的游戏版本选择常量名：
            // Java 1.19.4 ~ 1.21.10 使用：GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT
            // Java 1.21.11 及以后使用：GameRules.RULE_MAX_BLOCK_MODIFICATIONS
            // 如果你编译报错找不到符号，就换成另一个名字试试。
            int ruleLimit = serverLevel.getGameRules()
                    .getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);

            // 硬上限兜底：防止规则被设为 21 亿导致服务器崩溃
            int maxCopyBlocks = Math.min(ruleLimit, 1_000_000);

            // 计算 p1~p2 之间的方块总数
            long total = (long) (box[1].getX() - box[0].getX() + 1)
                    * (box[1].getY() - box[0].getY() + 1)
                    * (box[1].getZ() - box[0].getZ() + 1);

            if (total > maxCopyBlocks) {
                player.sendSystemMessage(Component.literal("§c区域过大（" + total + " 个方块），超过上限 "
                        + maxCopyBlocks + "，已取消！"));
                data.removeP1(uuid);
                data.removeP2(uuid);
                return;
            }

            // 执行填充（含 p1/p2 自身位置，复制方块本身也会被覆盖）
            fillArea(level, box[0], box[1], templateState, templateBeTag);

            data.removeP1(uuid);
            data.removeP2(uuid);
            greenEntity.clearTemplate();

            // 不再恢复绿方块 LIT，让复制方块本身也被覆盖

            String verb = isAir ? "清除" : "替换";
            player.sendSystemMessage(Component.literal("§a填充完成，共" + verb + " " + total + " 个方块"));
        }
    }

    private static void fillArea(Level level, BlockPos min, BlockPos max,
                                 BlockState templateState, @Nullable CompoundTag templateBeTag) {
        boolean isAir = templateState.isAir();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos placePos = new BlockPos(x, y, z);
                    level.getChunkAt(placePos);

                    level.setBlock(placePos, templateState, Block.UPDATE_ALL);

                    // 空气模板无 BlockEntity，跳过
                    if (!isAir && templateBeTag != null) {
                        CompoundTag beTag = templateBeTag.copy();
                        beTag.putInt("x", placePos.getX());
                        beTag.putInt("y", placePos.getY());
                        beTag.putInt("z", placePos.getZ());

                        BlockEntity newBE = BlockEntity.loadStatic(placePos, templateState, beTag, level.registryAccess());
                        if (newBE != null) {
                            level.setBlockEntity(newBE);
                            newBE.setChanged();
                        }
                    }
                }
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            PlayerCopyData.get((ServerLevel) level).removeP2(player.getUUID());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
