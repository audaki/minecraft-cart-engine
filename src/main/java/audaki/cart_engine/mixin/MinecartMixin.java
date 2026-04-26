package audaki.cart_engine.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecart.class)
public abstract class MinecartMixin extends AbstractMinecart {

  @Shadow
  public abstract boolean isRideable();

  protected MinecartMixin(EntityType<?> type, Level level) {
    super(type, level);
  }

  @ModifyExpressionValue(
      method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/entity/vehicle/Minecart;useExperimentalMovement(Lnet/minecraft/world/level/Level;)Z"
      )
  )
  private boolean ace$changeExperimentalMovementInPositionRiderForNormalMinecart(boolean original) {
    return true;
  }
}
