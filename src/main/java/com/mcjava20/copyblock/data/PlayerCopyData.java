package com.mcjava20.copyblock.data;

import com.mcjava20.copyblock.CopyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCopyData extends SavedData {
    private static final String DATA_NAME = CopyBlock.MODID + "_player_data";

    // p1：绿方块(第一点)；p2：红方块(第二点)
    private final Map<UUID, BlockPos> point1Map = new HashMap<>();
    private final Map<UUID, BlockPos> point2Map = new HashMap<>();

    public PlayerCopyData() {
    }

    public static PlayerCopyData load(CompoundTag tag, HolderLookup.Provider registries) {
        PlayerCopyData data = new PlayerCopyData();
        ListTag list = tag.getList("PlayerPoints", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID uuid = entry.getUUID("UUID");
            if (entry.contains("P1")) {
                data.point1Map.put(uuid, BlockPos.of(entry.getLong("P1")));
            }
            if (entry.contains("P2")) {
                data.point2Map.put(uuid, BlockPos.of(entry.getLong("P2")));
            }
        }
        return data;
    }

    public static PlayerCopyData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PlayerCopyData::new, PlayerCopyData::load, null),
                DATA_NAME
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        // 合并两个map的全部玩家uuid
        Map<UUID, Boolean> allUuids = new HashMap<>();
        point1Map.keySet().forEach(u -> allUuids.put(u, true));
        point2Map.keySet().forEach(u -> allUuids.put(u, true));

        for (UUID uuid : allUuids.keySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", uuid);
            if(point1Map.containsKey(uuid)){
                entryTag.putLong("P1", point1Map.get(uuid).asLong());
            }
            if(point2Map.containsKey(uuid)){
                entryTag.putLong("P2", point2Map.get(uuid).asLong());
            }
            list.add(entryTag);
        }
        tag.put("PlayerPoints", list);
        return tag;
    }

    public @Nullable BlockPos getP1(UUID uuid) {
        return point1Map.get(uuid);
    }
    public void setP1(UUID uuid, BlockPos pos) {
        point1Map.put(uuid, pos);
        setDirty();
    }
    public void removeP1(UUID uuid) {
        point1Map.remove(uuid);
        setDirty();
    }

    public @Nullable BlockPos getP2(UUID uuid) {
        return point2Map.get(uuid);
    }
    public void setP2(UUID uuid, BlockPos pos) {
        point2Map.put(uuid, pos);
        setDirty();
    }
    public void removeP2(UUID uuid) {
        point2Map.remove(uuid);
        setDirty();
    }

    /**
     * 获取两点包围盒：[minPos, maxPos]
     * 返回null代表p1/p2有一个缺失
     */
    public @Nullable BlockPos[] getBox(UUID uuid){
        BlockPos p1 = getP1(uuid);
        BlockPos p2 = getP2(uuid);
        if(p1 == null || p2 == null) return null;

        int minX = Math.min(p1.getX(), p2.getX());
        int minY = Math.min(p1.getY(), p2.getY());
        int minZ = Math.min(p1.getZ(), p2.getZ());

        int maxX = Math.max(p1.getX(), p2.getX());
        int maxY = Math.max(p1.getY(), p2.getY());
        int maxZ = Math.max(p1.getZ(), p2.getZ());

        return new BlockPos[]{new BlockPos(minX,minY,minZ), new BlockPos(maxX,maxY,maxZ)};
    }
}
