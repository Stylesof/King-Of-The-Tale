package styles;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import styles.commands.KOTTCommand;
import styles.config.KOTTConfig;
import styles.events.ECS_DamageEvent;
import styles.events.ECS_OnDamageBlockEvent;
import styles.events.OnPlayerConnectEvent;
import styles.events.OnPlayerDisconnectEvent;
import styles.player.KOTTMoney;
import styles.tick.KOTTMatchPointTickHandler;
import styles.tick.KOTTMatchZoneCompassTickHandler;
import styles.util.log.LogTypesDebug;
import styles.world.KOTTMatch;
import styles.tick.EntityTickHandler;

import javax.annotation.Nonnull;

import static styles.util.PrintMacros.printL;
import static styles.util.log.PrintLog.printLogDebug;

public class KOTT extends JavaPlugin {

    private static KOTT instance;

    public ComponentType<EntityStore, KOTTMoney> kottMoneyComponent;
    public final Config<KOTTConfig> kottConfigRef = this.withConfig("KOTTConfig", KOTTConfig.CODEC);

    public KOTT(@Nonnull JavaPluginInit init) {
        super(init);
        printLogDebug(LogTypesDebug.KOTTLoadSuccess);
        instance = this;
    }

    @Override
    protected void setup() {
        kottConfigRef.load().thenCompose((var) -> kottConfigRef.save());

        this.getCommandRegistry().registerCommand(new KOTTCommand());

        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, OnPlayerConnectEvent::onPlayerConnect);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, OnPlayerDisconnectEvent::onPlayerDisconnect);

        this.getEntityStoreRegistry().registerSystem(new EntityTickHandler());
        this.getEntityStoreRegistry().registerSystem(new KOTTMatchPointTickHandler());
        this.getEntityStoreRegistry().registerSystem(new KOTTMatchZoneCompassTickHandler());

        this.getEntityStoreRegistry().registerSystem(new ECS_OnDamageBlockEvent());
        this.getEntityStoreRegistry().registerSystem(new ECS_DamageEvent());

        this.kottMoneyComponent = this.getEntityStoreRegistry().registerComponent(
                KOTTMoney.class,
                "KOTTMoneyComponent",
                KOTTMoney.CODEC
        );

    }

    @Override
    protected void start() {
        printL("[KOTT] KOTT Mod Started!");
    }

    @Override
    protected void shutdown() {
        KOTTMatch.getMatchesList().clear();

        super.shutdown();
    }

    public static KOTT getInstance() { return instance; }
}