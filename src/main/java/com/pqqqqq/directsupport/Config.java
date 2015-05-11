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

            CommentedConfigurationNode tickets = getNodeAndComment(root, "Tickets settings and configurations", "tickets");
            messageFormat = Utilities.formatColours(getNodeAndComment(tickets, "The format for general messages in tickets.", "format", "message").getString("&3[DS -> %OTHER%] &b%PLAYERNAME%: &f%MESSAGE%"));
            sayIntoFormat = Utilities.formatColours(getNodeAndComment(tickets, "The format for when an admin speaks into a ticket.", "format", "say-into").getString("&3[DS Admin] &b%PLAYERNAME%: &f%MESSAGE%"));
            dateFormat = new SimpleDateFormat(getNodeAndComment(tickets,
                    "The date format, based off of SimpleDateFormat." +
                            "\n Visit http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html for more information.", "format", "date").getString("EEEE, MMMM dd, yyyy hh:mm:ss a"));
            reminderTicks = getNodeAndComment(tickets, "The delay, in ticks, between reminding helpers of active tickets.", "reminder-ticks").getInt(6000);
            creationDelay = getNodeAndComment(tickets, "The delay, in seconds, that a user must wait before creating another ticket.", "creation-delay-seconds").getInt(60);

            allowCreationNoAdmins = tickets.getNode("settings", "allow-no-admin-creation").getBoolean(false);

            cfg.save(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CommentedConfigurationNode getNodeAndComment(boolean last, CommentedConfigurationNode root, String comment, Object... path) {
        CommentedConfigurationNode node = root.getNode(path);

        if (last) {
            node.setComment(comment);
        } else {
            node.setComment(null);
        }

        return node;
    }

    public CommentedConfigurationNode getNodeAndComment(CommentedConfigurationNode root, String comment, Object... path) {
        return getNodeAndComment(true, root, comment, path);
    }
 }
