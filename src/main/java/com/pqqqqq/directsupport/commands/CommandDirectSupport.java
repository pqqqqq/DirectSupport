package com.pqqqqq.directsupport.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.util.Utilities;
import com.pqqqqq.directsupport.Config;
import com.pqqqqq.directsupport.DirectSupport;
import com.pqqqqq.directsupport.ticket.Ticket;
import com.pqqqqq.directsupport.ticket.channel.TicketChannel;
import com.pqqqqq.directsupport.util.pagination.PaginatedList;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
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
 * Created by Kevin on 2015-05-06.
 */
public class CommandDirectSupport extends CommandBase {
    private static final Optional<Text> desc = Optional.<Text> of(Texts.of("Main directsupport command."));
    private static final Optional<Text> help = Optional.<Text> of(Texts.of("Main directsupport command."));

    public CommandDirectSupport(DirectSupport plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource commandSource, String s) throws CommandException {
        s = s.trim(); // Trim any excess spaces
        String[] args = s.split(" ");

        // Non player-only commands
        if (args[0].equalsIgnoreCase("reload")) {
            if (!commandSource.hasPermission("ds.reload") && !commandSource.hasPermission("ds.admin")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            getPlugin().getCfg().load();

            commandSource.sendMessage(getSuccessMessage("DirectSupport reloaded."));
            return Optional.of(CommandResult.success());
        } else if (args[0].equalsIgnoreCase("clear")) {
            if (!commandSource.hasPermission("ds.clear") && !commandSource.hasPermission("ds.admin")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            for (Ticket ticket : getPlugin().getTicketManager().getMap().values()) {
                if (!ticket.isCompleted()) {
                    ticket.terminateTicket();
                }
            }

            getPlugin().getTicketManager().clear();
            commandSource.sendMessage(getSuccessMessage("DirectSupport tickets cleared."));
            return Optional.of(CommandResult.success());
        } else if (args[0].equalsIgnoreCase("info")) {
            if (!commandSource.hasPermission("ds.info") && !commandSource.hasPermission("ds.mod") && !commandSource.hasPermission("ds.admin")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            if (args.length < 2) {
                commandSource.sendMessage(getErrorMessage("/ds info <ticket#>"));
                return Optional.of(CommandResult.success());
            }

            try {
                int id = Integer.parseInt(args[1]);
                Ticket ticket = getPlugin().getTicketManager().getValue(id);

                if (ticket == null) {
                    commandSource.sendMessage(getErrorMessage("There is no ticket with that id."));
                    return Optional.of(CommandResult.success());
                }

                commandSource.sendMessage(getNormalMessage("Ticket &f#" + id));
                commandSource.sendMessage(getNormalMessage("Synopsis: &f" + ticket.getMessage()));
                commandSource.sendMessage(getNormalMessage("Player: &f" + ticket.getPlayer().getLastCachedUsername()));
                commandSource.sendMessage(getNormalMessage("Helper: &f" + (ticket.getHelper() == null ? "&8NONE" : ticket.getHelper().getLastCachedUsername())));
                commandSource.sendMessage(getNormalMessage("State: &f" + ticket.getState().name()));
                commandSource.sendMessage(getNormalMessage("Creation: &f" + ticket.getFormattedCreationDate()));
                commandSource.sendMessage(getNormalMessage("At: &f" + ticket.getCreationLocation().getPosition().toString()));
                return Optional.of(CommandResult.success());
            } catch (NumberFormatException e) {
                commandSource.sendMessage(getErrorMessage("Invalid ticket id: &4" + args[1]));
                return Optional.of(CommandResult.success());
            }
        } else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("terminate")) {
                if (!commandSource.hasPermission("ds.delete") && !commandSource.hasPermission("ds.mod") && !commandSource.hasPermission("ds.admin")) {
                    commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                    return Optional.of(CommandResult.success());
                }

                if (args.length < 2) {
                    commandSource.sendMessage(getErrorMessage("/ds delete <ticket#>"));
                    return Optional.of(CommandResult.success());
                }

                try {
                    int id = Integer.parseInt(args[1]);
                    Ticket ticket = getPlugin().getTicketManager().getValue(id);

                    if (ticket == null) {
                        commandSource.sendMessage(getErrorMessage("There is no ticket with that id."));
                        return Optional.of(CommandResult.success());
                    }

                    if (ticket.isCompleted()) {
                        commandSource.sendMessage(getErrorMessage("This ticket has already been completed."));
                        return Optional.of(CommandResult.success());
                    }

                    ticket.terminateTicket();
                    commandSource.sendMessage(getNormalMessage("Ticket successfully terminated."));
                    return Optional.of(CommandResult.success());
                } catch (NumberFormatException e) {
                    commandSource.sendMessage(getErrorMessage("Invalid ticket id: &4" + args[1]));
                    return Optional.of(CommandResult.success());
                }
        } else if (args[0].equalsIgnoreCase("helpers") || args[0].equalsIgnoreCase("mods") || args[0].equalsIgnoreCase("ops")) {
            String message = "";

            for (Player online : getPlugin().getGame().getServer().getOnlinePlayers()) {
                if (online.hasPermission("ds.mod") || online.hasPermission("ds.admin")) {
                    message += "&3" + online.getName() + "&b, ";
                }
            }

            if (message.isEmpty()) {
                commandSource.sendMessage(getNormalMessage("There are no helpers online."));
            } else {
                commandSource.sendMessage(getMessageInColour("Online helpers: " + message.substring(0, message.length() - 2), TextColors.DARK_AQUA));
            }
            return Optional.of(CommandResult.success());
        } else if (args[0].equalsIgnoreCase("active") || args[0].equalsIgnoreCase("activet") || args[0].equalsIgnoreCase("list")) {
            if (!commandSource.hasPermission("ds.list") && !commandSource.hasPermission("ds.mod") && !commandSource.hasPermission("ds.admin")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            int page = 1;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    commandSource.sendMessage(getErrorMessage("Invalid page: &4" + args[1]));
                    return Optional.of(CommandResult.success());
                }
            }

            PaginatedList pList = new PaginatedList("/ds active", 3);
            for (Ticket ticket : getPlugin().getTicketManager().getMap().values()) {
                if (ticket.getState() == Ticket.State.WAITING_FOR_HELP) {
                    pList.add(Texts.builder(Utilities.formatColours("&3ID#" + ticket.getId() + " &f(" + ticket.getPlayer().getLastCachedUsername() + ") &8-> &b" + ticket.getMessage()))
                            .onClick(TextActions.runCommand("/ds accept " + ticket.getId())).onHover(TextActions.showText(Texts.of(TextColors.WHITE, "Accept this ticket."))).build());
                }
            }

            if (pList.size() == 0) {
                commandSource.sendMessage(getErrorMessage("There are no active tickets."));
                return Optional.of(CommandResult.success());
            }

            pList.displayLineNumbers(false);
            pList.setHeader(getNormalMessage("Active tickets: &3(&f" + pList.size() + "&3)"));
            pList.setFooter(getNormalMessage("\nUse &f/ds accept [ticket#] &b or click the entry to begin helping."));

            if (page < 1) {
                commandSource.sendMessage(getErrorMessage("Page number must be greater than 0."));
                return Optional.of(CommandResult.success());
            }

            int total = pList.getTotalPages();
            if (page > total) {
                commandSource.sendMessage(getErrorMessage("There are only &4" + total + " &cpage(s)."));
                return Optional.of(CommandResult.success());
            }

            commandSource.sendMessage(pList.getPage(page));
            return Optional.of(CommandResult.success());
        } else if (args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("completed")) {
            if (!commandSource.hasPermission("ds.completed") && !commandSource.hasPermission("ds.mod") && !commandSource.hasPermission("ds.admin")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            int page = 1;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    commandSource.sendMessage(getErrorMessage("Invalid page: &4" + args[1]));
                    return Optional.of(CommandResult.success());
                }
            }

            PaginatedList pList = new PaginatedList("/ds all", 3);
            for (Ticket ticket : getPlugin().getTicketManager().getMap().values()) {
                pList.add(Texts.of(getMessageInColour("ID#" + ticket.getId() + " &f(" + ticket.getPlayer().getLastCachedUsername() + ") &8-> &b" + ticket.getMessage(), TextColors.DARK_AQUA)));
            }

            if (pList.size() == 0) {
                commandSource.sendMessage(getErrorMessage("No tickets have been created."));
                return Optional.of(CommandResult.success());
            }

            pList.displayLineNumbers(false);
            pList.setHeader(getNormalMessage("All tickets: &3(&f" + pList.size() + "&3)"));

            if (page < 1) {
                commandSource.sendMessage(getErrorMessage("Page number must be greater than 0."));
                return Optional.of(CommandResult.success());
            }

            int total = pList.getTotalPages();
            if (page > total) {
                commandSource.sendMessage(getErrorMessage("There are only &4" + total + " &cpage(s)."));
                return Optional.of(CommandResult.success());
            }

            commandSource.sendMessage(pList.getPage(page));
            return Optional.of(CommandResult.success());
        } else if (args[0].equalsIgnoreCase("view")) {
            if (!commandSource.hasPermission("ds.view") && !commandSource.hasPermission("ds.admin")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            int id;
            int page = 1;

            if (args.length < 2) {
                commandSource.sendMessage(getErrorMessage("/ds view <ticket#> [page]."));
                return Optional.of(CommandResult.success());
            } else if (args.length > 2) {
                try {
                    page = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    commandSource.sendMessage(getErrorMessage("Invalid page: &4" + args[2]));
                    return Optional.of(CommandResult.success());
                }
            }

            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                commandSource.sendMessage(getErrorMessage("Invalid ID: &4" + args[1]));
                return Optional.of(CommandResult.success());
            }

            Ticket ticket = getPlugin().getTicketManager().getValue(id);
            if (ticket == null) {
                commandSource.sendMessage(getErrorMessage("There is no ticket with this ID."));
                return Optional.of(CommandResult.success());
            }

            if (ticket.getTicketChannel() == null || ticket.getTicketChannel().getConversation().isEmpty()) {
                commandSource.sendMessage(getErrorMessage("There is no conversation in this ticket."));
                return Optional.of(CommandResult.success());
            }

            PaginatedList pList = new PaginatedList("/ds view " + id, 3);
            pList.addAll(ticket.getTicketChannel().getConversation());

            pList.displayLineNumbers(false);
            pList.setHeader(getNormalMessage("&f" + ticket.getPlayer().getLastCachedUsername() + "&b's ticket &f#" + id));

            if (page < 1) {
                commandSource.sendMessage(getErrorMessage("Page number must be greater than 0."));
                return Optional.of(CommandResult.success());
            }

            int total = pList.getTotalPages();
            if (page > total) {
                commandSource.sendMessage(getErrorMessage("There are only &4" + total + " &cpage(s)."));
                return Optional.of(CommandResult.success());
            }

            commandSource.sendMessage(pList.getPage(page));
            return Optional.of(CommandResult.success());
        }

        // Player-only commands
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(getErrorMessage("Player-only command"));
            return Optional.of(CommandResult.success());
        }

        final Player player = (Player) commandSource;
        final Member member = getPlugin().getDirectChat().getMembers().getValue(player.getUniqueId().toString());

        if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("c")) {
            if (args.length < 2) {
                commandSource.sendMessage(getErrorMessage("/ds create <ticket ...>"));
                return Optional.of(CommandResult.success());
            }

            // See if it hasn't been the creation time yet.
            Object creationDelay = member.getExtraData().get("DirectSupport:CREATIONDELAY");
            if (creationDelay != null && (Boolean) creationDelay) {
                commandSource.sendMessage(getErrorMessage("Please wait before creating another ticket."));
                return Optional.of(CommandResult.success());
            }

            String ticketMessage = s.substring(s.indexOf(' ') + 1);
            if (ticketMessage.length() < 5) {
                commandSource.sendMessage(getErrorMessage("Your ticket message must be a minimum of 5 characters long."));
                return Optional.of(CommandResult.success());
            }

            if (ticketMessage.length() > 20) {
                commandSource.sendMessage(getErrorMessage("Your ticket message must be a maximum of 20 characters long."));
                return Optional.of(CommandResult.success());
            }

            if (member.getExtraData().containsKey("DirectSupport:CURRENTTICKET")) {
                commandSource.sendMessage(getErrorMessage("You are already in a ticket. Use &4/ds leave &cto leave it."));
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
                commandSource.sendMessage(getErrorMessage("There must be an admin online to assist you."));
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

            player.sendMessage(getSuccessMessage("Ticket successfully created. Please wait for a helper to assist you."));

            Text adminMsg = Texts.builder(Utilities.formatColours("&f" + player.getName() + " &bhas requested assistance (ID &f#" + id + "&b)"))
                    .onClick(TextActions.runCommand("/ds accept " + ticket.getId())).onHover(TextActions.showText(Texts.of(TextColors.WHITE, "Accept this ticket."))).build();
            for (Player admin : admins) {
                admin.sendMessage(adminMsg);
            }
        } else if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("join")) {
            if (!player.hasPermission("ds.accept") && !player.hasPermission("ds.mod") && !player.hasPermission("ds.admin")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            if (member.getExtraData().containsKey("DirectSupport:CURRENTTICKET")) {
                commandSource.sendMessage(getErrorMessage("You are already assisting someone - multi-tasking isn't that easy, you know."));
                return Optional.of(CommandResult.success());
            }

            Ticket toAssist = null;
            if (args.length == 1) {
                // Find first unassisted ticket
                for (Ticket ticket : getPlugin().getTicketManager().getMap().values()) {
                    if (ticket.getState() == Ticket.State.WAITING_FOR_HELP && !ticket.getPlayer().equals(member)) {
                        toAssist = ticket;
                        break;
                    }
                }

                if (toAssist == null) {
                    commandSource.sendMessage(getErrorMessage("No one requires assistance at this time."));
                    return Optional.of(CommandResult.success());
                }
            } else {
                // Find ticket via ID
                try {
                    toAssist = getPlugin().getTicketManager().getValue(Integer.parseInt(args[1]));
                    if (toAssist == null) {
                        commandSource.sendMessage(getErrorMessage("There is no uncompleted/unassisted ticket with this ID."));
                        return Optional.of(CommandResult.success());
                    }

                    if (toAssist.isBeingHelped() || toAssist.isCompleted()) {
                        commandSource.sendMessage(getErrorMessage("This ticket does not require help."));
                        return Optional.of(CommandResult.success());
                    }

                    if (toAssist.getPlayer().equals(member)) {
                        commandSource.sendMessage(getErrorMessage("You cannot assist yourself."));
                        return Optional.of(CommandResult.success());
                    }
                } catch (NumberFormatException e) {
                    commandSource.sendMessage(getErrorMessage("Invalid id: &4" + args[1]));
                    return Optional.of(CommandResult.success());
                }
            }

            // We have a valid, unassisted ticket, start assisting
            toAssist.startHelping(player);
        } else if (args[0].equalsIgnoreCase("leave")) {
            Object cur = member.getExtraData().get("DirectSupport:CURRENTTICKET");
            Ticket tc = (cur == null ? null : getPlugin().getTicketManager().getValue((Integer) cur));

            if (tc == null) {
                commandSource.sendMessage(getErrorMessage("You are not currently in a ticket."));
                return Optional.of(CommandResult.success());
            }

            tc.terminateTicket();
        } else if (args[0].equalsIgnoreCase("goto") || args[0].equalsIgnoreCase("tele") || args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport")) {
            if (!player.hasPermission("ds.goto") && !player.hasPermission("ds.mod") && !player.hasPermission("ds.admin")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            if (args.length < 2) {
                commandSource.sendMessage(getErrorMessage("/ds goto <ticket#>"));
                return Optional.of(CommandResult.success());
            }

            try {
                int id = Integer.parseInt(args[1]);
                Ticket ticket = getPlugin().getTicketManager().getValue(id);

                if (ticket == null) {
                    commandSource.sendMessage(getErrorMessage("There is no ticket with that id."));
                    return Optional.of(CommandResult.success());
                }

                player.setLocation(ticket.getCreationLocation());
                player.sendMessage(getSuccessMessage("Successfully teleported to ticket &f#" + id));
            } catch (NumberFormatException e) {
                commandSource.sendMessage(getErrorMessage("Invalid ticket id: &4" + args[1]));
                return Optional.of(CommandResult.success());
            }
        } else if (args[0].equalsIgnoreCase("say") || args[0].equalsIgnoreCase("speak")) {
            if (!player.hasPermission("ds.say") && !player.hasPermission("ds.admin")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            if (args.length < 2) {
                commandSource.sendMessage(getErrorMessage("/ds say <ticket#> <message ...>"));
                return Optional.of(CommandResult.success());
            }

            try {
                int id = Integer.parseInt(args[1]);
                Ticket ticket = getPlugin().getTicketManager().getValue(id);

                if (ticket == null) {
                    commandSource.sendMessage(getErrorMessage("There is no ticket with that id."));
                    return Optional.of(CommandResult.success());
                }

                if (!ticket.isBeingHelped() || ticket.getTicketChannel() == null) {
                    commandSource.sendMessage(getErrorMessage("There is no currently active channel for this ticket."));
                    return Optional.of(CommandResult.success());
                }

                String message = s.substring(s.indexOf(' ') + 1).substring(s.indexOf(' ') + 1).trim();
                if (player.hasPermission("directchat.colour")) {
                    message = Utilities.formatColours(message);
                }

                String format = Config.sayIntoFormat;
                format = format.replace("%PLAYERNAME%", player.getName());
                format = format.replace("%MESSAGE%", message);

                ticket.getTicketChannel().broadcast(Texts.of(format));
                player.sendMessage(Texts.of(format));
                player.sendMessage(getSuccessMessage("Message sent successfully."));
            } catch (NumberFormatException e) {
                commandSource.sendMessage(getErrorMessage("Invalid ticket id: &4" + args[1]));
                return Optional.of(CommandResult.success());
            }
        } else if (args[0].equalsIgnoreCase("select") || args[0].equalsIgnoreCase("s")) {
            Object cur = member.getExtraData().get("DirectSupport:CURRENTTICKET");
            Ticket tc = (cur == null ? null : getPlugin().getTicketManager().getValue((Integer) cur));

            if (tc == null) {
                commandSource.sendMessage(getErrorMessage("You are not currently in a ticket."));
                return Optional.of(CommandResult.success());
            }

            if (tc.getTicketChannel() == null) {
                commandSource.sendMessage(getErrorMessage("This ticket has not yet formed a channel."));
                return Optional.of(CommandResult.success());
            }

            if (member.getActive() != null && member.getActive().equals(tc.getTicketChannel())) {
                commandSource.sendMessage(getErrorMessage("You are already actively speaking in this ticket."));
                return Optional.of(CommandResult.success());
            }

            member.setActive(tc.getTicketChannel());
            commandSource.sendMessage(getSuccessMessage("Successfully set ticket as active channel."));
        } else if (args[0].equalsIgnoreCase("takeover")) {
            if (!player.hasPermission("ds.takeover") && !player.hasPermission("ds.admin")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            if (args.length < 2) {
                commandSource.sendMessage(getErrorMessage("/ds takeover <ticket#>"));
                return Optional.of(CommandResult.success());
            }

            try {
                int id = Integer.parseInt(args[1]);
                Ticket ticket = getPlugin().getTicketManager().getValue(id);

                if (ticket == null) {
                    commandSource.sendMessage(getErrorMessage("There is no ticket with that id."));
                    return Optional.of(CommandResult.success());
                }

                if (!ticket.isBeingHelped()) {
                    commandSource.sendMessage(getErrorMessage("This ticket does not have an active channel."));
                    return Optional.of(CommandResult.success());
                }

                if (ticket.isInTicket(member)) {
                    commandSource.sendMessage(getErrorMessage("You cannot take over a ticket you are already in."));
                    return Optional.of(CommandResult.success());
                }

                ticket.swapHelper(player);
            } catch (NumberFormatException e) {
                commandSource.sendMessage(getErrorMessage("Invalid ticket id: &4" + args[1]));
                return Optional.of(CommandResult.success());
            }
        } else {
            commandSource.sendMessage(getErrorMessage(getUsage(commandSource)));
        }

        return Optional.of(CommandResult.success());
    }

    public Optional<Text> getShortDescription(CommandSource commandSource) {
        return desc;
    }

    public Optional<Text> getHelp(CommandSource commandSource) {
        return help;
    }

    public Text getUsage(CommandSource commandSource) {
        return Texts.of("/ds <create|accept|leave|list|all|info|reload|clear|helpers|goto|say|view|select|takeover>");
    }
}
