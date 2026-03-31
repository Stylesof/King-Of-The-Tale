package styles.events;

import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.player.KOTTMoney;

public class OnPlayerConnectEvent {
    public static void onPlayerConnect(PlayerConnectEvent evt) {
        PlayerRef playerRef = evt.getPlayerRef();
        if (playerRef != null && playerRef.getHolder() != null) {
            float moneyQnt = 0.0f;
            if (KOTTMoney.getPlayerMoneyMap().containsKey(playerRef.getUsername())) moneyQnt = KOTTMoney.getPlayerMoneyMap().get(playerRef.getUsername());

            KOTTMoney money = playerRef.getHolder().getComponent(KOTTMoney.getComponentType());
            if (money == null) {
                money = new KOTTMoney();
                money.moneyQuantity = moneyQnt;
                evt.getHolder().addComponent(KOTTMoney.getComponentType(), money);
                playerRef.getHolder().addComponent(KOTTMoney.getComponentType(), money);
            }

            money.moneyQuantity = moneyQnt;
        }
    }
}
