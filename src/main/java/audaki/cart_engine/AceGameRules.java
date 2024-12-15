package audaki.cart_engine;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;

public class AceGameRules {
    public static GameRules.Key<GameRules.IntegerValue> ACE_CART_SPEED;

    public static void register() {
        ACE_CART_SPEED = GameRuleRegistry.register("aceCartSpeed",
                GameRules.Category.PLAYER,
                GameRuleFactory.createIntRule(20));
    }
}
