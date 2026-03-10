package styles.world;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import styles.team.KOTHTeam;

public class KOTHTeamZone extends KOTHZone {

    private final KOTHTeam ownerTeam;

    public KOTHTeamZone(Vector3i zonePosition, KOTHTeam owner) {
        super(zonePosition);
        this.ownerTeam = owner;
    }



    public KOTHTeam getOwner() { return ownerTeam; }
}
