package com.potionhalo.client;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AntiCheatHelper {
    
    public static boolean canRenderEntity(LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        if (!entity.isAlive()) {
            return false;
        }

        if (entity.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            return false;
        }

        if (entity.isInvisible()) {
            return false;
        }

        return true;
    }

    public static List<StatusEffectInstance> getVisibleEffects(LivingEntity entity) {
        List<StatusEffectInstance> visibleEffects = new ArrayList<>();
        
        Collection<StatusEffectInstance> allEffects = entity.getStatusEffects();
        
        for (StatusEffectInstance effect : allEffects) {
            if (isEffectVisible(effect)) {
                visibleEffects.add(effect);
            }
        }
        
        return visibleEffects;
    }

    private static boolean isEffectVisible(StatusEffectInstance effect) {
        if (effect == null) {
            return false;
        }

        if (effect.getEffectType().value().isInstant()) {
            return false;
        }

        return true;
    }

    public static boolean isSafeToRender() {
        return true;
    }
}
