package styles.permissions;

import java.util.HashMap;
import java.util.Map;

public class PermissionsHandler {
    public enum Permission {
        KOTT,
        KOTT_START,
        KOTT_STOP,
        KOTT_END,
        KOTT_JOIN,
        KOTT_CLEAR_MARKERS,
        KOTT_TEAM_ALL,
        KOTT_MATCH_ALL
    }

    public final static Map<Permission, String> Permissions = new HashMap<>();
    static {
        Permissions.put(Permission.KOTT, "kott.command");
        Permissions.put(Permission.KOTT_START, "kott.command.start");
        Permissions.put(Permission.KOTT_STOP, "kott.command.stop");
        Permissions.put(Permission.KOTT_END, "kott.command.end");
        Permissions.put(Permission.KOTT_JOIN, "kott.command.join");
        Permissions.put(Permission.KOTT_CLEAR_MARKERS, "kott.command.cm");
        Permissions.put(Permission.KOTT_TEAM_ALL, "kott.command.team.all");
        Permissions.put(Permission.KOTT_MATCH_ALL, "kott.command.match.all");
    }
}
