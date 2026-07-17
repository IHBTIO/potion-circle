package com.potionhalo.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

public class SafeHaloRenderer {
    private static final float HALO_RADIUS = 0.7f;
    private static final float ICON_SIZE = 0.2f;
    private static final float HALO_HEIGHT_OFFSET = 0.5f;
    private static final float ROTATION_SPEED = 40f;

    public static void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            LivingEntity entity,
            int light,
            float tickDelta) {

        if (!AntiCheatHelper.canRenderEntity(entity)) {
            return;
        }

        if (!AntiCheatHelper.isSafeToRender()) {
            return;
        }

        List<StatusEffectInstance> effects = AntiCheatHelper.getVisibleEffects(entity);
        
        if (effects.isEmpty()) {
            return;
        }

        renderHalo(matrices, vertexConsumers, entity, effects, light, tickDelta);
    }

    private static void renderHalo(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            LivingEntity entity,
            List<StatusEffectInstance> effects,
            int light,
            float tickDelta) {

        matrices.push();

        float height = entity.getHeight() + HALO_HEIGHT_OFFSET;
        matrices.translate(0, height, 0);

        MinecraftClient client = MinecraftClient.getInstance();
        float time = (client.player == null ? 0 : client.player.age) + tickDelta;
        float baseRotation = time * ROTATION_SPEED;
        baseRotation = baseRotation % 360f;

        int count = effects.size();
        float angleStep = 360f / count;

        for (int i = 0; i < count; i++) {
            StatusEffectInstance effect = effects.get(i);
            float angle = baseRotation + (i * angleStep);

            matrices.push();

            float radians = (float) Math.toRadians(angle);
            float x = MathHelper.cos(radians) * HALO_RADIUS;
            float z = MathHelper.sin(radians) * HALO_RADIUS;

            matrices.translate(x, 0, z);

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-angle));

            float scale = ICON_SIZE;
            matrices.scale(scale, scale, scale);

            renderIcon(matrices, effect, light);

            matrices.pop();
        }

        matrices.pop();
    }

    private static void renderIcon(MatrixStack matrices, StatusEffectInstance effect, int light) {
        RegistryEntry<StatusEffect> effectEntry = effect.getEffectType();
        Identifier texture = effectEntry.value().getIconTexture();

        if (texture == null) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, texture);

        float alpha = getEffectAlpha(effect);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        RenderSystem.disableDepthTest();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getBufferBuilders() != null) {
            renderTextureQuad(matrices, texture);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderTextureQuad(MatrixStack matrices, Identifier texture) {
        RenderSystem.setShaderTexture(0, texture);
        
        int x = -8;
        int y = -8;
        int size = 16;

        RenderSystem.drawTexture(
            texture,
            x, y,
            0, 0,
            size, size,
            size, size
        );
    }

    private static float getEffectAlpha(StatusEffectInstance effect) {
        int duration = effect.getDuration();

        if (duration < 200) {
            float flash = (System.currentTimeMillis() % 250) / 250f;
            return 0.4f + (flash * 0.6f);
        }

        return 0.85f;
    }
}
