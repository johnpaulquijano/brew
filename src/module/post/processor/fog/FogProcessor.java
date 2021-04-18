package module.post.processor.fog;

import core.GL;
import core.RenderingProcessor;
import core.math.Vector3;
import core.shader.Executable;
import core.shader.Function;
import core.shader.Shader;
import core.shader.Variable;
import core.utility.Colors;
import core.utility.Reader;
import module.post.PostRenderer;

/**
 * Approximates aerial perspective.
 *
 * @author John Paul Quijano
 */
public class FogProcessor extends RenderingProcessor {
    public static final float DEFAULT_DENSITY = 0.25f;
    public static final Vector3 DEFAULT_COLOR = new Vector3(Colors.WHITE3);

    private static final float DENSITY_SCALE = 0.05f;

    private Shader shader;
    private PostRenderer module;

    private Variable color_u;
    private Variable density_u;
    private Function fog_f;
    private Executable fog_e;

    private float density;
    private boolean colorDirty;
    private boolean densityDirty;
    private Vector3 color;

    public FogProcessor() {
        density = DEFAULT_DENSITY;
        color = new Vector3(DEFAULT_COLOR);

        colorDirty = true;
        densityDirty = true;
    }

    /**
     * Sets the fog density.
     *
     * @param density - fog density
     */
    public void setDensity(float density) {
        this.density = density;
        densityDirty = true;
    }

    /**
     * Gives the fog density.
     *
     * @return fog density
     */
    public float getDensity() {
        return density;
    }

    /**
     * Sets the fog color.
     *
     * @param r - red color component
     * @param g - green color component
     * @param b - blue color component
     */
    public void setColor(float r, float g, float b) {
        color.set(r, g, b);
        colorDirty = true;
    }

    /**
     * Sets the fog color.
     *
     * @param color - 3-component color vector
     */
    public void setColor(Vector3 color) {
        setColor(color.getX(), color.getY(), color.getZ());
    }

    /**
     * Gives the fog color.
     *
     * @return fog color
     */
    public Vector3 getColor() {
        return color;
    }

    @Override
    protected void build() {
        module = (PostRenderer) getRenderingModule();
        renderer = module.getRenderer();
        shader = renderer.getShader();

        /**
         * Initialize uniforms.
         */
        color_u = new Variable(Shader.Type.VEC3, "fogColor", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(color_u);

        density_u = new Variable(Shader.Type.FLOAT, "fogDensity", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(density_u);

        /**
         * Initialize functions.
         */
        fog_f = new Function(
                Shader.Type.VEC4,
                "fog",
                Shader.Stage.FRAGMENT,
                new Variable(Shader.Type.VEC3, "color", null, 0, Shader.Qualifier.IN),
                new Variable(Shader.Type.FLOAT, "density", null, 0, Shader.Qualifier.IN)
        );
        fog_f.setSource(Reader.read(getClass().getResource("shader/fog-ff.glsl")));
        fog_f.setComment("Approximates aerial perspective.");
        shader.addFunction(fog_f);

        /**
         * Initialize executables.
         */
        fog_e = new Executable("FOG");
        fog_e.setSource(Shader.Stage.VERTEX, "fsquad();");
        fog_e.setSource(Shader.Stage.FRAGMENT, "output0 = fog(fogColor, fogDensity);");
        shader.addExecutable(fog_e);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void render() {
        update();

        GL.bindTexture2D(module.getOutputBuffer().getColorBuffer(0).getID(), renderer.getUtilSamplerUnit(0));
        GL.bindTexture2D(module.getSourceBuffer().getDepthBuffer().getID(), renderer.getUtilSamplerUnit(1));
        GL.setViewport(0, 0, renderer.getViewportWidth(), renderer.getViewportHeight());
        GL.writeFrameBuffer(module.getOutputBuffer().getID());

        shader.execute(fog_e);
        renderer.drawFullscreenQuad();
    }

    @Override
    protected void clean() {
        colorDirty = false;
        densityDirty = false;
    }

    private void update() {
        if (colorDirty) {
            GL.setVector3(color_u.getID(), color.getX(), color.getY(), color.getZ());
        }

        if (densityDirty) {
            GL.setScalar(density_u.getID(), density * DENSITY_SCALE);
        }
    }
}
