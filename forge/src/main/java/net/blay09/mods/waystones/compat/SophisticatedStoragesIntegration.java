package net.blay09.mods.waystones.compat;

import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.p3pp3rf1y.sophisticatedbackpacks.network.RequestBackpackInventoryContentsMessage;
import net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.network.RequestStorageContentsMessage;
import net.p3pp3rf1y.sophisticatedstorage.network.StoragePacketHandler;

import java.util.UUID;

public class SophisticatedStoragesIntegration implements ISophisticated {
    public SophisticatedStoragesIntegration() {
        PlayerWaystoneManager.setSophisticatedStoragesIntegration(this);
    }

    @Override
    public Container getContainerWithUUID(UUID uuid) {

        if (FMLEnvironment.dist == Dist.CLIENT) StoragePacketHandler.INSTANCE.sendToServer(new RequestStorageContentsMessage(uuid));

        ItemContentsStorage s = ItemContentsStorage.get();

        CompoundTag tag = s.getOrCreateStorageContents(uuid);
        CompoundTag tagWrap = tag.getCompound("storageWrapper");
        CompoundTag tagCont = tagWrap.getCompound("contents");
        CompoundTag tagInv = tagCont.getCompound("inventory");

        ListTag lst = tagInv.getList("Items", Tag.TAG_COMPOUND);

        Container container = new SimpleContainer(lst.size());

        for (int i = 0; i < lst.size(); i++) {
            CompoundTag itemTag = lst.getCompound(i);
            ItemStack stack = ItemStack.of(itemTag);
            container.setItem(i, stack);
            container.getItem(i).setCount(itemTag.getInt("realCount"));
        }

        return container;
    }
}
