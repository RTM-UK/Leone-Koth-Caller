package com.example.kothcall;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebhookStorage {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final File FILE =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("kothcall-webhooks.json")
                    .toFile();

    private static final Map<UUID, String> WEBHOOKS =
            new HashMap<>();

    public static void load() {
        try {
            if (!FILE.exists()) {
                save();
                return;
            }

            Type type =
                    new TypeToken<HashMap<UUID, String>>(){}.getType();

            try (FileReader reader = new FileReader(FILE)) {

                Map<UUID, String> loaded =
                        GSON.fromJson(reader, type);

                if (loaded != null) {
                    WEBHOOKS.putAll(loaded);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setWebhook(UUID uuid, String webhook) {
        WEBHOOKS.put(uuid, webhook);
        save();
    }

    public static String getWebhook(UUID uuid) {
        return WEBHOOKS.get(uuid);
    }

    private static void save() {
        try (FileWriter writer = new FileWriter(FILE)) {

            GSON.toJson(
                    WEBHOOKS,
                    writer
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
