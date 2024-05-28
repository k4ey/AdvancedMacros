package com.theincgi.advancedmacros.misc;

import java.util.LinkedList;

import com.mojang.blaze3d.systems.RenderSystem;

public class RenderUtils {
	private static LinkedList<float[]> shaderColorStack = new LinkedList<>();
	
	public static void pushShaderColor() {
		shaderColorStack.add( RenderSystem.getShaderColor() );
	}
	
	public static void popShaderColor() {
		float[] color = shaderColorStack.removeLast();
		float r = color[0];
		float g = color[1];
		float b = color[2];
		float a = color[3];
		RenderSystem.setShaderColor(r, g, b, a);
	}
}
