package com.pqqqqq.directsupport.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.channel.member.Member;
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
public class CommandGoto extends CommandBase {
    public static final Optional<Text> description = Optional.<Text> of(Texts.of(TextColors.AQUA, "Travels to the scene of a ticket."));
    public static final Optional<Text> help = Optional.<Text> of(Texts.of(TextColors.AQUA, "A command to travel to tickets."));
    public static final Text usage = Texts.of(TextColors.RED, "/ds goto <ticket#>");

    public CommandGoto(DirectSupport plugin) {
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

        arguments = arguments.trim();
        if (arguments.isEmpty()) {
            player.sendMessage(usage);
            return Optional.of(CommandResult.success());
        }

        try {
            int id = Integer.parseInt(arguments);
            Ticket ticket = getPlugin().getTicketManager().getValue(id);

            if (ticket == null) {
                player.sendMessage(Texts.of(TextColors.RED, "There is no ticket with that id."));
                return Optional.of(CommandResult.success());
            }

            player.setLocation(ticket.getCreationLocation());
            player.sendMessage(Texts.of(TextColors.GREEN, "Successfully teleported to ticket ", TextColors.WHITE, "#" + id));
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
        return source.hasPermission("ds.goto") || source.hasPermission("ds.mod") || source.hasPermission("ds.admin");
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
