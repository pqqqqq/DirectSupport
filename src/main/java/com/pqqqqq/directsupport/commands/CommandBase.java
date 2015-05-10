package com.pqqqqq.directsupport.commands;

import com.pqqqqq.directsupport.DirectSupport;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;

import java.util.Collections;
import java.util.List;

/**
 * Created by Kevin on 2015-05-04.
 */
public abstract class CommandBase implements CommandCallable {
    DirectSupport plugin;

    public CommandBase(DirectSupport plugin) {
        this.plugin = plugin;
    }

    public List<String> getSuggestions(CommandSource commandSource, String s) throws CommandException {
        return Collections.emptyList();
    }

    public DirectSupport getPlugin() {
        return plugin;
    }
}
