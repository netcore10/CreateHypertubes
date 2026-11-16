package com.pedrorok.hypertube.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = HypertubeMod.MOD_ID, value = Dist.CLIENT)
public enum ModKeybinds {

	ESCAPE("tube_escape", GLFW.GLFW_KEY_LEFT_SHIFT);



	private final KeyMapping mapping;

	ModKeybinds(String description, int defaultKey) {
		this.mapping = new KeyMapping("key.hypertube." + description, defaultKey, Categories.HYPERTUBE);
	}

	public boolean isDown() {
		return !this.mapping.isUnbound() && isKeyPressed();
	}

	private boolean isKeyPressed() {
		int keyCode = mapping.getKey().getValue();
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), keyCode) && mapping.isConflictContextAndModifierActive();
	}

	public Component message() {
		return this.mapping.getTranslatedKeyMessage();
	}

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) {
		event.registerCategory(Categories.HYPERTUBE);
		for (ModKeybinds key : values()) {
			event.register(key.mapping);
		}
	}

	/** 26.2: key categories are registered objects (label key: key.category.create_hypertube.hypertube). Held outside the enum, which can't read its own statics in constructors. */
	private static final class Categories {
		static final KeyMapping.Category HYPERTUBE = new KeyMapping.Category(HypertubeMod.of("hypertube"));
	}
}
