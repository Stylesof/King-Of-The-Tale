package styles.utils;

import com.hypixel.hytale.math.vector.Vector3i;

public class MathHelper {

    public static Vector3i vectorAngleSum(float firstAngle, float angleToSum) {

        double angle = Math.toRadians(firstAngle) + Math.toRadians(angleToSum);

        int x = (int) Math.sin(angle);
        int z = (int) Math.cos(angle);

        return new Vector3i(x, 0, z);
    }

    public static Vector3i scalarVector(Vector3i vector, int scalar) {
        return new Vector3i(vector.x * scalar, vector.y * scalar, vector.z * scalar);
    }

    public static Vector3i vectorSum(Vector3i first, Vector3i second) {
        return new Vector3i(first.x + second.x, first.y + second.y, first.z + second.z);
    }

}
