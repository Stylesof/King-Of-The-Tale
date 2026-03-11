package styles.world.util.filter;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockFilter {

    private static Map<FilterTypes, List<String>> filterList = new HashMap<>();

    static {

        filterList.put(FilterTypes.PLANT, new ArrayList<>());
        filterList.get(FilterTypes.PLANT).add("Plant");

    }

    public static boolean applyFilter(BlockType blockType, FilterTypes filterType) {

        for(String filter : filterList.get(filterType)){
            if(blockType.getId().toString().contains(filter)) return true;
        }

        return false;
    }

}
