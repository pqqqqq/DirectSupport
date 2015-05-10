package com.pqqqqq.directsupport.commands;

import com.pqqqqq.directchat.util.Utilities;
import com.pqqqqq.directsupport.DirectSupport;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;

import java.util.Collections;
import java.util.List;

/**
 * Created by Kevin on 2015-05-04.
 */
public abstract class CommandBase implements CommandCallable {
    private DirectSupport plugin;

    public CommandBase(DirectSupport plugin) {
        this.plugin = plugin;
    }

    public List<String> getSuggestions(CommandSource commandSource, String s) throws CommandException {
        return Collections.emptyList();
    }

    public boolean testPermission(CommandSource commandSource) {
        return false;
    }

    public Text getErrorMessage(Object message) {
        return getMessageInColour(message, TextColors.RED);
    }

    public Text getSuccessMessage(Object message) {
        return getMessageInColour(message, TextColors.GREEN);
    }

    public Text getNormalMessage(Object message) {
        return getMessageInColour(message, TextColors.AQUA);
    }

    public Text getMessageInColour(Object message, TextColor color) {
        Text of = (message instanceof Text ? (Text) message : Texts.of(Utilities.formatColours(message.toString())));
        return Texts.builder().color(color).append(of).build();
    }

    public DirectSupport getPlugin() {
        return plugin;
    }
}
