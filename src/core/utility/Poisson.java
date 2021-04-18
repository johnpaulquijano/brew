package core.utility;

import core.math.EngineMath;
import core.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Robert Bridson's algorithm described in his paper "Fast Poisson Disk Sampling In Arbitrary Dimensions"
 * which uses a grid for fast spatial queries of neighboring samples.
 *
 * @author John Paul Quijano
 */
public final class Poisson {
    private Poisson() {}

    /**
     * Generates samples constrained by a rectangle.
     *
     * @param width     - width of area to cover
     * @param height    - height of area to cover
     * @param seedX     - initial value in the x axis
     * @param seedY     - initial value in the y axis
     * @param proximity - minimum distance of samples
     * @param density   - number of samples to generate per existing sample
     *
     * @return - list of samples
     */
    public static List<Vector2> generateQuad(float width, float height, float proximity, float seedX, float seedY, int density) {
        float cellSize = proximity / EngineMath.sqrt(2f);
        int rows = (int) (width / cellSize);
        int cols = (int) (height / cellSize);

        int numPoints = rows * cols;
        Vector2[][] grid = new Vector2[rows][cols];
        List<Vector2> samples = new ArrayList<>(numPoints);
        List<Vector2> seeds = new ArrayList<>(numPoints);

        Vector2 init = new Vector2(seedX, seedY);

        samples.add(init);
        seeds.add(init);
        grid[getRow(seedX, cellSize)][getCol(seedX, cellSize)] = init;

        while (!seeds.isEmpty()) {
            Vector2 currentSample = seeds.remove(EngineMath.RANDOM.nextInt(seeds.size()));

            for (int i = 0; i < density; i++) {
                Vector2 newSample = newSample(currentSample, proximity, width, height);
                int row = getRow(newSample.getX(), cellSize);
                int col = getCol(newSample.getY(), cellSize);

                if (isValid(grid, row, col)) {
                    samples.add(newSample);
                    seeds.add(newSample);
                    grid[row][col] = newSample;
                }
            }
        }

        return samples;
    }

    /**
     * Generates samples constrained by a circle.
     *
     * @param radius    - radius of the bounding circle
     * @param seedX     - initial value in the x axis
     * @param seedY     - initial value in the y axis
     * @param proximity - minimum distance of samples
     * @param density   - number of samples to generate per existing sample
     *                  
     * @return - list of samples
     */
    public static List<Vector2> generateDisk(float radius, float proximity, float seedX, float seedY, int density) {
        float diameter = radius * 2f;
        float cellSize = proximity / EngineMath.sqrt(2f);
        int rows = (int) (diameter / cellSize);
        int cols = (int) (diameter / cellSize);

        int numPoints = rows * cols;
        Vector2[][] grid = new Vector2[rows][cols];
        List<Vector2> samples = new ArrayList<>(numPoints);
        List<Vector2> seeds = new ArrayList<>(numPoints);

        Vector2 init = new Vector2(seedX, seedY);

        if (!inDisk(init, radius)) {
            throw new EngineException("Seed is not within the disk.");
        }

        samples.add(init);
        seeds.add(init);
        grid[getRow(seedX, cellSize)][getCol(seedX, cellSize)] = init;

        while (!seeds.isEmpty()) {
            Vector2 currentSample = seeds.remove(EngineMath.RANDOM.nextInt(seeds.size()));

            for (int i = 0; i < density; i++) {
                Vector2 newSample = newSample(currentSample, proximity, diameter, diameter);
                int row = getRow(newSample.getX(), cellSize);
                int col = getCol(newSample.getY(), cellSize);

                if (isValid(grid, row, col) && inDisk(newSample, radius)) {
                    samples.add(newSample);
                    seeds.add(newSample);
                    grid[row][col] = newSample;
                }
            }
        }

        return samples;
    }

    private static boolean isValid(Vector2[][] grid, int row, int col) {
        if (row >= grid.length || col >= grid[row].length) {
            return false;
        }

        if (grid[row][col] != null) {
            return false;
        }

        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                int absRow = EngineMath.abs(i);
                int absCol = EngineMath.abs(j);

                /** Skip checking center and corners. */
                if (i == 0 && j == 0 || absRow == absCol && absRow > 1) {
                    continue;
                }

                int neighRow = row + i;
                int neighCol = col + j;

                /** Sample is too close to another sample. */
                if (neighRow >= 0 && neighRow < grid.length && neighCol >= 0 && neighCol < grid[row].length) {
                    if (grid[neighRow][neighCol] != null) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static int getRow(float x, float cellSize) {
        return (int) (x / cellSize);
    }

    private static int getCol(float y, float cellSize) {
        return (int) (y / cellSize);
    }

    private static boolean inDisk(Vector2 sample, float radius) {
        return sample.distance(radius, radius) < radius;
    }

    private static Vector2 newSample(Vector2 origin, float radius, float width, float height) {
        float r = radius * (EngineMath.RANDOM.nextFloat() + 1f);
        float a = EngineMath.TWO_PI * EngineMath.RANDOM.nextFloat();
        float x = EngineMath.clamp(origin.getX() + r * EngineMath.cos(a), 0f, width);
        float y = EngineMath.clamp(origin.getY() + r * EngineMath.sin(a), 0f, height);
        return new Vector2(x, y);
    }
}
