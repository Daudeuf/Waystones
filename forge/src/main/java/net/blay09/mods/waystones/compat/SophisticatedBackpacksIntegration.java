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
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.network.RequestBackpackInventoryContentsMessage;
import net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler;
import net.p3pp3rf1y.sophisticatedstorage.network.RequestStorageContentsMessage;
import net.p3pp3rf1y.sophisticatedstorage.network.StoragePacketHandler;

import java.util.UUID;

public class SophisticatedBackpacksIntegration implements ISophisticated {
    public SophisticatedBackpacksIntegration() {
        PlayerWaystoneManager.setSophisticatedBackpacksIntegration(this);
    }

    @Override
    public Container getContainerWithUUID(UUID uuid) {

        if (FMLEnvironment.dist == Dist.CLIENT) SBPPacketHandler.INSTANCE.sendToServer(new RequestBackpackInventoryContentsMessage(uuid));

        BackpackStorage s = BackpackStorage.get();

        CompoundTag tag = s.getOrCreateBackpackContents(uuid);
        CompoundTag tagInv = tag.getCompound("inventory");

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
