package styles.world;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.util.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class KOTHZone {

    private final Vector3i zonePosition;
    private final List<NPCEntity> npcsInZone = new ArrayList<>();
    private final List<PlayerRef> playersInZone = new ArrayList<>();

    public static final int zoneRadius = 100;

    public KOTHZone(Vector3i zonePosition) { this.zonePosition = zonePosition; }

    public List<PlayerRef> getPlayersInZone() { return playersInZone; }

    public List<NPCEntity> getNpcsInZone() { return npcsInZone; }

    public Vector3i getPosition() { return this.zonePosition; }

    public Boolean hasPlayer(PlayerRef player) { return playersInZone.contains(player); }

    public Boolean hasNPC(NPCEntity npc) { return npcsInZone.contains(npc); }

    public Boolean isInside(Vector3d position) {

        // just need to verify if the distance from the center
        // is less or equal to the zoneRadius

        double distance = MathHelper.positionDistance(zonePosition.toVector3d(), position);
        distance = Math.ceil(distance);

        return distance <= zoneRadius;
    }
}
