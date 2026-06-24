package com.example.kothcall;

import net.fabricmc.api.ModInitializer;

public class KothCallMod implements ModInitializer {

    public static final String MOD_ID = "kothcall";

    @Override
    public void onInitialize() {
        WebhookStorage.load();
        CommandManager.register();
    }
}
