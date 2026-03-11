package styles.world;

import com.hypixel.hytale.math.vector.Vector3i;
import styles.team.KOTTTeam;

public class KOTTTeamZone extends KOTTZone {

    private final KOTTTeam owner;

    public KOTTTeamZone(Vector3i zonePosition, KOTTTeam owner) {
        super(zonePosition);
        this.owner = owner;
    }

    public KOTTTeam getOwner() { return owner; }
}
