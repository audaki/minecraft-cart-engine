package audaki.cart_engine;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class AceBlockTags {
    public static final TagKey<Block> SLOW_RAIL = create("slow_rail");
    public static final TagKey<Block> BONUS_SPEED_RAIL = create("bonus_speed_rail");

    private static TagKey<Block> create(String path) {
        return TagKey.create(Registries.BLOCK, AceMod.id(path));
    }
}
