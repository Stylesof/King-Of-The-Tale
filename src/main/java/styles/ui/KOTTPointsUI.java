package styles.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import styles.team.KOTTTeam;
import styles.util.ColorHandler;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static styles.util.PrintMacros.printL;

public class KOTTPointsUI extends CustomUIHud {

    private final KOTTMatch match;
    public boolean isInZone;

    public KOTTPointsUI(@Nonnull PlayerRef playerRef, @Nonnull KOTTMatch match, boolean inZone) {
        super(playerRef);
        this.match = match;
        this.isInZone = inZone;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("KOTT/KOTTPointsUI.ui");
        setDefaults(uiCommandBuilder);
    }

    private void setDefaults(UICommandBuilder uiCommandBuilder) {
        int i = 1;
        for (KOTTTeam team : match.getTeams().values()) {
            uiCommandBuilder.set("#Team" + i + ".Visible", true);
            String colorHex =  ColorHandler.getHexFromColor(Objects.requireNonNull(ColorHandler.getColorType(team.getBaseZone().getZoneMarker().getColorTint())));
            uiCommandBuilder.set("#Team" + i + ".Background.Color", colorHex);
            uiCommandBuilder.set("#Team" + i + "Label.Text", "" + team.teamPoints);

            if (colorHex.equals("#ffffff") || colorHex.equals("#fff000")) {
                uiCommandBuilder.set("#Team" + i + "Label.Style.TextColor", "#000000");
            }

            i++;
        }

        if (isInZone) {
            uiCommandBuilder.set("#InZoneIcon.Color", "#00ff00");
            uiCommandBuilder.set("#InZoneLabel.Style.TextColor", "#00ff00");
            uiCommandBuilder.set("#InZoneLabel.Text", "You are inside the Zone");
        }
    }
}


