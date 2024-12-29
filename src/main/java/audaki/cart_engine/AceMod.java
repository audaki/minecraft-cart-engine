package audaki.cart_engine;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

public class AceMod implements ModInitializer {
    public static final String MOD_ID = "audaki_cart_engine";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        AceGameRules.register();
    }
}
