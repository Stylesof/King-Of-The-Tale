package styles.util;

import com.hypixel.hytale.math.vector.Vector3d;

public class MathHelper {

    public static Vector3d convertAngleToUnitVector(float angleInDegree) {

        double angle = Math.toRadians(angleInDegree);

        double x = Math.cos(angle);
        double z = Math.sin(angle);

        return new Vector3d(x, 0, z);
    }

    public static Vector3d scalarVector(Vector3d vector, int scalar) {
        return new Vector3d(vector.x * scalar, vector.y * scalar, vector.z * scalar);
    }

    public static Vector3d vectorSum(Vector3d first, Vector3d second) {
        return new Vector3d(first.x + second.x, first.y + second.y, first.z + second.z);
    }

    public static double positionDistance(Vector3d first, Vector3d second) {
        double pow = Math.pow(second.x - first.x, 2) + Math.pow(second.y - first.y, 2) + Math.pow(second.z - first.z, 2);
        return Math.sqrt(pow);
    }

}
