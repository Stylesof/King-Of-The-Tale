package styles.world;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembershipSystems;

import javax.management.Query;
import java.util.ArrayList;
import java.util.List;

import static styles.utils.Utils.print;

public class KOTHZone {

    private final Vector3i zonePosition;
    private final List<FlockMembershipSystems.EntityRef> entityInZone = new ArrayList<>();

    public static final int zoneRadius = 100;

    public KOTHZone(Vector3i zonePosition) { this.zonePosition = zonePosition; }

    public void Tick(World world) {

        

    }

    public Vector3i getPosition() { return this.zonePosition; }

}
