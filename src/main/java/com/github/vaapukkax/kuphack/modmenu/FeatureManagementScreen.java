package com.github.vaapukkax.kuphack.modmenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.vaapukkax.kuphack.Feature;
import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.SupportedServer;
import com.github.vaapukkax.kuphack.flagclash.FriendFeature;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class FeatureManagementScreen extends Screen {

	private ButtonList buttonList;

	private final Screen parent;
	private boolean initialized;

	public FeatureManagementScreen(Screen parent) {
		super(Text.of("Feature Management"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		if (initialized) this.clearChildren();
		this.buttonList = new ButtonList(this.client, this);

		Map<List<SupportedServer>, List<Feature>> map = this.getSharedServers();
		for (List<SupportedServer> group : map.keySet()) {
			Text groupText = Text.of("-- " + group.stream().map(SupportedServer::toString).collect(Collectors.joining(" & ")) + " --");
			int groupWidth = this.textRenderer.getWidth(groupText);
			TextWidget groupWidget = new TextWidget(
				this.width / 2 - groupWidth / 2, 0,
				groupWidth, 28,
				groupText, this.textRenderer
			);
			buttonList.addWidget(groupWidget);
			for (Feature feature : map.get(group)) {
				Text text = Text.of(feature.getName() + (feature instanceof FriendFeature ? "..." : " ["+feature.getTextState()+"]"));
				ButtonWidget widget = ButtonWidget.builder(text, button -> {
					if (feature instanceof FriendFeature friendFeature) {
						boolean loaded = true;
						for (UUID uuid : friendFeature.getFriends()) {
							if (!FriendManagementScreen.names.containsKey(uuid)) loaded = false;
						}
						if (!loaded) client.setScreenAndRender(new MessageScreen(Text.of("Loading friends...")));
						client.setScreen(new FriendManagementScreen(this));
					} else {
						feature.toggle();
						button.setMessage(Text.of(feature.getName() + " ["+feature.getTextState()+"]"));
					}
				}).tooltip(Tooltip.of(Text.of(
					feature.getDescription()
				))).position(this.width / 2 - 150 / 2, 0).build();
				this.buttonList.addWidget(widget);
			}
		}
		this.addSelectableChild(this.buttonList);
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close())
			.position(this.width / 2 - 100, this.height - 28).width(200).build());
		this.initialized = true;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context);
		this.buttonList.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}

	public Map<List<SupportedServer>, List<Feature>> getSharedServers() {
		Map<List<SupportedServer>, List<Feature>> map = new LinkedHashMap<>();
		for (Feature feature : Kuphack.get().getFeatures()) {
			List<SupportedServer> list = Arrays.asList(feature.servers);
			map.computeIfAbsent(list, key -> new ArrayList<>()).add(feature);
		}
		return Collections.unmodifiableMap(map);
	}

}