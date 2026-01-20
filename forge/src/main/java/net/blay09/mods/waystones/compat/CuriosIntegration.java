package net.blay09.mods.waystones.compat;

import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CuriosIntegration implements ICurios {
    public CuriosIntegration() {
        PlayerWaystoneManager.setCuriosIntegration(this);
    }

    @Override
    public Container getContainerForCuriosInv(Player player) {
        AtomicReference<Container> c = new AtomicReference<>();
        AtomicInteger count = new AtomicInteger();

        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            c.set(new SimpleContainer(curiosInventory.getSlots()));
            Map<String, ICurioStacksHandler> curios = curiosInventory.getCurios();
            curios.forEach((identifier, slotInventory) -> {
                for (int i = 0; i < slotInventory.getSlots(); i++) {
                    c.get().setItem(count.getAndIncrement(), slotInventory.getStacks().getStackInSlot(i));
                }
            });
        });

        return c.get();
    }
}
