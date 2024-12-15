package audaki.cart_engine.mixin;

import audaki.cart_engine.AceGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin extends MinecartBehavior {

    protected NewMinecartBehaviorMixin(AbstractMinecart abstractMinecart) {
        super(abstractMinecart);
    }

    @Inject(at = @At("HEAD"), method = "getMaxSpeed", cancellable = true)
    public void _getMaxSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
        int speedBlocksPerSecond = 8;
        if (minecart.isRideable()) {
            speedBlocksPerSecond = level.getGameRules().getInt(AceGameRules.ACE_CART_SPEED);;
        }
        cir.setReturnValue(speedBlocksPerSecond * (this.minecart.isInWater() ? 0.5 : 1.0) / 20.0);
        cir.cancel();
    }
}
