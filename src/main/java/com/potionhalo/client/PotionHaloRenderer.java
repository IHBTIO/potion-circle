package com.potionhalo.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PotionHaloRenderer {
    private static final float HALO_RADIUS = 0.8f;
    private static final float ICON_SIZE = 0.25f;
    private static final float HALO_HEIGHT = 2.2f;
    private static final float ROTATION_SPEED = 0.5f;

    public static void renderPotionHalo(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            LivingEntity entity,
            int light,
            float tickDelta) {
        
        if (shouldSkipRender(entity)) {
            return;
        }

        Collection<StatusEffectInstance> effects = entity.getStatusEffects();
        if (effects.isEmpty()) {
            return;
        }

        List<StatusEffectInstance> visibleEffects = new ArrayList<>();
        for (StatusEffectInstance effect : effects) {
            if (shouldShowEffect(effect)) {
                visibleEffects.add(effect);
            }
        }

        if (visibleEffects.isEmpty()) {
            return;
        }

        matrices.push();

        float entityHeight = entity.getHeight();
        matrices.translate(0, entityHeight + HALO_HEIGHT - entityHeight, 0);

        long time = System.currentTimeMillis();
        float baseRotation = (time / 1000f) * ROTATION_SPEED * 360f;
        baseRotation = baseRotation % 360f;

        int effectCount = visibleEffects.size();
        float angleStep = 360f / effectCount;

        MinecraftClient client = MinecraftClient.getInstance();
        float partialTick = client.getTickDelta();

        for (int i = 0; i < effectCount; i++) {
            StatusEffectInstance effect = visibleEffects.get(i);
            float angle = baseRotation + (i * angleStep);
            
            matrices.push();

            float radians = (float) Math.toRadians(angle);
            float x = MathHelper.cos(radians) * HALO_RADIUS;
            float z = MathHelper.sin(radians) * HALO_RADIUS;
            
            matrices.translate(x, 0, z);
            
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-angle));
            
            float scale = ICON_SIZE;
            matrices.scale(scale, scale, scale);

            renderEffectIcon(matrices, vertexConsumers, effect, light);

            matrices.pop();
        }

        matrices.pop();
    }

    private static boolean shouldSkipRender(LivingEntity entity) {
        if (entity.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (entity != client.player) {
                return true;
            }
        }

        if (entity.isInvisibleTo(client.player)) {
            return true;
        }

        return false;
    }

    private static boolean shouldShowEffect(StatusEffectInstance effect) {
        return !effect.getEffectType().value().isInstant();
    }

    private static void renderEffectIcon(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            StatusEffectInstance effect,
            int light) {
        
        RegistryEntry<StatusEffect> effectEntry = effect.getEffectType();
        Identifier texture = effectEntry.value().getIconTexture();
        
        if (texture == null) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        RenderSystem.setShaderTexture(0, texture);
        
        float alpha = calculateAlpha(effect);
        
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        int x = -8;
        int y = -8;
        int width = 16;
        int height = 16;

        RenderSystem.disableDepthTest();
        
        DrawContext drawContext = new DrawContext(
            MinecraftClient.getInstance(),
            MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers()
        );
        
        drawContext.drawTexture(texture, x, y, 0, 0, width, height, width, height);
        
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static float calculateAlpha(StatusEffectInstance effect) {
        int duration = effect.getDuration();
        
        if (duration < 200) {
            float flash = (System.currentTimeMillis() % 500) / 500f;
            return 0.3f + (flash * 0.7f);
        }
        
        return 0.9f;
    }

    private static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }
}
