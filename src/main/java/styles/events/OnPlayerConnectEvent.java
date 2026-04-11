package styles.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.player.component.InvulnerabilityComponent;
import styles.player.component.KOTTMoney;

public class OnPlayerConnectEvent {
    public static void onPlayerConnect(PlayerConnectEvent evt) {
        PlayerRef playerRef = evt.getPlayerRef();
        if (playerRef != null && playerRef.getHolder() != null) {
            int moneyQnt = 0;
            if (KOTTMoney.getPlayerMoneyMap().containsKey(playerRef.getUsername())) moneyQnt = KOTTMoney.getPlayerMoneyMap().get(playerRef.getUsername());

            KOTTMoney money = playerRef.getHolder().getComponent(KOTTMoney.getComponentType());
            if (money == null) {
                money = new KOTTMoney();
                evt.getHolder().addComponent(KOTTMoney.getComponentType(), money);
            }

            money.moneyQuantity = Math.max(moneyQnt, 0);

            InvulnerabilityComponent inv = playerRef.getHolder().getComponent(InvulnerabilityComponent.getComponentType());
            if (inv != null) {
                evt.getHolder().removeComponent(InvulnerabilityComponent.getComponentType());
            }
        }
    }
}
