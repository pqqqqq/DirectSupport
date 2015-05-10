package com.pqqqqq.directsupport;

import com.google.inject.Inject;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directsupport.commands.*;
import com.pqqqqq.directsupport.events.CoreEvents;
import com.pqqqqq.directsupport.ticket.Ticket;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.api.util.command.dispatcher.SimpleDispatcher;

import java.io.File;

/**
 * Created by Kevin on 2015-05-06.
 * Main class container.
 */

@Plugin(id = DirectSupport.ID, name = DirectSupport.NAME, version = DirectSupport.VERSION, dependencies = "required-after:directchat")
public class DirectSupport {
    public static final String ID = "directsupport";
    public static final String NAME = "DirectSupport";
    public static final String VERSION = "0.1 BETA";

    private Config cfg;
    private Ticket.Manager tickets;

    private DirectChat directchat;
    private Game game;

    public static DirectSupport plugin;

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private File file;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    public DirectSupport(Logger logger) {
        this.logger = logger;
    }

    @Subscribe
    public void init(InitializationEvent event) {
        plugin = this;
        game = event.getGame();

        // Register commands to dispatcher.
        SimpleDispatcher simpleDispatcher = new SimpleDispatcher();
        simpleDispatcher.register(new CommandReload(this), "reload");
        simpleDispatcher.register(new CommandClear(this), "clear");
        simpleDispatcher.register(new CommandInfo(this), "info");
        simpleDispatcher.register(new CommandDelete(this), "delete", "remove");
        simpleDispatcher.register(new CommandHelpers(this), "helpers", "ops", "admins", "mods");
        simpleDispatcher.register(new CommandList(this), "list", "active", "activet");
        simpleDispatcher.register(new CommandAll(this), "all", "completed");
        simpleDispatcher.register(new CommandView(this), "view");
        simpleDispatcher.register(new CommandCreate(this), "create", "c");
        simpleDispatcher.register(new CommandAccept(this), "accept", "a");
        simpleDispatcher.register(new CommandLeave(this), "leave");
        simpleDispatcher.register(new CommandGoto(this), "goto", "tp", "tele", "teleport");
        simpleDispatcher.register(new CommandSay(this), "say");
        simpleDispatcher.register(new CommandSelect(this), "select", "s");
        simpleDispatcher.register(new CommandTakeover(this), "takeover");

        // Register dispatcher to main command service.
        CommandService cmdService = game.getCommandDispatcher();
        cmdService.register(this, simpleDispatcher, "ds", "directsupport", "ticket", "help");

        // Register events
        EventManager eventManager = game.getEventManager();
        eventManager.register(this, new CoreEvents(this));

        // Instantiate managers
        tickets = new Ticket.Manager();

        // Retrieve DirectChat plugin
        directchat = (DirectChat) game.getPluginManager().getPlugin(DirectChat.ID).get().getInstance(); // DirectChat MUST be present for this plugin to work at all.

        // Load config
        cfg = new Config(file, configManager, this);
        cfg.init();
        cfg.load();

        // Start reminder task
        game.getSyncScheduler().runRepeatingTask(this, new Ticket.Reminder(), Config.reminderTicks);

        logger.info("DirectSupport initialized");
    }

    public Logger getLogger() {
        return logger;
    }

    public Game getGame() {
        return game;
    }

    public Config getCfg() {
        return cfg;
    }

    public Ticket.Manager getTicketManager() {
        return tickets;
    }

    public DirectChat getDirectChat() {
        return directchat;
    }
}
