package audaki.cart_engine.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecart.Type;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartEntityMixin extends VehicleEntity {
    public AbstractMinecartEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow
    protected abstract boolean isRedstoneConductor(BlockPos pos);

    @Shadow
    public abstract Vec3 getPos(double x, double y, double z);

    @Shadow
    protected abstract void applyNaturalSlowdown();

    @Shadow
    protected abstract double getMaxSpeed();

    @Shadow
    protected abstract Type getMinecartType();

    @Shadow
    private static Pair<Vec3i, Vec3i> exits(RailShape shape) {
        // This is just fake code, the shadowed private function will be executed
        return Pair.of(Direction.NORTH.getNormal(), Direction.SOUTH.getNormal());
    }

    private static boolean isEligibleFastRail(BlockState state) {
        return state.is(Blocks.RAIL) || (state.is(Blocks.POWERED_RAIL) && state.getValue(PoweredRailBlock.POWERED));
    }

    private static RailShape getRailShape(BlockState state) {
        if (!(state.getBlock() instanceof BaseRailBlock railBlock))
            throw new IllegalArgumentException("No rail shape found");

        return state.getValue(railBlock.getShapeProperty());
    }

    @Inject(at = @At("HEAD"), method = "moveAlongTrack", cancellable = true)
    protected void moveAlongTrackOverwrite(BlockPos pos, BlockState state, CallbackInfo ci) {

        // We only change logic for rideable minecarts so we don't break hopper/chest minecart creations
        if (this.getMinecartType() != Type.RIDEABLE) {
            return;
        }

        // We only change logic when the minecart is currently being ridden by a living entity (player/villager/mob)
        boolean hasLivingRider = this.getFirstPassenger() instanceof LivingEntity;
        if (!hasLivingRider) {
            return;
        }

        this.modifiedMoveAlongTrack(pos, state);
        ci.cancel();
    }

    protected void modifiedMoveAlongTrack(BlockPos startPos, BlockState state) {

        final double tps = 20.;
        final double maxSpeed = 34. / tps;
        final double maxMomentum = maxSpeed * 5.;
        final double vanillaMaxSpeedPerTick = 0.4D;

        this.resetFallDistance();
        double thisX = this.getX();
        double thisY = this.getY();
        double thisZ = this.getZ();

        Vec3 vec3 = this.getPos(thisX, thisY, thisZ);

        thisY = startPos.getY();
        boolean onPoweredRail = false;
        boolean onBrakeRail = false;
        if (state.is(Blocks.POWERED_RAIL)) {
            onPoweredRail = state.getValue(PoweredRailBlock.POWERED);
            onBrakeRail = !onPoweredRail;
        }

        double g = 0.0078125D;
        if (this.isInWater()) {
            g *= 0.2D;
        }

        Vec3 momentum = this.getDeltaMovement();
        RailShape railShape = getRailShape(state);
        boolean isAscending = railShape.isAscending();
        boolean isDiagonal = (railShape == RailShape.SOUTH_WEST || railShape == RailShape.NORTH_EAST ||
                railShape == RailShape.SOUTH_EAST || railShape == RailShape.NORTH_WEST);

        switch (railShape) {
            case ASCENDING_EAST -> {
                this.setDeltaMovement(momentum.add(-g, 0.0D, 0.0D));
                ++thisY;
            }
            case ASCENDING_WEST -> {
                this.setDeltaMovement(momentum.add(g, 0.0D, 0.0D));
                ++thisY;
            }
            case ASCENDING_NORTH -> {
                this.setDeltaMovement(momentum.add(0.0D, 0.0D, g));
                ++thisY;
            }
            case ASCENDING_SOUTH -> {
                this.setDeltaMovement(momentum.add(0.0D, 0.0D, -g));
                ++thisY;
            }
        }


        momentum = this.getDeltaMovement();
        Pair<Vec3i, Vec3i> exitPair = exits(railShape);
        Vec3i exitRelPos1 = exitPair.getFirst();
        Vec3i exitRelPos2 = exitPair.getSecond();
        // The exit relative X and Z here can be either -1, 0 or 1
        //
        // Example for an EAST_WEST rail would be:
        // exitRelPos1.getX() = -1
        // exitRelPos2.getX() = 1
        // exitRelPos1.getZ() = 0
        // exitRelPos2.getZ() = 0
        // Therefore
        // exitDiffX = 2
        // exitDiffZ = 0
        // exitHypotenuse = 4
        //
        // Example for an SOUTH_EAST rail would be:
        // exitRelPos1.getX() = 0
        // exitRelPos2.getX() = 1
        // exitRelPos1.getZ() = 1
        // exitRelPos2.getZ() = 0
        // Therefore
        // exitDiffX = 1
        // exitDiffZ = -1
        // exitHypotenuse = 1.414
        //
        // Note: exitDiffX and exitDiffY can be either -1, 0, 1 or 2
        // (-2 isn’t possible, I think the south-east rule starts here)
        //
        // By some magic, this works out to find the correct new velocity depending on incoming velocity and rail shape
        double exitDiffX = exitRelPos2.getX() - exitRelPos1.getX();
        double exitDiffZ = exitRelPos2.getZ() - exitRelPos1.getZ();
        double exitHypotenuse = Math.sqrt(exitDiffX * exitDiffX + exitDiffZ * exitDiffZ);
        double k = momentum.x * exitDiffX + momentum.z * exitDiffZ;
        // Every rail shape has a "forward" movement according to the diffs using the exits()
        // If it’s backwards the direction is flipped.
        boolean movementIsBackwards = k < 0.0D;
        if (movementIsBackwards) {
            exitDiffX = -exitDiffX;
            exitDiffZ = -exitDiffZ;
        }

        double horizontalMomentum = Math.min(this.getDeltaMovement().horizontalDistance(), maxMomentum);
        // The horizontal speed is redistributed using the hypotenuse of the exit rail positions
        this.setDeltaMovement(new Vec3(horizontalMomentum * exitDiffX / exitHypotenuse, momentum.y, horizontalMomentum * exitDiffZ / exitHypotenuse));


        BlockPos exitPos;
        {
            BlockPos pos = isAscending ? startPos.above() : startPos;
            BlockPos exitPos1 = pos.offset(exitRelPos1);
            if (this.level().getBlockState(new BlockPos(exitPos1.getX(), exitPos1.getY() - 1, exitPos1.getZ())).is(BlockTags.RAILS)) {
                exitPos1 = exitPos1.below();
            }
            BlockPos exitPos2 = pos.offset(exitRelPos2);
            if (this.level().getBlockState(new BlockPos(exitPos2.getX(), exitPos2.getY() - 1, exitPos2.getZ())).is(BlockTags.RAILS)) {
                exitPos2 = exitPos2.below();
            }
            Vec3 momentumPos = pos.getCenter().add(this.getDeltaMovement()).multiply(1, 0, 1);
            exitPos = momentumPos.distanceTo(exitPos1.getCenter().multiply(1, 0, 1)) < momentumPos.distanceTo(exitPos2.getCenter().multiply(1, 0, 1)) ? exitPos1 : exitPos2;
        }

        ArrayList<BlockPos> adjRailPositions = new ArrayList<>();
        Supplier<Double> calculateMaxSpeedForThisTick = () -> {

            double fallback = this.getMaxSpeed();

            if (!this.isVehicle())
                return fallback;

            if (this.getDeltaMovement().horizontalDistance() < vanillaMaxSpeedPerTick)
                return fallback;

            if (!isEligibleFastRail(state))
                return fallback;

            HashSet<BlockPos> checkedPositions = new HashSet<>();
            checkedPositions.add(startPos);


            BiFunction<BlockPos, RailShape, ArrayList<Pair<BlockPos, RailShape>>> checkNeighbors = (checkPos, checkRailShape) -> {

                Pair<Vec3i, Vec3i> nExitPair = exits(checkRailShape);

                ArrayList<Pair<BlockPos, RailShape>> newNeighbors = new ArrayList<>();

                BlockPos sourcePos = checkRailShape.isAscending() ? checkPos.above() : checkPos;

                for (Vec3i nExitRelPos: List.of(nExitPair.getFirst(), nExitPair.getSecond())) {
                    BlockPos nPos = sourcePos.offset(nExitRelPos);
                    if (this.level().getBlockState(new BlockPos(nPos.getX(), nPos.getY() - 1, nPos.getZ())).is(BlockTags.RAILS)) {
                        nPos = nPos.below();
                    }

                    if (checkedPositions.contains(nPos))
                        continue;

                    BlockState nState = this.level().getBlockState(nPos);
                    if (!isEligibleFastRail(nState))
                        return new ArrayList<>();

                    RailShape nShape = getRailShape(nState);
                    boolean sameDiagonal = (railShape == RailShape.SOUTH_WEST && nShape == RailShape.NORTH_EAST
                            || railShape == RailShape.NORTH_EAST && nShape == RailShape.SOUTH_WEST
                            || railShape == RailShape.SOUTH_EAST && nShape == RailShape.NORTH_WEST
                            || railShape == RailShape.NORTH_WEST && nShape == RailShape.SOUTH_EAST);

                    if (nShape != railShape && !sameDiagonal)
                        return new ArrayList<>();

                    checkedPositions.add(nPos);
                    adjRailPositions.add(nPos);
                    // Adding the neighbor rail shape currently has no use, since we abort on rail shape change anyway
                    // Code stays as is for now so we can differentiate between types of rail shape changes later
                    newNeighbors.add(Pair.of(nPos, nShape));
                }

                return newNeighbors;
            };


            ArrayList<Pair<BlockPos, RailShape>> newNeighbors = checkNeighbors.apply(startPos, railShape);

            double checkFactor = (isDiagonal || isAscending) ? 2. : 1.;
            final int cutoffPoint = 3;
            int sizeToCheck = (int)(2 * (cutoffPoint + (checkFactor * maxSpeed)));
            sizeToCheck -= (sizeToCheck % 2);

            while (!newNeighbors.isEmpty() && adjRailPositions.size() < sizeToCheck) {
                ArrayList<Pair<BlockPos, RailShape>> tempNewNeighbors = new ArrayList<>(newNeighbors);
                newNeighbors.clear();

                for (Pair<BlockPos, RailShape> newNeighbor : tempNewNeighbors) {
                    ArrayList<Pair<BlockPos, RailShape>> result = checkNeighbors.apply(newNeighbor.getFirst(), newNeighbor.getSecond());

                    // Abort when one direction is empty
                    if (result.isEmpty()) {
                        newNeighbors.clear();
                        break;
                    }

                    newNeighbors.addAll(result);
                }
            }

            int railCountEachDirection = adjRailPositions.size() / 2;
            final double dynamicCutoffSpeedPerSec = 20.;
            switch (railCountEachDirection) {
                case 0:
                case 1:
                    return fallback;
                case 2:
                    return 12. / tps;
                case 3:
                    return dynamicCutoffSpeedPerSec / tps;
                default:
            }

            int railCountPastBegin = railCountEachDirection - cutoffPoint;
            return (dynamicCutoffSpeedPerSec + ((20. / checkFactor) * railCountPastBegin)) / tps;
        };

        double maxSpeedForThisTick = Math.min(calculateMaxSpeedForThisTick.get(), maxSpeed);
        if (isDiagonal || isAscending) {
            // Diagonal and Ascending/Descending is 1.4142 times faster, we correct this here
            maxSpeedForThisTick = Math.min(maxSpeedForThisTick, 0.7071 * maxSpeed);
        }

        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player) {
            Vec3 playerDeltaMovement = entity.getDeltaMovement();
            double m = playerDeltaMovement.horizontalDistanceSqr();
            double n = this.getDeltaMovement().horizontalDistanceSqr();
            if (m > 1.0E-4D && n < 0.01D) {
                this.setDeltaMovement(this.getDeltaMovement().add(playerDeltaMovement.x * 0.1D, 0.0D, playerDeltaMovement.z * 0.1D));
                onBrakeRail = false;
            }
        }

        if (onBrakeRail) {
            if (horizontalMomentum < 0.03D) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                double brakeFactor = 0.5D;

                if (horizontalMomentum > 4.0D * vanillaMaxSpeedPerTick) {
                    brakeFactor = Math.pow(brakeFactor, 1.0D + ((horizontalMomentum - 3.99D * vanillaMaxSpeedPerTick) / 1.2D));
                }

                this.setDeltaMovement(this.getDeltaMovement().multiply(brakeFactor, 0.0D, brakeFactor));
            }
        }

        double p = (double) startPos.getX() + 0.5D + (double) exitRelPos1.getX() * 0.5D;
        double q = (double) startPos.getZ() + 0.5D + (double) exitRelPos1.getZ() * 0.5D;
        double r = (double) startPos.getX() + 0.5D + (double) exitRelPos2.getX() * 0.5D;
        double s = (double) startPos.getZ() + 0.5D + (double) exitRelPos2.getZ() * 0.5D;
        exitDiffX = r - p;
        exitDiffZ = s - q;
        double x;
        double v;
        double w;
        if (exitDiffX == 0.0D) {
            x = thisZ - (double) startPos.getZ();
        } else if (exitDiffZ == 0.0D) {
            x = thisX - (double) startPos.getX();
        } else {
            v = thisX - p;
            w = thisZ - q;
            x = (v * exitDiffX + w * exitDiffZ) * 2.0D;
        }

        thisX = p + exitDiffX * x;
        thisZ = q + exitDiffZ * x;

        v = this.isVehicle() ? 0.75D : 1.0D;
        w = maxSpeedForThisTick;
        momentum = this.getDeltaMovement();
        // The clamp here differentiates between momentum and actual allowed speed in this tick
        Vec3 movement = new Vec3(Mth.clamp(v * momentum.x, -w, w), 0.0D, Mth.clamp(v * momentum.z, -w, w));

        double extraY = 0;
        if (railShape.isAscending()) {
            if (exitPos.getY() > startPos.getY()) {
//                System.out.println("is higher!");
                extraY = (int) (0.5 + movement.horizontalDistance());
                thisY += extraY;
            }
        }

        this.setPos(thisX, thisY, thisZ);
        this.move(MoverType.SELF, movement);

//        System.out.println("Actual: " + movement.horizontalDistance()
//                + " " + this.level().getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY() - 2.), Mth.floor(this.getZ()))).is(BlockTags.RAILS)
//                + " " + this.level().getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY() - 1.), Mth.floor(this.getZ()))).is(BlockTags.RAILS)
//                + " " + this.level().getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY() - 0.), Mth.floor(this.getZ()))).is(BlockTags.RAILS)
//                + " " + this.level().getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY() + 1.), Mth.floor(this.getZ()))).is(BlockTags.RAILS));

        {
            // Snap down after extra snap ups on ascending rails
            // Also snap down on descending rails
            if (railShape.isAscending()
                    && !this.level().getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()))).is(BlockTags.RAILS)
                    && !this.level().getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY() - 1.), Mth.floor(this.getZ()))).is(BlockTags.RAILS)) {

                if (this.level().getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY() - 2.), Mth.floor(this.getZ()))).is(BlockTags.RAILS)) {
                    this.setPos(this.getX(), this.getY() - 1, this.getZ());
                } else if (this.level().getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY() - 3.), Mth.floor(this.getZ()))).is(BlockTags.RAILS)) {
                    this.setPos(this.getX(), this.getY() - 2, this.getZ());
                }
            }

            // Old vanilla code to snap down on descending rails (only the descending exit had a different Y rel pos)
//        if (exitRelPos1.getY() != 0 && Mth.floor(this.getX()) - startPos.getX() == exitRelPos1.getX() && Mth.floor(this.getZ()) - startPos.getZ() == exitRelPos1.getZ()) {
//            this.setPos(this.getX(), this.getY() + (double) exitRelPos1.getY(), this.getZ());
//        } else if (exitRelPos2.getY() != 0 && Mth.floor(this.getX()) - startPos.getX() == exitRelPos2.getX() && Mth.floor(this.getZ()) - startPos.getZ() == exitRelPos2.getZ()) {
//            this.setPos(this.getX(), this.getY() + (double) exitRelPos2.getY(), this.getZ());
//        }
        }

        this.applyNaturalSlowdown();
        Vec3 vec3d4 = this.getPos(this.getX(), this.getY(), this.getZ());
        if (vec3d4 != null && vec3 != null) {
            double aa = (vec3.y - vec3d4.y) * 0.05D;
            momentum = this.getDeltaMovement();
            horizontalMomentum = momentum.horizontalDistance();
            if (horizontalMomentum > 0.0D) {
                this.setDeltaMovement(momentum.multiply((horizontalMomentum + aa) / horizontalMomentum, 1.0D, (horizontalMomentum + aa) / horizontalMomentum));
            }

            this.setPos(this.getX(), vec3d4.y, this.getZ());
        }

        int ac = Mth.floor(this.getX());
        int ad = Mth.floor(this.getZ());
        if (ac != startPos.getX() || ad != startPos.getZ()) {
            momentum = this.getDeltaMovement();
            horizontalMomentum = momentum.horizontalDistance();
            this.setDeltaMovement(
                    horizontalMomentum * Mth.clamp(ac - startPos.getX(), -1.0D, 1.0D),
                    momentum.y,
                    horizontalMomentum * Mth.clamp(ad - startPos.getZ(), -1.0D, 1.0D));
        }

        if (onPoweredRail) {
            momentum = this.getDeltaMovement();
            horizontalMomentum = momentum.horizontalDistance();
            final double basisAccelerationPerTick = 0.021D;
            if (horizontalMomentum > 0.01D) {

                if (this.isVehicle()) {
                    // Based on a 10 ticks per second basis spent per powered block we calculate a fair acceleration per tick
                    // due to spending less ticks per powered block on higher speeds (and even skipping blocks)
                    final double basisTicksPerSecond = 10.0D;
                    // Tps = Ticks per second
                    final double tickMovementForBasisTps = 1.0D / basisTicksPerSecond;
                    final double maxSkippedBlocksToConsider = 3.0D;


                    double acceleration = basisAccelerationPerTick;
                    final double distanceMovedHorizontally = movement.horizontalDistance();

                    if (distanceMovedHorizontally > tickMovementForBasisTps) {
                        acceleration *= Math.min((1.0D + maxSkippedBlocksToConsider) * basisTicksPerSecond, distanceMovedHorizontally / tickMovementForBasisTps);

                        // Add progressively slower (or faster) acceleration for higher speeds;
                        double highspeedFactor = 1.0D + Mth.clamp(-0.45D * (distanceMovedHorizontally / tickMovementForBasisTps / basisTicksPerSecond), -0.7D, 2.0D);
                        acceleration *= highspeedFactor;
                    }
                    this.setDeltaMovement(momentum.add(acceleration * (momentum.x / horizontalMomentum), 0.0D, acceleration * (momentum.z / horizontalMomentum)));
                }
                else {
                    this.setDeltaMovement(momentum.add(momentum.x / horizontalMomentum * 0.06D, 0.0D, momentum.z / horizontalMomentum * 0.06D));
                }


            } else {
                momentum = this.getDeltaMovement();
                double ah = momentum.x;
                double ai = momentum.z;
                final double railStopperAcceleration = basisAccelerationPerTick * 16.0D;
                if (railShape == RailShape.EAST_WEST) {
                    if (this.isRedstoneConductor(startPos.west())) {
                        ah = railStopperAcceleration;
                    } else if (this.isRedstoneConductor(startPos.east())) {
                        ah = -railStopperAcceleration;
                    }
                } else {
                    if (railShape != RailShape.NORTH_SOUTH) {
                        return;
                    }

                    if (this.isRedstoneConductor(startPos.north())) {
                        ai = railStopperAcceleration;
                    } else if (this.isRedstoneConductor(startPos.south())) {
                        ai = -railStopperAcceleration;
                    }
                }

                this.setDeltaMovement(ah, momentum.y, ai);
            }
        }
    }
}
