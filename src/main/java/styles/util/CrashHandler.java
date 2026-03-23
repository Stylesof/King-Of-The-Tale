package styles.util;

import com.hypixel.hytale.server.worldgen.zone.Zone;
import styles.world.KOTTMatch;
import styles.world.KOTTTeamZone;
import styles.world.KOTTZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrashHandler {

    private static Map<KOTTMatch, KOTTZone> CrashMatchHandler;

    public static void setValues(List<KOTTMatch> matches, List<KOTTZone> zones, List<KOTTTeamZone> teamZones) {
        CrashMatchHandler = new HashMap<>();
        for (KOTTMatch match : matches) {
            for (KOTTZone _zone : zones) {
                CrashMatchHandler.put(match, _zone);
            }
            for (KOTTTeamZone _teamZone : teamZones) {
                CrashMatchHandler.put(match, _teamZone);
            }
        }
    }

    public static Map<KOTTMatch, KOTTZone> getCrashMatchHandler() { return CrashMatchHandler; }
}
