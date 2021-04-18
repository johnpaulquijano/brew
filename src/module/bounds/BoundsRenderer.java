package module.bounds;

import core.Camera;
import core.GL;
import core.RenderingModule;
import core.Spatial;
import core.event.TraverserEvent;
import core.event.listener.TraverserListener;
import core.event.type.TraverserEventType;
import core.shader.Executable;
import core.shader.Shader;
import core.utility.Reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the rendering of visual bounding volumes.
 *
 * @author John Paul Quijano
 */
public class BoundsRenderer extends RenderingModule implements TraverserListener {
    private Shader shader;
    private Camera camera;

    private Executable bounds_e;
    private List<Spatial> spatialList;
    private Map<Spatial, VisualBounds> boundsSpatialMap;


    public BoundsRenderer() {
        spatialList = new ArrayList<>();
        boundsSpatialMap = new HashMap<>();
    }

    @Override
    protected void build() {
        shader = renderer.getShader();

        bounds_e = new Executable("BOUNDS");
        bounds_e.setSource(Shader.Stage.VERTEX, Reader.read(getClass().getResource("shader/bounds-vertex.glsl")));
        bounds_e.setSource(Shader.Stage.FRAGMENT, Reader.read(getClass().getResource("shader/bounds-fragment.glsl")));
        shader.addExecutable(bounds_e);
    }

    @Override
    protected void init() {
    }

    @Override
    protected void render() {
        if (!spatialList.isEmpty()) {
            GL.setFaceCullingEnabled(false);
            GL.setPolygonMode(GL.PolygonMode.LINE);

            camera = getRenderer().getCamera();
            renderer.setVPMatrix(camera.getViewProjectionBuffer());
            shader.execute(bounds_e);

            for (Spatial spatial : spatialList) {
                VisualBounds visualBounds = boundsSpatialMap.get(spatial);

                if (visualBounds == null) {
                    visualBounds = new VisualBounds();
                    visualBounds.build(getRenderer());
                    boundsSpatialMap.put(spatial, visualBounds);
                }

                if (spatial.isTransformDirty() || spatial.isBoundsDirty()) {
                    visualBounds.update(spatial.getWorldBounds());
                }

                visualBounds.draw();
            }
        }

        renderer.resetStates();
    }

    @Override
    protected void clean() {
        spatialList.clear();
    }

    @Override
    public boolean listen(TraverserEvent event) {
        if (event.getType() == TraverserEventType.BRANCH_NEXT) {
            if (event.getSource().getCurrent().isReset()) {
                spatialList.add(event.getSource().getCurrent());
            }
        } else if (event.getType() == TraverserEventType.LEAF) {
            spatialList.add(event.getSource().getCurrent());
        }

        return false;
    }
}
