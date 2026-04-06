package styles.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.NotificationUtil;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MessageHandler {

    public static final Map<ItemTypes, ItemWithAllMetadata> Icons = new HashMap<>();
    public enum ItemTypes {
        MITHRIL_SWORD,
        }
    static {
        Icons.put(ItemTypes.MITHRIL_SWORD, new ItemStack("Weapon_Sword_Mithril", 1).toPacket());
    }

    @Nonnull
    public enum NotificationTypes {
        SUCCESS,
        WARNING,
        ERROR
    }

    private static Color getNotficationTitleColor(NotificationTypes type) {
        return switch (type){
            case SUCCESS -> Color.green;
            case WARNING -> Color.yellow;
            case ERROR -> Color.red;
        };
    }

    private static String getNotificationSubTitleColor(NotificationTypes type) {
        return switch (type) {
            case SUCCESS -> "#228B22";
            case WARNING -> "#8B8B22";
            case ERROR -> "#8B2222";
        };
    }

    public static void printNotification(PlayerRef playerRef, String title, String subTitle, ItemTypes icon, NotificationTypes type) {
        if (playerRef == null) return;
        NotificationUtil.sendNotification(
                playerRef.getPacketHandler(),
                Message.raw(title).color(getNotficationTitleColor(type)),
                Message.raw(subTitle).color(getNotificationSubTitleColor(type)),
                Icons.get(icon)
        );
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
