package styles.network.player;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interaction.CancelInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static styles.util.MessageHandler.printChat;

public class PlayerAttackHandler implements PlayerPacketFilter {
    @Override
    public boolean test(PlayerRef playerRef, Packet packet) {
        if(!(packet instanceof SyncInteractionChains syncPacket)) return false;
        if (playerRef == null) return false;

        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        if (world != null) {
            KOTTMatch match = KOTTMatch.getMatch(world.getName());
            if (match != null) {
                for (KOTTTeam team : match.getTeams()) {
                    if (team.getBaseZone().getPlayersInZone().contains(playerRef)) {
                        for (SyncInteractionChain chain : syncPacket.updates) {
                            if (chain.interactionType == InteractionType.Primary) {
                                printChat(playerRef, Message.raw("You can't shoot while inside your Base").color(Color.RED));
                                return  true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
