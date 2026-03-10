package styles.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class Utils {

    public static void print(@Nonnull CommandContext cmdctx, String msg) {
        cmdctx.sendMessage(Message.raw(msg));
    }

    public static void print(@Nonnull PlayerRef playerRef, String msg) {
        playerRef.sendMessage(Message.raw(msg));
    }

    public static void printL(String message, Level level){
        HytaleLogger.getLogger().at(level).log(message);
    }
    public static void printL(String message){
        HytaleLogger.getLogger().at(Level.INFO).log(message);
    }

}
