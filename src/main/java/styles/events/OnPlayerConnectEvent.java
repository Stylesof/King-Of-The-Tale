package styles.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import styles.world.KOTTMatch;
import styles.util.log.LogTypes;

import static styles.util.PrintMacros.printL;
import static styles.util.log.PrintLog.printLog;

public class OnPlayerConnectEvent {

    public static void onPlayerConnect(PlayerConnectEvent evt) {

        KOTTMatch match = KOTTMatch.getMatchesList().get(evt.getWorld());

        if(match != null && match.getKOTHMatchStatus()){
            // entered while a match is being played
            PlayerRef playerRef = evt.getPlayerRef();
            if(KOTTMatch.getMatchesList().get(evt.getWorld()).join(playerRef)){
                printLog(playerRef, LogTypes.KOTTMatchJoin);
            }
        }

        UserMapMarkersStore store = evt.getWorld().getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());
        for (UserMapMarker marker : store.getUserMapMarkers()) {
            printL("Marker: " + marker.getName());
        }
    }

}
