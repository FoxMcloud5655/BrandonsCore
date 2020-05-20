package com.brandon3055.brandonscore.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.RenderState;

/**
 * Created by FoxMcloud5655 on 19/5/2020.
 * Helper function hold the new render types since they are protected in Minecraft by default.
 */
public class RenderHelperBC {
	public static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = new RenderState.TransparencyState("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
     }, () -> {
        RenderSystem.disableBlend();
     });
}
