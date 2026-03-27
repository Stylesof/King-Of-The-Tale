package styles.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

public class KOTTPointsUI extends CustomUIHud {

    private final KOTTMatch match;

    public KOTTPointsUI(@Nonnull PlayerRef playerRef, @Nonnull KOTTMatch match) {
        super(playerRef);

        this.match = match;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("KOTT/KOTTPointsUI.ui");

        for (int i = 1; i <= match.getTeams().size(); i++) {
            uiCommandBuilder.set("#Team" + i + ".Visible", true);
            uiCommandBuilder.set("#Team" + i + ".Background.Color", "#ffffff");
            uiCommandBuilder.set("#Team" + i + "Label.Text", "0");
        }
    }
}


