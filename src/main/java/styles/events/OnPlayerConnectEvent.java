package styles.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.KOTHMatch;
import styles.util.log.LogTypes;

import static styles.util.PrintMacros.print;
import static styles.util.log.PrintLog.printLog;

public class OnPlayerConnectEvent {

    public static void onPlayerConnect(PlayerConnectEvent evt) {
        if(KOTHMatch.getKOTHMatchStatus()){
            // entered while a match is being played
            PlayerRef playerRef = evt.getPlayerRef();
            if(KOTHMatch.join(playerRef)){
                printLog(playerRef, LogTypes.KOTHMatchJoin);
            }
        }
    }

}
