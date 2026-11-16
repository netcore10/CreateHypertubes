package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperAcceleratorBlock;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.TubePathBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/04/2025
 * @project Create Hypertube
 * <p>
 * 26.2 / Create Fly port: plain DeferredRegister instead of Create's Registrate (not in Create Fly). Block models, states,
 * loot and mineable tags come from the checked-in Registrate datagen output (src/generated).
 */
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HypertubeMod.MOD_ID);

    private static BlockBehaviour.Properties properties(BlockBehaviour.Properties p) {
        return p.destroyTime(1.0f)
            .explosionResistance(10.0f)
            .sound(SoundType.METAL)
            .noOcclusion()
            .isViewBlocking((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false);
    }

    public static final DeferredBlock<HypertubeBlock> HYPERTUBE = BLOCKS.registerBlock("hypertube", HypertubeBlock::new, ModBlocks::properties);
    public static final DeferredBlock<HyperEntranceBlock> HYPERTUBE_ENTRANCE = BLOCKS.registerBlock("hypertube_entrance", HyperEntranceBlock::new, ModBlocks::properties);
    public static final DeferredBlock<HyperAcceleratorBlock> HYPER_ACCELERATOR = BLOCKS.registerBlock("hypertube_accelerator", HyperAcceleratorBlock::new, ModBlocks::properties);
    public static final DeferredBlock<HyperJunctionBlock> HYPER_JUNCTION = BLOCKS.registerBlock("hypertube_junction", HyperJunctionBlock::new, ModBlocks::properties);
    public static final DeferredBlock<TubePathBlock> TUBE_PATH = BLOCKS.registerBlock("tube_path", TubePathBlock::new, p -> p
        .strength(-1.0F, 3600000.0F)
        .noOcclusion()
        .noLootTable()
        .isViewBlocking((state, level, pos) -> false)
        .isSuffocating((state, level, pos) -> false));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
