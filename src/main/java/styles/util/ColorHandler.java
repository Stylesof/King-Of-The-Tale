package styles.util;

import com.hypixel.hytale.protocol.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ColorHandler {

    public static final Map<ColorType, Color> Colors = new HashMap<>();

    public enum ColorType {

        WHITE,
        GREEN,
        BLUE,
        YELLOW,
        CYAN,
        PURPLE;

    }

    static  {

        // Colors.add(new Color((byte) 255, (byte) 0   ,(byte) 0));   // RED
        Colors.put(ColorType.WHITE     , new Color((byte) 255, (byte) 255 ,(byte) 255));   // WHITE
        Colors.put(ColorType.GREEN     , new Color((byte) 0  , (byte) 255 ,(byte) 0));   // GREEN
        Colors.put(ColorType.BLUE      , new Color((byte) 0  , (byte) 0   ,(byte) 255)); // BLUE
        Colors.put(ColorType.YELLOW    , new Color((byte) 255, (byte) 255 ,(byte) 0));   // YELLOW
        Colors.put(ColorType.CYAN, new Color((byte) 0  , (byte) 255 ,(byte) 255));       // CYAN
        Colors.put(ColorType.PURPLE    , new Color((byte) 255, (byte) 0   ,(byte) 255)); // PURPLE

    }

    public static List<Color> genRandomColorList(int count) {
        List<Color> temp = new ArrayList<>(Colors.values());
        Random random = new Random();

        List<Color> res = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            int r = random.nextInt(temp.size());
            res.add(temp.get(r));
            temp.remove(r);
        }

        return res;
    }

    @Nullable
    public static ColorType getColorType(Color color) {
        for (ColorType type : Colors.keySet()) {
            if (Colors.get(type) == color) {
                return  type;
            }
        }

        return null;
    }

    @Nonnull
    public static String getHexFromColor(ColorType colorType) {
        return switch (colorType) {
            case WHITE -> "#ffffff";
            case GREEN -> "#00ff00";
            case BLUE -> "#0000ff";
            case YELLOW -> "#ffff00";
            case CYAN -> "#00ffff";
            case PURPLE -> "#ff00ff";
        };
    }
}
