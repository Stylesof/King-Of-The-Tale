package styles.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

import static styles.util.MessageHandler.printChat;

public class KOTTScoreBoardUI extends CustomUIPage {

    private final PlayerRef playerRef;
    private final KOTTMatch match;

    public KOTTScoreBoardUI(@Nonnull PlayerRef playerRef, @Nonnull KOTTMatch match) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.playerRef = playerRef;
        this.match = match;
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        uiCommandBuilder.append("KOTT/KOTTScoreBoard.ui");

        int i = 0;
        for (PlayerRef _playerRef : match.getPlayersInMatch().values()) {
            if (match.getPlayerTeam(_playerRef) == null) continue;

            Color teamColor = match.getPlayerTeam(_playerRef).getTeamColor();
            String playerName = _playerRef.getUsername();
            int kills = match.getScoreBoard().getPlayersKillCount().get(_playerRef);
            int deaths = match.getScoreBoard().getPlayersDeathCount().get(_playerRef);

            uiCommandBuilder.append("#Content #List", "KOTT/scoreboard/PlayerScores.ui");
            uiCommandBuilder.set("#Content #List[" +  i + "] #TeamColor.Background", teamColor.toString());
            uiCommandBuilder.set("#Content #List[" + i + "] #PlayerName #NameLabel.Text", playerName);
            uiCommandBuilder.set("#Content #List[" + i + "] #Deaths.Background", "#00FF00");
            uiCommandBuilder.set("#Content #List[" + i + "] #TeamColor.Background", "#00FF00");
        }
    }

    public static class Data {
        public static BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .build();
    }
}
