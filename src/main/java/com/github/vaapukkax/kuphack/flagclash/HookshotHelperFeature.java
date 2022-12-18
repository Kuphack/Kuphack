package com.github.vaapukkax.kuphack.flagclash;

import java.util.Arrays;
import java.util.List;

import com.github.vaapukkax.kuphack.Event.EventHolder;
import com.github.vaapukkax.kuphack.Event.EventMention;
import com.github.vaapukkax.kuphack.Feature;
import com.github.vaapukkax.kuphack.SupportedServer;
import com.github.vaapukkax.kuphack.events.BlockInteractEvent;
import com.github.vaapukkax.kuphack.events.InteractEvent;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class HookshotHelperFeature extends Feature implements HudRenderCallback, EventHolder {

	private static final List<Block> BLOCKS = Arrays.asList(Blocks.HAY_BLOCK, Blocks.TARGET);
	
	public HookshotHelperFeature() {
		super("Has an indicator on the crosshair when you are capable of hooking into a hook. Also disallows shooting when not looking at one.", SupportedServer.FLAGCLASH);
		HudRenderCallback.EVENT.register(this);
	}
	
	@EventMention
	public void onEvent(InteractEvent e) {
		if (e.getItem() == Items.GOLDEN_HOE && !isHookAhead()) e.setCancelled(true);
	}
	
	@EventMention
	public void onEvent(BlockInteractEvent e) {
		if (e.getItem() == Items.GOLDEN_HOE && (!BLOCKS.contains(e.getBlock()) && !e.getState().isIn(BlockTags.DOORS))) {
			System.out.println("aaaaaa");
			e.setCancelled(true);
		}
	}

	@Override
	public void onHudRender(MatrixStack matrices, float tickDelta) {
		if (!isPlaying() || !isHookAhead()) return;
		client.textRenderer.draw(matrices, ">    <",
			client.getWindow().getScaledWidth() / 2 - client.textRenderer.getWidth(">    <") / 2,
			client.getWindow().getScaledHeight()/2 - client.textRenderer.fontHeight / 2 + 1
		, 0xffffff);
	}
	
	public boolean isHookAhead() {
		HitResult result = client.player.raycast(this.getHookReach(), client.getTickDelta(), true);
		if (result instanceof BlockHitResult blockResult) {
			Block block = client.player.world.getBlockState(blockResult.getBlockPos()).getBlock();
			return BLOCKS.contains(block);
		}
		return false;
	}
	
	public double getHookReach() {
		boolean found = false;
		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = client.player.getInventory().getStack(slot);
			if (stack.getItem() != Items.GOLDEN_HOE) continue;
			String id = stack.getNbt().getString("objectID");
			if (id.equals("item_SuperHookshot")) return 30;
			else if (id.equals("item_Hookshot")) found = true;
		}
		return found ? 22 : 0;
	}
	
}
