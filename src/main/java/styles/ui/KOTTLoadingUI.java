package styles.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.HudComponent;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.protocol.packets.player.UpdateMovementSettings;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.Objects;

public class KOTTLoadingUI extends InteractiveCustomUIPage<KOTTLoadingUI.NullClass> {

    Player player;

    public KOTTLoadingUI(@NonNullDecl PlayerRef playerRef, Player player) {
        super(playerRef, CustomPageLifetime.CantClose, BuilderCodec.builder(NullClass.class, NullClass::new).build());
        this.player = player;
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        uiCommandBuilder.append("KOTT/KOTTLoading.ui");

        // this.player.getHudManager().hideHudComponents(getPlayerRef(), HudComponent.Chat);
        this.player.getHudManager().hideHudComponents(playerRef, HudComponent.VALUES);
        this.player.getHudManager().showHudComponents(playerRef, HudComponent.Chat);
    }

    public static void loadHud(@Nonnull PlayerRef playerRef) {
        assert playerRef.getWorldUuid() != null;
        World world = Universe.get().getWorld(playerRef.getWorldUuid());

        assert world != null;
        world.execute(() -> {
            Player _player = world.getEntityStore().getStore().getComponent(Objects.requireNonNull(playerRef.getReference()), Player.getComponentType());
            assert _player != null;
            _player.getPageManager().openCustomPage(playerRef.getReference(), world.getEntityStore().getStore(),  new KOTTLoadingUI(playerRef, _player));
        });
    }

    public static void unloadHud(@Nonnull PlayerRef playerRef) {
        assert playerRef.getWorldUuid() != null;
        World world = Universe.get().getWorld(playerRef.getWorldUuid());

        assert world != null;
        world.execute(() -> {
            if (playerRef.getReference() != null) {
                Player _player = world.getEntityStore().getStore().getComponent(playerRef.getReference(), Player.getComponentType());

                assert _player != null;
                _player.getPageManager().setPage(playerRef.getReference(), world.getEntityStore().getStore(), Page.None);
                _player.getHudManager().resetHud(playerRef);
            } else if (playerRef.getHolder() != null) {
                Player _player = playerRef.getHolder().getComponent(Player.getComponentType());

                assert _player != null;
                _player.getHudManager().resetHud(playerRef);
            }
        });
    }

    public static class NullClass {
    }
}