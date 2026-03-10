package styles.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.KOTHMatch;

import static styles.util.Utils.print;

public class OnPlayerConnectEvent {

    public static void onPlayerConnect(PlayerConnectEvent evt) {
        if(KOTHMatch.getKOTHMatchStatus()){
            // entered while a match is being played
            PlayerRef playerRef = evt.getPlayerRef();
            if(KOTHMatch.join(playerRef)){
                print(playerRef, "[KOTH] You have joined an in progress match!");
            }
        }
    }

}
