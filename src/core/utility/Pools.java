package core.utility;

import core.math.*;

/**
 * In-engine pool.
 *
 * @author John Paul Quijano
 */
public final class Pools {
    public static final int MAX_POOL_SIZE = 10;
    public static final Pool<Point> Point = new Pool(Point.class, MAX_POOL_SIZE, false);
    public static final Pool<Matrix3> Matrix3 = new Pool(Matrix3.class, MAX_POOL_SIZE, false);
    public static final Pool<Matrix4> Matrix4 = new Pool(Matrix4.class, MAX_POOL_SIZE, false);
    public static final Pool<Vector2> Vector2 = new Pool(Vector2.class, MAX_POOL_SIZE, false);
    public static final Pool<Vector3> Vector3 = new Pool(Vector3.class, MAX_POOL_SIZE, false);
    public static final Pool<Vector4> Vector4 = new Pool(Vector4.class, MAX_POOL_SIZE, false);
    public static final Pool<Quaternion> Quaternion = new Pool(Quaternion.class, MAX_POOL_SIZE, false);

    private Pools() {}
}
