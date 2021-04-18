package module.shape;

import core.RenderingProcessor;
import core.Shape;

public abstract class ShapeProcessor extends RenderingProcessor {
    public abstract void apply(Shape shape);
}
