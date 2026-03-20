package styles;

import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerInput;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import styles.commands.KOTTCommand;
import styles.events.OnAddPlayerToWorldEvent;
import styles.events.OnPlayerDisconnectEvent;
import styles.util.log.LogTypesDebug;
import styles.world.KOTTMatch;
import styles.world.tick.EntityTickHandler;

import javax.annotation.Nonnull;

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

        // this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, OnPlayerConnectEvent::onPlayerConnect);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, OnPlayerDisconnectEvent::onPlayerDisconnect);
        this.getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, OnAddPlayerToWorldEvent::onAddPlayerToWorld);

        this.getEntityStoreRegistry().registerSystem(new EntityTickHandler());
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
}