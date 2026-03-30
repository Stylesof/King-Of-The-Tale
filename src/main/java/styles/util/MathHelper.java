package styles.util;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;

public class MathHelper {

    public static Vector3d convertAngleToUnitVector(float angleInDegree) {

        double angleRad = Math.toRadians(angleInDegree);

        double x = Math.cos(angleRad);
        double z = Math.sin(angleRad);

        return new Vector3d(x, 0, z);
    }

    public static double convertUnitVectorToAngle(Vector3d vector) {

        double x = Math.acos(vector.x);
        double z = Math.asin(vector.z);

        return Math.toDegrees(x + z);
    }

    public static Vector3d convertVectorToUnitVector(Vector3d pos, double size) {
        return new Vector3d(pos.x / size, pos.y / size, pos.z / size);
    }

    public static Vector3d scalarVector(Vector3d vector, int scalar) {
        return new Vector3d(vector.x * scalar, vector.y * scalar, vector.z * scalar);
    }

    public static Vector3d scalarVector(Vector3d vector, double scalar) {
        return new Vector3d(vector.x * scalar, vector.y * scalar, vector.z * scalar);
    }

    public static Vector3d vectorSum(Vector3d first, Vector3d second) {
        return new Vector3d(first.x + second.x, first.y + second.y, first.z + second.z);
    }

    public static Vector3i vectorSum(Vector3i first, Vector3i second) {
        return new Vector3i(first.x + second.x, first.y + second.y, first.z + second.z);
    }

    public static Vector3d vectorSub(Vector3d first, Vector3d second) {
        return new Vector3d(second.x - first.x, second.y - first.y, second.z - first.z);
    }

    public static double positionDistance(Vector3d first, Vector3d second) {
        double pow = Math.pow(second.x - first.x, 2) + Math.pow(second.y - first.y, 2) + Math.pow(second.z - first.z, 2);
        return Math.sqrt(pow);
    }

}
