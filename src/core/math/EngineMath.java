package core.math;

import core.utility.Buffers;

import java.nio.LongBuffer;
import java.util.Random;

/**
 * Convenience in-engine math library.
 *
 * @author John Paul Quijano
 */
public final class EngineMath {
    public static final float E = (float) Math.E;
    public static final float PI = (float) Math.PI;
    public static final float TWO_PI = PI * 2f;
    public static final float HALF_PI = PI * 0.5f;
    public static final float QUARTER_PI = PI * 0.25f;
    public static final float EPSILON = 1.1920928955078125E-7f;
    public static final Random RANDOM = new Random(System.currentTimeMillis());

    public static int abs(int a) {
        return Math.abs(a);
    }

    public static long abs(long a) {
        return Math.abs(a);
    }

    public static float abs(float a) {
        return Math.abs(a);
    }

    public static float acos(float a) {
        return (float) Math.acos(a);
    }

    public static float asin(float a) {
        return (float) Math.asin(a);
    }

    public static float atan(float a) {
        return (float) Math.atan(a);
    }

    public static float atan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }

    public static float cbrt(float a) {
        return (float) Math.cbrt(a);
    }

    public static float ceil(float a) {
        return (float) Math.ceil(a);
    }

    public static float copySign(float magnitude, float sign) {
        return Math.copySign(magnitude, sign);
    }

    public static float cos(float a) {
        return (float) Math.cos(a);
    }

    public static float cosh(float x) {
        return (float) Math.cosh(x);
    }

    public static float exp(float a) {
        return (float) Math.exp(a);
    }

    public static float expm1(float a) {
        return (float) Math.expm1(a);
    }

    public static float floor(float a) {
        return (float) Math.floor(a);
    }

    public static int getExponent(float a) {
        return Math.getExponent(a);
    }

    public static float hypot(float x, float y) {
        return (float) Math.hypot(x, y);
    }

    public static float invSqrt(float value) {
        return (float) (1f / Math.sqrt(value));
    }

    public static float remainder(float f1, float f2) {
        return (float) Math.IEEEremainder(f1, f2);
    }

    public static float log(float a) {
        return (float) Math.log(a);
    }

    public static float log10(float a) {
        return (float) Math.log10(a);
    }

    public static float log1p(float a) {
        return (float) Math.log1p(a);
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static long max(long a, long b) {
        return Math.max(a, b);
    }

    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    public static long min(long a, long b) {
        return Math.min(a, b);
    }

    public static float nextAfter(float start, float direction) {
        return Math.nextAfter(start, direction);
    }

    public static float nextUp(float f) {
        return Math.nextUp(f);
    }

    public static float pow(float a, float b) {
        return (float) Math.pow(a, b);
    }

    public static float random() {
        return (float) Math.random();
    }

    public static float rint(float a) {
        return (float) Math.rint(a);
    }

    public static int round(float a) {
        return Math.round(a);
    }

    public static float scalb(float f, int scaleFactor) {
        return Math.scalb(f, scaleFactor);
    }

    public static float signum(float f) {
        return Math.signum(f);
    }

    public static float sin(float a) {
        return (float) Math.sin(a);
    }

    public static float sinh(float x) {
        return (float) Math.sinh(x);
    }

    public static float sqrt(float a) {
        return (float) Math.sqrt(a);
    }

    public static float tan(float a) {
        return (float) Math.tan(a);
    }

    public static float tanh(float x) {
        return (float) Math.tanh(x);
    }

    public static float toDegrees(float rad) {
        return (float) Math.toDegrees(rad);
    }

    public static float toRadians(float deg) {
        return (float) Math.toRadians(deg);
    }

    public static float ulp(float a) {
        return Math.ulp(a);
    }

    public static float lerp(float min, float max, float delta) {
        return ((1f - delta) * min) + (delta * max);
    }

    /**
     * Inclusively clamps value to the given lower and upper bounds.
     *
     * @param x - input value
     * @param lower - lower bound
     * @param upper - upper bound
     *
     * @return the clamped value
     */
    public static float clamp(float x, float lower, float upper) {
        return max(min(x, upper), lower);
    }

    /**
     * Inclusively clamps value to the given lower and upper bounds.
     *
     * @param x - input value
     * @param lower - lower bound
     * @param upper - upper bound
     *
     * @return the clamped value
     */
    public static int clamp(int x, int lower, int upper) {
        return max(min(x, upper), lower);
    }

    public static long clamp(long x, long lower, long upper) {
        return max(min(x, upper), lower);
    }

    /**
     * Calculates the factorial of the given integer.
     *
     * @param k - integer to calculate factorial of
     *
     * @return factorial of the given integer
     */
    public static int factorial(int k) {
        int factorial = 1;

        for (int i = 2; i <= k; i++) {
            factorial *= i;
        }

        return factorial;
    }

    /**
     * Calculates a continuous Gaussian weight at the given distance from the center of the kernel.
     *
     * @param sigma - standard deviation
     * @param distance - distance from the kernel origin
     *
     * @return calculated continuous Gaussian weight
     */
    public static float gaussian(float sigma, float distance) {
        return (float) (Math.exp(-(distance * distance) / (2f * sigma * sigma)) / (Math.sqrt(2f * PI) * sigma));
    }

    /**
     * Calculates the elements in a pascal triangle row given a row index.
     *
     * @param index - row index
     * @param output - storage buffer
     *
     * @return buffer containing elements of the row
     */
    public static LongBuffer pascal(int index, LongBuffer output) {
        if (output == null) {
            output = Buffers.createLongBuffer(index + 1);
        } else {
            output.clear();
        }

        int half = (index + 1) / 2;

        output.put(1);

        for (int k = 0; k < index; k++) {
            if (k > half) {
                output.put(output.get(index - (k + 1))); /** mirror first half */
            } else {
                output.put(output.get(k) * (index - k) / (k + 1)); /** calculate first half */
            }
        }

        output.flip();

        return output;
    }
}
