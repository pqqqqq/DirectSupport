package com.pqqqqq.directsupport.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.util.Utilities;
import com.pqqqqq.directsupport.DirectSupport;
import com.pqqqqq.directsupport.ticket.Ticket;
import com.pqqqqq.directsupport.util.pagination.PaginatedList;
import org.spongepowered.api.text.Text;
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
public class CommandList extends CommandBase {
    public static final Optional<Text> description = Optional.<Text> of(Texts.of(TextColors.AQUA, "Lists the currently active tickets."));
    public static final Optional<Text> help = Optional.<Text> of(Texts.of(TextColors.AQUA, "A command to list the current active tickets."));
    public static final Text usage = Texts.of(TextColors.RED, "/ds list [page#]");

    public CommandList(DirectSupport plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource source, String arguments) throws CommandException {
        if (!testPermission(source)) {
            source.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
            return Optional.of(CommandResult.success());
        }

        arguments = arguments.trim();

        int page = 1;
        if (!arguments.isEmpty()) {
            try {
                page = Integer.parseInt(arguments);
            } catch (NumberFormatException e) {
                source.sendMessage(Texts.of(TextColors.RED, "Invalid page: ", TextColors.WHITE, arguments));
                return Optional.of(CommandResult.success());
            }
        }

        PaginatedList pList = new PaginatedList("/ds list", 3);
        for (Ticket ticket : plugin.getTicketManager().getMap().values()) {
            if (ticket.getState() == Ticket.State.WAITING_FOR_HELP) {
                pList.add(Texts.builder(Utilities.formatColours("&3ID#" + ticket.getId() + " &f(" + ticket.getPlayer().getLastCachedUsername() + ") &8-> &b" + ticket.getMessage()))
                        .onClick(TextActions.runCommand("/ds accept " + ticket.getId())).onHover(TextActions.showText(Texts.of(TextColors.WHITE, "Accept this ticket."))).build());
            }
        }

        if (pList.size() == 0) {
            source.sendMessage(Texts.of(TextColors.RED, "There are no active tickets."));
            return Optional.of(CommandResult.success());
        }

        pList.displayLineNumbers(false);
        pList.setHeader(Texts.of(TextColors.AQUA, "Active tickets: (", TextColors.WHITE, pList.size(), TextColors.AQUA, ")"));
        pList.setFooter(Texts.of(TextColors.AQUA, "\nUse ", TextColors.WHITE, "/ds accept [ticket#] ", TextColors.AQUA, "or click the entry to begin helping."));

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
        return source.hasPermission("ds.list") || source.hasPermission("ds.mod") || source.hasPermission("ds.admin");
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
