package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.items.HypertubeItem;
import com.pedrorok.hypertube.items.TubeAttachmentItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/04/2025
 * @project Create Hypertube
 * <p>
 * 26.2 / Create Fly port: plain DeferredRegister instead of Registrate. Block items are registered here too.
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HypertubeMod.MOD_ID);

    public static final DeferredItem<HypertubeItem> HYPERTUBE = ITEMS.registerItem("hypertube",
        p -> new HypertubeItem(ModBlocks.HYPERTUBE.get(), p), Item.Properties::useBlockDescriptionPrefix);
    public static final DeferredItem<BlockItem> HYPERTUBE_ENTRANCE = ITEMS.registerSimpleBlockItem(ModBlocks.HYPERTUBE_ENTRANCE);
    public static final DeferredItem<BlockItem> HYPER_ACCELERATOR = ITEMS.registerSimpleBlockItem(ModBlocks.HYPER_ACCELERATOR);
    public static final DeferredItem<BlockItem> HYPER_JUNCTION = ITEMS.registerSimpleBlockItem(ModBlocks.HYPER_JUNCTION);

    public static final DeferredItem<Item> HYPERTUBE_FUNNEL = ITEMS.registerSimpleItem("hypertube_funnel");
    public static final DeferredItem<TubeAttachmentItem> REDSTONE_DETECTOR = ITEMS.registerItem("redstone_detector_tube_attachment",
        p -> new TubeAttachmentItem("redstone_input", p));
    public static final DeferredItem<TubeAttachmentItem> TUBE_SCANNER = ITEMS.registerItem("tube_scanner_attachment",
        p -> new TubeAttachmentItem("tube_scanner", p));
    public static final DeferredItem<Item> TUBE_SCANNER_UNFINISHED = ITEMS.registerSimpleItem("tube_scanner_unfinished");

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
