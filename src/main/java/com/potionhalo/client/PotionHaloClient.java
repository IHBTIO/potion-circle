package com.potionhalo.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PotionHaloClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("potionhalo");
    public static final String MOD_ID = "potionhalo";

    @Override
    public void onInitializeClient() {
        LOGGER.info("Potion Halo mod initialized");
    }
}
