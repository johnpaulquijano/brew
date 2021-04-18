package core;

import core.utility.EngineException;

/**
 * Base class for sub-modules that do specific rendering sub-tasks.
 *
 * @author John Paul Quijano
 */
public abstract class RenderingProcessor extends RenderingModule {
    protected RenderingModule renderingModule;

    /**
     * Gives the owning rendering module or null if this rendering module has not yet been added to any renderer.
     *
     * @return owning renderer
     */
    public RenderingModule getRenderingModule() {
        return renderingModule;
    }

    /**
     * Called by the rendering module when this rendering processor is added to it.
     *
     * @param renderingModule - owner rendering module
     */
    protected void addNotify(RenderingModule renderingModule) {
        if (this.renderingModule != null) {
            throw new EngineException("Rendering processor already runs on another rendering module.");
        }

        this.renderingModule = renderingModule;
    }
}
