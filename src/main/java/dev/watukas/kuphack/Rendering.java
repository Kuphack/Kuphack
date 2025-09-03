package dev.watukas.kuphack;

import java.util.Arrays;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;

import dev.watukas.kuphack.flagclash.FlagClash;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase.Target;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class Rendering {
	
	public static final RenderPipeline SECTION_QUAD_PIPELINE = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET).withLocation(Identifier.of("kuphack", "pipeline/quads"))
			.withCull(false)
			.withBlend(BlendFunction.OVERLAY)
			.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
			.build()
	);
	
	public static final RenderLayer.MultiPhase SECTION_QUADS = RenderLayer.of(
		"section_quads",
		1536,
		false,
		true,
		SECTION_QUAD_PIPELINE,
		RenderLayer.MultiPhaseParameters.builder()
			.target(Target.MAIN_TARGET)
			.layering(RenderLayer.VIEW_OFFSET_Z_LAYERING).build(false)
	);
	
	public static void renderText(List<Text> lines, MatrixStack matrices, VertexConsumerProvider consumer) {
		MinecraftClient client = MinecraftClient.getInstance();
		final int light = 255;
		
		matrices.push();
		matrices.scale(-0.025F, -0.025F, -0.025F);
		matrices.multiply(new Quaternionf().rotateY(client.gameRenderer.getCamera().getYaw() / -MathHelper.DEGREES_PER_RADIAN));
		
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		TextRenderer textRenderer = client.textRenderer;
		for (int i = 0; i < lines.size(); i++) {
			OrderedText text = lines.get(i).asOrderedText();
			float h = textRenderer.getWidth(text) / -2.0f;
			textRenderer.drawWithOutline(text, h, (i - lines.size() + 1) * (textRenderer.fontHeight + 2), -1, 0xFF440000, matrix4f, consumer, light);
		}
		
		matrices.pop();
	}
	
    public static void renderSidebar(DrawContext context, ScoreboardObjective objective) {
    	MinecraftClient client = MinecraftClient.getInstance();
    	TextRenderer textRenderer = client.textRenderer;
		int scaledWidth = client.getWindow().getScaledWidth(),
			scaledHeight= client.getWindow().getScaledHeight();
    	List<Text> lines = Kuphack.getModifiedSidebar();

        Text title = objective.getDisplayName();
        final int titleWidth = textRenderer.getWidth(title);
        int width = titleWidth;

        for (Text line : lines) width = Math.max(width, textRenderer.getWidth(line));

        int footerHeight = lines.size() * textRenderer.fontHeight;
        int bottom = scaledHeight / 2 + footerHeight / 3;

        int textX = scaledWidth - width - 1;
        int footerAlpha = client.options.getTextBackgroundColor(0.3f);
        int i = 0;
        for (Text line : Lists.reverse(lines)) {
            int y = bottom - ++i * textRenderer.fontHeight;
            int right = scaledWidth - 1;
            context.fill(textX - 2, y, right, y + textRenderer.fontHeight, footerAlpha);
            context.drawText(textRenderer, line, textX, y, -1, false);
            
//            if (i != lines.size()) continue;
//            context.fill(textX - 2, y - textRenderer.fontHeight - 1, right, y - 1, tabAlpha);
//            context.fill(textX - 2, y - 1, right, y, footerAlpha);
//            context.drawText(textRenderer, title, (textX + width / 2 - titleWidth / 2), (y - textRenderer.fontHeight), -1, false);
        }
        
	}

	public static void flagClashDebug() {
		MinecraftClient client = MinecraftClient.getInstance();
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
			Identifier.of("kuphack", "flagclash-debug-info"), (context, tickCounter) -> {
				if (SupportedServer.current() != SupportedServer.FLAGCLASH)
					return;
				if (!client.getDebugHud().shouldShowDebugHud())
					return;
				
				TextRenderer textRenderer = client.textRenderer;
				List<String> lines = Arrays.asList(
					"Level: " + FlagClash.getLevel(),
					"Gold: " + FlagClash.toVisualValue(FlagClash.getGold()),
					"GPS: " + FlagClash.getGPS(),
					"Multi: " + FlagClash.getMultiplier()	
				);
				
				int i = lines.size();
				for (String line : lines) {
					Text text = Text.literal(line);
					context.drawTextWithBackground(textRenderer, text, 2, client.getWindow().getScaledHeight() - (i--) * textRenderer.fontHeight, textRenderer.getWidth(text), 0xFFFFFFFF);
				}
			}
		);
	}
	
}
