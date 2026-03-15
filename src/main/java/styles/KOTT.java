package styles;

import com.hypixel.hytale.builtin.buildertools.commands.PasteCommand;
import com.hypixel.hytale.builtin.buildertools.commands.PrefabCommand;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.commands.PrefabEditLoadCommand;
import com.hypixel.hytale.builtin.buildertools.utils.PasteToolUtil;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabLoader;
import com.hypixel.hytale.server.core.util.PrefabUtil;
import styles.commands.KOTTCommand;
import styles.events.OnPlayerConnectEvent;
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

        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, OnPlayerConnectEvent::onPlayerConnect);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, OnPlayerDisconnectEvent::onPlayerDisconnect);

        this.getEntityStoreRegistry().registerSystem(new EntityTickHandler());

    }

    @Override
    protected void start() {
        printL("Started!");
    }

    @Override
    protected void shutdown() {
        KOTTMatch.getMatchesList().clear();

        super.shutdown();
    }
}