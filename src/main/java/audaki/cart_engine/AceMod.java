package audaki.cart_engine;

import net.fabricmc.api.ModInitializer;

public class AceMod implements ModInitializer {
    @Override
    public void onInitialize() {
        AceGameRules.register();
    }
}
