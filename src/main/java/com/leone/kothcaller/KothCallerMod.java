package com.leone.kothcaller;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class KothCallerMod implements ModInitializer {
    public static final String MOD_ID = "leone_koth_caller";
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

    private static TeamDataManager teamDataManager;
    private static DiscordWebhookSender webhookSender;

    @Override
    public void onInitialize() {
        teamDataManager = new TeamDataManager(CONFIG_DIR.resolve("teams.json"));
        teamDataManager.load();
        webhookSender = new DiscordWebhookSender();
    }

    public static TeamDataManager getTeamDataManager() {
        return teamDataManager;
    }

    public static DiscordWebhookSender getWebhookSender() {
        return webhookSender;
    }
}
