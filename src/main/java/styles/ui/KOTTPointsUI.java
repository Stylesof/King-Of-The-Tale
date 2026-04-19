package styles.ui;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.player.component.KOTTMoney;
import styles.team.KOTTTeam;
import styles.util.ColorHandler;
import styles.util.MathHelper;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

public class KOTTPointsUI extends CustomUIHud {

    private final PlayerRef playerRef;
    private final KOTTMatch match;
    private boolean isInZone;
    private boolean isInTeamZone;

    public KOTTPointsUI(@Nonnull PlayerRef playerRef, @Nonnull KOTTMatch match) {
        super(playerRef);
        this.playerRef = playerRef;
        this.match = match;
        //this.isInZone = inZone;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("KOTT/KOTTPointsUI.ui");

        this.isInZone = false;
        this.isInTeamZone = false;

        int i = 1;
        for (KOTTTeam team : match.getTeams()) {
            uiCommandBuilder.set("#Team" + i + ".Visible", true);
            String colorHex = ColorHandler.getHexFromColor(ColorHandler.getColorType(team.getBaseZone().getZoneMarker().getColorTint()));
            uiCommandBuilder.set("#Team" + i + ".Background.Color", colorHex);
            uiCommandBuilder.set("#Team" + i + "Label.Text", "" + team.teamPoints);

            if (colorHex.equals("#ffffff") || colorHex.equals("#ffff00")) {
                uiCommandBuilder.set("#Team" + i + "Label.Style.TextColor", "#000000");
            }

            if (team.getBaseZone().getPlayersInZone().contains(playerRef)) {
                this.isInTeamZone = true;
            }

            i++;
        }

        if (match.getZone() == null) return;

        this.isInZone = match.getZone().getPlayersInZone().contains(playerRef);

        if (this.isInTeamZone) {
            uiCommandBuilder.set("#InZoneLabel.Style.TextColor", "#ffff00");
            uiCommandBuilder.set("#InZoneLabel.Text", "You are in your Base");
        } else if (isInZone) {
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

        if (Double.isNaN(angle)) {
            angle = 0.0d;
        }

        uiCommandBuilder.set("#InZoneIcon.Angle", angle);

        if (KOTTMoney.getPlayerMoneyMap().containsKey(playerRef.getUsername())) {
        uiCommandBuilder.set("#PlayerMoneyLabel.Text", "$" + KOTTMoney.getPlayerMoneyMap().get(playerRef.getUsername()) + ".00");
        }
    }

    public static void loadHud(@Nonnull PlayerRef playerRef, @Nonnull KOTTMatch match) {
        match.getMatchWorld().execute(() -> {
            if (playerRef.getWorldUuid() == null || playerRef.getReference() == null) return;

            World world = Universe.get().getWorld(playerRef.getWorldUuid());
            if (world == null) {
                printLog("ERROR: Failed to execute loadHud(), invalid playerRef World!");
                return;
            }

            Player playerComp = world.getEntityStore().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
            if (playerComp == null) {
                printLog("Invalid PlayerComponent!");
                return;
            }

            KOTTPointsUI newHud = new KOTTPointsUI(playerRef, match);
            playerComp.getHudManager().setCustomHud(playerRef, newHud);
        });
    }

    public static void unloadHud(@Nonnull PlayerRef playerRef, @Nonnull World world) {
        world.execute(() -> {
            Player playerComp = world.getEntityStore().getStore().getComponent(Objects.requireNonNull(playerRef.getReference()), Player.getComponentType());
            if (playerComp == null) {
                printLog("Invalid PlayerComponent!");
                return;
            }

            playerComp.getHudManager().resetHud(playerRef);
        });
    }

    public static void unloadHud(@Nonnull PlayerRef playerRef, @Nonnull Holder<EntityStore> holder) {
        Player playerComp = holder.getComponent(Player.getComponentType());

        assert playerComp != null;
        playerComp.getHudManager().resetHud(playerRef);
    }
}


