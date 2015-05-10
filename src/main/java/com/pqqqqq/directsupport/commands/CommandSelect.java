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
public class CommandSelect extends CommandBase {
    public static final Optional<Text> description = Optional.<Text> of(Texts.of(TextColors.AQUA, "Selects the current active ticket."));
    public static final Optional<Text> help = Optional.<Text> of(Texts.of(TextColors.AQUA, "A command to select the active ticket."));
    public static final Text usage = Texts.of(TextColors.RED, "/ds select");

    public CommandSelect(DirectSupport plugin) {
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

        Object cur = member.getExtraData().get("DirectSupport:CURRENTTICKET");
        Ticket tc = (cur == null ? null : getPlugin().getTicketManager().getValue((Integer) cur));

        if (tc == null) {
            player.sendMessage(Texts.of(TextColors.RED, "You are not currently in a ticket."));
            return Optional.of(CommandResult.success());
        }

        if (tc.getTicketChannel() == null) {
            player.sendMessage(Texts.of(TextColors.RED, "This ticket has not yet formed a channel."));
            return Optional.of(CommandResult.success());
        }

        if (member.getActive() != null && member.getActive().equals(tc.getTicketChannel())) {
            player.sendMessage(Texts.of(TextColors.RED, "You are already actively speaking in this ticket."));
            return Optional.of(CommandResult.success());
        }

        member.setActive(tc.getTicketChannel());
        player.sendMessage(Texts.of(TextColors.GREEN, "Successfully set ticket as active channel."));
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
