package com.pqqqqq.directsupport.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directsupport.DirectSupport;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

import java.util.Collections;
import java.util.List;

/**
 * Created by Kevin on 2015-05-10.
 */
public class CommandHelpers extends CommandBase {
    public static final Optional<Text> description = Optional.<Text> of(Texts.of(TextColors.AQUA, "Prints out a list of people online capable of helping."));
    public static final Optional<Text> help = Optional.<Text> of(Texts.of(TextColors.AQUA, "A command to show the helpers online."));
    public static final Text usage = Texts.of(TextColors.RED, "/ds helpers");

    public CommandHelpers(DirectSupport plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource source, String arguments) throws CommandException {
        if (!testPermission(source)) {
            source.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
            return Optional.of(CommandResult.success());
        }

        TextBuilder builder = Texts.builder();
        builder.append(Texts.of(TextColors.AQUA, "Online helpers: "));

        int appended = 0;
        for (Player player : plugin.getGame().getServer().getOnlinePlayers()) {
            if (player.hasPermission("ds.accept") || player.hasPermission("ds.mod") || player.hasPermission("ds.admin")) {
                builder.append(Texts.builder((appended > 0 ? ", " : "") + player.getName()).color(TextColors.WHITE)
                        .onClick(TextActions.suggestCommand("/msg " + player.getName() + " ")).onHover(TextActions.showText(Texts.of(TextColors.WHITE, "Message this helper."))).build());
                appended++;
            }
        }

        if (appended == 0) {
            source.sendMessage(Texts.of(TextColors.RED, "There are no helpers online."));
        } else {
            source.sendMessage(builder.build());
        }
        return Optional.of(CommandResult.success());
    }

    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return Collections.emptyList();
    }

    public boolean testPermission(CommandSource source) {
        return true;
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