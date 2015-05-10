package com.pqqqqq.directsupport.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directsupport.DirectSupport;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

import java.util.Collections;
import java.util.List;

/**
 * Created by Kevin on 2015-05-10.
 */
public class CommandReload extends CommandBase {
    public static final Optional<Text> description = Optional.<Text> of(Texts.of(TextColors.AQUA, "Reloads DirectSupport's configuration."));
    public static final Optional<Text> help = Optional.<Text> of(Texts.of(TextColors.AQUA, "A command to reload configuration."));
    public static final Text usage = Texts.of(TextColors.RED, "/ds reload");

    public CommandReload(DirectSupport plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource source, String arguments) throws CommandException {
        if (!testPermission(source)) {
            source.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
            return Optional.of(CommandResult.success());
        }

        plugin.getCfg().load();
        source.sendMessage(Texts.of(TextColors.GREEN, "DirectSupport successfully reloaded."));
        return Optional.of(CommandResult.success());
    }

    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return Collections.emptyList();
    }

    public boolean testPermission(CommandSource source) {
        return source.hasPermission("ds.reload") || source.hasPermission("ds.admin");
    }

    public Optional<Text> getShortDescription(CommandSource source) {
        return description;
    }

    public Optional<Text> getHelp(CommandSource source) {
        return help;
    }

    public Text getUsage(CommandSource source) {
        return usage;
    }
}
