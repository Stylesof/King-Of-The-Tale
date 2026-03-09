package styles.world;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.ArrayList;
import java.util.Collection;

public class KOTHZone {

    private final Vector3i zonePosition;
    private final Collection<PlayerRef> playerInZone = new ArrayList<>();

    public KOTHZone(Vector3i zonePosition) {
        this.zonePosition = zonePosition;
    }

    public void Tick() {



    }

    public Vector3i getPosition() { return this.zonePosition; }

}
