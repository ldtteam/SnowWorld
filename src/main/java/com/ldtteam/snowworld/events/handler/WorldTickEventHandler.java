package com.ldtteam.snowworld.events.handler;

import java.util.Random;

import com.ldtteam.snowworld.config.Configuration;
import com.ldtteam.snowworld.util.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.jmx.Server;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldTickEventHandler {

    private static Random r = new Random();

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END)
            return;

        if (!(event.world instanceof ServerWorld))
            return;

        ServerWorld world = (ServerWorld) event.world;


        if (world.isRaining()) {
            onTickSnowIncrease(world);
        } else if (world.dimension.isDaytime()) {
            onTickSnowDecrease(world);
        }
    }

    private static BlockPos getRandomPosInChunk(Chunk chunk) {
        int inWorldXPos = chunk.getPos().getXStart();
        int inWorldZPos = chunk.getPos().getZStart();

        int posX = r.nextInt(16);
        int posZ = r.nextInt(16);
        return new BlockPos(inWorldXPos + posX, 0, inWorldZPos + posZ);
    }

    private static BlockPos getSnowTopPosition(ServerWorld world, BlockPos pos) {
        final ChunkPos chunkPos = new ChunkPos(pos);
        final int inWorldXPos = chunkPos.getXStart();
        final int inWorldZPos = chunkPos.getZStart();
        pos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(inWorldXPos, 0, inWorldZPos, 15));

        // Precipitation height ignores snow blocks, need to loop to the top of the stack
        while (world.getBlockState(pos.up()).getBlock() == Blocks.SNOW) {
            pos = pos.up();
        }

        return pos;
    }

    private static BlockPos getIceTopPosition(ServerWorld world, BlockPos pos) {
        BlockPos blockPos = getSnowTopPosition(world, pos);
        while (world.getBlockState(blockPos).getBlock() == Blocks.SNOW) {
            blockPos.down();
        }

        if (world.getBlockState(blockPos).getBlock() != Blocks.ICE)
        {
            return new BlockPos(-1, -1, -1);
        }

        return blockPos;
    }

    private static boolean isSnowyArea(ServerWorld world, BlockPos pos) {
        return world.getBiome(pos).getTemperature(pos) < 0.15F;
    }

    private static void onTickSnowDecrease(ServerWorld world) {
        for (final ChunkHolder iterator : world.getChunkProvider().chunkManager.getLoadedChunksIterable()) {
            Chunk chunk = iterator.func_219298_c();
            if (chunk == null)
                continue;

            if (r.nextInt(Configuration.getInstance().getCommonConfiguration().snowMeltRate.get()) != 0) {
                continue;
            }

            BlockPos pos = getRandomPosInChunk(chunk);

            pos = getSnowTopPosition(world, pos);

            int layers = snowHeightAt(world, pos);

            if (layers <= Configuration.getInstance().getCommonConfiguration().snowMinLayers.get()) {
                continue;
            }

            if (layers % 8 != 1) {
                // decrement layer
                world.setBlockState(pos, Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, (layers-1)%8));
            } else {
                //remove last layer
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
    }

    private static void onTickSnowIncrease(ServerWorld world) {
        int baseRate = Configuration.getInstance().getCommonConfiguration().accumulationRate.get();

        if (world.isThundering()) {
            baseRate /= 2;
        }


        for (final ChunkHolder iterator : world.getChunkProvider().chunkManager.getLoadedChunksIterable()) {
            Chunk chunk = iterator.func_219298_c();
            if (chunk == null)
                continue;

            if (r.nextInt(baseRate) != 0) {
                continue;
            }

            if (!world.dimension.canDoRainSnowIce(chunk)) {
                continue;
            }

            BlockPos pos = getRandomPosInChunk(chunk);

            pos = getSnowTopPosition(world, pos);
            int layers = snowHeightAt(world, pos);

            int surroundingAtLayer = 0;
            for(Direction side : Direction.Plane.HORIZONTAL){
                if (snowHeightAt(world, getSnowTopPosition(world, pos.offset(side))) >= layers) {
                    surroundingAtLayer++;
                }
            }

            if (surroundingAtLayer < Configuration.getInstance().getCommonConfiguration().smoothing.get()) {
                continue;
            }

            switch(Configuration.getInstance().getCommonConfiguration().snowDriftArea.get()) {
            case 9:
                incrementSnowHeight(world, pos.north().east());
            case 8:
                incrementSnowHeight(world, pos.south().west());
            case 7:
                incrementSnowHeight(world, pos.north().west());
            case 6:
                incrementSnowHeight(world, pos.south().east());
            case 5:
                incrementSnowHeight(world, pos.west());
            case 4:
                incrementSnowHeight(world, pos.east());
            case 3:
                incrementSnowHeight(world, pos.north());
            case 2:
                incrementSnowHeight(world, pos.south());
            case 1:
                incrementSnowHeight(world, pos);
            }
        }
    }

    private static void incrementSnowHeight(ServerWorld world, BlockPos pos) {
        pos = getSnowTopPosition(world, pos);

        int layers = snowHeightAt(world, pos);

        // Check if we can snow here if this is the first snow layer
        if(layers == 0 && !canSnowAt(world, pos)) {
            return;
        } else if (!isSnowyArea(world, pos)) {
            return;
        }

        if (layers >= Configuration.getInstance().getCommonConfiguration().maxSnowLayers.get() && Configuration.getInstance().getCommonConfiguration().maxSnowLayers.get() > -1) {
            return;
        }

        if (layers == 0 || layers % 8 != 0) {
            // Continue stacking on current stack
            world.setBlockState(pos, Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, layers%8+1));
        } else {
            // Add onto stack on block above, this one is full
            world.setBlockState(pos.up(), Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, layers%8+1));
        }

        if (canSnowTurnIntoIce(world, pos))
        {
            final BlockPos icePos = findLowestSnowBlockOfTopStack(world, pos);
            world.setBlockState(icePos, Blocks.ICE.getDefaultState(), 6);
        }
    }

    private static int snowHeightAt(ServerWorld world, BlockPos pos) {
        BlockState currentState = world.getBlockState(pos);
        if (currentState.getBlock() == Blocks.SNOW) {
            return snowHeightAt(world, pos.down()) + currentState.get(SnowBlock.LAYERS);
        }
        if (currentState.getBlock() == Blocks.AIR && world.getBlockState(pos.down()).getBlock() != Blocks.AIR) {
            return snowHeightAt(world, pos.down());
        }
        return 0;
    }

    private static int iceHeightAt(ServerWorld world, BlockPos pos) {
        BlockPos blockPos = findLowestSnowBlockOfTopStack(world, pos).down();
        if (world.getBlockState(blockPos).getBlock() == Blocks.ICE)
            return 0;

        int count = 0;
        while (world.getBlockState(blockPos).getBlock() == Blocks.ICE)
        {
            count++;
            blockPos = blockPos.down();
        }

        return count;
    }

    private static BlockPos findLowestSnowBlockOfTopStack(ServerWorld world, BlockPos pos) {
        BlockPos workingPos = getSnowTopPosition(world, pos);
        while(world.getBlockState(workingPos).getBlock() == Blocks.SNOW)
        {
            workingPos = workingPos.down();
        }

        return workingPos.up();
    }

    private static BlockPos findLowestIceBlockOfTopStack(ServerWorld world, BlockPos pos) {
        BlockPos workingPos = getIceTopPosition(world, pos);

    }

    private static boolean canSnowAt(ServerWorld world, BlockPos pos)
    {
        Biome biome = world.getBiome(pos);
        return true; // biome.doesSnowGenerate(world, pos);
    }

    private static boolean canSnowTurnIntoIce(ServerWorld world, BlockPos pos)
    {
        final int snowHeight = snowHeightAt(world, pos);
        if (snowHeight < Configuration.getInstance().getCommonConfiguration().minLayersForIce.get()) {
            return false;
        }


        final BlockPos targetPos = findLowestSnowBlockOfTopStack(world, pos);

        for (final Direction direction : Direction.Plane.HORIZONTAL) {
            final BlockPos posToCheck = targetPos.offset(direction);
            if (!Block.hasSolidSide(world.getBlockState(posToCheck), world, posToCheck, direction.getOpposite()) ||
                !Block.isOpaque(world.getBlockState(posToCheck).getShape(world, pos))) {
                return false;
            }
        }

        return true;
    }

    private static boolean canIceTurnIntoPacked(ServerWorld world, BlockPos pos)
    {
        final int iceHeight = iceHeightAt(world, pos);
        if (iceHeight < Configuration.getInstance().getCommonConfiguration().minIceBlocksForPackedIce.get()) {
            return false;
        }


    }
}
