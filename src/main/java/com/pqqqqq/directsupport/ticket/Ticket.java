package com.pqqqqq.directsupport.ticket;

import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.util.Utilities;
import com.pqqqqq.directsupport.Config;
import com.pqqqqq.directsupport.DirectSupport;
import com.pqqqqq.directsupport.ticket.channel.TicketChannel;
import com.pqqqqq.directsupport.util.MappedManager;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kevin on 2015-05-06.
 * Class that represents a directsupport ticket
 */
public class Ticket {
    private final int id;
    private final Member player;
    private final String message;
    private final DirectSupport plugin;

    @Nullable
    private Member helper = null;

    @Nullable
    private TicketChannel channel = null;

    private final Location location;
    private final Date creationDate;

    private State state = State.WAITING_FOR_HELP;

    public Ticket(int id, Player player, String message) {
        this.id = id;
        this.player = DirectSupport.plugin.getDirectChat().getMembers().getValue(player.getUniqueId().toString());
        this.message = message.trim();
        this.plugin = DirectSupport.plugin;

        // Finals
        this.location = player.getLocation();
        this.creationDate = new Date();

        // Set extra data
        this.player.getExtraData().put("DirectSupport:CURRENTTICKET", getId());
    }

    public int getId() {
        return id;
    }

    public Member getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public Member getHelper() {
        return helper;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Location getCreationLocation() {
        return location;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getFormattedCreationDate() {
        return Config.dateFormat.format(creationDate);
    }

    public void startHelping(Player helper) {
        if (isCompleted() || isBeingHelped()) {
            return;
        }

        this.helper = DirectSupport.plugin.getDirectChat().getMembers().getValue(helper.getUniqueId().toString());

        // Create a private room with just the two players
        DirectChat dc = this.plugin.getDirectChat();

        this.channel = new TicketChannel("DIRECTSUPPORT *" + this.player.getLastCachedUsername() + "* | *" + helper.getName() + "*", this);
        this.channel.setUndetectable(true);
        this.channel.setSilent(true);
        this.channel.setLeaveOnExit(true);

        this.player.enterChannel(channel, false);
        this.helper.enterChannel(channel, false);

        // Let them know
        this.player.sendMessage(Texts.of(TextColors.WHITE, helper.getName(), TextColors.AQUA, " is now assisting you. Say hi!"));
        helper.sendMessage(Texts.of(TextColors.AQUA, "You are assisting ", TextColors.WHITE, this.player.getLastCachedUsername(), TextColors.AQUA, " for: ", TextColors.WHITE, getMessage()));

        this.state = State.BEING_HELPED;

        // Set extra datas
        this.helper.getExtraData().put("DirectSupport:CURRENTTICKET", getId());
    }

    public void swapHelper(Player newHelper) {
        if (isCompleted() || !isBeingHelped()) {
            return;
        }

        Member oldm = this.helper;
        Member newm = DirectSupport.plugin.getDirectChat().getMembers().getValue(newHelper.getUniqueId().toString());
        this.helper = newm;

        oldm.sendMessage(Texts.of(TextColors.AQUA, "You have been booted from helping this ticket."));

        // Silently swap
        newm.enterChannel(channel, false);
        oldm.leaveChannel(channel, false);

        this.player.sendMessage(Texts.of(TextColors.WHITE, newHelper.getName(), TextColors.AQUA, " has now taken over your ticket to assist you. Say hi!"));
        newHelper.sendMessage(Texts.of(TextColors.AQUA, "You are assisting ", TextColors.WHITE, this.player.getLastCachedUsername(), TextColors.AQUA, " for: ", TextColors.WHITE, getMessage()));

        // Set extra datas
        newm.getExtraData().put("DirectSupport:CURRENTTICKET", getId());
        oldm.getExtraData().remove("DirectSupport:CURRENTTICKET");
    }

    public void terminateTicket() {
        if (!isCompleted()) {
            state = State.COMPLETED;

            player.sendMessage(Texts.of(TextColors.AQUA, "The ticket you were in has been terminated."));
            this.player.getExtraData().remove("DirectSupport:CURRENTTICKET");

            if (helper != null) {
                this.helper.getExtraData().remove("DirectSupport:CURRENTTICKET");
                helper.sendMessage(Texts.of(TextColors.AQUA, "The ticket you were in has been terminated."));
            }

            if (channel != null) {
                Set<Member> members = new HashSet<Member>();
                members.addAll(channel.getMembers());

                for (Member member : members) {
                    member.leaveChannel(channel, false);
                }
            }
        }
    }

    public boolean isBeingHelped() {
        return helper != null && state == State.BEING_HELPED && channel != null;
    }

    public boolean isCompleted() {
        return state == State.COMPLETED;
    }

    public boolean isInTicket(Member member) {
        return getPlayer().equals(member) || this.helper != null && this.helper.equals(member);
    }

    public TicketChannel getTicketChannel() {
        return channel;
    }

    public static class Manager extends MappedManager<Integer, Ticket> {
    }

    public static class Reminder implements Runnable {

        public void run() {
            int num = 0;

            for (Ticket ticket : DirectSupport.plugin.getTicketManager().getMap().values()) {
                if (ticket.getState() == State.WAITING_FOR_HELP) {
                    num++;
                }
            }

            if (num == 0) { // You're lucky
                return;
            }

            TextBuilder build = Texts.builder();
            build.append(Texts.of(TextColors.AQUA, "There are ", TextColors.WHITE, num, TextColors.AQUA, " active ticket(s). Use "));
            build.append(Texts.builder("/ds list ").color(TextColors.WHITE).onClick(TextActions.runCommand("/ds list")).onHover(TextActions.showText(Texts.of(TextColors.WHITE, "Run /ds list"))).build());
            build.append(Texts.of(TextColors.AQUA, "to start helping."));
            Text text = build.build();

            for (Player admin : DirectSupport.plugin.getGame().getServer().getOnlinePlayers()) {
                if (admin.hasPermission("ds.mod") || admin.hasPermission("ds.admin")) {
                    admin.sendMessage(text);
                }
            }
        }
    }

    public enum State {
        WAITING_FOR_HELP, BEING_HELPED, COMPLETED;
    }
}
