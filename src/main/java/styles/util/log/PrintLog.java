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

    static {

        //debug
        logsDebug.put(LogTypesDebug.KOTTLoadFailed,
                "[KOTH Debug] Failed to load the KOTH mod!");
        logsDebug.put(LogTypesDebug.KOTTLoadSuccess,
                "[KOTH Debug] KOTH mod successfuly loaded!");

        //normal
        logs.put(LogTypes.KOTHMatchJoin,
                "[KOTH] You have joined an in progress match!");
    }

    public static void printLogDebug(LogTypesDebug logTypeDebug) { printL(logsDebug.get(logTypeDebug)); }

    public static void printLog(PlayerRef playerRef, LogTypes logType) { print(playerRef, logs.get(logType)); }
}
