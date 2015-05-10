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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kevin on 2015-05-10.
 */
public class CommandCreate extends CommandBase {
    public static final Optional<Text> description = Optional.<Text> of(Texts.of(TextColors.AQUA, "Creates a new ticket."));
    public static final Optional<Text> help = Optional.<Text> of(Texts.of(TextColors.AQUA, "A command to create tickets."));
    public static final Text usage = Texts.of(TextColors.RED, "/ds create <message ...>");

    public CommandCreate(DirectSupport plugin) {
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
        final Member member = plugin.getDirectChat().getMembers().getValue(player.getUniqueId().toString());

        arguments = arguments.trim();
        if (arguments.isEmpty()) {
            source.sendMessage(usage);
            return Optional.of(CommandResult.success());
        }

        // See if it hasn't been the creation time yet.
        Object creationDelay = member.getExtraData().get("DirectSupport:CREATIONDELAY");
        if (creationDelay != null && (Boolean) creationDelay) {
            source.sendMessage(Texts.of(TextColors.RED, "Please wait before creating another ticket."));
            return Optional.of(CommandResult.success());
        }

        String ticketMessage = arguments;
        if (ticketMessage.length() < 5) {
            source.sendMessage(Texts.of(TextColors.RED, "Your ticket message must be a minimum of 5 characters long."));
            return Optional.of(CommandResult.success());
        }

        if (ticketMessage.length() > 20) {
            source.sendMessage(Texts.of(TextColors.RED, "Your ticket message must be a maximum of 20 characters long."));
            return Optional.of(CommandResult.success());
        }

        if (member.getExtraData().containsKey("DirectSupport:CURRENTTICKET")) {
            source.sendMessage(Texts.of(TextColors.RED, "You are already in a ticket. Use &4/ds leave &cto leave it."));
            return Optional.of(CommandResult.success());
        }

        int id = getPlugin().getTicketManager().size() + 1;
        boolean okay = false;
        List<Player> admins = new ArrayList<Player>();

        for (Member admin : getPlugin().getDirectChat().getMembers().getMap().values()) {
            Optional<Player> adminP = admin.getPlayer();

            if (!adminP.isPresent() || admin.equals(member)) {
                continue;
            }

            if (adminP.get().hasPermission("ds.mod") || adminP.get().hasPermission("ds.admin")) {
                admins.add(adminP.get());
                if (admin.isActive()) {
                    okay = true;
                }
            }
        }

        if (!okay && !Config.allowCreationNoAdmins) {
            source.sendMessage(Texts.of(TextColors.RED, "There must be an admin online to assist you."));
            return Optional.of(CommandResult.success());
        }

        Ticket ticket = new Ticket(id, player, ticketMessage);
        getPlugin().getTicketManager().add(id, ticket);

        // Add creation delay
        member.getExtraData().put("DirectSupport:CREATIONDELAY", true);
        getPlugin().getGame().getAsyncScheduler().runTaskAfter(getPlugin(), new Runnable() {

            public void run() {
                member.getExtraData().remove("DirectSupport:CREATIONDELAY");
            }
        }, TimeUnit.SECONDS, Config.creationDelay);

        player.sendMessage(Texts.of(TextColors.GREEN, "Ticket successfully created. Please wait for a helper to assist you."));

        Text adminMsg = Texts.builder(Utilities.formatColours("&f" + player.getName() + " &bhas requested assistance (ID &f#" + id + "&b)"))
                .onClick(TextActions.runCommand("/ds accept " + ticket.getId())).onHover(TextActions.showText(Texts.of(TextColors.WHITE, "Accept this ticket."))).build();
        for (Player admin : admins) {
            admin.sendMessage(adminMsg);
        }

        return Optional.of(CommandResult.success());
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
