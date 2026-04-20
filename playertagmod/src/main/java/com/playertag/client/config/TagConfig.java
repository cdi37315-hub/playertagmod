package com.playertag.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TagConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("playertag.json");

    private static TagConfig INSTANCE;

    public Set<String> friends = new HashSet<>();
    public Set<String> enemies = new HashSet<>();
    public boolean enabled = true;
    public boolean showNametagColor = true;
    public boolean showGlow = true;
    public float glowOpacity = 0.6f;

    public static TagConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(r, TagConfig.class);
                if (INSTANCE == null) INSTANCE = new TagConfig();
            } catch (Exception e) {
                INSTANCE = new TagConfig();
            }
        } else {
            INSTANCE = new TagConfig();
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(INSTANCE, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFriend(String name) {
        enemies.remove(name);
        friends.add(name);
        save();
    }

    public void addEnemy(String name) {
        friends.remove(name);
        enemies.add(name);
        save();
    }

    public void remove(String name) {
        friends.remove(name);
        enemies.remove(name);
        save();
    }

    public PlayerCategory getCategory(String name) {
        if (friends.contains(name)) return PlayerCategory.FRIEND;
        if (enemies.contains(name)) return PlayerCategory.ENEMY;
        return PlayerCategory.NONE;
    }

    public enum PlayerCategory {
        FRIEND, ENEMY, NONE
    }
}
