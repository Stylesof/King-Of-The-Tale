package styles.util;

import com.hypixel.hytale.protocol.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ColorGenerator {

    public static final List<Color> Colors = new ArrayList<>();

    static  {

        // Colors.add(new Color((byte) 255, (byte) 0   ,(byte) 0));   // RED
        Colors.add(new Color((byte) 0  , (byte) 0   ,(byte) 0));   // WHITE
        Colors.add(new Color((byte) 0  , (byte) 255 ,(byte) 0));   // GREEN
        Colors.add(new Color((byte) 0  , (byte) 0   ,(byte) 255)); // BLUE
        Colors.add(new Color((byte) 255, (byte) 255 ,(byte) 0));   // YELLOW
        Colors.add(new Color((byte) 0  , (byte) 255 ,(byte) 255)); // LIGHT BLUE
        Colors.add(new Color((byte) 255, (byte) 0   ,(byte) 255)); // PURPLE

    }

    public static List<Color> genRandomColorList(int count) {
        List<Color> temp = new ArrayList<>(Colors);
        Random random = new Random();

        List<Color> res = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            int r = random.nextInt(temp.size());
            res.add(temp.get(r));
            temp.remove(r);
        }

        return res;
    }
}
