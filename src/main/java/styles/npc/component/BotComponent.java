package styles.npc.component;

import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.KOTT;

import javax.annotation.Nullable;
import java.util.List;

public class BotComponent implements Component<EntityStore> {

    private final TransientPath paths = new TransientPath();
    private boolean pathState = false;

    private final List<Vector3d> points;

    public static final BuilderCodec<BotComponent> CODEC = BuilderCodec.builder(BotComponent.class, BotComponent::new).build();

    public BotComponent() {
        this(null);
    }

    public BotComponent(@Nullable List<Vector3d> points) {
        this.points = points;

        if (points != null) {
            for (Vector3d pos : points) {
                paths.addWaypoint(pos, new Vector3f(0.0f, 0.0f, 0.0f));
            }
        }
    }

    public void setPathState(boolean state) { this.pathState = state; }

    public boolean getPathState() { return this.pathState; }

    public TransientPath getPaths() { return this.paths; }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new BotComponent(null);
    }

    public static ComponentType<EntityStore, BotComponent> getComponentType() { return KOTT.getInstance().botComponentType; }
}
