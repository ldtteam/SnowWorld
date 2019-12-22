package com.ldtteam.snowworld.events.handler;

import java.util.Iterator;
import java.util.Random;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

public class WorldTickEventHandler {

    private static Random r = new Random();

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
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

    private static boolean isSnowyArea(ServerWorld world, BlockPos pos) {
        return world.getBiome(pos).getTemperature(pos) < 0.15F;
    }

    private static void onTickSnowDecrease(ServerWorld world) {
        for (Iterator<Chunk> iterator = world.getChunkProvider().chunkManager.getLoadedChunkIterator(); iterator.hasNext();) {
            Chunk chunk = iterator.next();

            if (r.nextInt(Config.snowMeltRate) != 0) {
                continue;
            }

            BlockPos pos = getRandomPosInChunk(chunk);

            pos = getSnowTopPosition(world, pos);

            int layers = snowHeightAt(world, pos);

            if (layers <= Config.snowMinLayers) {
                continue;
            }

            if (layers % 8 != 1) {
                // decrement layer
                world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, (layers-1)%8));
            } else {
                //remove last layer
                world.setBlockToAir(pos);
            }
        }
    }

    private static void onTickSnowIncrease(WorldServer world) {
        int baseRate = Config.accumulationRate;

        if (world.isThundering()) {
            baseRate /= 2;
        }


        for (Iterator<Chunk> iterator = world.getPersistentChunkIterable(world.getPlayerChunkMap().getChunkIterator()); iterator.hasNext();) {
            Chunk chunk = iterator.next();

            if (r.nextInt(baseRate) != 0) {
                continue;
            }

            if (!world.provider.canDoRainSnowIce(chunk)) {
                continue;
            }

            BlockPos pos = getRandomPosInChunk(chunk);

            pos = getSnowTopPosition(world, pos);
            int layers = snowHeightAt(world, pos);

            int surroundingAtLayer = 0;
            for(EnumFacing side : EnumFacing.HORIZONTALS){
                if (snowHeightAt(world, getSnowTopPosition(world, pos.offset(side))) >= layers) {
                    surroundingAtLayer++;
                }
            }

            if (surroundingAtLayer < Config.smoothing) {
                continue;
            }

            switch(Config.snowDriftArea) {
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

    private static void incrementSnowHeight(WorldServer world, BlockPos pos) {
        pos = getSnowTopPosition(world, pos);

        int layers = snowHeightAt(world, pos);

        // Check if we can snow here if this is the first snow layer
        if(layers == 0 && !world.canSnowAt(pos, true)) {
            return;
        } else if (!isSnowyArea(world, pos)) {
            return;
        }

        if (layers >= Config.maxSnowLayers ) {
            return;
        }

        if (layers == 0 || layers % 8 != 0) {
            // Continue stacking on current stack
            world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, layers%8+1));
        } else {
            // Add onto stack on block above, this one is full
            world.setBlockState(pos.up(), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, layers%8+1));
        }
    }
    private static int snowHeightAt(WorldServer world, BlockPos pos) {
        IBlockState currentBlock = world.getBlockState(pos);
        if (currentBlock.getBlock() == Blocks.SNOW_LAYER) {
            return snowHeightAt(world, pos.down()) + currentBlock.getValue(BlockSnow.LAYERS);
        }
        if (currentBlock.getBlock() == Blocks.AIR && world.getBlockState(pos.down()).getBlock() != Blocks.AIR) {
            return snowHeightAt(world, pos.down());
        }
        return 0;
    }
}
