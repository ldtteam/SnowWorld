package com.ldtteam.snowworld.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfiguration extends AbstractConfiguration {

    public final ForgeConfigSpec.IntValue accumulationRate;
    public final ForgeConfigSpec.IntValue smoothing;
    public final ForgeConfigSpec.IntValue snowDriftArea;
    public final ForgeConfigSpec.IntValue maxSnowLayers;
    public final ForgeConfigSpec.IntValue snowMeltRate;
    public final ForgeConfigSpec.IntValue snowMinLayers;
    public final ForgeConfigSpec.IntValue minLayersForIce;
    public final ForgeConfigSpec.IntValue minIceBlocksForPackedIce;

    public CommonConfiguration(final ForgeConfigSpec.Builder builder) {
        createCategory(builder, "snowaccumulation");

        accumulationRate = defineInteger(builder, "accumulationrate", 200, 10, 200);
        smoothing = defineInteger(builder , "smoothing", 2, 0, 4);
        snowDriftArea = defineInteger(builder, "driftarea", 5, 1, 9);
        maxSnowLayers = defineInteger(builder, "maxlayers", -1, -1, Integer.MAX_VALUE);
        snowMeltRate = defineInteger(builder, "meltrate", 400, 20, 400);
        snowMinLayers = defineInteger(builder, "minlayers", 1, 0, 8);
        minLayersForIce = defineInteger(builder, "iceforming", 36, 9, Integer.MAX_VALUE);
        minIceBlocksForPackedIce = defineInteger(builder, "icepacking", 3, 0, Integer.MAX_VALUE);

        finishCategory(builder);


    }
}
