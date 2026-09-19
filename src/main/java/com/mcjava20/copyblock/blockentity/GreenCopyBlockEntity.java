package com.mcjava20.copyblock.blockentity;

import com.mcjava20.copyblock.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class GreenCopyBlockEntity extends BlockEntity {

    private UUID playerUUID;

    /** 模板方块状态（p1 上方那一格） */
    @Nullable
    private BlockState templateState;

    /** 模板方块的 BlockEntity NBT（可为 null） */
    @Nullable
    private CompoundTag templateBlockEntityTag;

    /**
     * 是否已"装填"：绿方块上方曾经放过非空气方块。
     * 一旦为 true，即使上方被挖成空气也保持 true，允许复制空气来移除方块。
     * 仅在绿方块刚放置且上方一直是空气时为 false（此时禁止复制）。
     */
    private boolean armed;

    public GreenCopyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GREEN_COPY_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * 读取 p1 上方那一格作为模板（含 NBT）。
     * 上方为非空气方块时会把 armed 置 true；为空气时不改变 armed。
     */
    public void captureTemplate(BlockPos p1) {
        Level level = this.getLevel();
        if (level == null) return;

        BlockPos templatePos = p1.above();
        BlockState state = level.getBlockState(templatePos);

        this.templateState = state;
        this.templateBlockEntityTag = null;

        // 上方放有实际方块时才"装填"，挖成空气时保留已装填状态
        if (!state.isAir()) {
            this.armed = true;
        }

        BlockEntity be = level.getBlockEntity(templatePos);
        if (be != null) {
            this.templateBlockEntityTag = be.saveWithFullMetadata(level.registryAccess());
        }
        setChanged();
    }

    public void clearTemplate() {
        this.templateState = null;
        this.templateBlockEntityTag = null;
        setChanged();
    }

    @Nullable
    public BlockState getTemplateState() {
        return templateState;
    }

    @Nullable
    public CompoundTag getTemplateBlockEntityTag() {
        return templateBlockEntityTag;
    }

    public boolean hasTemplate() {
        return templateState != null;
    }

    /**
     * 是否已装填（上方曾经放过非空气方块）。LIT 与是否允许复制都依赖此状态。
     */
    public boolean isArmed() {
        return armed;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID uuid) {
        this.playerUUID = uuid;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (playerUUID != null) {
            tag.putUUID("PlayerUUID", playerUUID);
        }
        if (templateState != null) {
            tag.put("TemplateState", NbtUtils.writeBlockState(templateState));
        }
        if (templateBlockEntityTag != null) {
            tag.put("TemplateBlockEntity", templateBlockEntityTag.copy());
        }
        tag.putBoolean("Armed", armed);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("PlayerUUID")) {
            playerUUID = tag.getUUID("PlayerUUID");
        }
        if (tag.contains("TemplateState")) {
            templateState = NbtUtils.readBlockState(
                    registries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),
                    tag.getCompound("TemplateState")
            );
        }
        if (tag.contains("TemplateBlockEntity")) {
            templateBlockEntityTag = tag.getCompound("TemplateBlockEntity").copy();
        }
        armed = tag.getBoolean("Armed");
    }
}
