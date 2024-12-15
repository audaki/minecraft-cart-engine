package audaki.cart_engine.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRules.class)
public abstract class GameRulesMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/flag/FeatureFlags;MINECART_IMPROVEMENTS:Lnet/minecraft/world/flag/FeatureFlag;"))
    private static FeatureFlag enableMinecartSpeed(FeatureFlag featureFlag) {
        return FeatureFlags.VANILLA;
    }
}
