package styles;

import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.ecs.ChunkSaveEvent;
import styles.commands.KOTTCommand;
import styles.events.OnChunkSaveEvent;
import styles.events.OnPlayerConnectEvent;
import styles.events.OnPlayerDisconnectEvent;
import styles.events.OnRemoveWorldEvent;
import styles.util.log.LogTypesDebug;
import styles.world.KOTTMatch;
import styles.world.tick.EntityTickHandler;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static styles.util.PrintMacros.printL;
import static styles.util.log.PrintLog.printLogDebug;

public class KOTT extends JavaPlugin {

    public KOTT(@Nonnull JavaPluginInit init) {
        super(init);
        printLogDebug(LogTypesDebug.KOTTLoadSuccess);
    }

    @Override
    protected void setup() {
        this.getCommandRegistry().registerCommand(new KOTTCommand());

        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, OnPlayerConnectEvent::onPlayerConnect);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, OnPlayerDisconnectEvent::onPlayerDisconnect);
        //this.getEventRegistry().registerGlobal(RemoveWorldEvent.class, OnRemoveWorldEvent::onRemoveWorld);
        //this.getEventRegistry().registerGlobal(ShutdownEvent.class, OnShutdownEvent::onShutdown);
        //ChunkSaveEvent
        //this.getEventRegistry().registerGlobal(ChunkSaveEvent.class, OnChunkSaveEvent::onChunkSave);


        this.getEntityStoreRegistry().registerSystem(new EntityTickHandler());
    }

    @Override
    protected void start() {
        printL("Started!");
    }
}