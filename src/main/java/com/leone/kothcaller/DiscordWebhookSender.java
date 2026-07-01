package com.leone.kothcaller;

import net.minecraft.entity.player.PlayerEntity;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DiscordWebhookSender {
    public void sendWebhook(TeamDataManager.Team team, PlayerEntity player, String message, BufferedImage image) throws IOException {
        String boundary = "----LeoneKothCaller" + UUID.randomUUID();
        byte[] payload = buildMultipartPayload(boundary, message, player, team, image);

        HttpURLConnection connection = (HttpURLConnection) new URL(team.webhookUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("User-Agent", "Leone-Koth-Caller/1.0");

        try (OutputStream output = connection.getOutputStream()) {
            output.write(payload);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("Discord webhook returned HTTP " + responseCode);
        }
    }

    private byte[] buildMultipartPayload(String boundary, String message, PlayerEntity player, TeamDataManager.Team team, BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        String content = "**KOTH Call**\n" +
                "Team: **" + team.name + "**\n" +
                "Caller: **" + player.getName().getString() + "**\n" +
                "Message: " + message + "\n" +
                "Channel: " + team.channelId;

        writePart(output, boundary, "content", content);
        writeBinaryPart(output, boundary, "file", "inventory.png", "image/png", image);
        output.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }

    private void writePart(ByteArrayOutputStream output, String boundary, String name, String value) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(value.getBytes(StandardCharsets.UTF_8));
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void writeBinaryPart(ByteArrayOutputStream output, String boundary, String name, String filename, String contentType, BufferedImage image) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));

        javax.imageio.ImageIO.write(image, "png", output);
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
