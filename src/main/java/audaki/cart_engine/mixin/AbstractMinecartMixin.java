package audaki.cart_engine.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends VehicleEntity {

    @Unique
    private static final ThreadLocal<Boolean> IS_RIDEABLE_CONTEXT = ThreadLocal.withInitial(() -> false);

    @Shadow
    public abstract boolean isRideable();

    @Mutable
    @Final
    @Shadow
    private MinecartBehavior behavior;

    public AbstractMinecartMixin(EntityType<?> type, net.minecraft.world.level.Level level) {
        super(type, level);
    }

    @Unique
    protected void juiceUpBehavior() {
        if (this.behavior instanceof OldMinecartBehavior) {
            AbstractMinecart instance = (AbstractMinecart) (Object) this;
            if (instance.isRideable()) {
                this.behavior = new NewMinecartBehavior(instance);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "setInitialPos")
    public void _setInitialPos(double d, double e, double f, CallbackInfo ci) {
        this.juiceUpBehavior();
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void _tick(CallbackInfo ci) {
        IS_RIDEABLE_CONTEXT.set(this.isRideable());
        this.juiceUpBehavior();
    }

    @Inject(at = @At("RETURN"), method = "tick")
    public void _tickEnd(CallbackInfo ci) {
        IS_RIDEABLE_CONTEXT.set(false);
    }

    @Inject(at = @At("HEAD"), method = "useExperimentalMovement", cancellable = true)
    private static void _useExperimentalMovement(net.minecraft.world.level.Level level, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(IS_RIDEABLE_CONTEXT.get());
        cir.cancel();
    }
}