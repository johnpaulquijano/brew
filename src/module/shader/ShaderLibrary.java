package module.shader;

import core.GL;
import core.RenderingModule;
import core.math.EngineMath;
import core.shader.Function;
import core.shader.Shader;
import core.shader.Variable;
import core.utility.Buffers;
import core.utility.Reader;

import java.nio.FloatBuffer;

/**
 * Collection of useful shader functions and constants.
 *
 * @author John Paul Quijano
 */
public class ShaderLibrary extends RenderingModule {
    public static final int MAX_POISSON_SAMPLES = 256;

    private Shader shader;

    private Variable poissonDisk_u;
    private Function fsquad_f;
    private Function random_f;
    private Function linearize_f;
    private Function luminance_f;

    public ShaderLibrary() {
    }

    @Override
    protected void build() {
        shader = renderer.getShader();

        /**
         * Initialize definitions.
         */
        shader.addDefinition("PI", String.valueOf(EngineMath.PI) + "f");
        shader.addDefinition("HALF_PI", String.valueOf(EngineMath.PI * 0.5f) + "f");
        shader.addDefinition("EPSILON", String.valueOf(EngineMath.EPSILON) + "f");

        /**
         * Initialize variables.
         */
        poissonDisk_u = new Variable(Shader.Type.VEC2, "poissonDisk", null, MAX_POISSON_SAMPLES, Shader.Qualifier.UNIFORM);
        shader.addVariable(poissonDisk_u);

        /**
         * Initialize functions.
         */
        fsquad_f = new Function(
                Shader.Type.VOID,
                "fsquad",
                Shader.Stage.VERTEX
        );
        fsquad_f.setSource(Reader.read(getClass().getResource("shader/fsquad-fv.glsl")));
        fsquad_f.setComment("Shader for rendering a fullscreen quad used in render-to-texture operations.");
        shader.addFunction(fsquad_f);

        random_f = new Function(
                Shader.Type.FLOAT,
                "random",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.VEC2, "seed", null, 0, Shader.Qualifier.IN)
        );
        random_f.setSource(Reader.read(getClass().getResource("shader/random-ff.glsl")));
        random_f.setComment("Pseudo-random number generator.");
        shader.addFunction(random_f);

        linearize_f = new Function(
                Shader.Type.FLOAT,
                "linearize",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.FLOAT, "depth", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "near", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "far", null, 0, Shader.Qualifier.IN)
        );
        linearize_f.setSource(Reader.read(getClass().getResource("shader/linearize-ff.glsl")));
        linearize_f.setComment("Linearizes the given perspective-space depth value.");
        shader.addFunction(linearize_f);

        luminance_f = new Function(
                Shader.Type.FLOAT,
                "luminance",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.VEC3, "color", null, 0, Shader.Qualifier.IN)
        );
        luminance_f.setSource(Reader.read(getClass().getResource("shader/luminance-ff.glsl")));
        luminance_f.setComment("Calculates luminance value of the given color.");
        shader.addFunction(luminance_f);
    }

    @Override
    protected void init() {
        loadPoissonDisk();
    }

    @Override
    protected void render() {
    }

    @Override
    protected void clean() {
    }

    /**
     * Loads Poisson Disk offsets to the shader.
     */
    private void loadPoissonDisk() {
        FloatBuffer buffer = Buffers.createFloatBuffer(MAX_POISSON_SAMPLES * 2);
        loadResource("resources/poisson-disk", buffer);
        GL.setArray2(poissonDisk_u.getID(), buffer);
    }

    /**
     * Reads data from the given file and stores it in a float buffer.
     *
     * @param path - path to file
     * @param storage - storage float buffer
     */
    private void loadResource(String path, FloatBuffer storage) {
        String[] lines = Reader.read(getClass().getResource(path)).split("\n");

        for (String offsets : lines) {
            String[] coords = offsets.split(" ");
            storage.put(Float.valueOf(coords[0])).put(Float.valueOf(coords[1]));
        }

        storage.flip();
    }
}
