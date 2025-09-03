package dev.watukas.kuphack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public interface EntityHelper {

	@Invoker
	public void invokeApplyGravity();
	
	@Invoker
	public void invokeTickBlockCollision();

	@Invoker
	public boolean invokeUpdateWaterState();
	
}
