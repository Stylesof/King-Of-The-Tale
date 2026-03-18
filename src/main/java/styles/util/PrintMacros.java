package styles.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class PrintMacros {

    public static void print(CommandContext cmdctx, String msg) {
        if (cmdctx != null) cmdctx.sendMessage(Message.raw(msg));
    }

    public static void print(PlayerRef playerRef, String msg) {
        if (playerRef != null) playerRef.sendMessage(Message.raw(msg));
    }

    // PRINT TO THE LOGGER
    public static void printL(String message, Level level){
        HytaleLogger.getLogger().at(level).log(message);
    }
    public static void printL(String message){
        HytaleLogger.getLogger().at(Level.INFO).log(message);
    }

}
