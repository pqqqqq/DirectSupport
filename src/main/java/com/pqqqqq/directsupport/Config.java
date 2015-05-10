package com.pqqqqq.directsupport;

import com.pqqqqq.directchat.util.Utilities;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by Kevin on 2015-05-07.
 */
public class Config {
    private File file;
    private ConfigurationLoader<CommentedConfigurationNode> cfg;
    private DirectSupport plugin;

    public static String messageFormat;
    public static String sayIntoFormat;
    public static int reminderTicks;
    public static int creationDelay;

    public static boolean allowCreationNoAdmins;

    public static SimpleDateFormat dateFormat;

    public Config(File file, ConfigurationLoader<CommentedConfigurationNode> cfg, DirectSupport plugin) {
        this.file = file;
        this.cfg = cfg;
        this.plugin = plugin;
    }

    public void init() {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            CommentedConfigurationNode root = cfg.load();

            CommentedConfigurationNode tickets = root.getNode("tickets");
            messageFormat = Utilities.formatColours(tickets.getNode("format", "message").getString("&3[DS -> %OTHER%] &b%PLAYERNAME%: &f%MESSAGE%"));
            sayIntoFormat = Utilities.formatColours(tickets.getNode("format", "say-into").getString("&3[DS Admin] &b%PLAYERNAME%: &f%MESSAGE%"));
            dateFormat = new SimpleDateFormat(tickets.getNode("format", "date").getString("EEEE, MMMM dd, yyyy hh:mm:ss a"));
            reminderTicks = tickets.getNode("reminder-ticks").getInt(6000);
            creationDelay = tickets.getNode("creation-delay-seconds").getInt(60);

            allowCreationNoAdmins = tickets.getNode("settings", "allow-no-admin-creation").getBoolean(false);

            cfg.save(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 }
