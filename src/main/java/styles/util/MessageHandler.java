package styles.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MessageHandler {

    public enum ItemTypes {
        MITHRIL_SWORD,
    }

    public static final Map<ItemTypes, ItemWithAllMetadata> Icons = new HashMap<>();

    static {
        Icons.put(ItemTypes.MITHRIL_SWORD, new ItemStack("Weapon_Sword_Mithril", 1).toPacket());
    }

    public static void printChat(CommandContext cmdctx, Message msg) {
        if (cmdctx != null) cmdctx.sendMessage(Message.join(Message.raw("[KOTT] ").color(Color.GREEN), msg));
    }

    public static void printChat(PlayerRef playerRef, Message msg) {
        if (playerRef != null) playerRef.sendMessage(Message.join(Message.raw("[KOTT] ").color(Color.GREEN), msg));
    }

    public static void printChat(CommandContext cmdctx, String msg) {
        if (cmdctx != null) cmdctx.sendMessage(Message.join(Message.raw("[KOTT] ").color(Color.GREEN), Message.raw(msg).color(Color.WHITE)));
    }

    public static void printChat(PlayerRef playerRef, String msg) {
        if (playerRef != null) playerRef.sendMessage(Message.join(Message.raw("[KOTT] ").color(Color.GREEN), Message.raw(msg).color(Color.WHITE)));
    }

    // PRINT TO THE LOGGER
    public static void printLog(String message, Level level){
        HytaleLogger.getLogger().at(level).log(message);
    }
    public static void printLog(String message){
        HytaleLogger.getLogger().at(Level.INFO).log("[KOTT Debug] " + message);
    }

}
