package styles.world.zone;

import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapMarkersHandler {

    public static Map<MarkerType, String> markers = new HashMap<>();

    public enum MarkerType {
        BASE,
        ATTACK
    }

    static {
        markers.put(MarkerType.BASE, "UserD.png");
        markers.put(MarkerType.ATTACK, "UserF.png");
    }


    public static UserMapMarker createMarker(@Nonnull String name, @Nonnull Color color, @Nonnull MarkerType type, @Nonnull World world, Vector3f pos) {
        return createMarker(UUID.randomUUID().toString(), name, color, type, world, pos);
    }

    public static UserMapMarker createMarker(@Nonnull String id, @Nonnull String name, @Nonnull Color color, @Nonnull MarkerType type, @Nonnull World world, Vector3f pos) {
        UserMapMarker zoneMarker = new UserMapMarker();
            zoneMarker.setId(id);
            zoneMarker.setPosition(pos.x, pos.z);
            zoneMarker.setName(name);
            zoneMarker.setIcon(markers.get(type));
            zoneMarker.setColorTint(color);

        UserMapMarkersStore userMapMarkerStore = world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());
        userMapMarkerStore.addUserMapMarker(zoneMarker);

        return zoneMarker;
    }
}
