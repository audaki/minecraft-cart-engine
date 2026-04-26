package audaki.cart_engine.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CollisionContext.class)
public interface CollisionContextMixin {

  @ModifyExpressionValue(
      method = "of(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/shapes/CollisionContext;",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;useExperimentalMovement(Lnet/minecraft/world/level/Level;)Z"
      ),
      require = 1
  )
  private static boolean ace$changeExperimentalMovementInOfForNormalMinecart(boolean original, Entity entity) {
    return entity instanceof Minecart;
  }
}
