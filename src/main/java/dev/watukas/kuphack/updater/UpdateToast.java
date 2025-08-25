package dev.watukas.kuphack.updater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UpdateToast implements Toast {
	
	private static final Identifier BACKGROUND_MIDDLE = Identifier.of("kuphack", "textures/gui/toast/middle.png");
	private static final Identifier BACKGROUND_BOTTOM = Identifier.of("kuphack", "textures/gui/toast/bottom.png");
	private static final Identifier BACKGROUND_TOP = Identifier.of("kuphack", "textures/gui/toast/top.png");
	
	public static final int DEFAULT_DURATION_MS = 5000;
	private final UpdateStatus status;
	private Toast.Visibility visibility = Toast.Visibility.HIDE;

	private final int height;
	
	public UpdateToast(UpdateStatus status) {
		this.status = status;
		
		MinecraftClient client = MinecraftClient.getInstance();
		this.height = content().size() * client.textRenderer.fontHeight + 8;
	}

	@Override
	public Toast.Visibility getVisibility() {
		return this.visibility;
	}

	@Override
	public void update(ToastManager manager, long time) {
		this.visibility = time >= 10_000 * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
	}

	@Nullable
	@Override
	public SoundEvent getSoundEvent() {
		return this.status != null && status.release() != null ? SoundEvents.ENTITY_VILLAGER_AMBIENT : SoundEvents.ENTITY_VILLAGER_NO;
	}
	
	public List<OrderedText> content() {
		if (this.status == null)
			return Arrays.asList(Text.of("Couldn't retrieve update status, an error occurred?").asOrderedText());
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		ArrayList<OrderedText> text = new ArrayList<>();
		if (status.additional() != null) {
			text.add(status.additional().asOrderedText());
			text.add(OrderedText.empty());
		}
		if (status.release() != null) {
			text.add(Text.of("Kuphack Update " + status.release().tag()).asOrderedText());
			text.add(OrderedText.empty());
			status.release().body().lines().forEach(line -> {
				text.addAll(textRenderer.wrapLines(Text.of(line), this.getWidth() - 10));
			});
		}
		return text;
	}
	
	@Override
	public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
		int height = this.getHeight();
		context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TOP, 0, 0, 0, 0, this.getWidth(), 4, 200, 4);
		int b = 4;
		for (int i = 4; i < height - 4; i++) {
			context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_MIDDLE, 0, b, 0, 0, this.getWidth(), 1, 200, 1);
			b++;
		}
		context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_BOTTOM, 0, height - 4, 0, 0, this.getWidth(), 4, 200, 4);
		
		int i = 0;
		for (OrderedText text : content()) {
			context.drawText(textRenderer, text, 8, 5 + i * textRenderer.fontHeight, -1, false);
			i++;
		}
	}
	
	
	@Override
	public int getWidth() {
		return 200;
	}
	
	@Override
	public int getHeight() {
		return this.height;
	}
	
}
