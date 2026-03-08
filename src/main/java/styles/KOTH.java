package styles;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import styles.commands.KOTHCommand;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

import static styles.utils.Utils.printL;

public class KOTH extends JavaPlugin {

    public KOTH(@Nonnull JavaPluginInit init) {
        super(init);
        printL("Loaded!");
    }

    @Override
    protected void setup() {
        this.getCommandRegistry().registerCommand(new KOTHCommand());
    }

    @Override
    protected void start() {
        printL("Started!");
    }

}