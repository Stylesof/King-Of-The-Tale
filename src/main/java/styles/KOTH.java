package styles;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import styles.commands.KOTHCommand;
import styles.events.OnPlayerConnectEvent;
import styles.world.tick.TickHandler;

import javax.annotation.Nonnull;

import static styles.util.Utils.printL;

public class KOTH extends JavaPlugin {

    public KOTH(@Nonnull JavaPluginInit init) {
        super(init);
        printL("Loaded!");
    }

    @Override
    protected void setup() {
        this.getCommandRegistry().registerCommand(new KOTHCommand());

        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, OnPlayerConnectEvent::onPlayerConnect);

        this.getEntityStoreRegistry().registerSystem(new TickHandler());
    }

    @Override
    protected void start() {
        printL("Started!");
    }

}