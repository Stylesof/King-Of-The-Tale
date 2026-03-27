package styles.world;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import styles.team.KOTTTeam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KOTTTeamZone extends KOTTZone {

    private final KOTTTeam owner;

    public KOTTTeamZone(int zoneRadius, @Nonnull Vector3i zonePosition, @Nonnull World world, KOTTTeam owner, @Nonnull UserMapMarker zoneMarker) {
        super(zoneRadius, zonePosition, world, zoneMarker);
        this.owner = owner;
    }

    public KOTTTeam getOwner() { return this.owner; }

}
