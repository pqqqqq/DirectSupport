package com.pqqqqq.directsupport.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directsupport.DirectSupport;
import com.pqqqqq.directsupport.ticket.Ticket;
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
public class CommandDelete extends CommandBase {
    public static final Optional<Text> description = Optional.<Text> of(Texts.of(TextColors.AQUA, "Deletes an active ticket."));
    public static final Optional<Text> help = Optional.<Text> of(Texts.of(TextColors.AQUA, "A command to delete active tickets."));
    public static final Text usage = Texts.of(TextColors.RED, "/ds delete <ticket#>");

    public CommandDelete(DirectSupport plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource source, String arguments) throws CommandException {
        if (!testPermission(source)) {
            source.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
            return Optional.of(CommandResult.success());
        }

        arguments = arguments.trim();

        if (arguments.isEmpty()) {
            source.sendMessage(usage);
            return Optional.of(CommandResult.success());
        }

        try {
            int id = Integer.parseInt(arguments);
            Ticket ticket = plugin.getTicketManager().getValue(id);

            if (ticket == null) {
                source.sendMessage(Texts.of(TextColors.RED, "There is no ticket with that id."));
                return Optional.of(CommandResult.success());
            }

            if (ticket.isCompleted()) {
                source.sendMessage(Texts.of(TextColors.RED, "This ticket has already been completed."));
                return Optional.of(CommandResult.success());
            }

            ticket.terminateTicket();
            source.sendMessage(Texts.of(TextColors.AQUA, "Ticket successfully terminated."));
            return Optional.of(CommandResult.success());
        } catch (NumberFormatException e) {
            source.sendMessage(Texts.of(TextColors.RED, "Invalid ticket id: ", TextColors.WHITE, arguments));
            return Optional.of(CommandResult.success());
        }
    }

    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return Collections.emptyList();
    }

    public boolean testPermission(CommandSource source) {
        return source.hasPermission("ds.delete") || source.hasPermission("ds.mod") || source.hasPermission("ds.admin");
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
