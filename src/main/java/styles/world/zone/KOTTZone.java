package styles.world.zone;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.util.MathHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class KOTTZone {

    private final Vector3i zonePosition;
    public final List<NPCEntity> npcsInZone = new ArrayList<>();
    private final List<PlayerRef> playersInZone = new ArrayList<>();
    private final World world;
    private UserMapMarker zoneMarker;
    private final int zoneRadius;

    public KOTTZone(int zoneRadius, @Nonnull Vector3i zonePosition, @Nonnull World world) {
        this.zoneRadius = zoneRadius;
        this.zonePosition = zonePosition;
        this.zoneMarker = null;
        this.world = world;
    }

    public void createUserMapMarker(@Nonnull String name, @Nonnull Color color, @Nonnull MapMarkersHandler.MarkerType type) {
        this.zoneMarker = MapMarkersHandler.createMarker(name, color, type, world, zonePosition.toVector3f());
    }

    public void removeFromZone(@Nonnull NPCEntity npcEntity) {
        npcsInZone.remove(npcEntity);
    }

    public void removeFromZone(PlayerRef playerRef) {
        playersInZone.remove(playerRef);
    }

    public void addToZone(PlayerRef playerRef) {
        playersInZone.add(playerRef);
    }

    public Vector3i getPosition() { return new Vector3i(this.zonePosition); }

    public boolean isInside(@Nonnull Vector3d position) {

        // just need to verify if the distance from the center
        // is less or equal to the zoneRadius

        double distance = MathHelper.positionDistance(zonePosition.toVector3d(), position);
        distance = Math.ceil(distance);

        return distance <= zoneRadius;
    }

    public UserMapMarker getZoneMarker() { return this.zoneMarker; }

    public World getWorld() { return this.world; }

    public int getZoneRadius() { return this.zoneRadius; }

    public List<PlayerRef> getPlayersInZone() { return this.playersInZone; }
}
