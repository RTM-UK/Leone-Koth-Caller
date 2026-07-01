package com.leone.kothcaller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class TeamDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TEAM_LIST_TYPE = new TypeToken<List<Team>>() {}.getType();

    private final Path file;
    private final List<Team> teams = new ArrayList<>();

    public TeamDataManager(Path file) {
        this.file = file;
    }

    public void load() {
        try {
            if (!Files.exists(file)) {
                save();
                return;
            }

            Files.createDirectories(file.getParent());
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                List<Team> loaded = GSON.fromJson(reader, TEAM_LIST_TYPE);
                if (loaded != null) {
                    teams.clear();
                    teams.addAll(loaded);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load team data", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(teams, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save team data", e);
        }
    }

    public boolean exists(String teamName) {
        return getTeamByName(teamName) != null;
    }

    public Team getTeamByName(String teamName) {
        for (Team team : teams) {
            if (team.name.equalsIgnoreCase(teamName)) {
                return team;
            }
        }
        return null;
    }

    public Team getTeamForPlayer(String playerUuid) {
        for (Team team : teams) {
            if (team.memberUuids.contains(playerUuid)) {
                return team;
            }
        }
        return null;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void createTeam(String teamName, String ownerUuid, String ownerName) {
        Team team = new Team();
        team.name = teamName;
        team.ownerUuid = ownerUuid;
        team.ownerName = ownerName;
        team.members = new ArrayList<>();
        team.memberUuids = new ArrayList<>();
        team.invites = new ArrayList<>();
        team.webhookUrl = "";
        team.channelId = "";
        team.members.add(ownerName);
        team.memberUuids.add(ownerUuid);
        teams.add(team);
        save();
    }

    public void deleteTeam(String teamName) {
        teams.removeIf(team -> team.name.equalsIgnoreCase(teamName));
        save();
    }

    public void addInvite(String teamName, String playerName) {
        Team team = getTeamByName(teamName);
        if (team == null) {
            return;
        }
        if (!team.invites.contains(playerName)) {
            team.invites.add(playerName);
            save();
        }
    }

    public boolean acceptInvite(String teamName, String playerName, String playerUuid) {
        Team team = getTeamByName(teamName);
        if (team == null || !team.invites.contains(playerName)) {
            return false;
        }
        team.invites.remove(playerName);
        if (!team.members.contains(playerName)) {
            team.members.add(playerName);
            team.memberUuids.add(playerUuid);
        }
        save();
        return true;
    }

    public void removeMember(String teamName, String playerName) {
        Team team = getTeamByName(teamName);
        if (team == null) {
            return;
        }

        int index = team.members.indexOf(playerName);
        if (index >= 0) {
            team.members.remove(index);
            if (index < team.memberUuids.size()) {
                team.memberUuids.remove(index);
            }
        }
        save();
    }

    public static class Team {
        public String name;
        public String ownerUuid;
        public String ownerName;
        public List<String> members = new ArrayList<>();
        public List<String> memberUuids = new ArrayList<>();
        public List<String> invites = new ArrayList<>();
        public String webhookUrl;
        public String channelId;
    }
}
