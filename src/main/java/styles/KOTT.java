package styles;

import com.hypixel.hytale.builtin.adventure.memories.page.MemoriesUnlockedPageSuplier;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.RemovedPlayerFromWorldEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import styles.commands.KOTTCommand;
import styles.events.ECS_OnDamageBlockEvent;
import styles.events.OnPlayerConnectEvent;
import styles.events.OnPlayerDisconnectEvent;
import styles.events.OnPlayerRemovedFromWorldEvent;
import styles.player.component.InvulnerabilityComponent;
import styles.player.component.KOTTMoney;
import styles.thread.ThreadSafetyProvider;
import styles.tick.KOTTMatchTickHandler;
import styles.tick.system.player.DeathSystem;
import styles.tick.system.player.InvulnerabilitySystem;
import styles.tick.system.ProjectileDetectionSystem;
import styles.ui.interaction.ATMBlockInteractionPage;
import styles.ui.interaction.ATMBlockInteractionPageSuplier;
import styles.util.log.LogTypesDebug;
import styles.world.KOTTMatch;
import styles.tick.PlayerTickHandler;

import javax.annotation.Nonnull;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

public class KOTT extends JavaPlugin {

    private static KOTT instance;

    public ComponentType<EntityStore, KOTTMoney> kottMoneyComponentType;
    public ComponentType<EntityStore, InvulnerabilityComponent> invulnerabilityComponentType;

    //private PacketFilter inboundFilter;

    public final Config<KOTTMoney.KOTTMoneySaveFile> kottMoneySaveFile = this.withConfig("KOTTPlayerMoneyList", KOTTMoney.KOTTMoneySaveFile.CODEC);

    public KOTT(@Nonnull JavaPluginInit init) {
        super(init);
        printLog(LogTypesDebug.KOTTLoadSuccess);
        instance = this;
    }

    public static KOTT getInstance() { return instance; }

    @Override
    protected void setup() {
        kottMoneySaveFile.load().thenCompose((var) -> kottMoneySaveFile.save());

        this.getCommandRegistry().registerCommand(new KOTTCommand());

        this.kottMoneyComponentType = this.getEntityStoreRegistry().registerComponent(
                KOTTMoney.class,
                "KOTTMoneyComponent",
                KOTTMoney.CODEC
        );

        this.invulnerabilityComponentType = this.getEntityStoreRegistry().registerComponent(
                InvulnerabilityComponent.class,
                "InvulnerabilityComponent",
                InvulnerabilityComponent.CODEC
        );

        // printLog(KOTTStartUI.Data.CODEC.toString());

        OpenCustomUIInteraction.registerCustomPageSupplier(this, ATMBlockInteractionPage.class, "ATMBlockInteraction", new ATMBlockInteractionPageSuplier());

        // Event Handler
        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, OnPlayerConnectEvent::onPlayerConnect);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, OnPlayerDisconnectEvent::onPlayerDisconnect);
        this.getEventRegistry().registerGlobal(RemovedPlayerFromWorldEvent.class, OnPlayerRemovedFromWorldEvent::onPlayerRemovedFromWorld);

        // ECS Event Handler
        this.getEntityStoreRegistry().registerSystem(new ECS_OnDamageBlockEvent());

        // Tick System
        this.getEntityStoreRegistry().registerSystem(new PlayerTickHandler());
        this.getEntityStoreRegistry().registerSystem(new KOTTMatchTickHandler());

        // Systems
        this.getEntityStoreRegistry().registerSystem(new InvulnerabilitySystem(this.invulnerabilityComponentType));
        this.getEntityStoreRegistry().registerSystem(new ProjectileDetectionSystem());
        this.getEntityStoreRegistry().registerSystem(new DeathSystem());
    }

    @Override
    protected void start() {
        printLog("KOTT Mod Started!");
    }

    @Override
    protected void shutdown() {
        KOTTMatch.getMatchesList().clear();
        ThreadSafetyProvider.shutdownAllThreads();
        //PacketAdapters.deregisterInbound(inboundFilter);

        super.shutdown();
    }
}