package styles.world.zone;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import styles.team.KOTTTeam;

import javax.annotation.Nonnull;

public class KOTTTeamZone extends KOTTZone {

    private final KOTTTeam owner;
    public final static int baseRadius = 100;

    public KOTTTeamZone(int zoneRadius, @Nonnull Vector3i zonePosition, @Nonnull World world, KOTTTeam owner) {
        super(zoneRadius, zonePosition, world);
        this.owner = owner;
    }

    public KOTTTeam getOwner() { return this.owner; }

}
