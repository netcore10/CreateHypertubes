package com.pedrorok.hypertube;

import net.minecraft.resources.Identifier;

import com.pedrorok.hypertube.config.ClientConfig;
import com.pedrorok.hypertube.config.ServerConfig;
import com.pedrorok.hypertube.core.smarttube.ITubeAttachment;
import com.pedrorok.hypertube.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/04/2025
 * @project Create Hypertube
 */
@Mod(HypertubeMod.MOD_ID)
public class HypertubeMod {
    public static final String MOD_ID = "create_hypertube";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);


    public HypertubeMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, MOD_ID + "-client.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, MOD_ID + "-server.toml");

        // Partial models are client-only (Create Fly's flywheel/catnip client classes)
        if (FMLEnvironment.getDist().isClient()) {
            ModPartialModels.init();
        }

        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);

        ModCreativeTab.register(modEventBus);
        ModDataComponent.register(modEventBus);

        ModParticles.register(modEventBus);

        ModSounds.register(modEventBus);

        ITubeAttachment.init();

    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }


    public static Identifier of(String resourceId) {
        return Identifier.fromNamespaceAndPath(MOD_ID, resourceId);
    }
}
