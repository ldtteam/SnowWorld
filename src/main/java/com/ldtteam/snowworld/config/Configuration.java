package com.ldtteam.snowworld.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class Configuration {
    private static final Configuration INSTANCE = new Configuration();

    public static Configuration getInstance()
    {
        return INSTANCE;
    }

    private final CommonConfiguration commonConfiguration;

    private Configuration() {
        final Pair<CommonConfiguration, ForgeConfigSpec> commonConfigSpec = new ForgeConfigSpec.Builder().configure(CommonConfiguration::new);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, commonConfigSpec.getRight());

        commonConfiguration = commonConfigSpec.getLeft();
    }

    public CommonConfiguration getCommonConfiguration() {
        return commonConfiguration;
    }
}
