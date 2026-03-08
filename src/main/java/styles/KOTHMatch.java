package styles;

public class KOTHMatch {

    private static boolean KOTHMatchStatus = false;

    public static void start() {
        setKOTHMatchStatus(true);
        

    }

    public static void stop() {
        setKOTHMatchStatus(false);

    }

    public static boolean getKOTHMatchStatus() {
        return KOTHMatchStatus;
    }

    public static void setKOTHMatchStatus(boolean state) {
        KOTHMatchStatus = state;
    }

}
