package styles.team.name;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TeamNameGenerator {

    public static final List<String> Names = new ArrayList<>(){};

    static {

        Names.add("Alpha");
        Names.add("Bravo");
        Names.add("Charlie");
        Names.add("Delta");
        Names.add("Echo");
        Names.add("Foxtrot");
        Names.add("Gol");
        Names.add("Hotel");
        Names.add("India");

    }

    public static List<String> genRandomNameList(int count) {
        List<String> temp = new ArrayList<>(Names);
        Random random = new Random();

        List<String> res = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            int r = random.nextInt(temp.size());
            res.add(temp.get(r));
            temp.remove(r);
        }

        return res;
    }

}
