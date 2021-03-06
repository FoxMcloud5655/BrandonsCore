package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.blocks.BlockBCore;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.inventory.ContainerBCore;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.DataUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 12/06/2017.
 * My implementation if IDataManager for tile {@link TileBCBase}
 */
@SuppressWarnings("DuplicatedCode")
public class TileDataManager<T extends TileEntity & IDataManagerProvider> implements IDataManager {

    protected LinkedList<IManagedData> managedDataList = new LinkedList<>();
    public final T tile;

    public TileDataManager(T tile) {
        this.tile = tile;
    }

    /**
     * Use this to create, Configure and register your Managed Data Objects<br>
     * Example Registration:<br><br>
     * <p>
     * public ManagedInt anInt = register(new ManagedInt("anInt", 0, {@link DataFlags#SAVE_BOTH_SYNC_TILE}));
     *
     * @param managedData A new instance of this managed data type.
     * @return Returns a generified data options class. Set the flags you need then call finish to get your shiny new ManagedData object!
     */
    public <M extends IManagedData> M register(M managedData) {
        managedData.init(this, managedDataList.size());
        managedDataList.add(managedData);
        return managedData;
    }

    /**
     * Use this to detect and send changes to the client via your own sync packet. See {@link TileDataManager} for an example
     * This should be called by your tile, container, etc every tick.
     */
    @Override
    public void detectAndSendChanges() {
        for (IManagedData data : managedDataList) {
            if (data.flags().syncTile && data.isDirty(true)) {
                PacketCustom syncPacket = createSyncPacket();
                syncPacket.writeByte((byte) data.getIndex());
                data.toBytes(syncPacket);
                syncPacket.sendToChunk(tile);
            }
        }
    }

    /**
     * This method is called each tick by {@link ContainerBCore} to sent updates to container listeners.
     *
     * @param listeners The list of container listeners.
     */
    public void detectAndSendChangesToListeners(List<IContainerListener> listeners) {
        for (IManagedData data : managedDataList) {
            if (data.flags().syncContainer && data.isDirty(true)) {
                PacketCustom syncPacket = createSyncPacket();
                syncPacket.writeByte((byte) data.getIndex());
                data.toBytes(syncPacket);
//                syncPacket.sendToChunk(tile);
                DataUtils.forEachMatch(listeners, p -> p instanceof ServerPlayerEntity, p -> syncPacket.sendToPlayer((ServerPlayerEntity) p));
            }
        }
    }

    /**
     * You may want to call this when the player opens a container is you have data that is only synced by the container and does not update often.
     * This may be required because normally data only syncs when it changes so if your container data isnt constantly changing the client
     * will see incorrect values until the next sync.
     */
    public void forceContainerSync(List<IContainerListener> listeners) {
        if (!tile.getWorld().isRemote) {
            for (IManagedData data : managedDataList) {
                if (data.flags().syncContainer) {
                    PacketCustom syncPacket = createSyncPacket();
                    syncPacket.writeByte((byte) data.getIndex());
                    data.toBytes(syncPacket);
                    DataUtils.forEachMatch(listeners, p -> p instanceof ServerPlayerEntity, p -> syncPacket.sendToPlayer((ServerPlayerEntity) p));
                }
            }
        }
    }

    public void forceSync() {
        if (!tile.getWorld().isRemote) {
            for (IManagedData data : managedDataList) {
                if (data.flags().syncTile) {
                    PacketCustom syncPacket = createSyncPacket();
                    syncPacket.writeByte((byte) data.getIndex());
                    data.toBytes(syncPacket);
                    syncPacket.sendToChunk(tile);
                }
            }
        }
    }

    public void forcePlayerSync(ServerPlayerEntity player) {
        if (!tile.getWorld().isRemote) {
            for (IManagedData data : managedDataList) {
                if (data.flags().syncContainer) {
                    PacketCustom syncPacket = createSyncPacket();
                    syncPacket.writeByte((byte) data.getIndex());
                    data.toBytes(syncPacket);
                    syncPacket.sendToPlayer(player);
                }
            }
        }
    }

    public void forceSync(IManagedData data) {
        if (!tile.getWorld().isRemote) {
            PacketCustom syncPacket = createSyncPacket();
            syncPacket.writeByte((byte) data.getIndex());
            data.toBytes(syncPacket);
            syncPacket.sendToChunk(tile);
        }
    }

    @Override
    public PacketCustom createSyncPacket() {
        PacketCustom packet = new PacketCustom(BCoreNetwork.CHANNEL, BCoreNetwork.C_TILE_DATA_MANAGER);
        packet.writePos(tile.getPos());
        return packet;
    }

    @Override
    public void receiveSyncData(MCDataInput input) {
        int index = input.readByte() & 0xFF;
        IManagedData data = getDataByIndex(index);
        if (data != null) {
            data.fromBytes(input);
            if (data.flags().triggerUpdate) {
                BlockState state = tile.getWorld().getBlockState(tile.getPos());
                tile.getWorld().notifyBlockUpdate(tile.getPos(), state, state, 3);
            }
        }
    }

    @Override
    public IManagedData getDataByName(String name) {
        return DataUtils.firstMatch(managedDataList, data -> data.getName().equals(name));
    }

    @Override
    public IManagedData getDataByIndex(int index) {
        return DataUtils.firstMatch(managedDataList, data -> data.getIndex() == index);
    }

    @Override
    public void writeToNBT(CompoundNBT compound) {
        CompoundNBT dataTag = new CompoundNBT();
        DataUtils.forEachMatch(managedDataList, data -> data.flags().saveNBT, data -> data.toNBT(dataTag));
        compound.put(BlockBCore.BC_MANAGED_DATA_FLAG, dataTag);
    }

    @Override
    public void readFromNBT(CompoundNBT compound) {
        if (compound.contains(BlockBCore.BC_MANAGED_DATA_FLAG, 10)) {
            CompoundNBT dataTag = compound.getCompound(BlockBCore.BC_MANAGED_DATA_FLAG);
            DataUtils.forEachMatch(managedDataList, data -> data.flags().saveNBT, data -> data.fromNBT(dataTag));
        }
    }

    @Override
    public void markDirty() {
        if (!tile.getWorld().isRemote) {
            tile.markDirty();
            for (IManagedData data : managedDataList) {
                if (data.flags().syncOnSet && data.isDirty(true)) {
                    PacketCustom syncPacket = createSyncPacket();
                    syncPacket.writeByte((byte) data.getIndex());
                    data.toBytes(syncPacket);
                    syncPacket.sendToChunk(tile);
                }
            }
        }
    }

    @Override
    public boolean isClientSide() {
        return tile.hasWorld() && tile.getWorld().isRemote;
    }

    @Override
    public void sendToServer(IManagedData data) {
        if (tile.getWorld().isRemote && data.flags().allowClientControl) {
            PacketCustom packet = new PacketCustom(BCoreNetwork.CHANNEL, BCoreNetwork.S_TILE_DATA_MANAGER);
            packet.writePos(tile.getPos());
            packet.writeByte((byte) data.getIndex());
            data.toBytes(packet);
            packet.sendToServer();
        }
    }

    public void receiveDataFromClient(MCDataInput input) {
        int index = input.readByte() & 0xFF;
        IManagedData data = getDataByIndex(index);
        if (data != null && data.flags().allowClientControl) {
            data.fromBytes(input);
            data.validate(); //This is very important! Assuming this data has a validator this prevents a malicious or bugged client from sending an invalid value.
            data.markDirty();
        }
    }

    /**
     * Used to sync data via getUpdatePacket and getUpdateTag in TileEntity
     */
    public void writeSyncNBT(CompoundNBT compound) {
        CompoundNBT dataTag = new CompoundNBT();
        DataUtils.forEachMatch(managedDataList, data -> data.flags().syncTile, data -> data.toNBT(dataTag));
        compound.put(BlockBCore.BC_MANAGED_DATA_FLAG, dataTag);
    }

    public void readSyncNBT(CompoundNBT compound) {
        if (compound.contains(BlockBCore.BC_MANAGED_DATA_FLAG, 10)) {
            CompoundNBT dataTag = compound.getCompound(BlockBCore.BC_MANAGED_DATA_FLAG);
            DataUtils.forEachMatch(managedDataList, data -> data.flags().syncTile, data -> data.fromNBT(dataTag));
        }
    }

    /**
     * Used to save data to the itemstack when the tile is broken.
     */
    public void writeToStackNBT(CompoundNBT compound) {
        CompoundNBT dataTag = new CompoundNBT();
        DataUtils.forEachMatch(managedDataList, data -> data.flags().saveItem, data -> data.toNBT(dataTag));
        if (!dataTag.isEmpty()) {
            compound.put(BlockBCore.BC_MANAGED_DATA_FLAG, dataTag);
        }
    }

    public void readFromStackNBT(CompoundNBT compound) {
        if (compound.contains(BlockBCore.BC_MANAGED_DATA_FLAG, 10)) {
            CompoundNBT dataTag = compound.getCompound(BlockBCore.BC_MANAGED_DATA_FLAG);
            DataUtils.forEachMatch(managedDataList, data -> data.flags().saveItem, data -> data.fromNBT(dataTag));
        }
    }
}
