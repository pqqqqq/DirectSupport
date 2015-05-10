package com.pqqqqq.directsupport.events;

import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directsupport.DirectSupport;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerQuitEvent;

/**
 * Created by Kevin on 2015-05-10.
 */
public class CoreEvents {
    private DirectSupport plugin;

    public CoreEvents(DirectSupport plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void leave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Member member = plugin.getDirectChat().getMembers().getValue(player.getUniqueId().toString());

        if (member == null) {
            return;
        }

        Object curTicket = member.getExtraData().get("DirectSupport:CURRENTTICKET");
        if (curTicket == null) {
            return;
        }

        plugin.getTicketManager().getValue((Integer) curTicket).terminateTicket();
    }
}
