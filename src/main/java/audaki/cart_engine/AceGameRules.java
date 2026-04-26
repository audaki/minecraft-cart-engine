package audaki.cart_engine;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;

public class AceGameRules {

  // All speeds in blocks per second
  private static final String NAMESPACE = "ace";
  private static final String ID_MAX_SPEED_PLAYER_RIDER = "speed_player";
  private static final String ID_MAX_SPEED_OTHER_RIDER = "speed_other";
  private static final String ID_MAX_SPEED_EMPTY_RIDER = "speed_empty";

  public static final GameRules.Key<GameRules.@NotNull IntegerValue> MINECART_MAX_SPEED_PLAYER_RIDER =
      GameRuleRegistry.register(ruleId(ID_MAX_SPEED_PLAYER_RIDER), GameRules.Category.MISC, GameRuleFactory.createIntRule(20));

  public static final GameRules.Key<GameRules.@NotNull IntegerValue> MINECART_MAX_SPEED_OTHER_RIDER =
      GameRuleRegistry.register(ruleId(ID_MAX_SPEED_OTHER_RIDER), GameRules.Category.MISC, GameRuleFactory.createIntRule(0));

  public static final GameRules.Key<GameRules.@NotNull IntegerValue> MINECART_MAX_SPEED_EMPTY_RIDER =
      GameRuleRegistry.register(ruleId(ID_MAX_SPEED_EMPTY_RIDER), GameRules.Category.MISC, GameRuleFactory.createIntRule(0));

  private static String ruleId(String id) {
    return NAMESPACE + ":" + id;
  }

  public static void register() {
  }
}
