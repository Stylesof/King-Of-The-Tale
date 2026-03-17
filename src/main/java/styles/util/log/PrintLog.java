package styles.util.log;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.HashMap;
import java.util.Map;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

public class PrintLog {

    /*
        WARNING: This class exist to make easier
        the lang translation, it's *STILL* in progress
     */

    private static final Map<LogTypesDebug, String> logsDebug = new HashMap<>();
    private static final Map<LogTypes, String> logs = new HashMap<>();

    private static final String toPlayer = "[KOTT] ";
    private static final String toServer = "[KOTT Debug] ";

    static {

        //debug
        logsDebug.put(LogTypesDebug.KOTTLoadFailed,
                toServer + "Failed to load the KOTH mod! ");
        logsDebug.put(LogTypesDebug.KOTTLoadSuccess,
                toServer + "KOTH mod successfuly loaded! ");

        //normal
        logs.put(LogTypes.KOTTInvalidAreaSize,
                toPlayer + "Invalid area size value! Use: (min: 100, max: 1000) ");
        logs.put(LogTypes.KOTTInvalidTeamCount,
                toPlayer + "Invalid team count value! Use: (min: 2, max: 5) ");
        logs.put(LogTypes.KOTTMatchAlreadyRunning,
                toPlayer + "Has already an KOTH Match happening in the world ");
        logs.put(LogTypes.KOTTInvalidWorld,
                toPlayer + "This world doesn't exist! Use \"/world list\" to see all available worlds!");
        logs.put(LogTypes.KOTTMatchJoin,
                toPlayer + "You have joined an in progress match! ");
        logs.put(LogTypes.KOTTMatchStarted,
                toPlayer + "Successfully started a KOTT match! ");
    }

    public static void printLogDebug(LogTypesDebug logTypeDebug) { printL(logsDebug.get(logTypeDebug)); }

    public static void printLog(PlayerRef playerRef, LogTypes logType, String extra) {
        print(playerRef, logs.get(logType) + extra);
    }

    public static void printLog(PlayerRef playerRef, LogTypes logType) {
        print(playerRef, logs.get(logType));
    }
}
