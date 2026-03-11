package styles.world;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import styles.KOTH;
import styles.team.KOTHTeam;

public class KOTHTeamZone extends KOTHZone {

    private final KOTHTeam owner;

    public KOTHTeamZone(Vector3i zonePosition, KOTHTeam owner) {
        super(zonePosition);
        this.owner = owner;
    }

    public KOTHTeam getOwner() { return owner; }
}
