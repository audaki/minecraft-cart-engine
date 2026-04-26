package audaki.cart_engine;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import org.jetbrains.annotations.NotNull;

public class AceGameRules {

  // All speeds in blocks per second
  private static final String NAMESPACE = "ace";
  private static final String ID_MAX_SPEED_PLAYER_RIDER = "speed_player";
  private static final String ID_MAX_SPEED_OTHER_RIDER = "speed_other";
  private static final String ID_MAX_SPEED_EMPTY_RIDER = "speed_empty";

  public static final GameRule<@NotNull Integer> MINECART_MAX_SPEED_PLAYER_RIDER =
      GameRuleBuilder.forInteger(20).buildAndRegister(Identifier.fromNamespaceAndPath(NAMESPACE, ID_MAX_SPEED_PLAYER_RIDER));

  public static final GameRule<@NotNull Integer> MINECART_MAX_SPEED_OTHER_RIDER =
      GameRuleBuilder.forInteger(0).buildAndRegister(Identifier.fromNamespaceAndPath(NAMESPACE, ID_MAX_SPEED_OTHER_RIDER));

  public static final GameRule<@NotNull Integer> MINECART_MAX_SPEED_EMPTY_RIDER =
      GameRuleBuilder.forInteger(0).buildAndRegister(Identifier.fromNamespaceAndPath(NAMESPACE, ID_MAX_SPEED_EMPTY_RIDER));

  public static void register() {
  }
}
