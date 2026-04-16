package styles.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.concurrent.CompletableFuture;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

public class KOTTStartUI extends InteractiveCustomUIPage<KOTTStartUI.Data> {

    private final PlayerRef playerRef;
    private final World world;

    private int teamCount = 1;
    private int zoneRadius = 100;
    private String worldName;
    private Vector3i worldPos;
    private boolean safe = false;
    private boolean loop = false;

    private boolean updateWorldName = true;

    private String errorMessage = "";

    public KOTTStartUI(@Nonnull PlayerRef playerRef, @Nonnull World world) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
        this.world = world;
        this.worldName = world.getName();
        this.worldPos = playerRef.getTransform().getPosition().toVector3i();
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("KOTT/KOTTStartUI.ui");

        setDefaults(uiCommandBuilder);

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TeamCount", EventData.of("@TeamCount", "#TeamCount.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ZoneRadius", EventData.of("@ZoneRadius", "#ZoneRadius.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#WorldName", EventData.of("@WorldName", "#WorldName.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#UseWorld", EventData.of("ClickedButton", "UseWorld"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#WorldPosX", EventData.of("@WorldPosX", "#WorldPosX.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#WorldPosY", EventData.of("@WorldPosY", "#WorldPosY.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#WorldPosZ", EventData.of("@WorldPosZ", "#WorldPosZ.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#UsePos", EventData.of("ClickedButton", "UsePos"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Safe #CheckBox", EventData.of("@Safe", "#Safe #CheckBox.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Loop #CheckBox", EventData.of("@Loop", "#Loop #CheckBox.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Start", EventData.of("ClickedButton", "Start"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);

        this.errorMessage = "";
        switch (data.key) {
            case "TeamCount":
                this.teamCount = Integer.parseInt(data.value);
                break;
            case "AreaSize":
                this.zoneRadius = Integer.parseInt(data.value);
                break;
            case "WorldName":
                this.worldName = data.value;
                break;
            case "UseWorld":
                this.worldName = this.world.getName();
                this.updateWorldName = true;
                break;
            case "WorldPosX":
                this.worldPos.x = Integer.parseInt(data.value);
                break;
            case "WorldPosY":
                this.worldPos.y = Integer.parseInt(data.value);
                break;
            case "WorldPosZ":
                this.worldPos.z = Integer.parseInt(data.value);
                break;
            case "UsePos":
                this.worldPos = playerRef.getTransform().getPosition().toVector3i();
                break;
            case "Safe":
                //this.safe = data.value.equals("true");
                break;
            case "Loop":
                this.loop = data.value.equals("true");
                break;
            case "Start":
                World _world = Universe.get().getWorld(this.worldName);
                if (_world == null && !this.safe) {
                    this.errorMessage = "Invalid World name!";
                    break;
                }

                if (this.safe) _world = world;

                World final_world = _world;
                CompletableFuture.runAsync(() -> {
                    KOTTMatch.tryCreateMatch(
                            this.worldPos,
                            this.teamCount,
                            this.zoneRadius,
                            this.safe,
                            this.loop,
                            this.playerRef,
                            null,
                            final_world,
                            world,
                            playerRef.getTransform().getPosition().toVector3i()
                    );
                });

                close();
            break;
        }

        sendUpdate(new UICommandBuilder());
    }

    @Override
    protected void sendUpdate(@Nullable UICommandBuilder uiCommandBuilder) {
        super.sendUpdate(uiCommandBuilder);

        if (uiCommandBuilder != null) {
            setDefaults(uiCommandBuilder);
        }
    }

    private void setDefaults (@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.set("#TeamCount.Value", this.teamCount);
        uiCommandBuilder.set("#ZoneRadius.Value", this.zoneRadius);
        if (this.updateWorldName) {
            uiCommandBuilder.set("#WorldName.Value", this.worldName);
            this.updateWorldName = false;
        }
        if (this.playerRef != null) {
            uiCommandBuilder.set("#WorldPosX.Value", worldPos.x);
            uiCommandBuilder.set("#WorldPosY.Value", worldPos.y);
            uiCommandBuilder.set("#WorldPosZ.Value", worldPos.z);
        }
        uiCommandBuilder.set("#Safe #CheckBox.Value", this.safe);
        uiCommandBuilder.set("#Loop #CheckBox.Value", this.loop);
        uiCommandBuilder.set("#ErrorMessage.Text", this.errorMessage);
    }

    public void closePage() {
        close();
    }

    public static class Data {
        private String key;
        private String value;

        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                // TEAM COUNT
                .append(new KeyedCodec<>("@TeamCount", Codec.INTEGER), (data, value) -> {
                    data.value = value.toString();
                    data.key = "TeamCount";
                }, (data) -> Integer.parseInt(data.value))
                .add()

                // AREA SIZE
                .append(new KeyedCodec<>("@ZoneRadius", Codec.INTEGER), (data, value) -> {
                    data.value = value.toString();
                    data.key = "AreaSize";
                }, (data) -> Integer.parseInt(data.value))
                .add()

                // WORLD NAME
                .append(new KeyedCodec<>("@WorldName", Codec.STRING), (data, value) -> {
                    data.value = value;
                    data.key = "WorldName";
                }, (data) -> data.value)
                .add()

                // USE WORLD
                .append(new KeyedCodec<>("@UseWorld", Codec.BOOLEAN), (data, value) -> {
                    data.value = value.toString();
                    data.key = "UseWorld";
                }, (data) -> Boolean.getBoolean(data.value))
                .add()

                // WORLD POS X
                .append(new KeyedCodec<>("@WorldPosX", Codec.INTEGER), (data, value) -> {
                    data.value = value.toString();
                    data.key = "WorldPosX";
                }, (data) -> Integer.parseInt(data.value))
                .add()

                // WORLD POS Y
                .append(new KeyedCodec<>("@WorldPosY", Codec.INTEGER), (data, value) -> {
                    data.value = value.toString();
                    data.key = "WorldPosY";
                }, (data) -> Integer.parseInt(data.value))
                .add()

                // WORLD POS Z
                .append(new KeyedCodec<>("@WorldPosZ", Codec.INTEGER), (data, value) -> {
                    data.value = value.toString();
                    data.key = "WorldPosZ";
                }, (data) -> Integer.parseInt(data.value))
                .add()

                // USE YOUR POSITION
                .append(new KeyedCodec<>("ClickedButton", Codec.STRING),
                        (data, value) -> data.key = value,
                        (data) -> data.key)
                .add()

                // SAFE
                .append(new KeyedCodec<>("@Safe", Codec.BOOLEAN), (data, value) -> {
                    data.value = value.toString();
                    data.key = "Safe";
                }, (data) -> Boolean.getBoolean(data.value))
                .add()

                // LOOP
                .append(new KeyedCodec<>("@Loop", Codec.BOOLEAN), (data, value) -> {
                    data.value = value.toString();
                    data.key = "Loop";
                }, (data) -> Boolean.getBoolean(data.value))
                .add()

                .append(new KeyedCodec<>("@Start", Codec.BOOLEAN), (data, value) -> {
                    data.value = value.toString();
                    data.key = "Start";
                }, (data) -> Boolean.getBoolean(data.value))
                .add()

                .build();
    }
}
