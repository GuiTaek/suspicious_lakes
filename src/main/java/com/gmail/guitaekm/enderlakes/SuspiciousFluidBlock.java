package com.gmail.guitaekm.enderlakes;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.Objects;
import java.util.Set;

public class SuspiciousFluidBlock extends FluidBlock {
    public static final BooleanProperty ACTIVATED = BooleanProperty.of("activated");

    public SuspiciousFluidBlock(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
        setDefaultState(getDefaultState().with(ACTIVATED, false));
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ACTIVATED);
    }

    public void updateActivated(BlockState state, World world, BlockPos pos) {
        boolean activated = world.getBiome(pos).isIn(Enderlakes.HAS_STRUCTURE_SUSPICIOUS_LAKE);
        world.setBlockState(pos, state.with(ACTIVATED, activated));
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        updateActivated(state, world, pos);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        updateActivated(state, world, pos);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess worldAccess, BlockPos pos, BlockPos neighborPos) {
        if (worldAccess instanceof World world) {
            updateActivated(state, world, pos);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, worldAccess, pos, neighborPos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape collisionShape = getFluidState(state).getShape(world, pos);
        if (!state.get(ACTIVATED)) {
            return collisionShape;
        }
        if (context instanceof EntityShapeContext entityShapeContext) {
            Entity entity = entityShapeContext.getEntity();
            if (entity == null) {
                return collisionShape;
            }
            if (entity.getType().isIn(Enderlakes.PERMEABLE_BY_SUSPICIOUS_FLUID)) {
                Box shrunkenRawShape = collisionShape
                        .getBoundingBox().contract(0.01);
                if (!(world instanceof ServerWorld serverWorld)) {
                    return collisionShape;
                }
                this.testTeleport(state, serverWorld, pos, entity);
                return VoxelShapes.cuboid(shrunkenRawShape);
            }
        }
        return collisionShape;
    }

    // the method name would get weird if it is inverted. something like shouldntTeleport
    // or inhibitTeleport
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldTeleport(Entity entity, BlockState state, BlockPos pos) {
        if (entity.getWorld().isClient) {
            return false;
        }
        if (!state.getBlock().equals(this)) {
            return false;
        }
        if (!state.get(ACTIVATED)) {
            return false;
        }
        VoxelShape centeredCollisionShape = getFluidState(state)
                .getShape(entity.getWorld(), pos);
        VoxelShape movedCollisionShape = centeredCollisionShape
                .offset(pos.getX(), pos.getY(), pos.getZ());

        Box boundingBox = entity.calculateBoundingBox();
        if (entity.getType().isIn(Enderlakes.PERMEABLE_BY_SUSPICIOUS_FLUID)) {
            // the stretch is because minecraft predicts collision before actually moving, and this is how it does that
            // and because the ender pearl vanishes on collision, we have to mimic the code
            boundingBox = boundingBox.stretch(entity.getVelocity());
        }

        return VoxelShapes.matchesAnywhere(
                movedCollisionShape,
                VoxelShapes.cuboid(boundingBox),
                BooleanBiFunction.AND
        );
    }

    public boolean teleport(World world, Entity entity, BlockPos fromPos) {
        if (!WorldBorderConfigUpdater.INSTANCE.mayUseLakes()) {
            return false;
        }
        long seed = Objects.requireNonNull(world.getServer()).getOverworld().getSeed();
        Enderlakes.finder.config.setSeed(seed);
        int g = Enderlakes.finder.config.g();
        int gInv = Enderlakes.finder.config.gInv();
        // todo: check if random is seed dependent
        ChunkPos destChunk = Enderlakes.finder.safeTeleportAim(world, new ChunkPos(fromPos), world.getRandom(), g, gInv, seed);
        if (destChunk == null) {
            return false;
        }
        BlockPos toPosRaw = destChunk.getBlockPos(8, 0, 8);
        if (!(world instanceof ServerWorld serverWorld)) {
            throw new IllegalStateException("this code shouldn't run client-side");
        }
        // the reason this is so weird and difficult is, that the chunk is not loaded and maybe not even generated
        Chunk chunk = serverWorld
                .getChunkManager()
                .getChunk(
                        destChunk.x,
                        destChunk.z,
                        ChunkStatus.FULL,
                        true
                );
        // chunk != null because we set "create" to true
        assert chunk != null;
        int lakeTopLayer = chunk.sampleHeightmap(
                        Heightmap.Type.WORLD_SURFACE,
                        toPosRaw.getX(),
                        toPosRaw.getZ()
                );
        lakeTopLayer = lakeTopLayer != -1 ? lakeTopLayer : world.getRandom().nextBetween(1, world.getTopY());
        BlockPos emergencyLake = toPosRaw.withY(lakeTopLayer);
        if (!chunk.getBlockState(emergencyLake).getBlock().equals(Enderlakes.SUSPICIOUS_LIQUID_BLOCK)) {
            chunk.setBlockState(emergencyLake, Enderlakes.SUSPICIOUS_LIQUID_BLOCK.getDefaultState(), false);
            for (Direction dir: Direction.values()) {
                if (dir.equals(Direction.UP)) {
                    continue;
                }
                BlockPos endstonePos = emergencyLake.add(dir.getVector());
                if (chunk.getBlockState(endstonePos).isIn(Enderlakes.REPLACEABLE_BY_SUSPICIOUS_LAKES)) {
                    chunk.setBlockState(endstonePos, Blocks.END_STONE.getDefaultState(), false);
                }
            }
        }
        int toY = lakeTopLayer + 1;
        BlockPos toPos = toPosRaw.withY(toY);
        Vec3d targetPosition = new Vec3d(toPos.getX() + 0.5, toPos.getY(), toPos.getZ() + 0.5);
        entity.speed = 0;
        entity.teleport(
                serverWorld,
                targetPosition.x,
                targetPosition.y,
                targetPosition.z,
                Set.of(),
                entity.getYaw(),
                entity.getPitch()
        );
        if (entity instanceof EnderPearlEntity enderPearl) {
            Entity owner = enderPearl.getOwner();
            if (owner == null) {
                return false;
            }
            owner.teleportTo(new TeleportTarget(
                    serverWorld,
                    targetPosition,
                    new Vec3d(0, 0, 0),
                    owner.getYaw(),
                    owner.getPitch(),
                    TeleportTarget.NO_OP
            ));
            enderPearl.kill();
        }
        return true;
    }

    public void testTeleport(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!this.shouldTeleport(entity, state, pos)) {
            return;
        }
        teleport(world, entity, pos);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        this.testTeleport(state, world, pos, entity);
    }

    @Override
    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override public boolean receiveNeighborFluids(World world, BlockPos pos, BlockState state) {
        if (world.getFluidState(pos).isIn(Enderlakes.SUSPICIOUS_LIQUID)) {
            boolean bl = world.getBlockState(pos.down()).isOf(Blocks.BEDROCK);

            for (Direction direction : FLOW_DIRECTIONS) {
                BlockPos blockPos = pos.offset(direction.getOpposite());
                if (world.getFluidState(blockPos).isIn(FluidTags.WATER) && !world.getFluidState(blockPos).isIn(Enderlakes.SUSPICIOUS_LIQUID)) {
                    Block block = Blocks.END_STONE;
                    world.setBlockState(pos, block.getDefaultState());
                    this.playExtinguishSound(world, pos);
                    return false;
                }
                if (world.getFluidState(blockPos).isIn(FluidTags.LAVA)) {
                    Block block = bl ? Blocks.OBSIDIAN : Blocks.END_STONE;
                    world.setBlockState(pos, block.getDefaultState());
                    this.playExtinguishSound(world, pos);
                    return false;
                }
            }
            return true;
        }
        return super.receiveNeighborFluids(world, pos, state);
    }
}
