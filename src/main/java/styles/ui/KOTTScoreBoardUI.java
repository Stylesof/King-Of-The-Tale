package styles.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

import static styles.util.PrintMacros.print;

public class KOTTScoreBoardUI extends InteractiveCustomUIPage<KOTTScoreBoardUI.Data> {

    private final PlayerRef playerRef;
    private final World world;

    public KOTTScoreBoardUI(@Nonnull PlayerRef playerRef, @Nonnull World world) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
        this.world = world;
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        KOTTMatch match = KOTTMatch.getMatch(world.getName());
        if (match != null) {
            uiCommandBuilder.append("KOTT/KOTTScoreBoard.ui");

            for (PlayerRef _playerRef : match.getPlayersInMatch().values()) {
                uiCommandBuilder.append("#Players", "KOTT/scoreboard/PlayerScores.ui");
            }
        } else {
            print(playerRef, "There isn't a match happening right now!");
        }
    }

    public static class Data {
        public static BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .build();
    }
}
