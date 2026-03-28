package styles.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.team.KOTTTeam;

import javax.annotation.Nonnull;

public class KOTTEndUI extends CustomUIHud {

    private final PlayerRef playerRef;
    private final KOTTTeam winnerTeam;
    private final int timeToNextMatch;

    public KOTTEndUI(@Nonnull PlayerRef playerRef, KOTTTeam team, int timeToNextMatch) {
        super(playerRef);
        this.playerRef = playerRef;
        this.winnerTeam = team;
        this.timeToNextMatch = timeToNextMatch;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("KOTT/KOTTEndUI.ui");

        if (!winnerTeam.containsPlayer(playerRef)) {
            uiCommandBuilder.set("#WinLabel.Text", "Game Over!");
            uiCommandBuilder.set("#WinLabel.Style.TextColor", "#ff0000");
        }

        uiCommandBuilder.set("#TeamWinLabel.Text", winnerTeam.getDisplayName());
        uiCommandBuilder.set("#NextMatchLabel.Text", "Wait " + timeToNextMatch + " seconds to start the next match...");
    }
}
