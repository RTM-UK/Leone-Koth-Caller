package com.example.kothcall;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.UUID;

public class CommandManager {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(
                CommandManager.literal("link")
                    .then(CommandManager.argument("webhook", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerCommandSource src = ctx.getSource();
                            UUID uuid = src.getPlayer().getUuid();

                            String webhook = StringArgumentType.getString(ctx, "webhook");

                            WebhookStorage.setWebhook(uuid, webhook);

                            src.sendFeedback(() -> Text.literal(
                                "Discord webhook linked!"
                            ), false);

                            return 1;
                        })
                    )
            );

            dispatcher.register(
                CommandManager.literal("kothcall")
                    .executes(ctx -> sendKothCall(ctx, ""))
                    .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String msg = StringArgumentType.getString(ctx, "message");
                            return sendKothCall(ctx, msg);
                        })
                    )
            );
        });
    }

    private static int sendKothCall(
            CommandContext<ServerCommandSource> ctx,
            String message
    ) {
        ServerCommandSource src = ctx.getSource();
        UUID uuid = src.getPlayer().getUuid();

        String webhook = WebhookStorage.getWebhook(uuid);

        if (webhook == null) {
            src.sendError(Text.literal(
                "You have not linked a Discord webhook. Use /link first."
            ));
            return 0;
        }

        String discordMessage = "@here KOTH fight starting!" 

        if (!message.isEmpty()) {
            discordMessage += "\n" + message; 
        }

        DiscordWebhook.send(webhook, discordMessage);

        src.sendFeedback(() -> Text.literal("KOTH call sent to Discord!"), false); 
        return 1; 
    }
}
