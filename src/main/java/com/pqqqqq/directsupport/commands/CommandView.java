package com.pqqqqq.directsupport.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directsupport.DirectSupport;
import com.pqqqqq.directsupport.ticket.Ticket;
import com.pqqqqq.directsupport.util.pagination.PaginatedList;
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
public class CommandView extends CommandBase {
    public static final Optional<Text> description = Optional.<Text> of(Texts.of(TextColors.AQUA, "Views the conversation in a ticket."));
    public static final Optional<Text> help = Optional.<Text> of(Texts.of(TextColors.AQUA, "A command to view the conversation in a ticket."));
    public static final Text usage = Texts.of(TextColors.RED, "/ds list [page#]");

    public CommandView(DirectSupport plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource source, String arguments) throws CommandException {
        if (!testPermission(source)) {
            source.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
            return Optional.of(CommandResult.success());
        }

        String[] args = arguments.trim().split(" ");

        int id;
        int page = 1;

        if (args.length < 2) {
            source.sendMessage(usage);
            return Optional.of(CommandResult.success());
        } else if (args.length > 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                source.sendMessage(Texts.of(TextColors.AQUA, "Invalid page: ", TextColors.WHITE, args[1]));
                return Optional.of(CommandResult.success());
            }
        }

        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            source.sendMessage(Texts.of(TextColors.AQUA, "Invalid ID: ", TextColors.WHITE, args[0]));
            return Optional.of(CommandResult.success());
        }

        Ticket ticket = plugin.getTicketManager().getValue(id);
        if (ticket == null) {
            source.sendMessage(Texts.of(TextColors.RED, "There is no ticket with this ID."));
            return Optional.of(CommandResult.success());
        }

        if (ticket.getTicketChannel() == null || ticket.getTicketChannel().getConversation().isEmpty()) {
            source.sendMessage(Texts.of(TextColors.RED, "There is no conversation in this ticket."));
            return Optional.of(CommandResult.success());
        }

        PaginatedList pList = new PaginatedList("/ds view " + id, 3);
        pList.addAll(ticket.getTicketChannel().getConversation());

        if (pList.size() == 0) {
            source.sendMessage(Texts.of(TextColors.RED, "Thereès no conversarion in this ticket."));
            return Optional.of(CommandResult.success());
        }

        pList.displayLineNumbers(false);
        pList.setHeader(Texts.of(TextColors.AQUA, "Conversation in ", TextColors.WHITE, "#" + id));

        if (page < 1) {
            source.sendMessage(Texts.of(TextColors.RED, "Page number must be greater than 0."));
            return Optional.of(CommandResult.success());
        }

        int total = pList.getTotalPages();
        if (page > total) {
            source.sendMessage(Texts.of(TextColors.RED, "There are only ", TextColors.WHITE, total, TextColors.AQUA, " page(s)."));
            return Optional.of(CommandResult.success());
        }

        source.sendMessage(pList.getPage(page));
        return Optional.of(CommandResult.success());
    }

    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return Collections.emptyList();
    }

    public boolean testPermission(CommandSource source) {
        return source.hasPermission("ds.view") || source.hasPermission("ds.admin");
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
