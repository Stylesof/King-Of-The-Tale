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

    public KOTTPointsUI(@Nonnull PlayerRef playerRef, @Nonnull KOTTMatch match) {
        super(playerRef);
        this.match = match;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        setDefaults(uiCommandBuilder);
    }

    @Override
    public void update(boolean clear, @Nonnull UICommandBuilder uiCommandBuilder) {
        setDefaults(uiCommandBuilder);
        super.update(clear, uiCommandBuilder);
    }

    private void setDefaults(UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("KOTT/KOTTPointsUI.ui");

        int i = 1;
        printL("Trying add, match team list: " + match.getTeams().size());
        for (KOTTTeam team : match.getTeams().values()) {
            uiCommandBuilder.set("#Team" + i + ".Visible", true);
            String colorHex =  ColorHandler.getHexFromColor(Objects.requireNonNull(ColorHandler.getColorType(team.getBaseZone().getZoneMarker().getColorTint())));
            uiCommandBuilder.set("#Team" + i + ".Background.Color", colorHex);
            uiCommandBuilder.set("#Team" + i + "Label.Text", "0");

            if (colorHex.equals("#ffffff")) {
                uiCommandBuilder.set("#Team" + i + "Label.Style.TextColor", "#000000");
            }

            i++;
        }
    }
}


