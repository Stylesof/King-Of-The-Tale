package styles.player.util;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import styles.team.KOTTTeam;
import styles.util.ColorHandler;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

public class InventoryManagement {
    public static class KOTTStarterKit {
        public static boolean applyKit(@Nonnull Player player, @Nonnull KOTTTeam team) {
            if (player.getInventory().getHotbar() == null || player.getInventory().getArmor() == null) return false;

            player.getInventory().clear();

            String armbandColor = ColorHandler.getColorType(team.getTeamColor()).toString();
            armbandColor = armbandColor.substring(0, 1).toUpperCase() + armbandColor.substring(1).toLowerCase();

            ItemStack armband = new ItemStack("Armband_" + armbandColor, 1);
            player.getInventory().getArmor().addItemStack(armband);

            ItemStack pistol = new ItemStack("Weapon_Handgun", 1);
            ItemStack pistolAmmo = new ItemStack("Weapon_Arrow_Crude", 20);
            player.getInventory().getHotbar().addItemStack(pistol);
            player.getInventory().getHotbar().addItemStack(pistolAmmo);

            return true;
        }
    }
}
