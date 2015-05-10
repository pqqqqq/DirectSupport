package com.pqqqqq.directsupport.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.util.Utilities;
import com.pqqqqq.directsupport.Config;
import com.pqqqqq.directsupport.DirectSupport;
import com.pqqqqq.directsupport.ticket.Ticket;
import org.spongepowered.api.entity.player.Player;
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
public class CommandSay extends CommandBase {
    public static final Optional<Text> description = Optional.<Text> of(Texts.of(TextColors.AQUA, "Talks into a ticket from outside."));
    public static final Optional<Text> help = Optional.<Text> of(Texts.of(TextColors.AQUA, "A command to talk into a ticket."));
    public static final Text usage = Texts.of(TextColors.RED, "/ds say <ticket#> <message ...>");

    public CommandSay(DirectSupport plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource source, String arguments) throws CommandException {
        if (!testPermission(source)) {
            source.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
            return Optional.of(CommandResult.success());
        }

        if (!(source instanceof Player)) {
            source.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) source;
        Member member = plugin.getDirectChat().getMembers().getValue(player.getUniqueId().toString());

        String[] args = arguments.trim().split(" ");

        if (args.length < 2) {
            player.sendMessage(usage);
            return Optional.of(CommandResult.success());
        }

        try {
            int id = Integer.parseInt(args[0]);
            Ticket ticket = getPlugin().getTicketManager().getValue(id);

            if (ticket == null) {
                player.sendMessage(Texts.of(TextColors.RED, "There is no ticket with that id."));
                return Optional.of(CommandResult.success());
            }

            if (!ticket.isBeingHelped() || ticket.getTicketChannel() == null) {
                source.sendMessage(Texts.of(TextColors.RED, "There is no currently active channel for this ticket."));
                return Optional.of(CommandResult.success());
            }

            String message = arguments.substring(arguments.indexOf(' ') + 1);
            if (player.hasPermission("directchat.colour")) {
                message = Utilities.formatColours(message);
            }

            String format = Config.sayIntoFormat;
            format = format.replace("%PLAYERNAME%", player.getName());
            format = format.replace("%MESSAGE%", message);

            ticket.getTicketChannel().broadcast(Texts.of(format));
            player.sendMessage(Texts.of(format));
            source.sendMessage(Texts.of(TextColors.GREEN, "Message sent successfully."));
            return Optional.of(CommandResult.success());
        } catch (NumberFormatException e) {
            player.sendMessage(Texts.of(TextColors.RED, "Invalid ticket id:", TextColors.WHITE, arguments));
            return Optional.of(CommandResult.success());
        }
    }

    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return Collections.emptyList();
    }

    public boolean testPermission(CommandSource source) {
        return source.hasPermission("ds.say") || source.hasPermission("ds.mod") || source.hasPermission("ds.admin");
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
