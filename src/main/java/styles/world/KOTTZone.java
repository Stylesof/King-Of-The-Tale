package styles.world;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.util.MathHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class KOTTZone {

    private final Vector3i zonePosition;
    private final List<NPCEntity> npcsInZone = new ArrayList<>();
    private final List<PlayerRef> playersInZone = new ArrayList<>();

    public static final int zoneRadius = 100;

    public KOTTZone(@Nonnull Vector3i zonePosition) { this.zonePosition = zonePosition; }

    @Nullable
    public List<PlayerRef> getPlayersInZone() { return playersInZone; }

    @Nullable
    public List<NPCEntity> getNpcsInZone() { return npcsInZone; }

    public Vector3i getPosition() { return this.zonePosition; }

    public boolean hasPlayer(@Nonnull PlayerRef player) { return playersInZone.contains(player); }

    public boolean hasNPC(@Nonnull NPCEntity npc) { return npcsInZone.contains(npc); }

    public boolean isInside(@Nonnull Vector3d position) {

        // just need to verify if the distance from the center
        // is less or equal to the zoneRadius

        double distance = MathHelper.positionDistance(zonePosition.toVector3d(), position);
        distance = Math.ceil(distance);

        return distance <= zoneRadius;
    }
}
