package audaki.cart_engine.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends VehicleEntity {

  public AbstractMinecartMixin(EntityType<?> type, Level level) {
    super(type, level);
  }

  @Shadow
  public abstract boolean isRideable();

  @Redirect(
      method = {
          "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
          "getCurrentBlockPosOrRailBelow",
          "move",
          "applyEffectsFromBlocks",
          "pushOtherMinecart"
      },
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;useExperimentalMovement(Lnet/minecraft/world/level/Level;)Z"
      ),
      require = 6,
      allow = 6
  )
  private boolean ace$useExperimentalMovement(Level level) {
    return this.isRideable();
  }
}
