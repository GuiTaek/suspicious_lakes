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
import net.minecraft.world.BlockView;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

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

    public void teleport(World world, Entity entity, BlockPos fromPos) {
        Vec3d targetPosition = new Vec3d(0.0, 128.0, 0.0);
        entity.speed = 0;
        entity.teleport((ServerWorld)world, targetPosition.x, targetPosition.y, targetPosition.z, Set.of(), 0, 0);
        if (entity instanceof EnderPearlEntity enderPearl) {
            if (enderPearl.getWorld() instanceof ServerWorld serverWorld) {
                Entity owner = enderPearl.getOwner();
                if (owner == null) {
                    return;
                }
                owner.teleportTo(new TeleportTarget(serverWorld, targetPosition, new Vec3d(0, 0, 0), owner.getYaw(), owner.getPitch(), TeleportTarget.NO_OP));
                enderPearl.kill();
            }
        }
    }

    public void testTeleport(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!this.shouldTeleport(entity, state, pos)) {
            return;
        }
        teleport(world, entity, pos);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        testTeleport(state, world, pos, entity);
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
