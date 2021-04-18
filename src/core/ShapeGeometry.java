package core;

import core.buffer.Vector3Buffer;
import core.math.Vector2;
import core.math.Vector3;
import core.shader.Shader;
import core.utility.Buffers;
import core.utility.Pools;
import core.animation.Animation;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

/**
 * A geometry augmented with data necessary for lighting and animation.
 *
 * @author John Paul Quijano
 */
public class ShapeGeometry extends Geometry {
    public enum VertexAttribute {
        NORMAL(3, Shader.Type.VEC3),
        TANGENT(4, Shader.Type.VEC3),
        JOINT(5, Shader.Type.IVEC4),
        WEIGHT(6, Shader.Type.VEC4);

        private int identifier;
        private Shader.Type type;

        VertexAttribute(int loc, Shader.Type type) {
            this.identifier = loc;
            this.type = type;
        }

        public int getIdentifier() {
            return identifier;
        }

        public Shader.Type getType() {
            return type;
        }
    }

    public static final int JOINTS_PER_VERTEX = 4;

    protected int numJoints;
    protected int normalBuffer;
    protected int tangentBuffer;
    protected int jointBuffer;
    protected int weightBuffer;
    protected boolean normalDirty;
    protected boolean tangentDirty;
    protected boolean jointDirty;
    protected boolean weightDirty;
    protected boolean normalEnabled;
    protected boolean tangentEnabled;
    protected boolean normalEnabledDirty;
    protected boolean tangentEnabledDirty;
    protected boolean jointEnabledDirty;
    protected boolean jointEnabled;
    protected IntBuffer joints;
    protected IntBuffer jointsReadOnly;
    protected FloatBuffer weights;
    protected FloatBuffer weightsReadOnly;
    protected Vector3Buffer normals;
    protected Vector3Buffer tangents;
    protected Animation animation;

    /**
     * Creates an empty shape geometry.
     *
     * @param type - geometry type, either one of TYPE_LINES, TYPE_TRIS, or TYPE_QUADS
     * @param numCoords - number of coordinates
     * @param numIndices - number of indices
     */
    public ShapeGeometry(Type type, int numCoords, int numIndices) {
        super(type, numCoords, numIndices);

        numJoints = numCoords * JOINTS_PER_VERTEX;

        joints = Buffers.createIntBuffer(numCoords * JOINTS_PER_VERTEX);
        jointsReadOnly = joints.asReadOnlyBuffer();

        weights = Buffers.createFloatBuffer(numCoords * JOINTS_PER_VERTEX);
        weightsReadOnly = weights.asReadOnlyBuffer();

        normals = new Vector3Buffer(numCoords);
        tangents = new Vector3Buffer(numCoords);
    }

    /**
     * Creates a shape geometry based on the given template.
     *
     * @param template - geometry to copy attributes from
     */
    public ShapeGeometry(Geometry template) {
        super(template);

        numJoints = numCoords * JOINTS_PER_VERTEX;

        joints = Buffers.createIntBuffer(numCoords * JOINTS_PER_VERTEX);
        jointsReadOnly = joints.asReadOnlyBuffer();

        weights = Buffers.createFloatBuffer(numCoords * JOINTS_PER_VERTEX);
        weightsReadOnly = weights.asReadOnlyBuffer();

        normals = new Vector3Buffer(numCoords);
        tangents = new Vector3Buffer(numCoords);
    }

    /**
     * Creates a shape geometry based on the given template.
     *
     * @param template - geometry to copy attributes from
     */
    public ShapeGeometry(ShapeGeometry template) {
        super(template);

        joints = Buffers.createIntBuffer(template.numCoords * JOINTS_PER_VERTEX);
        jointsReadOnly = joints.asReadOnlyBuffer();

        weights = Buffers.createFloatBuffer(template.numCoords * JOINTS_PER_VERTEX);
        weightsReadOnly = weights.asReadOnlyBuffer();

        normals = new Vector3Buffer(template.numCoords);
        tangents = new Vector3Buffer(template.numCoords);

        numJoints = template.numJoints;
        animation = template.animation;

        jointEnabled = template.jointEnabled;
        normalEnabled = template.normalEnabled;
        tangentEnabled = template.tangentEnabled;

        joints.put(template.joints).flip();
        template.joints.flip();

        weights.put(template.weights).flip();
        template.weights.flip();

        normals.set(template.normals);
        tangents.set(template.tangents);
    }

    /**
     * Sets all attributes of this geometry to the given template.
     *
     * @param template - geometry to copy attributes from
     *
     * @return this geometry
     */
    public ShapeGeometry set(ShapeGeometry template) {
        super.set(template);

        if (numCoords != template.numCoords) {
            joints = Buffers.createIntBuffer(template.numCoords * JOINTS_PER_VERTEX);
            jointsReadOnly = joints.asReadOnlyBuffer();

            weights = Buffers.createFloatBuffer(template.numCoords * JOINTS_PER_VERTEX);
            weightsReadOnly = weights.asReadOnlyBuffer();

            normals = new Vector3Buffer(template.numCoords);
            tangents = new Vector3Buffer(template.numCoords);
        }

        numJoints = template.numJoints;
        animation = template.animation;

        setJointEnabled(template.jointEnabled);
        setNormalEnabled(template.normalEnabled);
        setTangentEnabled(template.tangentEnabled);

        setJoints(template.joints);
        setWeights(template.weights);
        setNormals(template.normals);
        setTangents(template.tangents);
        setAnimation(template.animation);

        return this;
    }

    /**
     * Sets the normal at the given buffer index.
     *
     * @param i buffer index
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public void setNormal(int i, float x, float y, float z) {
        normals.set(i, x, y, z);
        normalDirty = true;
        dirty = true;
    }

    /**
     * Sets the normal at the given buffer index.
     *
     * @param i buffer index
     * @param normal - three-element vector normal
     */
    public void setNormal(int i, Vector3 normal) {
        setNormal(i, normal.getX(), normal.getY(), normal.getZ());
    }

    /**
     * Copies the contents of the given array to this geometry's normal buffer.
     *
     * @param normals - array of three-element vector normals
     */
    public void setNormals(Vector3[] normals) {
        this.normals.set(normals);
        normalDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given buffer to this geometry's normal buffer.
     *
     * @param normals - buffer of three-element vector normals
     */
    public void setNormals(Vector3Buffer normals) {
        this.normals.set(normals);
        normalDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given list to this geometry's normal buffer.
     *
     * @param normals - list of three-element vector normals
     */
    public void setNormals(List<Vector3> normals) {
        this.normals.set(normals);
        normalDirty = true;
        dirty = true;
    }

    /**
     * Stores the normal at the given index into the given output vector.
     *
     * @param index - buffer index
     * @param output - output vector
     *
     * @return the output vector
     */
    public Vector3 getNormal(int index, Vector3 output) {
        return normals.get(index, output);
    }

    /**
     * Gives the vector buffer containing the normals.
     *
     * @return the vector buffer containing the normals
     */
    public Vector3Buffer getNormals() {
        return normals;
    }

    /**
     * Gives the float buffer containing the normals.
     *
     * @return the float buffer containing the normals
     */
    public FloatBuffer getNormalBuffer() {
        return normals.toFloatBuffer();
    }

    /**
     * If set, normal attribute is used.
     *
     * @param enabled - normal attribute state
     */
    public void setNormalEnabled(boolean enabled) {
        normalEnabled = enabled;
        normalEnabledDirty = true;
        dirty = true;
    }

    /**
     * Gives the state of the normal attribute.
     *
     * @return normal attribute state
     */
    public boolean isNormalEnabled() {
        return normalEnabled;
    }

    /**
     * Gives the normal enabled dirty flag.
     *
     * @return true if this geometry's normal enabled state has been modified since the last frame
     */
    public boolean isNormalEnabledDirty() {
        return normalEnabledDirty;
    }

    /**
     * Gives the normal dirty flag.
     *
     * @return true if this geometry's normals have been modified since the last frame
     */
    public boolean isNormalDirty() {
        return normalDirty;
    }

    /**
     * Sets the tangent at the given buffer index.
     *
     * @param i buffer index
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public void setTangent(int i, float x, float y, float z) {
        tangents.set(i, x, y, z);
        tangentDirty = true;
        dirty = true;
    }

    /**
     * Sets the tangent at the given buffer index.
     *
     * @param i buffer index
     * @param tangent - three-element vector normal
     */
    public void setTangent(int i, Vector3 tangent) {
        setTangent(i, tangent.getX(), tangent.getY(), tangent.getZ());
    }

    /**
     * Copies the contents of the given array to this geometry's tangent buffer.
     *
     * @param tangents - array of three-element vector tangents
     */
    public void setTangents(Vector3[] tangents) {
        this.tangents.set(tangents);
        tangentDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given buffer to this geometry's tangent buffer.
     *
     * @param tangents - buffer of three-element vector tangents
     */
    public void setTangents(Vector3Buffer tangents) {
        this.tangents.set(tangents);
        tangentDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given list to this geometry's tangent buffer.
     *
     * @param tangents - list of three-element vector tangents
     */
    public void setTangents(List<Vector3> tangents) {
        this.tangents.set(tangents);
        tangentDirty = true;
        dirty = true;
    }

    /**
     * Stores the tangent at the given index into the given output vector.
     *
     * @param index - buffer index
     * @param output - output vector
     *
     * @return the output vector
     */
    public Vector3 getTangent(int index, Vector3 output) {
        return tangents.get(index, output);
    }

    /**
     * Gives the vector buffer containing the tangents.
     *
     * @return the vector buffer containing the tangents
     */
    public Vector3Buffer getTangents() {
        return tangents;
    }

    /**
     * Gives the float buffer containing the tangents.
     *
     * @return the float buffer containing the tangents
     */
    public FloatBuffer getTangentBuffer() {
        return tangents.toFloatBuffer();
    }

    /**
     * If set, tangent attribute is used.
     *
     * @param enabled - tangent attribute state
     */
    public void setTangentEnabled(boolean enabled) {
        tangentEnabled = enabled;
        tangentEnabledDirty = true;
        dirty = true;
    }

    /**
     * Gives the state of the tangent attribute.
     *
     * @return tangent attribute state
     */
    public boolean isTangentEnabled() {
        return tangentEnabled;
    }

    /**
     * Gives the tangent enabled enabled dirty flag.
     *
     * @return true if this geometry's tangent enabled state has been modified since the last frame
     */
    public boolean isTangentEnabledDirty() {
        return tangentEnabledDirty;
    }

    /**
     * Gives the tangent dirty flag.
     *
     * @return true if this geometry's tangent have been modified since the last frame
     */
    public boolean isTangentDirty() {
        return tangentDirty;
    }

    /**
     * Sets the joint index values at the given buffer index.
     *
     * @param index coordinate index
     * @param values input values
     */
    public void setJoints(int index, int[] values) {
        int start = index * 4;

        for (int i = 0; i < values.length; i++) {
            joints.put(start + i, values[i]);
        }

        jointDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given array to this geometry's joint index buffer.
     *
     * @param joints - array of joint indices
     */
    public void setJoints(int[] joints) {
        this.joints.clear();
        this.joints.put(joints);
        this.joints.flip();

        jointDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given buffer to this geometry's joint index buffer.
     *
     * @param joints - buffer of joint indices
     */
    public void setJoints(IntBuffer joints) {
        joints.clear();
        this.joints.clear();
        this.joints.put(joints);
        this.joints.flip();
        joints.flip();

        jointDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given list to this geometry's joint index buffer.
     *
     * @param joints - list of joint indices
     */
    public void setJoints(List<Integer> joints) {
        this.joints.clear();

        for (int i = 0; i < joints.size(); i++) {
            this.joints.put(i, joints.get(i));
        }

        jointDirty = true;
        dirty = true;
    }

    /**
     * Gives the joint index at the given component index for the coordinate
     * at the given coordinate index.
     *
     * @param coordIndex - coordinate index
     * @param compIndex - component index
     */
    public int getJoint(int coordIndex, int compIndex) {
        return joints.get(coordIndex * JOINTS_PER_VERTEX + compIndex);
    }

    /**
     * Gives the immutable buffer containing this geometry's joint indices.
     *
     * @return immutable buffer containing this geometry's joint indices
     */
    public IntBuffer getJointBuffer() {
        return jointsReadOnly;
    }

    /**
     * Sets the weight values at the given buffer index.
     *
     * @param index coordinate index
     * @param values input values
     */
    public void setWeights(int index, float[] values) {
        int start = index * 4;

        for (int i = 0; i < values.length; i++) {
            weights.put(start + i, values[i]);
        }

        weightDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given array to this geometry's weights buffer.
     *
     * @param weights - array of vertex weights
     */
    public void setWeights(float[] weights) {
        this.weights.clear();
        this.weights.put(weights);
        this.weights.flip();

        weightDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given buffer to this geometry's weights buffer.
     *
     * @param weights - buffer of vertex weights
     */
    public void setWeights(FloatBuffer weights) {
        weights.clear();
        this.weights.clear();
        this.weights.put(weights);
        this.weights.flip();
        weights.flip();

        weightDirty = true;
        dirty = true;
    }

    /**
     * Copies the contents of the given list to this geometry's weights buffer.
     *
     * @param weights - list of vertex weights
     */
    public void setWeights(List<Float> weights) {
        this.weights.clear();

        for (int i = 0; i < weights.size(); i++) {
            this.weights.put(i, weights.get(i));
        }

        weightDirty = true;
        dirty = true;
    }

    /**
     * Gives the weight at the given component index for the coordinate at the given coordinate index.
     *
     * @param coordIndex - coordinate index
     * @param compIndex - component index
     */
    public float getWeights(int coordIndex, int compIndex) {
        return weights.get(coordIndex * JOINTS_PER_VERTEX + compIndex);
    }

    /**
     * Gives the immutable buffer containing this geometry's weights.
     */
    public FloatBuffer getWeightsBuffer() {
        return weightsReadOnly;
    }

    /**
     * Gives the number of skeletal animation joints.
     *
     * @return number of skeletal animation joints
     */
    public int numJoints() {
        return numJoints;
    }

    /**
     * If set, joint and weight attributes are used.
     *
     * @param enabled - joint and weight attribute state
     */
    public void setJointEnabled(boolean enabled) {
        jointEnabled = enabled;
        jointEnabledDirty = true;
        dirty = true;
    }

    /**
     * Gives the state of the joint and weight attributes.
     *
     * @return joint and weight attributes state
     */
    public boolean isJointEnabled() {
        return jointEnabled;
    }

    /**
     * Gives the joint enabled enabled dirty flag.
     *
     * @return true if this geometry's joint enabled state has been modified since the last frame
     */
    public boolean isJointEnabledDirty() {
        return jointEnabledDirty;
    }

    /**
     * Gives the joint dirty flag.
     *
     * @return true if this geometry's joints have been modified since the last frame
     */
    public boolean isJointDirty() {
        return jointDirty;
    }

    /**
     * Gives the weight dirty flag.
     *
     * @return true if this geometry's joints have been modified since the last frame
     */
    public boolean isWeightDirty() {
        return weightDirty;
    }

    /**
     * Sets this geometry's skeletal animation.
     *
     * @param animation - animation containing keyframes
     */
    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    /**
     * Gives this geometry's animation object.
     *
     * @return this geometry's animation object
     */
    public Animation getAnimation() {
        return animation;
    }

    /**
     * Calculates a normal for each vertex.
     */
    public void generateNormals() {
        generateNormals(this);
    }

    /**
     * Calculates tangent vector for each vertex.
     *
     * NOTE: Texture coordinates must be properly set before calling this method. Tangent calculations use texture
     * coordinates to properly orient them with the normal vector.
     */
    public void generateTangents() {
        generateTangents(this);
    }

    @Override
    public void clean() {
        super.clean();

        jointDirty = false;
        weightDirty = false;
        normalDirty = false;
        tangentDirty = false;
        normalEnabledDirty = false;
        jointEnabledDirty = false;
        tangentEnabledDirty = false;
    }

    @Override
    public void update() {
        super.update();

        if (normalDirty) {
            GL.updateVertexBuffer(normalBuffer, normals.toFloatBuffer());
        }

        if (tangentDirty) {
            GL.updateVertexBuffer(tangentBuffer, tangents.toFloatBuffer());
        }

        if (jointDirty) {
            GL.updateVertexBuffer(jointBuffer, joints);
        }

        if (weightDirty) {
            GL.updateVertexBuffer(weightBuffer, weights);
        }

        if (normalEnabledDirty) {
            GL.setAttributeEnabled(VertexAttribute.NORMAL.getIdentifier(), normalEnabled);
        }

        if (tangentEnabledDirty) {
            GL.setAttributeEnabled(VertexAttribute.TANGENT.getIdentifier(), tangentEnabled);
        }

        if (jointEnabledDirty) {
            GL.setAttributeEnabled(VertexAttribute.JOINT.getIdentifier(), jointEnabled);
            GL.setAttributeEnabled(VertexAttribute.WEIGHT.getIdentifier(), jointEnabled);
        }
    }

    @Override
    protected void destroy() {
        super.destroy();

        GL.freeBuffer(normalBuffer);
        GL.freeBuffer(tangentBuffer);
        GL.freeBuffer(jointBuffer);
        GL.freeBuffer(weightBuffer);

        jointBuffer = 0;
        weightBuffer = 0;
        normalBuffer = 0;
        tangentBuffer = 0;

        clean();
    }

    @Override
    protected void build() {
        super.build();

        normalBuffer = GL.createVertexBuffer();
        tangentBuffer = GL.createVertexBuffer();
        jointBuffer = GL.createVertexBuffer();
        weightBuffer = GL.createVertexBuffer();

        GL.fillVertexBuffer(normalBuffer, 3, VertexAttribute.NORMAL.getIdentifier(), normals.toFloatBuffer());
        GL.fillVertexBuffer(tangentBuffer, 3, VertexAttribute.TANGENT.getIdentifier(), tangents.toFloatBuffer());
        GL.fillVertexBuffer(jointBuffer, JOINTS_PER_VERTEX, VertexAttribute.JOINT.getIdentifier(), joints);
        GL.fillVertexBuffer(weightBuffer, JOINTS_PER_VERTEX, VertexAttribute.WEIGHT.getIdentifier(), weights);

        if (normalEnabled) {
            GL.setAttributeEnabled(VertexAttribute.NORMAL.getIdentifier(), true);
        }

        if (tangentEnabled) {
            GL.setAttributeEnabled(VertexAttribute.TANGENT.getIdentifier(), true);
        }

        if (jointEnabled) {
            GL.setAttributeEnabled(VertexAttribute.JOINT.getIdentifier(), true);
            GL.setAttributeEnabled(VertexAttribute.WEIGHT.getIdentifier(), true);
        }

        clean();
    }

    /**
     * Moves vertices along the normal.
     *
     * @param offset - scaling factor
     * @param input - geometry to inflate
     * @param output - storage object for the output
     */
    public static ShapeGeometry inflate(float offset, ShapeGeometry input, ShapeGeometry output) {
        if (output == null) {
            output = new ShapeGeometry(input);
        }

        Vector3 temp = Pools.Vector3.get();
        Vector3 coord = Pools.Vector3.get();

        for (int i = 0; i < input.numCoordinates(); i++) {
            input.getNormal(i, temp);
            input.getCoordinate(i, coord);

            temp.multiply(offset).add(coord);

            output.setCoordinate(i, temp);
        }

        Pools.Vector3.put(coord);
        Pools.Vector3.put(temp);

        return output;
    }

    /**
     * Calculates a normal vector for the triangle defined by the given vertices.
     *
     * @param vertex0 - first triangle vertex
     * @param vertex1 - second triangle vertex
     * @param vertex2 - third triangle vertex
     * @param store - storage object for the calculated normal
     *
     * @return calculated normal vector
     */
    public static Vector3 calculateNormal(Vector3 vertex0, Vector3 vertex1, Vector3 vertex2, Vector3 store) {
        Vector3 normal = store;

        if (normal == null) {
            normal = new Vector3();
        }

        normal.set(vertex1).subtract(vertex0).cross(vertex2.getX() - vertex0.getX(), vertex2.getY() - vertex0.getY(), vertex2.getZ() - vertex0.getZ());
        normal.normalize();

        return normal;
    }

    /**
     * Calculates the normal vector for each vertex in the given geometry. The normal vector is averaged for vertices
     * that are being shared by multiple triangles.
     *
     * @param geom - geometry for which to compute surface normals
     */
    public static void generateNormals(ShapeGeometry geom) {
        int index;
        int adjacent;
        Vector3 coord0 = Pools.Vector3.get();
        Vector3 coord1 = Pools.Vector3.get();
        Vector3 coord2 = Pools.Vector3.get();
        Vector3 normal = Pools.Vector3.get();
        Vector3 totalNormal = Pools.Vector3.get();

        for (int i = 0; i < geom.numCoordinates(); i++) {
            index = 0;
            adjacent = 0;
            totalNormal.set(Vector3.ZERO);

            /**
             * Find all faces sharing this vertex.
             * Calculate normal for each then add to the total.
             */
            while (index < geom.numIndices()) {
                int index0 = geom.getIndex(index++);
                int index1 = geom.getIndex(index++);
                int index2 = geom.getIndex(index++);

                if (index0 == i || index1 == i || index2 == i) {
                    geom.getCoordinate(index0, coord0);
                    geom.getCoordinate(index1, coord1);
                    geom.getCoordinate(index2, coord2);

                    calculateNormal(coord0, coord1, coord2, normal);

                    totalNormal.add(normal);
                    adjacent++;
                }
            }

            /**
             * Average result.
             */
            totalNormal.divide(adjacent).normalize();
            geom.setNormal(i, totalNormal);
        }

        Pools.Vector3.put(coord0);
        Pools.Vector3.put(coord1);
        Pools.Vector3.put(coord2);
        Pools.Vector3.put(normal);
        Pools.Vector3.put(totalNormal);
    }

    /**
     * Calculates the tangent and bi-tangent vectors for each vertex in the given geometry. The resulting vectors are
     * averaged for vertices that are being shared by multiple triangles.
     *
     * @param geom - geometry for which to compute tangent and bi-tangent vectors
     */
    public static void generateTangents(ShapeGeometry geom) {
        int index;
        int adjacent;
        Vector3 tangent = Pools.Vector3.get();
        Vector3 totalTangent = Pools.Vector3.get();
        Vector3 coord0 = Pools.Vector3.get();
        Vector3 coord1 = Pools.Vector3.get();
        Vector3 coord2 = Pools.Vector3.get();
        Vector3 deltaCoord0 = Pools.Vector3.get();
        Vector3 deltaCoord1 = Pools.Vector3.get();
        Vector2 texCoord0 = Pools.Vector2.get();
        Vector2 texCoord1 = Pools.Vector2.get();
        Vector2 texCoord2 = Pools.Vector2.get();
        Vector2 deltaTexCoord0 = Pools.Vector2.get();
        Vector2 deltaTexCoord1 = Pools.Vector2.get();

        for (int i = 0; i < geom.numCoordinates(); i++) {
            index = 0;
            adjacent = 0;
            totalTangent.set(Vector3.ZERO);

            /**
             * Find all faces sharing this vertex.
             * Calculate tangent and bi-tangent for each then add to the total.
             */
            while (index < geom.numIndices()) {
                int index0 = geom.getIndex(index++);
                int index1 = geom.getIndex(index++);
                int index2 = geom.getIndex(index++);

                if (index0 == i || index1 == i || index2 == i) {
                    geom.getCoordinate(index0, coord0);
                    geom.getCoordinate(index1, coord1);
                    geom.getCoordinate(index2, coord2);

                    geom.getTextureCoordinate(index0, texCoord0);
                    geom.getTextureCoordinate(index1, texCoord1);
                    geom.getTextureCoordinate(index2, texCoord2);

                    deltaCoord0.set(coord1).subtract(coord0);
                    deltaCoord1.set(coord2).subtract(coord0);

                    deltaTexCoord0.set(texCoord1).subtract(texCoord0);
                    deltaTexCoord1.set(texCoord2).subtract(texCoord0);

                    float r = 1f / (deltaTexCoord0.getX() * deltaTexCoord1.getY() - deltaTexCoord0.getY() * deltaTexCoord1.getX());

                    tangent.set(deltaCoord0.multiply(deltaTexCoord1.getY())).subtract(deltaCoord1.multiply(deltaTexCoord0.getY())).multiply(r);

                    totalTangent.add(tangent);
                    adjacent++;
                }
            }

            totalTangent.divide(adjacent).normalize();
            geom.setTangent(i, totalTangent);
        }

        Pools.Vector3.put(tangent);
        Pools.Vector3.put(totalTangent);
        Pools.Vector3.put(coord0);
        Pools.Vector3.put(coord1);
        Pools.Vector3.put(coord2);
        Pools.Vector3.put(deltaCoord0);
        Pools.Vector3.put(deltaCoord1);
        Pools.Vector2.put(texCoord0);
        Pools.Vector2.put(texCoord1);
        Pools.Vector2.put(texCoord2);
        Pools.Vector2.put(deltaTexCoord0);
        Pools.Vector2.put(deltaTexCoord1);
    }
}
