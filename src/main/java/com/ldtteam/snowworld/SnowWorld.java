package com.ldtteam.snowworld;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;

import com.ldtteam.snowworld.config.Configuration;
import com.ldtteam.snowworld.util.LanguageHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

@Mod("snowworld")
public class SnowWorld {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public SnowWorld() {
        LanguageHandler.loadLangPath("assets/snowworld/lang/%s.json");
        LOGGER.info("Creating SnowWorld config: " + Configuration.getInstance().getClass().getSimpleName() + " class created and populated.");

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ForgeRegistries.BIOMES.getValues().forEach(biome -> {
            biome.precipitation = Biome.RainType.SNOW;
        });
    }

}
