package styles.world;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import styles.team.KOTTTeam;

import javax.annotation.Nullable;

public class KOTTTeamZone extends KOTTZone {

    private final KOTTTeam owner;

    public KOTTTeamZone(Vector3i zonePosition, KOTTTeam owner, @Nullable UserMapMarker zoneMarker) {
        super(zonePosition, zoneMarker);
        this.owner = owner;
    }

    public KOTTTeam getOwner() { return this.owner; }

}
