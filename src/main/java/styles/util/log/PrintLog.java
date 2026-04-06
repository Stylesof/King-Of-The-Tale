package styles.util.log;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.util.MessageHandler;
import styles.util.Tuple;

import java.util.HashMap;
import java.util.Map;

import static styles.util.MessageHandler.*;

@Deprecated (
        forRemoval = false
)
public class PrintLog {

    /*
        WARNING: This class exist to make easier
        the lang translation, it's *STILL* in progress
     */

    private static final Map<LogTypesDebug, String> logsDebug = new HashMap<>();
    private static final Map<LogTypes, String> logs = new HashMap<>();

    static {
        //debug
        logsDebug.put(LogTypesDebug.KOTTLoadFailed,
                "Failed to load the KOTH mod! ");
        logsDebug.put(LogTypesDebug.KOTTLoadSuccess,
                "KOTT mod successfuly loaded! ");

        //normal
        logs.put(LogTypes.KOTTInvalidAreaSize,
                "Invalid Area Size! Use: (min: 100, max: 500)");
        logs.put(LogTypes.KOTTInvalidTeamCount,
                "Invalid Team Count! Use: (min: 2, max: 5)");
        logs.put(LogTypes.KOTTMatchAlreadyRunning,
                "Has already an KOTT Match happening in the world ");
        logs.put(LogTypes.KOTTInvalidWorld,
                "This world doesn't exist! Use \"/world list\" to see all available worlds!");
        logs.put(LogTypes.KOTTMatchJoin,
                "You have joined an in progress match! ");
        logs.put(LogTypes.KOTTMatchStarted,
                "Successfully started a KOTT match! ");
    }

    public static void printLogDebug(LogTypesDebug logTypeDebug) { MessageHandler.printLog(logsDebug.get(logTypeDebug)); }

    public static void printLog(PlayerRef playerRef, LogTypes logType, String extra) {
        printChat(playerRef, logs.get(logType) + extra);
    }

    public static void printLog(PlayerRef playerRef, LogTypes logType) {
        printChat(playerRef, logs.get(logType));
    }
}
