package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(modid = HypertubeMod.MOD_ID)
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HypertubeMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TUBE_TAB =
            CREATIVE_MODE_TABS.register("create_hypertubes", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + HypertubeMod.MOD_ID))
                            .icon(ModBlocks.HYPERTUBE::toStack)
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    @SubscribeEvent
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(TUBE_TAB.getKey())) {
            for (var entry : ModItems.ITEMS.getEntries()) {
                if (entry == ModItems.TUBE_SCANNER_UNFINISHED) continue;
                event.accept(entry.get());
            }
        }
    }

}