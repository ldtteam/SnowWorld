package com.ldtteam.snowworld.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfiguration extends AbstractConfiguration {
    //@Comment({"How quickly snow accumulates [200 = slow, 10 = fast]"})
    public final ForgeConfigSpec.IntValue accumulationRate; // = 200;

    //@Comment({"How many sides need to be at the same level before more snow is allowed to accumulate [0 = none, 4 = all]"})
    public final ForgeConfigSpec.IntValue smoothing; // = 2;

    //@Comment({"Number of blocks to increase the snow level at around a snow accumulation event [1 = single block, 5 = a + sign, 9 = all around"})
    public final ForgeConfigSpec.IntValue snowDriftArea; // = 5;

    //@Comment({"Max snow layers, 8 layers per block"})
    public final ForgeConfigSpec.IntValue maxSnowLayers; // = 8;

    //@Comment({"How quickly snow melts [400 = slow, 20 = fast]"})
    public final ForgeConfigSpec.IntValue snowMeltRate; // = 400;

    //@Comment({"Base snow layers to leave when melting"})
    public final ForgeConfigSpec.IntValue snowMinLayers; // = 1;

    public CommonConfiguration(final ForgeConfigSpec.Builder builder) {
        createCategory(builder, "snowaccumulation");

        accumulationRate = defineInteger(builder, "accumulationrate", 200, 10, 200);
        smoothing = defineInteger(builder , "smoothing", 2, 0, 4);
        snowDriftArea = defineInteger(builder, "driftarea", 5, 1, 9);
        maxSnowLayers = defineInteger(builder, "maxlayers", 8, 0, 8);
        snowMeltRate = defineInteger(builder, "meltrate", 400, 20, 400);
        snowMinLayers = defineInteger(builder, "minlayers", 1, 0, 8);

        finishCategory(builder);
    }
}
