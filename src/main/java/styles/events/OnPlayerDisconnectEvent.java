package styles.events;

import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMap;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

import static styles.util.PrintMacros.printL;

public class OnPlayerDisconnectEvent {
    public static void onPlayerDisconnect(PlayerDisconnectEvent evt) {
        PlayerRef ref = evt.getPlayerRef();

        printL("Chegou aq hi");

        World world = Universe.get().getWorld(ref.getWorldUuid());
        for(KOTTTeam team : KOTTMatch.getMatchesList().get(world.getName()).getTeams().values()) {
            ref.getPacketHandler().writeNoCache(new UpdateWorldMap(
                    null,
                    null,
                    new String[] {team.getBaseZone().getZoneMarker().getId()}
            ));
        }
    }
}
