package styles.world;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.flock.FlockMembershipSystems;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.ArrayList;
import java.util.List;

import static styles.util.Utils.print;

public class KOTHZone {

    private final Vector3i zonePosition;
    private final List<NPCEntity> npcInZone = new ArrayList<>();
    private final List<PlayerRef> playerInZone = new ArrayList<>();

    public static final int zoneRadius = 100;

    public KOTHZone(Vector3i zonePosition) { this.zonePosition = zonePosition; }

    public Vector3i getPosition() { return this.zonePosition; }

    public void tick() {


    }
}
