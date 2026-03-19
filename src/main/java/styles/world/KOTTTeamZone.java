package styles.world;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import styles.team.KOTTTeam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KOTTTeamZone extends KOTTZone {

    private final KOTTTeam owner;

    public KOTTTeamZone(@Nonnull int zoneRadius, @Nonnull Vector3i zonePosition, @Nonnull World world, KOTTTeam owner, @Nullable UserMapMarker zoneMarker) {
        super(zoneRadius, zonePosition, world, zoneMarker);
        this.owner = owner;
    }

    public KOTTTeam getOwner() { return this.owner; }

}
