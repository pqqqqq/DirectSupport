package com.pqqqqq.directsupport.ticket.channel;

import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directsupport.Config;
import com.pqqqqq.directsupport.ticket.Ticket;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 2015-05-06.
 */
public class TicketChannel extends Channel {
    private Ticket ticket;
    private List<Text> conversation = new ArrayList<Text>();

    public TicketChannel(String name, Ticket ticket) {
        super(name);
        this.ticket = ticket;

        setFormat(Config.messageFormat);
    }

    public Ticket getTicket() {
        return ticket;
    }

    @Override
    public EnterResult canEnter(Member member) {
        if (member.equals(getTicket().getHelper()) || member.equals(getTicket().getPlayer())) {
            return EnterResult.SUCCESS;
        }

        return EnterResult.OTHER_FAILURE;
    }

    @Override
    public void onLeave(Member member) {
        if (getMembers().size() < 2) {
            getTicket().terminateTicket();
        }
    }

    @Override
    public void onMessage(Member member, Text message) {
        this.conversation.add(message);
    }

    @Override
    public String getFormattedJoinMessage() {
        String formatM = super.getFormattedJoinMessage();
        return formatM == null || formatM.trim().isEmpty() ? null : formatM.replace("%HELPER%", (ticket.getHelper() == null ? "" : ticket.getHelper().getLastCachedUsername())).replace("%PLAYER%", ticket.getPlayer().getLastCachedUsername());
    }

    @Override
    public String formatMessage(Player sender, Member member, String message) {
        String format = getFormat();
        format = format.replace("%MESSAGE%", message);
        format = format.replace("%PLAYERNAME%", sender.getName());
        format = format.replace("%HELPER%", (ticket.getHelper() == null ? "" : ticket.getHelper().getLastCachedUsername()));
        format = format.replace("%TICKETER%", ticket.getPlayer().getLastCachedUsername());

        Member other = null;
        if (member.equals(ticket.getPlayer())) {
            other = ticket.getHelper();
        } else if (ticket.getHelper() != null && member.equals(ticket.getHelper())) {
            other = ticket.getPlayer();
        }

        format = format.replace("%OTHER%", (other == null ? "" : other.getLastCachedUsername()));
        return format;
    }

    public List<Text> getConversation() {
        return this.conversation;
    }
}
