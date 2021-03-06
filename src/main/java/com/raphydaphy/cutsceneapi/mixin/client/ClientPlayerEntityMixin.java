package com.raphydaphy.cutsceneapi.mixin.client;

import com.raphydaphy.cutsceneapi.cutscene.CutsceneManager;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin
{
	@Shadow
	public abstract boolean isUsingItem();

	@Shadow
	public abstract void method_6023();

	@Inject(at = @At("HEAD"), method = "updateMovement", cancellable = true)
	private void updateMovement(CallbackInfo info)
	{
		if (CutsceneManager.isActive((ClientPlayerEntity) (Object) this))
		{
			if (!CutsceneManager.hideHud((ClientPlayerEntity) (Object) this))
			{
				this.method_6023();
			}
			info.cancel();
		}
	}
}
