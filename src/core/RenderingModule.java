package core;

import core.utility.EngineException;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for modules that do specific rendering tasks.
 *
 * @author John Paul Quijano
 */
public abstract class RenderingModule {
    protected boolean enabled;
    protected boolean initialized;
    protected Renderer renderer;
    protected List<RenderingProcessor> processors;

    public RenderingModule() {
        enabled = true;
        processors = new ArrayList<>();
    }

    /**
     * Gives the owning renderer or null if this rendering module has not yet been added to any renderer.
     *
     * @return owning renderer
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Checks if the init() method has already been called.
     *
     * @return true if the init() method has already been called.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Attaches a rendering processor. Rendering processors are executed in the order which they were added.
     *
     * @param processor - rendering processor to add
     */
    public void addRenderingProcessor(RenderingProcessor processor) {
        if (initialized) {
            throw new EngineException("Cannot add a rendering processor after initialization.");
        }

        if (processors.contains(processor)) {
            throw new EngineException("Rendering processor already exists.");
        }

        processors.add(processor);
        processor.addNotify(this);
    }

    /**
     * Gives the rendering processor at the given index.
     *
     * @param index - rendering processor position in the list
     *
     * @return rendering processor at the given index
     */
    public RenderingProcessor getRenderingProcessor(int index) {
        return processors.get(index);
    }

    /**
     * Gives the number of attached rendering processors.
     *
     * @return number rendering processors
     */
    public int numRenderingProcessors() {
        return processors.size();
    }

    /**
     * Sets this rendering module's enabled state.
     *
     * @param enabled - if true, this rendering module is executed once per frame
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gives this rendering module's enabled state.
     *
     * @return true if this rendering module is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }



    /**
     * Iterates through all processors and calls their build method.
     */
    protected void buildProcessors() {
        for (RenderingProcessor processor : processors) {
            processor.build();
        }
    }

    /**
     * Iterates through all processors and calls their init method.
     */
    protected void initProcessors() {
        for (RenderingProcessor processor : processors) {
            processor.init();
        }
    }

    /**
     * Iterates through all processors and calls their process method.
     */
    protected void runProcessors() {
        for (RenderingProcessor processor : processors) {
            if (processor.isEnabled()) {
                processor.render();
            }
        }
    }

    /**
     * Iterates through all processors and calls their clean method.
     */
    protected void cleanProcessors() {
        for (RenderingProcessor processor : processors) {
            if (processor.isEnabled()) {
                processor.clean();
            }
        }
    }

    /**
     * Called by the renderer before compiling the shader. This is where shader initialization takes place.
     */
    protected abstract void build();

    /**
     * Called by the renderer before entering the rendering loop.
     */
    protected abstract void init();

    /**
     * Called by the renderer every frame.
     */
    protected abstract void render();

    /**
     * Called by the renderer after all rendering modules have executed.
     */
    protected abstract void clean();

    /**
     * Called by the renderer when this rendering module is added to it.
     *
     * @param renderer - owner renderer
     */
    protected void addNotify(Renderer renderer) {
        if (this.renderer != null) {
            throw new EngineException("This rendering module is already owned by another renderer.");
        }

        this.renderer = renderer;
    }
}
