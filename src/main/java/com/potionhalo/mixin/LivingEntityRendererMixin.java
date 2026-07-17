package com.potionhalo.mixin;

import com.potionhalo.client.SafeHaloRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> extends EntityRenderer<T> {
    
    protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(
        method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("TAIL")
    )
    private void onRender(
            T livingEntity,
            float f,
            float g,
            MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider,
            int i,
            CallbackInfo ci) {
        
        if (livingEntity == null) {
            return;
        }

        if (livingEntity.getWorld() == null) {
            return;
        }

        SafeHaloRenderer.render(matrixStack, vertexConsumerProvider, livingEntity, i, g);
    }
}
