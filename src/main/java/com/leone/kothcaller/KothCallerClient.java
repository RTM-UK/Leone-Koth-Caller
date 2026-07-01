package com.leone.kothcaller;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class KothCallerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("kothcall")
            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("message", StringArgumentType.greedyString())
                .executes(this::handleKothCall)));

        dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("dcwebhook")
            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("input", StringArgumentType.greedyString())
                .executes(context -> handleDcWebhook(context, StringArgumentType.getString(context, "input")))));

        dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("kothteam")
            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("create")
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("team", StringArgumentType.word())
                    .executes(this::handleCreateTeam)))
            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("invite")
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("team", StringArgumentType.word())
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("player", StringArgumentType.word())
                        .executes(this::handleInviteTeam))))
            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("accept")
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("team", StringArgumentType.word())
                    .executes(this::handleAcceptTeam)))
            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("list")
                .executes(this::handleListTeams)));
    }

    private int handleKothCall(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            source.sendError(Text.literal("You must be in-game to use this command."));
            return 1;
        }

        String message = StringArgumentType.getString(context, "message").trim();
        if (message.isEmpty()) {
            source.sendError(Text.literal("Please provide a message."));
            return 1;
        }

        TeamDataManager.Team team = KothCallerMod.getTeamDataManager().getTeamForPlayer(client.player.getUuidAsString());
        if (team == null) {
            source.sendError(Text.literal("You are not in a team. Create or join one first."));
            return 1;
        }

        if (team.webhookUrl == null || team.webhookUrl.isBlank() || team.channelId == null || team.channelId.isBlank()) {
            source.sendError(Text.literal("This team has no Discord webhook configured yet."));
            return 1;
        }

        BufferedImage image = InventoryImageGenerator.createInventoryImage(client.player);
        try {
            KothCallerMod.getWebhookSender().sendWebhook(team, client.player, message, image);
            source.sendFeedback(Text.literal("KOTH call sent to team \"" + team.name + "\"."));
            return 1;
        } catch (IOException e) {
            source.sendError(Text.literal("Failed to send Discord webhook: " + e.getMessage()));
            return 1;
        }
    }

    private int handleDcWebhook(CommandContext<FabricClientCommandSource> context, String rawInput) {
        FabricClientCommandSource source = context.getSource();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            source.sendError(Text.literal("You must be in-game to use this command."));
            return 1;
        }

        String input = rawInput == null ? "" : rawInput.trim();
        String[] parts = input.split("\\s+");
        if (parts.length < 2) {
            source.sendError(Text.literal("Please provide both a webhook URL and the channel ID."));
            return 1;
        }

        String teamName = null;
        String webhookValue;
        String channelValue;

        TeamDataManager.Team team = null;
        if (parts.length >= 3 && KothCallerMod.getTeamDataManager().getTeamByName(parts[0]) != null) {
            teamName = parts[0];
            webhookValue = parts[1];
            channelValue = parts[2];
            team = KothCallerMod.getTeamDataManager().getTeamByName(teamName);
        } else {
            webhookValue = parts[0];
            channelValue = parts[1];
            team = KothCallerMod.getTeamDataManager().getTeamForPlayer(client.player.getUuidAsString());
        }

        if (team == null) {
            source.sendError(Text.literal("You are not in a team. Create or join one first."));
            return 1;
        }

        if (!team.ownerUuid.equals(client.player.getUuidAsString())) {
            source.sendError(Text.literal("Only the team owner can configure the webhook."));
            return 1;
        }

        team.webhookUrl = webhookValue;
        team.channelId = channelValue;
        KothCallerMod.getTeamDataManager().save();
        source.sendFeedback(Text.literal("Webhook configuration updated for team \"" + team.name + "\"."));
        return 1;
    }

    private int handleCreateTeam(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            source.sendError(Text.literal("You must be in-game to use this command."));
            return 1;
        }

        String teamName = StringArgumentType.getString(context, "team");
        TeamDataManager teamDataManager = KothCallerMod.getTeamDataManager();
        if (teamDataManager.exists(teamName)) {
            source.sendError(Text.literal("Team \"" + teamName + "\" already exists."));
            return 1;
        }

        teamDataManager.createTeam(teamName, client.player.getUuidAsString(), client.player.getName().getString());
        source.sendFeedback(Text.literal("Created team \"" + teamName + "\"."));
        return 1;
    }

    private int handleInviteTeam(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            source.sendError(Text.literal("You must be in-game to use this command."));
            return 1;
        }

        String teamName = StringArgumentType.getString(context, "team");
        String invitee = StringArgumentType.getString(context, "player");
        TeamDataManager.Team team = KothCallerMod.getTeamDataManager().getTeamByName(teamName);
        if (team == null) {
            source.sendError(Text.literal("Team \"" + teamName + "\" does not exist."));
            return 1;
        }
        if (!team.ownerUuid.equals(client.player.getUuidAsString())) {
            source.sendError(Text.literal("Only the team owner can invite players."));
            return 1;
        }

        KothCallerMod.getTeamDataManager().addInvite(teamName, invitee);
        source.sendFeedback(Text.literal("Invited \"" + invitee + "\" to team \"" + teamName + "\"."));
        return 1;
    }

    private int handleAcceptTeam(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            source.sendError(Text.literal("You must be in-game to use this command."));
            return 1;
        }

        String teamName = StringArgumentType.getString(context, "team");
        TeamDataManager.Team team = KothCallerMod.getTeamDataManager().getTeamByName(teamName);
        if (team == null) {
            source.sendError(Text.literal("Team \"" + teamName + "\" does not exist."));
            return 1;
        }

        boolean accepted = KothCallerMod.getTeamDataManager().acceptInvite(teamName, client.player.getName().getString(), client.player.getUuidAsString());
        if (!accepted) {
            source.sendError(Text.literal("You do not have a pending invite for team \"" + teamName + "\"."));
            return 1;
        }

        source.sendFeedback(Text.literal("Joined team \"" + teamName + "\"."));
        return 1;
    }

    private int handleListTeams(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        StringBuilder builder = new StringBuilder("Teams:\n");
        for (TeamDataManager.Team team : KothCallerMod.getTeamDataManager().getTeams()) {
            builder.append("- ").append(team.name).append(" (owner: ").append(team.ownerName).append(")");
            if (!team.members.isEmpty()) {
                builder.append(" members: ").append(String.join(", ", team.members));
            }
            builder.append("\n");
        }
        source.sendFeedback(Text.literal(builder.toString()));
        return 1;
    }
}
