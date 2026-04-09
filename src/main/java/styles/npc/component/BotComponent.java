package styles.npc.component;

import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.KOTT;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BotComponent implements Component<EntityStore> {

    public static final BuilderCodec<BotComponent> CODEC = BuilderCodec.builder(BotComponent.class, BotComponent::new).build();
    private final KOTTMatch match;
    private boolean pathState = false;

    public BotComponent() { this(null);}

    public BotComponent(KOTTMatch match) {
        this.match = match;
    }

    public void setPathState(boolean state) { this.pathState = state; }

    public boolean getPathState() { return this.pathState; }

    public KOTTMatch getMatch() { return this.match; }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new BotComponent(this.match);
    }

    public static ComponentType<EntityStore, BotComponent> getComponentType() { return KOTT.getInstance().botComponentType; }
}
