package com.example.kothcall;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    public static void send(String webhookUrl, String message) {
        new Thread(() -> {
            try {
                URL url = new URL(webhookUrl);

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setDoOutput(true);

                String json =
                        "{\"content\":\"" +
                        escape(message) +
                        "\"}";

                try (OutputStream stream = connection.getOutputStream()) {
                    stream.write(
                        json.getBytes(StandardCharsets.UTF_8)
                    );
                }

                connection.getResponseCode(); // send request

                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static String escape(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
