package styles;

import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import styles.commands.KOTTCommand;
import styles.events.OnPlayerConnectEvent;
import styles.events.OnShutdownEvent;
import styles.events.OnRemoveWorldEvent;
import styles.util.log.LogTypesDebug;
import styles.world.KOTTMatch;
import styles.world.tick.EntityTickHandler;

import javax.annotation.Nonnull;

import java.util.Map;

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
        this.getEventRegistry().registerGlobal(RemoveWorldEvent.class, OnRemoveWorldEvent::onRemoveWorld);
        this.getEventRegistry().registerGlobal(ShutdownEvent.class, OnShutdownEvent::onShutdown);

        this.getEntityStoreRegistry().registerSystem(new EntityTickHandler());
    }

    @Override
    protected void start() {
        printL("Started!");
    }


    /*
        WIP
        PROBLEM WITH UNLOAD MATCHES, CUZ THE WORLD IS UNLOAD FIRST THAN THE PLUGIN
        IS SHUT DOWN
     */
    @Override
    protected void shutdown() {
        printL("Finishing...");

        for (KOTTMatch match : KOTTMatch.getMatchesList().values()) {
            if (match.getKOTHMatchStatus()) {
               match.stop();
            }
        }

        printL("Finished!");
        super.shutdown();
    }
}