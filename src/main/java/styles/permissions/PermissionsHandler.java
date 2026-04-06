package styles.permissions;

import java.util.HashMap;
import java.util.Map;

public class PermissionsHandler {
    public enum PermissionType {
        KOTT,
        KOTT_START,
        KOTT_STOP,
        KOTT_END,
        KOTT_JOIN,
        KOTT_GUI,
        KOTT_CLEAR_MARKERS,

        KOTT_TEAM,

        KOTT_MATCH
    }

    public final static Map<PermissionType, String> Permissions = new HashMap<>();
    static {
        Permissions.put(PermissionType.KOTT, "kott.command");
        Permissions.put(PermissionType.KOTT_START, "kott.command.start");
        Permissions.put(PermissionType.KOTT_STOP, "kott.command.stop");
        Permissions.put(PermissionType.KOTT_END, "kott.command.end");
        Permissions.put(PermissionType.KOTT_JOIN, "kott.command.join");
        Permissions.put(PermissionType.KOTT_GUI, "kott.command.gui");
        Permissions.put(PermissionType.KOTT_CLEAR_MARKERS, "kott.command.cm");

        Permissions.put(PermissionType.KOTT_TEAM, "kott.command.team");

        Permissions.put(PermissionType.KOTT_MATCH, "kott.command.match");
    }
}
