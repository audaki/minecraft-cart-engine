package audaki.cart_engine.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends VehicleEntity {

    @Shadow
    public abstract boolean isRideable();

    @Shadow
    public abstract boolean isFurnace();

    @Shadow
    private boolean onRails;

    @Mutable
    @Final
    @Shadow
    private MinecartBehavior behavior;

    public AbstractMinecartMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Unique
    protected void juiceUpBehavior() {
        if (this.behavior instanceof OldMinecartBehavior) {
            AbstractMinecart instance = (AbstractMinecart) (Object) this;
            this.behavior = new NewMinecartBehavior(instance);
        }
    }

    @Inject(at = @At("HEAD"), method = "setInitialPos")
    public void _setInitialPos(double d, double e, double f, CallbackInfo ci) {
        this.juiceUpBehavior();
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void _tick(CallbackInfo ci) {
        this.juiceUpBehavior();
    }

    @Inject(at = @At("HEAD"), method = "useExperimentalMovement", cancellable = true)
    private static void _useExperimentalMovement(Level level, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        cir.cancel();
    }
}
