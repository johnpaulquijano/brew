package module.sky;

import core.*;
import core.event.TraverserEvent;
import core.event.listener.TraverserListener;
import core.event.type.TraverserEventType;
import core.shader.Executable;
import core.shader.Sampler;
import core.shader.Shader;
import core.utility.Reader;

import java.util.ArrayList;
import java.util.List;

public class SkyRenderer extends RenderingModule implements TraverserListener {
    private Shader shader;

    private Sampler skySampler_u;
    private Executable sky_e;

    private List<Sky> skies;

    public SkyRenderer() {
        skies = new ArrayList<>();
    }

    /**
     * Renders each sky the given list.
     *
     * @param skies - list of skies
     */
    public void renderSkies(List<Sky> skies) {
        if (!skies.isEmpty()) {
            GL.setFaceCullingEnabled(false);
            GL.setPolygonMode(GL.PolygonMode.FILL);

            for (Sky sky : skies) {
                Geometry geometry = sky.getGeometry();
                TextureCube texture = sky.getTexture();

                if (!geometry.isBuilt()) {
                    renderer.build(geometry);
                } else if (geometry.isDirty()) {
                    geometry.update();
                }

                if (!texture.isBuilt()) {
                    renderer.build(texture);
                } else if (texture.isDirty()) {
                    texture.update();
                }

                GL.bindTextureCube(texture.getID(), skySampler_u.getUnit(0));

                sky.calculateWVP(renderer.getCamera());
                renderer.setWVPMatrix(sky.getWorldViewProjectionBuffer());
                shader.execute(sky_e);

                geometry.draw();
            }
        }
    }

    @Override
    protected void build() {
        shader = renderer.getShader();

        skySampler_u = new Sampler(Shader.Type.SAMPLERCUBE, "skySampler", 0, Shader.Qualifier.UNIFORM);
        shader.addVariable(skySampler_u);

        sky_e = new Executable("SKY");
        sky_e.setSource(Shader.Stage.VERTEX, Reader.read(getClass().getResource("shader/sky-ev.glsl")));
        sky_e.setSource(Shader.Stage.FRAGMENT, Reader.read(getClass().getResource("shader/sky-ef.glsl")));
        shader.addExecutable(sky_e);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void render() {
        renderSkies(skies);
    }

    @Override
    protected void clean() {
        for (Sky sky : skies) {
            sky.clean();
        }

        skies.clear();
    }

    @Override
    public boolean listen(TraverserEvent event) {
        if (event.getType() == TraverserEventType.LEAF) {
            if (event.getSource().getCurrent() instanceof Sky) {
                skies.add((Sky) event.getSource().getCurrent());
            }
        }

        return false;
    }
}
