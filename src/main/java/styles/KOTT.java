package styles;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import styles.commands.KOTTCommand;
import styles.events.OnPlayerConnectEvent;
import styles.util.log.LogTypesDebug;
import styles.world.tick.TickHandler;

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

        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, OnPlayerConnectEvent::onPlayerConnect);

        this.getEntityStoreRegistry().registerSystem(new TickHandler());
    }

    @Override
    protected void start() {
        printL("Started!");
    }

}