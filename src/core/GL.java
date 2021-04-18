package core;

import core.utility.Buffers;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Wrapper for native graphics API functions.
 *
 * @author John Paul Quijano
 */
public final class GL {
    public enum BlendFunction {
        ZERO(GL11.GL_ZERO),
        ONE(GL11.GL_ONE),
        SRC_COLOR(GL11.GL_SRC_COLOR),
        ONE_MINUS_SRC_COLOR(GL11.GL_ONE_MINUS_SRC_COLOR),
        DST_COLOR(GL11.GL_DST_COLOR),
        ONE_MINUS_DST_COLOR(GL11.GL_ONE_MINUS_DST_COLOR),
        SRC_ALPHA(GL11.GL_SRC_ALPHA),
        ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA),
        DST_ALPHA(GL11.GL_DST_ALPHA),
        ONE_MINUS_DST_ALPHA(GL11.GL_ONE_MINUS_DST_ALPHA),
        CONSTANT_COLOR(GL14.GL_CONSTANT_COLOR),
        ONE_MINUS_CONSTANT_COLOR(GL14.GL_ONE_MINUS_CONSTANT_COLOR),
        SRC_ALPHA_SATURATE(GL11.GL_SRC_ALPHA_SATURATE),
        SRC1_COLOR(GL33.GL_SRC1_COLOR),
        ONE_MINUS_SRC1_COLOR(GL33.GL_ONE_MINUS_SRC1_COLOR),
        SRC1_ALPHA(GL15.GL_SRC1_ALPHA),
        ONE_MINUS_SRC1_ALPHA(GL33.GL_ONE_MINUS_SRC1_ALPHA);

        private final int value;

        BlendFunction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Attachment {
        NONE(GL11.GL_NONE),
        COLOR_0(GL30.GL_COLOR_ATTACHMENT0),
        COLOR_1(GL30.GL_COLOR_ATTACHMENT1),
        COLOR_2(GL30.GL_COLOR_ATTACHMENT2),
        COLOR_3(GL30.GL_COLOR_ATTACHMENT3),
        COLOR_4(GL30.GL_COLOR_ATTACHMENT4),
        COLOR_5(GL30.GL_COLOR_ATTACHMENT5),
        COLOR_6(GL30.GL_COLOR_ATTACHMENT6),
        COLOR_7(GL30.GL_COLOR_ATTACHMENT7);

        private final int value;

        Attachment(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Buffer {
        COLOR(GL11.GL_COLOR_BUFFER_BIT),
        DEPTH(GL11.GL_DEPTH_BUFFER_BIT),
        STENCIL(GL11.GL_STENCIL_BUFFER_BIT);

        private final int value;

        Buffer(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum PolygonMode {
        FILL(GL11.GL_FILL),
        LINE(GL11.GL_LINE),
        POINT(GL11.GL_POINT);

        private final int value;

        PolygonMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum CullFace {
        FRONT(GL11.GL_FRONT),
        BACK(GL11.GL_BACK);

        private final int value;

        CullFace(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum WrapMode {
        REPEAT(GL11.GL_REPEAT),
        MIRRORED_REPEAT(GL14.GL_MIRRORED_REPEAT),
        CLAMP_TO_EDGE(GL12.GL_CLAMP_TO_EDGE),
        MIRRORED_CLAMP_TO_EDGE(GL44.GL_MIRROR_CLAMP_TO_EDGE);

        private final int value;

        WrapMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Filter {
        NEAREST(GL11.GL_NEAREST),
        LINEAR(GL11.GL_LINEAR),
        NEAREST_MIPMAP_NEAREST(GL11.GL_NEAREST_MIPMAP_NEAREST),
        NEAREST_MIPMAP_LINEAR(GL11.GL_NEAREST_MIPMAP_LINEAR),
        LINEAR_MIPMAP_NEAREST(GL11.GL_LINEAR_MIPMAP_NEAREST),
        LINEAR_MIPMAP_LINEAR(GL11.GL_LINEAR_MIPMAP_LINEAR);

        private final int value;

        Filter(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Error {
        OUT_OF_MEMORY(GL11.GL_OUT_OF_MEMORY, "Out of memory."),
        CONTEXT_LOST(GL45.GL_CONTEXT_LOST, "OpenGL context has been lost, due to a graphics card reset."),
        INVALID_ENUM(GL11.GL_INVALID_ENUM, "Enumeration parameter is not a legal enumeration for that function."),
        INVALID_VALUE(GL11.GL_INVALID_VALUE, "Value parameter is not a legal value for that function."),
        INVALID_OPERATION(GL11.GL_INVALID_OPERATION, "Set of state for a command is not legal for the parameters given to that command."),
        INVALID_FRAMEBUFFER_OPERATION(GL30.GL_INVALID_FRAMEBUFFER_OPERATION, "Invalid framebuffer operation.");

        private static final HashMap<Integer, Error> VALUE_ENUM_MAP = new HashMap<>();

        static {
            for (Error error : Error.values()) {
                VALUE_ENUM_MAP.put(error.getValue(), error);
            }
        }

        private final int value;
        private final String description;

        Error(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static Error getError(int value) {
            return VALUE_ENUM_MAP.get(value);
        }
    }

    public enum FrameBufferStatus {
        UNDEFINED(GL30.GL_FRAMEBUFFER_UNDEFINED, "Default framebuffer does not exist."),
        UNSUPPORTED(GL30.GL_FRAMEBUFFER_UNSUPPORTED, "Combination of internal formats of the attached images violates an implementation-dependent set of restrictions."),
        INCOMPLETE_ATTACHMENT(GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT, "Framebuffer attachment points are incomplete."),
        INCOMPLETE_MISSING_ATTACHMENT(GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT, "Framebuffer does not have at least one image attached."),
        INCOMPLETE_DRAW_BUFFER(GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER, "Value of attachment object is GL_NONE for any color attachment point named by GL_DRAW_BUFFERi."),
        INCOMPLETE_READ_BUFFER(GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER, "Value of attachment object is GL_NONE for the color attachment point named by GL_READ_BUFFER."),
        INCOMPLETE_MULTISAMPLE(GL30.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE, "Value of GL_RENDERBUFFER_SAMPLES is not the same for all attached renderbuffers."),
        INCOMPLETE_LAYER_TARGETS(GL32.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS, "Value of GL_TEXTURE_FIXED_SAMPLE_LOCATIONS is not the same for all attached textures.");

        private static final HashMap<Integer, FrameBufferStatus> VALUE_ENUM_MAP = new HashMap<>();

        static {
            for (FrameBufferStatus status : FrameBufferStatus.values()) {
                VALUE_ENUM_MAP.put(status.getValue(), status);
            }
        }

        private final int value;
        private final String description;

        FrameBufferStatus(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static FrameBufferStatus getStatus(int value) {
            return VALUE_ENUM_MAP.get(value);
        }
    }



    public static final int DEFAULT_VERTEX_ARRAY = 0;
    public static final int DEFAULT_READ_FRAMEBUFFER = 0;
    public static final int DEFAULT_WRITE_FRAMEBUFFER = 0;
    public static final int DEFAULT_TEXTURE2D_ID = 0;
    public static final int DEFAULT_TEXTURE2D_UNIT = GL13.GL_TEXTURE0;
    public static final int DEFAULT_TEXTUREARRAY_ID = 0;
    public static final int DEFAULT_TEXTUREARRAY_UNIT = GL13.GL_TEXTURE0;
    public static final int DEFAULT_TEXTURECUBE_ID = 0;
    public static final int DEFAULT_TEXTURECUBE_UNIT = GL13.GL_TEXTURE0;
    public static final int DEFAULT_VIEWPORT_X = 0;
    public static final int DEFAULT_VIEWPORT_Y = 0;
    public static final int DEFAULT_VIEWPORT_WIDTH = 512;
    public static final int DEFAULT_VIEWPORT_HEIGHT = 512;
    public static final float DEFAULT_LINE_WIDTH = 1f;
    public static final float DEFAULT_POINT_SIZE = 1f;
    public static final float DEFAULT_CLEAR_COLOR_R = 0.2f;
    public static final float DEFAULT_CLEAR_COLOR_G = 0.2f;
    public static final float DEFAULT_CLEAR_COLOR_B = 0.2f;
    public static final float DEFAULT_CLEAR_COLOR_A = 1f;
    public static final float DEFAULT_POLYGON_OFFSET_FACTOR = 0f;
    public static final float DEFAULT_POLYGON_OFFSET_UNITS = 0f;
    public static final boolean DEFAULT_FACE_CULLING_STATE = true;
    public static final boolean DEFAULT_BLEND_STATE = false;
    public static final boolean DEFAULT_DEPTH_TEST_STATE = true;
    public static final boolean DEFAULT_DEPTH_CLAMP_STATE = true;
    public static final boolean DEFAULT_DEPTH_WRITE_STATE = true;
    public static final boolean DEFAULT_COLOR_WRITE_STATE = true;
    public static final boolean DEFAULT_SEAMLESS_CUBE_TEXTURE_STATE = true;
    public static final boolean DEFAULT_POLYGON_OFFSET_FILL_STATE = false;
    public static final boolean DEFAULT_POLYGON_OFFSET_LINE_STATE = false;
    public static final boolean DEFAULT_POLYGON_OFFSET_POINT_STATE = false;
    public static final CullFace DEFAULT_CULL_FACE = CullFace.BACK;
    public static final PolygonMode DEFAULT_POLYGON_MODE = PolygonMode.FILL;

    private static int uniformBufferBindingPoint = 1;
    private static int fbReadState;
    private static int fbWriteState;
    private static int vertexArrayState;
    private static int viewportStateX;
    private static int viewportStateY;
    private static int viewportStateWidth;
    private static int viewportStateHeight;
    private static float lineWidthState;
    private static float pointSizeState;
    private static float clearColorStateR;
    private static float clearColorStateG;
    private static float clearColorStateB;
    private static float clearColorStateA;
    private static float polygonOffsetFactor;
    private static float polygonOffsetUnits;
    private static boolean faceCullingState;
    private static boolean blendState;
    private static boolean depthTestState;
    private static boolean depthClampState;
    private static boolean depthWriteState;
    private static boolean colorWriteState;
    private static boolean polygonOffsetFillState;
    private static boolean polygonOffsetLineState;
    private static boolean polygonOffsetPointState;
    private static boolean seamlessCubeTextureState;
    private static CullFace cullFaceState;
    private static PolygonMode polygonModeState;
    private static List<Integer> vaos = new ArrayList<>();
    private static List<Integer> buffers = new ArrayList<>();
    private static List<Integer> queries = new ArrayList<>();
    private static List<Integer> shaders = new ArrayList<>();
    private static List<Integer> programs = new ArrayList<>();
    private static List<Integer> framebuffers = new ArrayList<>();
    private static List<Integer> renderbuffers = new ArrayList<>();
    private static List<Integer> textures = new ArrayList<>();

    private GL() {
    }

    /**
     * Checks if an error was thrown after the last function call.
     *
     * @return the last thrown error
     */
    public static Error getError() {
        return Error.getError(GL11.glGetError());
    }

    /**
     * Creates a shader program.
     *
     * @return shader program id
     */
    public static int createProgram() {
        int program = GL20.glCreateProgram();
        programs.add(program);
        return program;
    }

    /**
     * Activates the shader program with the given id.
     *
     * @param program - program id
     */
    public static void bindProgram(int program) {
        GL20.glUseProgram(program);
    }

    /**
     * Creates a vertex shader.
     *
     * @return shader id
     */
    public static int createVertexShader(String source) {
        int shader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);

        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        shaders.add(shader);

        return shader;
    }

    /**
     * Creates a fragment shader.
     *
     * @return shader id
     */
    public static int createFragmentShader(String source) {
        int shader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        shaders.add(shader);

        return shader;
    }

    /**
     * Attaches the given shader given program.
     *
     * @param program - program id
     * @param shader - shader id
     */
    public static void attachShader(int program, int shader) {
        GL20.glAttachShader(program, shader);
    }

    /**
     * Links the given program.
     *
     * @param program - program id
     */
    public static void linkProgram(int program) {
        GL20.glLinkProgram(program);
    }

    /**
     * Validates the given program.
     *
     * @param program - program id
     *
     * @return true if program is valid
     */
    public static boolean validateProgram(int program) {
        GL20.glValidateProgram(program);
        return GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_TRUE;
    }

    /**
     * Gives information about the given program.
     *
     * @param program - program id
     *
     * @return information about the given program
     */
    public static String getProgramInfo(int program) {
        return GL20.glGetProgramInfoLog(program);
    }

    /**
     * Gives information about the given program.
     *
     * @param shader - shader id
     *
     * @return information about the given shader
     */
    public static String getShaderInfo(int shader) {
        return GL20.glGetShaderInfoLog(shader);
    }

    /**
     * Sets the source code of the given shader.
     *
     * @param shader - shader id
     * @param source - source code
     */
    public static void setShaderSource(int shader, String source) {
        GL20.glShaderSource(shader, source);
    }

    /**
     * Gives source code of the given shader.
     *
     * @param shader - shader id
     *
     * @return source code of the given shader
     */
    public static String getShaderSource(int shader) {
        return GL20.glGetShaderSource(shader);
    }

    /**
     * Gives a uniform qualified variable's integer identifier.
     *
     * @param program - program id
     * @param identifier - uniform's identifier
     *
     * @return uniform qualified variable's id
     */
    public static int getUniformLocation(int program, String identifier) {
        return GL20.glGetUniformLocation(program, identifier);
    }

    /**
     * Creates a uniform buffer object.
     *
     * @param program - shader program
     * @param capacity - total numCached of the buffer
     * @param name - string identifier
     *
     * @return uniform buffer object id
     */
    public static int createUniformBuffer(int program, long capacity, String name) {
        int point = uniformBufferBindingPoint++;
        int buffer = GL15.glGenBuffers();
        int index = GL31.glGetUniformBlockIndex(program, name);

        GL31.glUniformBlockBinding(program, index, point);

        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, buffer);
        GL15.glBufferData(GL31.GL_UNIFORM_BUFFER, capacity, GL15.GL_DYNAMIC_DRAW);
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, point, buffer);

        buffers.add(buffer);

        return buffer;
    }

    /**
     * Updates the given uniform buffer with the given float data starting from the given index offset.
     *
     * @param buffer - buffer to update
     * @param offset - index offset
     * @param data - float data
     */
    public static void updateUniformBuffer(int buffer, long offset, FloatBuffer data) {
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, buffer);
        GL15.glBufferSubData(GL31.GL_UNIFORM_BUFFER, offset * 4, data);
    }

    /**
     * Sets the shader variable's value at the given location to the given boolean value.
     *
     * @param loc - variable id
     * @param bool - boolean value
     */
    public static void setBoolean(int loc, boolean bool) {
        GL20.glUniform1i(loc, bool ? 1 : 0);
    }

    /**
     * Sets the shader variable's value at the given location to the given scalar value.
     *
     * @param loc - variable id
     * @param i - integer scalar value
     */
    public static void setScalar(int loc, int i) {
        GL20.glUniform1i(loc, i);
    }

    /**
     * Sets the shader variable's value at the given location to the given floating point scalar value.
     *
     * @param loc - variable id
     * @param f - scalar value
     */
    public static void setScalar(int loc, float f) {
        GL20.glUniform1f(loc, f);
    }

    /**
     * Sets the shader variable's value at the given location to the given two-component integer vector value.
     *
     * @param loc - variable id
     * @param x - first integer vector component
     * @param y - second integer vector component
     */
    public static void setVector2(int loc, int x, int y) {
        GL20.glUniform2i(loc, x, y);
    }

    /**
     * Sets the shader variable's value at the given location to the given two-component floating point vector value.
     *
     * @param loc - variable id
     * @param x - first floating point vector component
     * @param y - second floating point vector component
     */
    public static void setVector2(int loc, float x, float y) {
        GL20.glUniform2f(loc, x, y);
    }

    /**
     * Sets the shader variable's value at the given location to the given three-component integer vector value.
     *
     * @param loc - variable id
     * @param x - first integer vector component
     * @param y - second integer vector component
     * @param z - third integer vector component
     */
    public static void setVector3(int loc, int x, int y, int z) {
        GL20.glUniform3i(loc, x, y, z);
    }

    /**
     * Sets the shader variable's value at the given location to the given three-component floating point vector value.
     *
     * @param loc - variable id
     * @param x - first floating point vector component
     * @param y - second floating point vector component
     * @param z - third floating point vector component
     */
    public static void setVector3(int loc, float x, float y, float z) {
        GL20.glUniform3f(loc, x, y, z);
    }

    /**
     * Sets the shader variable's value at the given location to the given four-component integer vector value.
     *
     * @param loc - variable id
     * @param x - first integer vector component
     * @param y - second integer vector component
     * @param z - third integer vector component
     * @param w - fourth integer vector component
     */
    public static void setVector4(int loc, int x, int y, int z, int w) {
        GL20.glUniform4i(loc, x, y, z, w);
    }

    /**
     * Sets the shader variable's value at the given location to the given four-component floating point vector value.
     *
     * @param loc - variable id
     * @param x - first floating point vector component
     * @param y - second floating point vector component
     * @param z - third floating point vector component
     * @param w - fourth floating point vector component
     */
    public static void setVector4(int loc, float x, float y, float z, float w) {
        GL20.glUniform4f(loc, x, y, z, w);
    }

    /**
     * Sets the shader variable's value at the given location to the given buffer of integer values.
     *
     * @param loc - variable id
     * @param buffer - buffer containing the integer values
     */
    public static void setArray1(int loc, IntBuffer buffer) {
        GL20.glUniform1iv(loc, buffer);
    }

    /**
     * Sets the shader variable's value at the given location to the given buffer of floating point values.
     *
     * @param loc - variable id
     * @param buffer - buffer containing the floating point values
     */
    public static void setArray1(int loc, FloatBuffer buffer) {
        GL20.glUniform1fv(loc, buffer);
    }

    /**
     * Sets the shader variable's value at the given location to the given buffer of integer values.
     *
     * @param loc - variable id
     * @param buffer - buffer containing the integer values
     */
    public static void setArray2(int loc, IntBuffer buffer) {
        GL20.glUniform2iv(loc, buffer);
    }

    /**
     * Sets the shader variable's value at the given location to the given buffer of floating point values.
     *
     * @param loc - variable id
     * @param buffer - buffer containing the floating point values
     */
    public static void setArray2(int loc, FloatBuffer buffer) {
        GL20.glUniform2fv(loc, buffer);
    }

    /**
     * Sets the shader variable's value at the given location to the given buffer of integer values.
     *
     * @param loc - variable id
     * @param buffer - buffer containing the integer values
     */
    public static void setArray3(int loc, IntBuffer buffer) {
        GL20.glUniform3iv(loc, buffer);
    }

    /**
     * Sets the shader variable's value at the given location to the given buffer of floating point values.
     *
     * @param loc - variable id
     * @param buffer - buffer containing the floating point values
     */
    public static void setArray3(int loc, FloatBuffer buffer) {
        GL20.glUniform3fv(loc, buffer);
    }

    /**
     * Sets the shader variable's value at the given location to the given buffer of integer values.
     *
     * @param loc - variable id
     * @param buffer - buffer containing the integer values
     */
    public static void setArray4(int loc, IntBuffer buffer) {
        GL20.glUniform4iv(loc, buffer);
    }

    /**
     * Sets the shader variable's value at the given location to the given buffer of floating point values.
     *
     * @param loc - variable id
     * @param buffer - buffer containing the floating point values
     */
    public static void setArray4(int loc, FloatBuffer buffer) {
        GL20.glUniform4fv(loc, buffer);
    }

    /**
     * Sets the shader variable's value at the given location to the given buffer of floating point values.
     *
     * @param loc - variable id
     * @param buffer - buffer containing the floating point values
     */
    public static void setMatrix2(int loc, boolean transpose, FloatBuffer buffer) {
        GL20.glUniformMatrix2fv(loc, transpose, buffer);
    }

    /**
     * Sets the shader variable's value at the given location to the given buffer of floating point values.
     *
     * @param loc - variable id
     * @param buffer - buffer containing the floating point values
     */
    public static void setMatrix3(int loc, boolean transpose, FloatBuffer buffer) {
        GL20.glUniformMatrix3fv(loc, transpose, buffer);
    }

    /**
     * Sets the shader variable's value at the given location to the given buffer of floating point values.
     *
     * @param loc - variable id
     * @param buffer - buffer containing the floating point values
     */
    public static void setMatrix4(int loc, boolean transpose, FloatBuffer buffer) {
        GL20.glUniformMatrix4fv(loc, transpose, buffer);
    }

    /**
     * Sets the thickness of rendered lines when polygon mode is PolygonMode.LINE.
     *
     * @param width - line width
     */
    public static void setLineWidth(float width) {
        if (lineWidthState != width) {
            GL11.glLineWidth(width);
            lineWidthState = width;
        }
    }

    /**
     * Gives the line width (thickness).
     *
     * @return line width
     */
    public static float getLineWidth() {
        return lineWidthState;
    }

    /**
     * Sets the numCached of rendered points when polygon mode is PolygonMode.POINT.
     *
     * @param size - point numCached
     */
    public static void setPointSize(float size) {
        if (pointSizeState != size) {
            GL11.glPointSize(size);
            pointSizeState = size;
        }
    }

    /**
     * Gives the point numCached.
     *
     * @return point numCached
     */
    public static float getPointSize() {
        return pointSizeState;
    }

    /**
     * Sets whether polygons are rendered as filled, lines, or points.
     *
     * @param mode - polygon mode
     */
    public static void setPolygonMode(PolygonMode mode) {
        if (polygonModeState != mode) {
            GL11.glPolygonMode(GL11.GL_BACK, mode.value);
            GL11.glPolygonMode(GL11.GL_FRONT, mode.value);
            polygonModeState = mode;
        }
    }

    /**
     * Gives the polygon mode.
     *
     * @return polygon mode
     */
    public static PolygonMode getPolygonMode() {
        return polygonModeState;
    }

    /**
     * Sets whether back or front faces are culled.
     *
     * @param face - back or front faces
     */
    public static void setCullFace(CullFace face) {
        if (cullFaceState != face) {
            GL11.glCullFace(face.value);
            cullFaceState = face;
        }
    }

    /**
     * Gives the cull face state.
     *
     * @return cull face state
     */
    public static CullFace getCullFace() {
        return cullFaceState;
    }

    /**
     * Enables or disables face culling.
     *
     * @param enabled - if true, face culling is enabled
     */
    public static void setFaceCullingEnabled(boolean enabled) {
        if (faceCullingState != enabled) {
            if (enabled) {
                GL11.glEnable(GL11.GL_CULL_FACE);
            } else {
                GL11.glDisable(GL11.GL_CULL_FACE);
            }

            faceCullingState = enabled;
        }
    }

    /**
     * Checks if face culling is enabled.
     *
     * @return true if face culling is enabled
     */
    public static boolean isFaceCullingEnabled() {
        return faceCullingState;
    }

    /**
     * Enables or disables blending.
     *
     * @param enabled - if true, blending is enabled
     */
    public static void setBlendEnabled(boolean enabled) {
        if (blendState != enabled) {
            if (enabled) {
                GL11.glEnable(GL11.GL_BLEND);
            } else {
                GL11.glDisable(GL11.GL_BLEND);
            }

            blendState = enabled;
        }
    }

    /**
     * Checks if blending is enabled.
     *
     * @return true if blending is enabled
     */
    public static boolean isBlendEnabled() {
        return blendState;
    }

    /**
     * Enables or disables depth testing.
     *
     * @param enabled - if true, depth testing is enabled
     */
    public static void setDepthTestEnabled(boolean enabled) {
        if (depthTestState != enabled) {
            if (enabled) {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            } else {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }

            depthTestState = enabled;
        }
    }

    /**
     * Checks if depth testing is enabled.
     *
     * @return true if depth testing is enabled
     */
    public static boolean isDepthTestEnabled() {
        return depthTestState;
    }

    /**
     * Enables or disables depth clamp.
     *
     * @param enabled - if true, depth clamp is enabled
     */
    public static void setDepthClampEnabled(boolean enabled) {
        if (depthClampState != enabled) {
            if (enabled) {
                GL11.glEnable(GL32.GL_DEPTH_CLAMP);
            } else {
                GL11.glDisable(GL32.GL_DEPTH_CLAMP);
            }

            depthClampState = enabled;
        }
    }

    /**
     * Checks if depth clamp is enabled.
     *
     * @return true if depth clamp is enabled
     */
    public static boolean isDepthClampEnabled() {
        return depthClampState;
    }

    /**
     * Enables or disables depth write.
     *
     * @param enabled - if true, depth write is enabled
     */
    public static void setDepthWriteEnabled(boolean enabled) {
        if (depthWriteState != enabled) {
            GL11.glDepthMask(enabled);
            depthWriteState = enabled;
        }
    }

    /**
     * Checks if depth write is enabled.
     *
     * @return true if depth write is enabled
     */
    public static boolean isDepthWriteEnabled() {
        return depthWriteState;
    }

    /**
     * Enables or disables color write.
     *
     * @param enabled - if true, color write is enabled
     */
    public static void setColorWriteEnabled(boolean enabled) {
        if (colorWriteState != enabled) {
            GL11.glColorMask(enabled, enabled, enabled, enabled);
            colorWriteState = enabled;
        }
    }

    /**
     * Checks if color write is enabled.
     *
     * @return true if color write is enabled
     */
    public static boolean isColorWriteEnabled() {
        return colorWriteState;
    }

    /**
     * Enables or disables polygon offset for filled polygons.
     *
     * @param enabled - if true, polygon offset for filled polygons is enabled
     */
    public static void setPolygonOffsetFillEnabled(boolean enabled) {
        if (polygonOffsetFillState != enabled) {
            if (enabled) {
                GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            } else {
                GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            }

            polygonOffsetFillState = enabled;
        }
    }

    /**
     * Checks if polygon offset for filled polygons is enabled.
     *
     * @return true if polygon offset for filled polygons is enabled
     */
    public static boolean isPolygonOffsetFillEnabled() {
        return polygonOffsetFillState;
    }

    /**
     * Enables or disables polygon offset for line polygons.
     *
     * @param enabled - if true, polygon offset for line polygons is enabled
     */
    public static void setPolygonOffsetLineEnabled(boolean enabled) {
        if (polygonOffsetLineState != enabled) {
            if (enabled) {
                GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
            } else {
                GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE);
            }

            polygonOffsetLineState = enabled;
        }
    }

    /**
     * Checks if polygon offset for line polygons is enabled.
     *
     * @return true if polygon offset for line polygons is enabled
     */
    public static boolean isPolygonOffsetLineEnabled() {
        return polygonOffsetLineState;
    }

    /**
     * Enables or disables polygon offset for point polygons.
     *
     * @param enabled - if true, polygon offset for point polygons is enabled
     */
    public static void setPolygonOffsetPointEnabled(boolean enabled) {
        if (polygonOffsetPointState != enabled) {
            if (enabled) {
                GL11.glEnable(GL11.GL_POLYGON_OFFSET_POINT);
            } else {
                GL11.glDisable(GL11.GL_POLYGON_OFFSET_POINT);
            }

            polygonOffsetPointState = enabled;
        }
    }

    /**
     * Checks if polygon offset for point polygons is enabled.
     *
     * @return true if polygon offset for point polygons is enabled
     */
    public static boolean isPolygonOffsetPonitEnabled() {
        return polygonOffsetPointState;
    }

    /**
     * Enables or disables seamless cube texture.
     *
     * @param enabled - if true, cube textures are interpolated along seams
     */
    public static void setSeamlessCubeTextureEnabled(boolean enabled) {
        if (seamlessCubeTextureState != enabled) {
            if (enabled) {
                GL11.glEnable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
            } else {
                GL11.glDisable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
            }

            seamlessCubeTextureState = enabled;
        }
    }

    /**
     * Checks if seamless cube texture is enabled.
     *
     * @return true if seamless cube texture is enabled
     */
    public static boolean isSeamlessCubeTextureEnabled() {
        return seamlessCubeTextureState;
    }

    /**
     * Sets the color the screen is cleared to every frame.
     * @param r - red color component
     * @param g - green color component
     * @param b - blue color component
     * @param a - alpha color component
     */
    public static void setClearColor(float r, float g, float b, float a) {
        if (clearColorStateR != r || clearColorStateG != g || clearColorStateB != b || clearColorStateA != a) {
            clearColorStateR = r;
            clearColorStateG = g;
            clearColorStateB = b;
            clearColorStateA = a;
            GL11.glClearColor(r, g, b, a);
        }
    }

    /**
     * Returns the red clear color component.
     *
     * @return red clear color component
     */
    public static float getClearColorR() {
        return clearColorStateR;
    }

    /**
     * Returns the green clear color component.
     *
     * @return green clear color component
     */
    public static float getClearColorG() {
        return clearColorStateG;
    }

    /**
     * Returns the blue clear color component.
     *
     * @return blue clear color component
     */
    public static float getClearColorB() {
        return clearColorStateB;
    }

    /**
     * Returns the alpha clear color component.
     *
     * @return alpha clear color component
     */
    public static float getClearColorA() {
        return clearColorStateA;
    }

    /**
     * Sets the polygon offset factor and units.
     *
     * @param factor - polygon offset factor
     * @param units - polygon offset units
     */
    public static void setPolygonOffset(float factor, float units) {
        if (polygonOffsetFactor != factor || polygonOffsetUnits != units) {
            GL11.glPolygonOffset(factor, units);
            polygonOffsetFactor = factor;
            polygonOffsetUnits = units;
        }
    }

    /**
     * Gives the polygon offset factor.
     *
     * @return polygon offset factor
     */
    public static float getPolygonOffsetFactor() {
        return polygonOffsetFactor;
    }

    /**
     * Gives the polygon offset units.
     *
     * @return polygon offset units
     */
    public static float getPolygonOffsetUnits() {
        return polygonOffsetUnits;
    }

    /**
     * Sets the viewport location and dimensions.
     *
     * @param x - horizontal location
     * @param y - vertical location
     * @param width - horizontal dimension
     * @param height - vertical dimension
     */
    public static void setViewport(int x, int y, int width, int height) {
        if (viewportStateX != x || viewportStateY != y || viewportStateWidth != width || viewportStateHeight != height) {
            GL11.glViewport(x, y, width, height);
            viewportStateX = x;
            viewportStateY = y;
            viewportStateWidth = width;
            viewportStateHeight = height;
        }
    }

    /**
     * Gives the viewport's horizontal location.
     *
     * @return horizontal location
     */
    public static int getViewPortX() {
        return viewportStateX;
    }

    /**
     * Gives the viewport's vertical location.
     *
     * @return vertical location
     */
    public static int getViewPortY() {
        return viewportStateY;
    }

    /**
     * Gives the viewport's horizontal dimension.
     *
     * @return horizontal dimension
     */
    public static int getViewPortWidth() {
        return viewportStateWidth;
    }

    /**
     * Gives the viewport's vertical dimension.
     *
     * @return vertical dimension
     */
    public static int getViewPortHeight() {
        return viewportStateHeight;
    }

    /**
     * Sets the blend function for the given buffer.
     *
     * @param buffer - draw buffer
     * @param src - source color blend function
     * @param dst - destination color blend function
     */
    public static void setBlendFunction(int buffer, BlendFunction src, BlendFunction dst) {
        GL40.glBlendFunci(buffer, src.value, dst.value);
    }

    /**
     * Sets the blend function.
     *
     * @param src - source color blend function
     * @param dst - destination color blend function
     */
    public static void setBlendFunction(BlendFunction src, BlendFunction dst) {
        GL11.glBlendFunc(src.value, dst.value);
    }

    /**
     * Clears the given buffer.
     *
     * @param buffer - color, depth, or stencil buffer
     */
    public static void clearBuffer(Buffer buffer) {
        GL11.glClear(buffer.value);
    }

    /**
     * Clears the given buffer. This version allows the use of the bitwise OR operator between multiple buffer types.
     *
     * @param buffer - integer id of the buffer type
     */
    public static void clearBuffer(int buffer) {
        GL11.glClear(buffer);
    }

    /**
     * Creates a vertex array.
     *
     * @return vertex array id
     */
    public static int createVertexArray() {
        int vao = GL30.glGenVertexArrays();
        vaos.add(vao);
        return vao;
    }

    /**
     * Creates a vertex buffer.
     *
     * @return vertex buffer id
     */
    public static int createVertexBuffer() {
        int vbo = GL15.glGenBuffers();
        buffers.add(vbo);
        return vbo;
    }

    /**
     * Activates a vertex array.
     *
     * @param vao - vertex array id
     */
    public static void bindVertexArray(int vao) {
        if (vertexArrayState != vao) {
            GL30.glBindVertexArray(vao);
            vertexArrayState = vao;
        }
    }

    /**
     * Sets the given index buffer's data to the given integer buffer data.
     *
     * @param id - index buffer id
     * @param data - integer buffer data
     */
    public static void fillIndexBuffer(int id, IntBuffer data) {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, id);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, GL15.GL_DYNAMIC_DRAW);
    }

    /**
     * Sets the given vertex buffer's data to the given integer buffer data.
     *
     * @param id - vertex buffer id
     * @param size - data numCached
     * @param attribLoc - attribute location
     * @param data - integer
     */
    public static void fillVertexBuffer(int id, int size, int attribLoc, IntBuffer data) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_DYNAMIC_DRAW);
        GL30.glVertexAttribIPointer(attribLoc, size, GL11.GL_INT, size * 4, 0);
    }

    /**
     * Sets the given vertex buffer's data to the given float buffer data.
     *
     * @param id - vertex buffer id
     * @param size - data numCached
     * @param attribLoc - attribute location
     * @param data - integer
     */
    public static void fillVertexBuffer(int id, int size, int attribLoc, FloatBuffer data) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_DYNAMIC_DRAW);
        GL20.glVertexAttribPointer(attribLoc, size, GL11.GL_FLOAT, false, size * 4, 0);
    }

    /**
     * Updates the given index buffer's data with the given integer buffer data.
     *
     * @param id - index buffer id
     * @param data - integer buffer data
     */
    public static void updateIndexBuffer(int id, IntBuffer data) {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, id);
        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, data);
    }

    /**
     * Updates the given vertex buffer's data with the given integer buffer data.
     *
     * @param id - vertex buffer id
     * @param data - integer buffer data
     */
    public static void updateVertexBuffer(int id, IntBuffer data) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);
    }

    /**
     * Updates the given vertex buffer's data with the given float buffer data.
     *
     * @param id - vertex buffer id
     * @param data - float buffer data
     */
    public static void updateVertexBuffer(int id, FloatBuffer data) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);
    }

    /**
     * Activates or deactivates the given vertex attribute.
     *
     * @param attribute - vertex attribute
     * @param enabled - if true, the given vertex attribute is enabled
     */
    public static void setAttributeEnabled(int attribute, boolean enabled) {
        if (enabled) {
            GL20.glEnableVertexAttribArray(attribute);
        } else {
            GL20.glDisableVertexAttribArray(attribute);
        }
    }

    /**
     * Renders the given vertex array object as triangles.
     *
     * @param vao - vertex array id
     * @param numIndices - number of indices
     */
    public static void drawTris(int vao, int numIndices) {
        bindVertexArray(vao);
        GL11.glDrawElements(GL11.GL_TRIANGLES, numIndices, GL11.GL_UNSIGNED_INT, 0);
    }

    /**
     * Renders the given vertex array object as lines.
     *
     * @param vao - vertex array id
     * @param numIndices - number of indices
     */
    public static void drawLines(int vao, int numIndices) {
        bindVertexArray(vao);
        GL11.glDrawElements(GL11.GL_LINES, numIndices, GL11.GL_UNSIGNED_INT, 0);
    }

    /**
     * Renders the given vertex array object as quadrilaterals.
     *
     * @param vao - vertex array id
     * @param numIndices - number of indices
     */
    public static void drawQuads(int vao, int numIndices) {
        bindVertexArray(vao);
        GL11.glDrawElements(GL11.GL_QUADS, numIndices, GL11.GL_UNSIGNED_INT, 0);
    }

    /**
     * Creates a frame buffer.
     *
     * @return the created frame buffer id
     */
    public static int createFrameBuffer() {
        int framebuffer = GL30.glGenFramebuffers();
        framebuffers.add(framebuffer);
        return framebuffer;
    }

    /**
     * Activates the given frame buffer for writing.
     *
     * @param fb - frame buffer id
     */
    public static void writeFrameBuffer(int fb) {
        if (fbWriteState != fb) {
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fb);
            fbWriteState = fb;
        }
    }

    /**
     * Gives the currently set framebuffer for writing.
     *
     * @return currently set framebuffer for writing
     */
    public static int getWriteFrameBuffer() {
        return fbWriteState;
    }

    /**
     * Activates the given frame buffer for reading.
     *
     * @param fb - frame buffer id
     */
    public static void readFrameBuffer(int fb) {
        if (fbReadState != fb) {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fb);
            fbReadState = fb;
        }
    }

    /**
     * Gives the currently set framebuffer for writing.
     *
     * @return currently set framebuffer for writing
     */
    public static int getReadFrameBuffer() {
        return fbReadState;
    }

    /**
     * Checks if frame buffer has been created successfully.
     *
     * @return - true if frame buffer has been created successfully
     */
    public static boolean isFrameBufferComplete() {
        return GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
    }

    /**
     * Gives the status of a framebuffer.
     *
     * @return - status code
     */
    public static FrameBufferStatus getFrameBufferStatus() {
        return FrameBufferStatus.getStatus(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER));
    }

    /**
     * Attaches the given color texture to the currently bound frame buffer at the given index.
     *
     * @param texture - texture id
     * @param index - attachment index
     */
    public static void attachColorTexture(int texture, int index, boolean multisampled) {
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL.Attachment.COLOR_0.getValue() + index, multisampled ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D, texture, 0);
    }

    /**
     * Attaches the given cube color texture to the currently bound frame buffer at the given face index.
     *
     * @param texture - shadow texture
     */
    public static void attachCubeColorTexture(int texture, int face) {
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL.Attachment.COLOR_0.getValue(), GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, texture, 0);
    }

    /**
     * Attaches the given depth texture to the currently bound frame buffer.
     *
     * @param texture - texture id
     */
    public static void attachDepthTexture(int texture) {
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, texture, 0);
    }

    /**
     * Attaches the given depth render buffer to the currently bound frame buffer.
     *
     * @param depthbuffer - texture id
     */
    public static void attachDepthRenderBuffer(int depthbuffer) {
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthbuffer);
    }

    /**
     * Attaches the given shadow texture to the currently bound frame buffer.
     *
     * @param texture - shadow texture
     */
    public static void attachShadowTexture(int texture) {
        GL11.glDrawBuffer(GL11.GL_NONE);
        GL11.glReadBuffer(GL11.GL_NONE);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, texture, 0);
    }

    /**
     * Attaches the given cube shadow texture to the currently bound frame buffer at the given face index.
     *
     * @param texture - shadow texture
     */
    public static void attachCubeShadowTexture(int texture, int face) {
        GL11.glDrawBuffer(GL11.GL_NONE);
        GL11.glReadBuffer(GL11.GL_NONE);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, texture, 0);
    }

    /**
     * Activates the given color attachment for writing.
     *
     * @param attachment - color attachment
     */
    public static void writeAttachment(Attachment attachment) {
        GL11.glDrawBuffer(attachment.value);
    }

    /**
     * Activates the given color attachment for writing.
     *
     * @param attachment - color attachment
     */
    public static void writeAttachment(int attachment) {
        GL11.glDrawBuffer(attachment);
    }

    /**
     * Activates the given array of color attachments for writing.
     *
     * @param attachments - array of color attachment
     */
    public static void writeAttachments(int[] attachments) {
        GL20.glDrawBuffers(attachments);
    }

    /**
     * Activates the given buffer of color attachments for writing.
     *
     * @param attachments - buffer of color attachment
     */
    public static void writeAttachments(IntBuffer attachments) {
        GL20.glDrawBuffers(attachments);
    }

    /**
     * Activates the given color attachment for reading.
     *
     * @param attachment - color attachment
     */
    public static void readAttachment(Attachment attachment) {
        GL11.glReadBuffer(attachment.getValue());
    }

    /**
     * Activates the given color attachment for reading.
     *
     * @param attachment - color attachment
     */
    public static void readAttachment(int attachment) {
        GL11.glReadBuffer(attachment);
    }

    /**
     * Creates a color attachment with the given parameter and byte data.
     *
     * @param components - number of color components
     * @param width - color buffer width
     * @param height - color buffer height
     * @param filtered - if true, color buffer is filtered
     * @param data - buffer of byte values
     *
     * @return the created color buffer id
     */
    public static int createColorBuffer(int components, int width, int height, boolean filtered, ByteBuffer data) {
        int texture = GL11.glGenTextures();
        int filter = filtered ? GL11.GL_LINEAR : GL11.GL_NEAREST;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        switch (components) {
            case 1: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R8, width, height, 0, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, data); break;
            case 2: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RG8, width, height, 0, GL30.GL_RG, GL11.GL_UNSIGNED_BYTE, data); break;
            case 3: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, data); break;
            case 4: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data); break;
        }

        textures.add(texture);

        return texture;
    }

    /**
     * Creates a color attachment with the given parameter and float data.
     *
     * @param components - number of color components
     * @param width - color buffer width
     * @param height - color buffer height
     * @param filtered - if true, color buffer is filtered
     * @param data - buffer of float values
     *
     * @return the created color buffer id
     */
    public static int createColorBuffer(int components, int width, int height, boolean filtered, FloatBuffer data) {
        int texture = GL11.glGenTextures();
        int filter = filtered ? GL11.GL_LINEAR : GL11.GL_NEAREST;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        switch (components) {
            case 1: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R16F, width, height, 0, GL11.GL_RED, GL11.GL_FLOAT, data); break;
            case 2: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RG16F, width, height, 0, GL30.GL_RG, GL11.GL_FLOAT, data); break;
            case 3: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGB16F, width, height, 0, GL11.GL_RGB, GL11.GL_FLOAT, data); break;
            case 4: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, data); break;
        }

        textures.add(texture);

        return texture;
    }

    /**
     * Updates a color buffer with the given parameters and byte data.
     *
     * @param id - color buffer id
     * @param components - number of color components
     * @param width - color buffer width
     * @param height - color buffer height
     * @param data - buffer of byte values
     */
    public static void updateColorBuffer(int id, int components, int width, int height, ByteBuffer data) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

        switch (components) {
            case 1: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R8, width, height, 0, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, data); break;
            case 2: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RG8, width, height, 0, GL30.GL_RG, GL11.GL_UNSIGNED_BYTE, data); break;
            case 3: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, data); break;
            case 4: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data); break;
        }
    }

    /**
     * Updates a color buffer with the given parameters and float data.
     *
     * @param id - color buffer id
     * @param components - number of color components
     * @param width - color buffer width
     * @param height - color buffer height
     * @param data - buffer of float values
     */
    public static void updateColorBuffer(int id, int components, int width, int height, FloatBuffer data) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

        switch (components) {
            case 1: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R16F, width, height, 0, GL11.GL_RED, GL11.GL_FLOAT, data); break;
            case 2: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RG16F, width, height, 0, GL30.GL_RG, GL11.GL_FLOAT, data); break;
            case 3: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGB16F, width, height, 0, GL11.GL_RGB, GL11.GL_FLOAT, data); break;
            case 4: GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, data); break;
        }
    }

    /**
     * Creates a multi-sampled color attachment with the given parameter and float data.
     *
     * @param components - number of color components
     * @param samples - number of samples per pixel
     * @param width - color buffer width
     * @param height - color buffer height
     * @param filtered - if true, color buffer is filtered
     *
     * @return the created color buffer id
     */
    public static int createColorBufferMultisampled(int components, int samples, int width, int height, boolean filtered) {
        int texture = GL11.glGenTextures();
        int filter = filtered ? GL11.GL_LINEAR : GL11.GL_NEAREST;

        GL11.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, texture);

        GL11.glTexParameteri(GL32.GL_TEXTURE_2D_MULTISAMPLE, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL32.GL_TEXTURE_2D_MULTISAMPLE, GL11.GL_TEXTURE_MAG_FILTER, filter);
        GL11.glTexParameteri(GL32.GL_TEXTURE_2D_MULTISAMPLE, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL32.GL_TEXTURE_2D_MULTISAMPLE, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        switch (components) {
            case 1: GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL30.GL_R16F, width, height, true); break;
            case 2: GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL30.GL_RG16F, width, height, true); break;
            case 3: GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL30.GL_RGB16F, width, height, true); break;
            case 4: GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL30.GL_RGBA16F, width, height, true); break;
        }

        textures.add(texture);

        return texture;
    }

    /**
     * Updates a color buffer with the given parameters and float data.
     *
     * @param id - color buffer id
     * @param components - number of color components
     * @param width - color buffer width
     * @param height - color buffer height
     */
    public static void updateColorBufferMultisampled(int id, int components, int samples, int width, int height) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

        switch (components) {
            case 1: GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL30.GL_R16F, width, height, true); break;
            case 2: GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL30.GL_RG16F, width, height, true); break;
            case 3: GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL30.GL_RGB16F, width, height, true); break;
            case 4: GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL30.GL_RGBA16F, width, height, true); break;
        }
    }

    /**
     * Creates a depth texture with the given dimensions.
     *
     * @param width - depth texture width
     * @param height - depth texture height
     *
     * @return the created depth buffer id
     */
    public static int createDepthTexture(int width, int height) {
        int texture = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);

        textures.add(texture);

        return texture;
    }

    /**
     * Updates a depth texture with the given dimensions.
     *
     * @param id - depth texture id
     * @param width - depth texture width
     * @param height - depth texture height
     */
    public static void updateDepthTexture(int id, int width, int height) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
    }

    /**
     * Creates a depth render buffer with the given dimensions.
     *
     * @param width - depth render buffer width
     * @param height - depth render buffer height
     *
     * @return the created render buffer id
     */
    public static int createDepthRenderBuffer(int width, int height) {
        int depthbuffer = GL30.glGenRenderbuffers();

        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthbuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);

        renderbuffers.add(depthbuffer);

        return depthbuffer;
    }

    /**
     * Updates a depth texture with the given dimensions.
     *
     * @param id - depth render buffer id
     * @param width - depth render buffer width
     * @param height - depth render buffer height
     */
    public static void updateDepthRenderBuffer(int id, int width, int height) {
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, id);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
    }

    /**
     * Creates a depth render buffer with the given dimensions.
     *
     * @param samples - number of samples
     * @param width - depth render buffer width
     * @param height - depth render buffer height
     *
     * @return the created render buffer id
     */
    public static int createDepthRenderBufferMultisampled(int samples, int width, int height) {
        int depthbuffer = GL30.glGenRenderbuffers();

        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthbuffer);
        GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, samples, GL14.GL_DEPTH_COMPONENT24, width, height);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);

        renderbuffers.add(depthbuffer);

        return depthbuffer;
    }

    /**
     * Updates a depth texture with the given dimensions.
     *
     * @param id - depth render buffer id
     * @param samples - number of samples
     * @param width - depth render buffer width
     * @param height - depth render buffer height
     */
    public static void updateDepthRenderBufferMultisampled(int id, int samples, int width, int height) {
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, id);
        GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, samples, GL14.GL_DEPTH_COMPONENT24, width, height);
    }

    /**
     * Copies the color buffer from the reading buffer to the writing buffer.
     *
     * @param srcX - source buffer x coordinate
     * @param srcY - source buffer y coordinate
     * @param srcWidth - source buffer width
     * @param srcHeight - source buffer height
     * @param destX - destination buffer x coordinate
     * @param destY - destination buffer y coordinate
     * @param destWidth - destination buffer width
     * @param destHeight - destination buffer height
     * @param filtered - whether bilinear filtering is applied or not
     */
    public static void copyColorBuffer(int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, boolean filtered) {
        GL30.glBlitFramebuffer(srcX, srcY, srcWidth, srcHeight, destX, destY, destWidth, destHeight, GL11.GL_COLOR_BUFFER_BIT, filtered ? GL11.GL_LINEAR : GL11.GL_NEAREST);
    }

    /**
     * Copies the depth buffer from the reading buffer to the writing buffer.
     *
     * @param srcX - source buffer x coordinate
     * @param srcY - source buffer y coordinate
     * @param srcWidth - source buffer width
     * @param srcHeight - source buffer height
     * @param destX - destination buffer x coordinate
     * @param destY - destination buffer y coordinate
     * @param destWidth - destination buffer width
     * @param destHeight - destination buffer height
     * @param filtered - whether bilinear filtering is applied or not
     */
    public static void copyDepthBuffer(int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, boolean filtered) {
        GL30.glBlitFramebuffer(srcX, srcY, srcWidth, srcHeight, destX, destY, destWidth, destHeight, GL11.GL_DEPTH_BUFFER_BIT, filtered ? GL11.GL_LINEAR : GL11.GL_NEAREST);
    }

    /**
     * Copies both depth and color buffers from the reading buffer to the writing buffer.
     *
     * @param srcX - source buffer x coordinate
     * @param srcY - source buffer y coordinate
     * @param srcWidth - source buffer width
     * @param srcHeight - source buffer height
     * @param destX - destination buffer x coordinate
     * @param destY - destination buffer y coordinate
     * @param destWidth - destination buffer width
     * @param destHeight - destination buffer height
     * @param filtered - whether bilinear filtering is applied or not
     */
    public static void copyBuffers(int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, boolean filtered) {
        GL30.glBlitFramebuffer(srcX, srcY, srcWidth, srcHeight, destX, destY, destWidth, destHeight, GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, filtered ? GL11.GL_LINEAR : GL11.GL_NEAREST);
    }

    /**
     * Activates the given two-dimensional texture on the given texture unit.
     *
     * @param id - texture id
     * @param unit - texture unit
     */
    public static void bindTexture2D(int id, int unit) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    /**
     * Activates the given two-dimensional texture array on the given texture unit.
     *
     * @param id - texture id
     * @param unit - texture unit
     */
    public static void bindTextureArray(int id, int unit) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
        GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, id);
    }

    /**
     * Activates the given two-dimensional texture cube on the given texture unit.
     *
     * @param id - texture id
     * @param unit - texture unit
     */
    public static void bindTextureCube(int id, int unit) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, id);
    }

    /**
     * Creates a two-dimensional texture with the given parameters.
     *
     * @param wrapModeS - horizontal wrap mode
     * @param wrapModeT - vertical wrap mode
     * @param minFilter - minification filter
     * @param magFilter - magnification filter
     *
     * @return the created texture id
     */
    public static int createTexture2D(WrapMode wrapModeS, WrapMode wrapModeT, Filter minFilter, Filter magFilter) {
        int texture = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapModeS.value);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapModeT.value);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter.value);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter.value);

        textures.add(texture);

        return texture;
    }

    /**
     * Sets the texture data.
     *
     * @param texture - texture id
     * @param width - texture width
     * @param height - texture height
     * @param compressed - if true, texture data is compressed
     * @param data - texture data
     */
    public static void setTexture2DData(int texture, int width, int height, boolean compressed, ByteBuffer data) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, compressed ? GL13.GL_COMPRESSED_RGBA : GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
    }

    /**
     * Generates mipmaps for the given texture.
     *
     * @param texture - texture id
     */
    public static void generateTexture2DMipmap(int texture) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
    }

    /**
     * Updates the given texture with the given parameters.
     *
     * @param texture - texture id
     * @param width - texture width
     * @param height - texture height
     * @param compressed - if true, texture data is compressed
     * @param data - texture data
     */
    public static void updateTexture2DData(int texture, int width, int height, boolean compressed, ByteBuffer data) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, compressed ? GL13.GL_COMPRESSED_RGBA : GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
    }

    /**
     * Updates the given texture with the given min and mag filters.
     *
     * @param texture - id of texture to update
     * @param minFilter - minification filter
     * @param magFilter - magnification filter
     */
    public static void updateTexture2DFilter(int texture, Filter minFilter, Filter magFilter) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter.value);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter.value);
    }

    /**
     * Updates the given texture with the given horizontal and vertical wrap modes.
     *
     * @param texture - id of texture to update
     * @param wrapModeS - horizontal wrap mode
     * @param wrapModeT - vertical wrap mode
     */
    public static void updateTexture2DWrapMode(int texture, WrapMode wrapModeS, WrapMode wrapModeT) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapModeS.value);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapModeT.value);
    }

    /**
     * Creates a cube texture with the given parameters.
     *
     * @param wrapModeS - horizontal wrap mode
     * @param wrapModeT - vertical wrap mode
     * @param wrapModeR - orthogonal wrap mode
     * @param minFilter - minification filter
     * @param magFilter - magnification filter
     *
     * @return id of the newly created texture
     */
    public static int createTextureCube(WrapMode wrapModeS, WrapMode wrapModeT, WrapMode wrapModeR, Filter minFilter, Filter magFilter) {
        int texture = GL11.glGenTextures();

        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, wrapModeS.value);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, wrapModeT.value);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, wrapModeR.value);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, minFilter.value);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, magFilter.value);

        textures.add(texture);

        return texture;
    }

    /**
     * Sets the texture data.
     *
     * @param index - cube face index
     * @param texture - texture id
     * @param width - texture width
     * @param height - texture height
     * @param compressed - if true, texture data is compressed
     * @param data - texture data
     */
    public static void setTextureCubeData(int index, int texture, int width, int height, boolean compressed, ByteBuffer data) {
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);
        GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + index, 0, compressed ? GL13.GL_COMPRESSED_RGBA : GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
    }

    /**
     * Sets the texture data.
     *
     * @param index - cube face index
     * @param texture - texture id
     * @param width - texture width
     * @param height - texture height
     * @param compressed - if true, texture data is compressed
     * @param data - texture data
     */
    public static void setTextureCubeData(int index, int texture, int width, int height, boolean compressed, FloatBuffer data) {
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);
        GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + index, 0, compressed ? GL13.GL_COMPRESSED_RGBA : GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, data);
    }

    /**
     * Generates mipmap for cube textures.
     *
     * @param texture - texture id
     * @param base - mipmap base level
     * @param max - mipmap max level
     */
    public static void generateTextureCubeMipmap(int texture, int base, int max) {
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_BASE_LEVEL, base);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_MAX_LEVEL, max);
        GL30.glGenerateMipmap(GL13.GL_TEXTURE_CUBE_MAP);
    }

    /**
     * Updates the texture data.
     *
     * @param index - cube face index
     * @param texture - texture id
     * @param width - texture width
     * @param height - texture height
     * @param compressed - if true, texture data is compressed
     * @param data - texture data
     */
    public static void updateTextureCubeData(int index, int texture, int width, int height, boolean compressed, ByteBuffer data) {
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);
        GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + index, 0, compressed ? GL13.GL_COMPRESSED_RGBA : GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
    }

    /**
     * Updates the given cube texture with the given min and mag filters.
     *
     * @param texture - id of the texture to update
     * @param minFilter - minification filter
     * @param magFilter - magnification filter
     */
    public static void updateTextureCubeFilter(int texture, Filter minFilter, Filter magFilter) {
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter.value);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter.value);
    }

    /**
     * Updates the given cube texture with the given horizontal, vertical, and orthogonal wrap modes.
     *
     * @param texture - id of texture to update
     * @param wrapModeS - horizontal wrap mode
     * @param wrapModeT - vertical wrap mode
     * @param wrapModeR - orthogonal wrap mode
     */
    public static void updateTextureCubeWrapMode(int texture, WrapMode wrapModeS, WrapMode wrapModeT, WrapMode wrapModeR) {
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapModeS.value);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapModeT.value);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_WRAP_R, wrapModeR.value);
    }

    /**
     * Creates a shadow texture with the given width and height.
     *
     * @param width - texture width
     * @param height - texture height
     *
     * @return shadow texture id
     */
    public static int createShadowTexture(int width, int height) {
        int texture = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL30.GL_COMPARE_REF_TO_TEXTURE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);

        textures.add(texture);

        return texture;
    }

    /**
     * Updates the given shadow texture with the given width and height.
     *
     * @param texture - id of texture to update
     * @param width - texture width
     * @param height - texture height
     */
    public static void updateShadowTexture(int texture, int width, int height) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
    }

    /**
     * Creates a shadow cube texture with the given width and height of each face.
     *
     * @param width - texture width
     * @param height - texture height
     *
     * @return shadow texture id
     */
    public static int createShadowTextureCube(int width, int height) {
        int texture = GL11.glGenTextures();

        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL14.GL_TEXTURE_COMPARE_MODE, GL30.GL_COMPARE_REF_TO_TEXTURE);

        for (int i = 0; i < 6; i++) {
            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }

        textures.add(texture);

        return texture;
    }

    /**
     * Updates the given shadow cube texture with the given width and height.
     *
     * @param texture - id of texture to update
     * @param width - texture width
     * @param height - texture height
     */
    public static void updateShadowTextureCube(int texture, int width, int height) {
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);

        for (int i = 0; i < 6; i++) {
            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }
    }

    /**
     * Destroys the context instance of the given program and shader.
     *
     * @param program - program id
     * @param shader - shader id
     */
    public static void freeShader(int program, int shader) {
        GL20.glDetachShader(program, shader);
        GL20.glDeleteShader(shader);
        shaders.remove((Integer) shader);
    }

    /**
     * Destroys the context instance of the given texture.
     *
     * @param id - texture id
     */
    public static void freeTexture(int id) {
        GL11.glDeleteTextures(id);
        textures.remove(Integer.valueOf(id));
    }

    /**
     * Destroys the context instance of the given buffer.
     *
     * @param id - buffer id
     */
    public static void freeBuffer(int id) {
        GL15.glDeleteBuffers(id);
        buffers.remove(Integer.valueOf(id));
    }

    /**
     * Destroys the context instance of the given vertex array.
     *
     * @param id - vertex array id
     */
    public static void freeVertexArray(int id) {
        GL30.glDeleteVertexArrays(id);
        vaos.remove(Integer.valueOf(id));
    }

    /**
     * Destroys the context instance of the given render buffer.
     *
     * @param id - render buffer id
     */
    public static void freeRenderBuffer(int id) {
        GL30.glDeleteRenderbuffers(id);
        renderbuffers.remove(Integer.valueOf(id));
    }

    /**
     * Destroys the context instance of the given frame buffer.
     *
     * @param id - frame buffer id
     */
    public static void freeFrameBuffer(int id) {
        GL30.glDeleteFramebuffers(id);
        framebuffers.remove(Integer.valueOf(id));
    }

    /**
     * Sets rendering states to default.
     */
    static void init() {
        readFrameBuffer(DEFAULT_READ_FRAMEBUFFER);
        writeFrameBuffer(DEFAULT_WRITE_FRAMEBUFFER);
        bindVertexArray(DEFAULT_VERTEX_ARRAY);
        bindTexture2D(DEFAULT_TEXTURE2D_ID, DEFAULT_TEXTURE2D_UNIT);
        bindTextureArray(DEFAULT_TEXTUREARRAY_ID, DEFAULT_TEXTUREARRAY_UNIT);
        bindTextureCube(DEFAULT_TEXTURECUBE_ID, DEFAULT_TEXTURECUBE_UNIT);
        setLineWidth(DEFAULT_LINE_WIDTH);
        setPointSize(DEFAULT_POINT_SIZE);
        setPolygonMode(DEFAULT_POLYGON_MODE);
        setCullFace(DEFAULT_CULL_FACE);
        setFaceCullingEnabled(DEFAULT_FACE_CULLING_STATE);
        setBlendEnabled(DEFAULT_BLEND_STATE);
        setDepthTestEnabled(DEFAULT_DEPTH_TEST_STATE);
        setDepthClampEnabled(DEFAULT_DEPTH_CLAMP_STATE);
        setDepthWriteEnabled(DEFAULT_DEPTH_WRITE_STATE);
        setColorWriteEnabled(DEFAULT_COLOR_WRITE_STATE);
        setPolygonOffsetFillEnabled(DEFAULT_POLYGON_OFFSET_FILL_STATE);
        setPolygonOffsetLineEnabled(DEFAULT_POLYGON_OFFSET_LINE_STATE);
        setPolygonOffsetPointEnabled(DEFAULT_POLYGON_OFFSET_POINT_STATE);
        setSeamlessCubeTextureEnabled(DEFAULT_SEAMLESS_CUBE_TEXTURE_STATE);
        setClearColor(DEFAULT_CLEAR_COLOR_R, DEFAULT_CLEAR_COLOR_G, DEFAULT_CLEAR_COLOR_B, DEFAULT_CLEAR_COLOR_A);
        setViewport(DEFAULT_VIEWPORT_X, DEFAULT_VIEWPORT_Y, DEFAULT_VIEWPORT_WIDTH, DEFAULT_VIEWPORT_HEIGHT);
    }

    /**
     * Destroys all graphics resources.
     */
    static void free() {
        IntBuffer vaosBuffer = Buffers.createIntBuffer(vaos.size());
        IntBuffer buffersBuffer = Buffers.createIntBuffer(buffers.size());
        IntBuffer queriesBuffer = Buffers.createIntBuffer(queries.size());
        IntBuffer shadersBuffer = Buffers.createIntBuffer(shaders.size());
        IntBuffer programsBuffer = Buffers.createIntBuffer(programs.size());
        IntBuffer texturesBuffer = Buffers.createIntBuffer(textures.size());
        IntBuffer framebuffersBuffer = Buffers.createIntBuffer(framebuffers.size());
        IntBuffer renderbuffersBuffer = Buffers.createIntBuffer(renderbuffers.size());

        for (int vao : vaos) {
            vaosBuffer.put(vao);
        }

        for (int buffer : buffers) {
            buffersBuffer.put(buffer);
        }

        for (int query : queries) {
            queriesBuffer.put(query);
        }

        for (int shader : shaders) {
            shadersBuffer.put(shader);
        }

        for (int program : programs) {
            programsBuffer.put(program);
        }

        for (int texture : textures) {
            texturesBuffer.put(texture);
        }

        for (int framebuffer : framebuffers) {
            framebuffersBuffer.put(framebuffer);
        }

        for (int framebuffer : renderbuffers) {
            renderbuffersBuffer.put(framebuffer);
        }

        vaosBuffer.flip();
        buffersBuffer.flip();
        queriesBuffer.flip();
        shadersBuffer.flip();
        programsBuffer.flip();
        texturesBuffer.flip();
        framebuffersBuffer.flip();
        renderbuffersBuffer.flip();

        GL15.glDeleteBuffers(buffersBuffer);
        GL15.glDeleteQueries(queriesBuffer);
        GL11.glDeleteTextures(texturesBuffer);
        GL30.glDeleteVertexArrays(vaosBuffer);
        GL30.glDeleteFramebuffers(framebuffersBuffer);
        GL30.glDeleteRenderbuffers(renderbuffersBuffer);

        while (programsBuffer.hasRemaining()) {
            GL20.glDeleteProgram(programsBuffer.get());
        }

        while (programsBuffer.hasRemaining()) {
            GL20.glDeleteShader(shadersBuffer.get());
        }

        vaos.clear();
        buffers.clear();
        queries.clear();
        shaders.clear();
        programs.clear();
        textures.clear();
        framebuffers.clear();
        renderbuffers.clear();
    }

    /**
     * Resets the context to the last set states. Call this method after recovering from a context loss such as when the
     * display has been destroyed and recreated.
     */
    static void restore() {
        writeFrameBuffer(fbWriteState);
        bindVertexArray(vertexArrayState);
        setLineWidth(lineWidthState);
        setPointSize(pointSizeState);
        setPolygonMode(polygonModeState);
        setCullFace(cullFaceState);
        setFaceCullingEnabled(faceCullingState);
        setBlendEnabled(blendState);
        setDepthTestEnabled(depthTestState);
        setDepthClampEnabled(depthClampState);
        setDepthWriteEnabled(depthWriteState);
        setColorWriteEnabled(colorWriteState);
        setSeamlessCubeTextureEnabled(seamlessCubeTextureState);
        setPolygonOffsetFillEnabled(polygonOffsetFillState);
        setPolygonOffsetLineEnabled(polygonOffsetLineState);
        setPolygonOffsetPointEnabled(polygonOffsetPointState);
        setPolygonOffset(polygonOffsetFactor, polygonOffsetUnits);
        setClearColor(clearColorStateR, clearColorStateG, clearColorStateB, clearColorStateA);
        setViewport(viewportStateX, viewportStateY, viewportStateWidth, viewportStateHeight);
    }
}
