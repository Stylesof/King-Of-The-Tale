package styles.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.team.KOTTTeam;

import javax.annotation.Nonnull;

public class KOTTEndUI extends CustomUIHud {

    private final PlayerRef playerRef;
    private KOTTTeam winnerTeam;
    private final int timeToNextMatch;
    private final boolean isLoop;

    public KOTTEndUI(@Nonnull PlayerRef playerRef, KOTTTeam team, int timeToNextMatch, boolean isLoop) {
        super(playerRef);
        this.playerRef = playerRef;
        this.winnerTeam = team;
        this.timeToNextMatch = timeToNextMatch;
        this.isLoop = isLoop;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("KOTT/KOTTEndUI.ui");

        if (winnerTeam != null) {
            if (!winnerTeam.containsPlayer(playerRef)) {
                uiCommandBuilder.set("#WinLabel.Text", "Game Over!");
                uiCommandBuilder.set("#WinLabel.Style.TextColor", "#ff0000");
            }

            uiCommandBuilder.set("#TeamWinLabel.Text", winnerTeam.getDisplayName());
        } else {
            uiCommandBuilder.set("#WinLabel.Text", "Match Stopped!");
            uiCommandBuilder.set("#WinLabel.Style.TextColor", "#ff0000");

            uiCommandBuilder.set("#TeamWinLabel.Text", "");
        }

        if (isLoop) {
            uiCommandBuilder.set("#NextMatchLabel.Text", "Wait " + timeToNextMatch + " seconds to start the next match...");
        } else {
            uiCommandBuilder.set("#NextMatchLabel.Text", "Wait " + timeToNextMatch + " seconds to end the match...");
        }
    }
}
