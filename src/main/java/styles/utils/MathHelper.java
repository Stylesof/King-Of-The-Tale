package styles.utils;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;

public class MathHelper {

    public static Vector3d vectorAngleSum(float angleInDegree) {

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

}
