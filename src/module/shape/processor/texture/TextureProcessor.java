package module.shape.processor.texture;

import core.GL;
import core.Renderer;
import core.Texture;
import core.event.TraverserEvent;
import core.event.listener.TraverserListener;
import core.event.type.TraverserEventType;
import core.shader.Function;
import core.shader.Shader;
import core.shader.Variable;
import core.utility.Reader;
import core.Material;
import core.Shape;
import module.shape.ShapeProcessor;
import module.shape.ShapeRenderer;

import java.util.HashSet;
import java.util.Set;

public class TextureProcessor extends ShapeProcessor implements TraverserListener {
    private Variable blendModes_u;
    private Variable numTextures_u;
    private Variable textureEnabled_u;
    private Function texture2D_f;

    private Shader shader;
    private Renderer renderer;
    private ShapeRenderer module;

    private Set<Texture> bucketTextures;

    public TextureProcessor() {
        bucketTextures = new HashSet<>();
    }

    @Override
    public void apply(Shape shape) {
        boolean texturingReady = shape.isTexturingReady();

        if (texturingReady) {
            int samplerIndex = 0;
            Material material = shape.getMaterialDetail(shape.getMaterialLevel());

            for (Texture texture : material.getTextures()) {
                GL.bindTexture2D(texture.getID(), renderer.getUtilSamplerUnit(samplerIndex++));
            }

            GL.setScalar(numTextures_u.getID(), material.getTextures().size());
            GL.setArray1(blendModes_u.getID(), material.getBlendModesBuffer());

            bucketTextures.addAll(material.getTextures());
        }

        GL.setBoolean(textureEnabled_u.getID(), texturingReady);
    }

    @Override
    protected void build() {
        module = (ShapeRenderer) getRenderingModule();
        renderer = module.getRenderer();
        shader = renderer.getShader();

        /**
         * Initialize uniforms.
         */
        textureEnabled_u = new Variable(Shader.Type.BOOL, "textureEnabled", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(textureEnabled_u);

        numTextures_u = new Variable(Shader.Type.INT, "numTextures", null, 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(numTextures_u);

        blendModes_u = new Variable(Shader.Type.INT, "blendModes", null, Material.MAX_TEXTURES, Shader.Qualifier.UNIFORM);
        shader.addVariable(blendModes_u);

        /**
         * Initialize functions.
         */
        texture2D_f = new Function(Shader.Type.VEC4, "texture", Shader.Stage.FRAGMENT, new Variable(Shader.Type.VEC4, "inputColor", null, 0, Shader.Qualifier.IN));
        texture2D_f.setSource(Reader.read(getClass().getResource("shader/texture-ff.glsl")));
        shader.addFunction(texture2D_f);

        /**
         * Initialize executable sources;
         */
        module.appendShaderSource(Shader.Stage.FRAGMENT, Reader.read(getClass().getResource("shader/texture-f.glsl")));
    }

    @Override
    protected void init() {
    }

    @Override
    protected void render() {
        for (Texture texture : bucketTextures) {
            if (!texture.isBuilt()) {
                renderer.build(texture);
            } else if (texture.isDirty()) {
                texture.update();
            }
        }
    }

    @Override
    protected void clean() {
        for (Texture texture : bucketTextures) {
            texture.clean();
        }

        bucketTextures.clear();
    }

    @Override
    public boolean listen(TraverserEvent event) {
        if (event.getType() == TraverserEventType.LEAF) {
            if (event.getSource().getCurrent() instanceof Shape) {
                Shape shape = (Shape) event.getSource().getCurrent();
                Material material = shape.getMaterialDetail(shape.getMaterialLevel());

                bucketTextures.addAll(material.getTextures());
            }
        }

        return false;
    }
}
