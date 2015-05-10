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
public class CommandAccept extends CommandBase {
    public static final Optional<Text> description = Optional.<Text> of(Texts.of(TextColors.AQUA, "Accepts a ticket."));
    public static final Optional<Text> help = Optional.<Text> of(Texts.of(TextColors.AQUA, "A command to accept tickets."));
    public static final Text usage = Texts.of(TextColors.RED, "/ds accept [ticket#]");

    public CommandAccept(DirectSupport plugin) {
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

        if (member.getExtraData().containsKey("DirectSupport:CURRENTTICKET")) {
            player.sendMessage(Texts.of(TextColors.RED, "You are already assisting someone - multi-tasking isn't that easy, you know."));
            return Optional.of(CommandResult.success());
        }

        arguments = arguments.trim();
        Ticket toAssist = null;
        if (arguments.isEmpty()) {
            // Find first unassisted ticket
            for (Ticket ticket : getPlugin().getTicketManager().getMap().values()) {
                if (ticket.getState() == Ticket.State.WAITING_FOR_HELP && !ticket.getPlayer().equals(member)) {
                    toAssist = ticket;
                    break;
                }
            }

            if (toAssist == null) {
                source.sendMessage(Texts.of(TextColors.RED, "No one requires assistance at this time."));
                return Optional.of(CommandResult.success());
            }
        } else {
            // Find ticket via ID
            try {
                toAssist = getPlugin().getTicketManager().getValue(Integer.parseInt(arguments));
                if (toAssist == null) {
                    source.sendMessage(Texts.of(TextColors.RED, "There is no uncompleted/unassisted ticket with this ID."));
                    return Optional.of(CommandResult.success());
                }

                if (toAssist.isBeingHelped() || toAssist.isCompleted()) {
                    source.sendMessage(Texts.of(TextColors.RED, "This ticket does not require help."));
                    return Optional.of(CommandResult.success());
                }

                if (toAssist.getPlayer().equals(member)) {
                    source.sendMessage(Texts.of(TextColors.RED, "You cannot assist yourself."));
                    return Optional.of(CommandResult.success());
                }
            } catch (NumberFormatException e) {
                source.sendMessage(Texts.of(TextColors.RED, "Invalid id: " + TextColors.WHITE, arguments));
                return Optional.of(CommandResult.success());
            }
        }

        // We have a valid, unassisted ticket, start assisting
        toAssist.startHelping(player);
        return Optional.of(CommandResult.success());
    }

    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return Collections.emptyList();
    }

    public boolean testPermission(CommandSource source) {
        return source.hasPermission("ds.accept") || source.hasPermission("ds.mod") || source.hasPermission("ds.admin");
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
