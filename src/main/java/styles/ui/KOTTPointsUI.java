package styles.ui;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.team.KOTTTeam;
import styles.util.ColorHandler;
import styles.util.MathHelper;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

public class KOTTPointsUI extends CustomUIHud {

    private AtomicLong start = new AtomicLong(System.currentTimeMillis()), end = new AtomicLong();
    private ExecutorService updateThread;

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

        uiCommandBuilder.set("#InZoneIcon.Angle", angle);
    }

    public void initializeThreadUpdate() {
        if (this.updateThread == null) {
            this.updateThread = Executors.newFixedThreadPool(1);
            updateThread.submit(() -> { // Code inspired by from AdminUI of Buuz135
                while (!Thread.currentThread().isInterrupted()) {
                    this.end.set(System.currentTimeMillis());

                    if (!match.getKOTTMatchStatus() || match.getIsEnding() || playerRef.getReference() == null || match.getZone() == null) {
                        this.updateThread.shutdown();
                        return;
                    }

                    if (end.get() - start.get() >= 20) { // 20 ms delay
                        if (!start.compareAndSet(start.get(), end.get())) return;

                        match.getMatchWorld().execute(() -> {
                            Player player = match.getMatchWorld().getEntityStore().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
                            if (player != null) {
                                player.getHudManager().setCustomHud(playerRef, new KOTTPointsUI(playerRef, match));
                            }
                        });
                    }
                }
            });
        }
    }
}


