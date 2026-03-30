package styles.ui;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.team.KOTTTeam;
import styles.util.ColorHandler;
import styles.util.MathHelper;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.Objects;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

public class KOTTPointsUI extends CustomUIHud {

    private final PlayerRef playerRef;
    private final KOTTMatch match;
    public boolean isInZone;

    public KOTTPointsUI(@Nonnull PlayerRef playerRef, @Nonnull KOTTMatch match, boolean inZone) {
        super(playerRef);
        this.playerRef = playerRef;
        this.match = match;
        this.isInZone = inZone;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("KOTT/KOTTPointsUI.ui");

        int i = 1;
        for (KOTTTeam team : match.getTeams()) {
            uiCommandBuilder.set("#Team" + i + ".Visible", true);
            String colorHex = ColorHandler.getHexFromColor(Objects.requireNonNull(ColorHandler.getColorType(team.getBaseZone().getZoneMarker().getColorTint())));
            uiCommandBuilder.set("#Team" + i + ".Background.Color", colorHex);
            uiCommandBuilder.set("#Team" + i + "Label.Text", "" + team.teamPoints);

            if (colorHex.equals("#ffffff") || colorHex.equals("#fff000")) {
                uiCommandBuilder.set("#Team" + i + "Label.Style.TextColor", "#000000");
            }

            i++;
        }

        if (isInZone) {
            uiCommandBuilder.set("#InZoneLabel.Style.TextColor", "#00ff00");
            uiCommandBuilder.set("#InZoneLabel.Text", "You are inside the Zone");
        }

        Vector3d matchZone = match.getZone().getPosition().toVector3d();
        Vector3d playerPos = playerRef.getTransform().getPosition();

        double playerZoneDist = MathHelper.positionDistance(matchZone, playerPos);
        Vector3d newCenter = new Vector3d(matchZone.x - playerPos.x, 0, matchZone.z - playerPos.z);

        Vector3d unitVec = new Vector3d(newCenter.x / playerZoneDist, 0, newCenter.z / playerZoneDist);
        if (unitVec.x > 1 || unitVec.x < -1) {
            unitVec.x = 0;
        }
        if (unitVec.z > 1 || unitVec.z < -1) {
            unitVec.z = 0;
        }

        Vector3d headRotRad = playerRef.getHeadRotation().toVector3d();
        double angleRot = Math.toDegrees(-headRotRad.y);
        if (angleRot < 0) angleRot = -angleRot;
        else angleRot = -angleRot + 360;

        double angle = Math.toDegrees(Math.atan2(unitVec.z, -unitVec.x));
        if (angle < 0) angle = -angle;
        else angle = -angle + 360;

        angle -= 90.0f; // Sprite rotation adjustment
        angle += angleRot;

        uiCommandBuilder.set("#InZoneIcon.Angle", angle);
    }
}


