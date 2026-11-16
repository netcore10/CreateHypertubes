package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.blockentities.HyperAcceleratorBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HyperJunctionBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.TubePathBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/04/2025
 * @project Create Hypertube
 * <p>
 * 26.2 / Create Fly port: plain DeferredRegister instead of Registrate. Renderers are registered in ModClientEvents.
 */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HypertubeMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HyperEntranceBlockEntity>> HYPERTUBE_ENTRANCE =
        register("hypertube_entrance_entity", HyperEntranceBlockEntity::new, ModBlocks.HYPERTUBE_ENTRANCE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HypertubeBlockEntity>> HYPERTUBE =
        register("hypertube_entity", HypertubeBlockEntity::new, ModBlocks.HYPERTUBE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HyperAcceleratorBlockEntity>> HYPER_ACCELERATOR =
        register("hyper_accelerator_entity", HyperAcceleratorBlockEntity::new, ModBlocks.HYPER_ACCELERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HyperJunctionBlockEntity>> HYPER_JUNCTION =
        register("hyper_junction_entity", HyperJunctionBlockEntity::new, ModBlocks.HYPER_JUNCTION);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TubePathBlockEntity>> TUBE_PATH =
        register("tube_path_entity", TubePathBlockEntity::new, ModBlocks.TUBE_PATH);

    /** Hypertube's block entities take their type first, like Registrate's factories. */
    @FunctionalInterface
    private interface Factory<T extends BlockEntity> {
        T create(BlockEntityType<?> type, BlockPos pos, BlockState state);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
        String name, Factory<T> factory, Supplier<? extends Block> block
    ) {
        BlockEntityType<T>[] self = new BlockEntityType[1];
        return BLOCK_ENTITIES.register(name, () -> self[0] = new BlockEntityType<>((pos, state) -> factory.create(self[0], pos, state), Set.of(block.get())));
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
