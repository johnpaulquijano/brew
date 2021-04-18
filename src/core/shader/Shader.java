package core.shader;

import core.GL;
import core.utility.EngineException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages initialization and execution of shaders.
 *
 * @author John Paul Quijano
 */
public class Shader {
    /**
     * Enumeration of shader execution stage.
     */
    public enum Stage {
        VERTEX, FRAGMENT
    }

    /**
     * Enumeration of shader data type groups.
     */
    public enum Group {
        VOID, SCALAR, VECTOR, MATRIX, SAMPLER, IMAGE, ATOMIC
    }

    /**
     * Enumeration of shader data types.
     */
    public enum Type {
        VOID("void", Group.VOID),
        BOOL("bool", Group.SCALAR),
        INT("int", Group.SCALAR),
        FLOAT("float", Group.SCALAR),
        DOUBLE("double", Group.SCALAR),
        VEC2("vec2", Group.VECTOR),
        VEC3("vec3", Group.VECTOR),
        VEC4("vec4", Group.VECTOR),
        DVEC2("dvec2", Group.VECTOR),
        DVEC3("dvec3", Group.VECTOR),
        DVEC4("dvec4", Group.VECTOR),
        BVEC2("bvec2", Group.VECTOR),
        BVEC3("bvec3", Group.VECTOR),
        BVEC4("bvec4", Group.VECTOR),
        IVEC2("ivec2", Group.VECTOR),
        IVEC3("ivec3", Group.VECTOR),
        IVEC4("ivec4", Group.VECTOR),
        UVEC2("uvec2", Group.VECTOR),
        UVEC3("uvec3", Group.VECTOR),
        UVEC4("uvec4", Group.VECTOR),
        MAT2("mat2", Group.MATRIX),
        MAT3("mat3", Group.MATRIX),
        MAT4("mat4", Group.MATRIX),
        DMAT2("dmat2", Group.MATRIX),
        DMAT3("dmat3", Group.MATRIX),
        DMAT4("dmat4", Group.MATRIX),
        SAMPLER1D("sampler1D", Group.SAMPLER),
        SAMPLER2D("sampler2D", Group.SAMPLER),
        SAMPLER3D("sampler3D", Group.SAMPLER),
        SAMPLERCUBE("samplerCube", Group.SAMPLER),
        SAMPLER2DRECT("sampler2DRect", Group.SAMPLER),
        SAMPLER1DARRAY("sampler1DArray", Group.SAMPLER),
        SAMPLER2DARRAY("sampler2DArray", Group.SAMPLER),
        SAMPLERCUBEARRAY("samplerCubeArray", Group.SAMPLER),
        SAMPLERBUFFER("samplerBuffer", Group.SAMPLER),
        SAMPLER2DMS("sampler2DMS", Group.SAMPLER),
        SAMPLER2DMSARRAY("sampler2DMSArray", Group.SAMPLER),
        SAMPLER1DSHADOW("sampler1DShadow", Group.SAMPLER),
        SAMPLER2DSHADOW("sampler2DShadow", Group.SAMPLER),
        SAMPLER2DRECTSHADOW("sampler2DRectShadow", Group.SAMPLER),
        SAMPLER1DARRAYSHADOW("sampler1DArrayShadow", Group.SAMPLER),
        SAMPLER2DARRAYSHADOW("sampler2DArrayShadow", Group.SAMPLER),
        SAMPLERCUBESHADOW("samplerCubeShadow", Group.SAMPLER),
        SAMPLERCUBEARRAYSHADOW("samplerCubeArrayShadow", Group.SAMPLER),
        ISAMPLER1D("isampler1D", Group.SAMPLER),
        ISAMPLER2D("isampler2D", Group.SAMPLER),
        ISAMPLER3D("isampler3D", Group.SAMPLER),
        ISAMPLERCUBE("isamplerCube", Group.SAMPLER),
        ISAMPLER2DRECT("isampler2DRect", Group.SAMPLER),
        ISAMPLER1DARRAY("isampler1DArray", Group.SAMPLER),
        ISAMPLER2DARRAY("isampler2DArray", Group.SAMPLER),
        ISAMPLERCUBEARRAY("isamplerCubeArray", Group.SAMPLER),
        ISAMPLERBUFFER("isamplerBuffer", Group.SAMPLER),
        ISAMPLER2DMS("isampler2DMS", Group.SAMPLER),
        ISAMPLER2DMSARRAY("isampler2DMSArray", Group.SAMPLER),
        USAMPLER1D("usampler1D", Group.SAMPLER),
        USAMPLER2D("usampler2D", Group.SAMPLER),
        USAMPLER3D("usampler3D", Group.SAMPLER),
        USAMPLERCUBE("usamplerCube", Group.SAMPLER),
        USAMPLER2DRECT("usampler2DRect", Group.SAMPLER),
        USAMPLER1DARRAY("usampler1DArray", Group.SAMPLER),
        USAMPLER2DARRAY("usampler2DArray", Group.SAMPLER),
        USAMPLERCUBEARRAY("usamplerCubeArray", Group.SAMPLER),
        USAMPLERBUFFER("usamplerBuffer", Group.SAMPLER),
        USAMPLER2DMS("usampler2DMS", Group.SAMPLER),
        USAMPLER2DMSARRAY("usampler2DMSArray", Group.SAMPLER),
        IMAGE1D("image1D", Group.IMAGE),
        IMAGE2D("image1D", Group.IMAGE),
        IMAGE3D("image1D", Group.IMAGE),
        IMAGECUBE("imageCube", Group.IMAGE),
        IMAGE2DRECT("image2DRect", Group.IMAGE),
        IMAGE1DARRAY("image1DArray", Group.IMAGE),
        IMAGE2DARRAY("image2DArray", Group.IMAGE),
        IMAGECUBEARRAY("imageCubeArray", Group.IMAGE),
        IMAGEBUFFER("imageBuffer", Group.IMAGE),
        IMAGE2DMS("image2DMS", Group.IMAGE),
        IMAGE2DMSARRAY("image2DMSArray", Group.IMAGE),
        IIMAGE1D("iimage1D", Group.IMAGE),
        IIMAGE2D("iimage1D", Group.IMAGE),
        IIMAGE3D("iimage1D", Group.IMAGE),
        IIMAGECUBE("iimageCube", Group.IMAGE),
        IIMAGE2DRECT("iimage2DRect", Group.IMAGE),
        IIMAGE1DARRAY("iimage1DArray", Group.IMAGE),
        IIMAGE2DARRAY("iimage2DArray", Group.IMAGE),
        IIMAGECUBEARRAY("iimageCubeArray", Group.IMAGE),
        IIMAGEBUFFER("iimageBuffer", Group.IMAGE),
        IIMAGE2DMS("iimage2DMS", Group.IMAGE),
        IIMAGE2DMSARRAY("iimage2DMSArray", Group.IMAGE),
        ATOMICUINT("atomic_uint", Group.ATOMIC);

        private Group group;
        private String value;

        Type(String value, Group group) {
            this.group = group;
            this.value = value;
        }

        /**
         * Gives a type's identification.
         */
        public String getValue() {
            return value;
        }

        /**
         * Gives a type's category.
         */
        public Group getGroup() {
            return group;
        }
    }

    /**
     * Enumeration of shader member qualifiers.
     */
    public enum Qualifier {
        CONST("const"),
        IN("in"),
        OUT("out"),
        ATTRIBUTE("attribute"),
        UNIFORM("uniform"),
        VARYING("varying"),
        BUFFER("buffer"),
        SHARED("shared"),
        CENTROID("centroid"),
        SAMPLE("sample"),
        PATCH("patch"),
        SMOOTH("smooth"),
        FLAT("flat"),
        NOPERSPECTIVE("noperspective"),
        INOUT("inout"),
        HIGHP("highp"),
        MEDIUMP("mediump"),
        LOWP("lowp"),
        INVARIANT("invariant"),
        PRECISE("precise");

        private String value;

        Qualifier(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static final String VERSION = "430 core";

    private int program;
    private int vShader;
    private int fShader;
    private int execState;
    private String vertexSource;
    private String fragmentSource;
    private Variable execUniform;
    private List<Variable> inputs;
    private List<Variable> outputs;
    private List<Variable> varyings;
    private List<Variable> uniforms;
    private List<Variable> constants;
    private List<Sampler> samplers;
    private List<Function> functions;
    private List<Structure> structures;
    private List<UniformBuffer> uniformBuffers;
    private List<Executable> executables;
    private Map<String, String> definitions;

    public Shader() {
        execState = -1;
        vertexSource = "";
        fragmentSource = "";
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        varyings = new ArrayList<>();
        uniforms = new ArrayList<>();
        constants = new ArrayList<>();
        samplers = new ArrayList<>();
        functions = new ArrayList<>();
        structures = new ArrayList<>();
        executables = new ArrayList<>();
        uniformBuffers = new ArrayList<>();
        definitions = new HashMap<>();

        execUniform = new Variable(Type.INT, "executable", null, 0, Qualifier.UNIFORM);
        addVariable(execUniform);
    }

    /**
     * Registers a variable.
     *
     * @param variable - the variable to register
     */
    public void addVariable(Variable variable) {
        if (variable.hasQualifier(Qualifier.IN)) {
            if (inputs.contains(variable)) {
                throw new EngineException("Input variable already exists: " + variable.getIdentifier());
            }

            inputs.add(variable);
        } else if (variable.hasQualifier(Qualifier.OUT)) {
            if (outputs.contains(variable)) {
                throw new EngineException("Output variable already exists: " + variable.getIdentifier());
            }

            outputs.add(variable);
        } else if (variable.hasQualifier(Qualifier.VARYING)) {
            if (varyings.contains(variable)) {
                throw new EngineException("Varying variable already exists: " + variable.getIdentifier());
            }

            varyings.add(variable);
        } else if (variable.hasQualifier(Qualifier.UNIFORM)) {
            if (uniforms.contains(variable)) {
                throw new EngineException("Uniform variable already exists: " + variable.getIdentifier());
            }

            uniforms.add(variable);
        } else if (variable.hasQualifier(Qualifier.CONST)) {
            if (constants.contains(variable)) {
                throw new EngineException("Constant already exists: " + variable.getIdentifier());
            }

            if (variable.getValue() == null || variable.getValue().equals("")) {
                throw new EngineException("Value is required for constants.");
            }

            constants.add(variable);
        }

        if (variable instanceof Sampler) {
            samplers.add((Sampler) variable);
        }
    }

    /**
     * Gives input qualified variables.
     *
     * @param index - variable index
     *
     * @return variable
     */
    public Variable getInput(int index) {
        return inputs.get(index);
    }

    /**
     * Gives output qualified variables.
     *
     * @param index - variable index
     *
     * @return variable
     */
    public Variable getOutput(int index) {
        return outputs.get(index);
    }

    /**
     * Gives varying qualified variables.
     *
     * @param index - variable index
     *
     * @return variable
     */
    public Variable getVarying(int index) {
        return varyings.get(index);
    }

    /**
     * Gives uniform qualified variables.
     *
     * @param index - variable index
     *
     * @return variable
     */
    public Variable getUniform(int index) {
        return uniforms.get(index);
    }

    /**
     * Gives constant qualified variables.
     *
     * @param index - variable index
     *
     * @return variable
     */
    public Variable getConstant(int index) {
        return constants.get(index);
    }

    /**
     * Adds a definition (#define IDENTIFIER value).
     *
     * @param identifier - definition identifier
     * @param value - definition value
     */
    public void addDefinition(String identifier, String value) {
        if (definitions.containsKey(identifier)) {
            throw new EngineException("Definition already exists: " + identifier);
        }

        definitions.put(identifier, value);
    }

    /**
     * Gives the defintion with the given identifier.
     *
     * @param id - definition identifier
     *
     * @return value of the definition
     */
    public String getDefinition(String id) {
        return definitions.get(id);
    }

    /**
     * Registers a structure.
     *
     * @param structure - the structure to add
     */
    public void addStructure(Structure structure) {
        if (structures.contains(structure)) {
            throw new EngineException("Structure already exists: " + structure.getIdentifier());
        }

        structures.add(structure);
    }

    /**
     * Gives the structure at the given index.
     *
     * @param index - index of the structure
     *
     * @return the structure
     */
    public Structure getStructure(int index) {
        return structures.get(index);
    }

    /**
     * Registers a uniform buffer.
     *
     * @param buffer - the uniform buffer to add
     */
    public void addUniformBuffer(UniformBuffer buffer) {
        if (uniformBuffers.contains(buffer)) {
            throw new EngineException("Uniform buffer already exists: " + buffer.getIdentifier());
        }

        uniformBuffers.add(buffer);
    }

    /**
     * Gives the uniform buffer at the given index.
     *
     * @param index - index of the uniform buffer
     *
     * @return the uniform buffer
     */
    public UniformBuffer getUniformBuffer(int index) {
        return uniformBuffers.get(index);
    }

    /**
     * Registers a function.
     *
     * @param function - the function to add
     */
    public void addFunction(Function function) {
        if (functions.contains(function)) {
            throw new EngineException("Function already exists: " + function.getIdentifier());
        }

        functions.add(function);
    }

    /**
     * Gives the function at the given index.
     *
     * @param index - index of the function
     *
     * @return the function
     */
    public Function getFunction(int index) {
        return functions.get(index);
    }

    /**
     * Registers an executable.
     *
     * @param exec - the executable to add
     */
    public void addExecutable(Executable exec) {
        if (executables.contains(exec)) {
            throw new EngineException("Executable already exists: " + exec.getIdentifier());
        }

        executables.add(exec);
    }

    /**
     * Gives the executable at the given index.
     *
     * @param index - index of the executable
     *
     * @return the executable
     */
    public Executable getExecutable(int index) {
        return executables.get(index);
    }

    /**
     * Runs the given executable.
     *
     * @param exec - the excutable to run
     */
    public void execute(Executable exec) {
        if (exec.getID() != execState) {
            GL.setScalar(execUniform.getID(), exec.getID());
            execState = exec.getID();
        }
    }

    /**
     * Gives the source code of the built shader at the vertex stage.
     *
     * @return source code of the built shader at the vertex stage
     */
    public String getVertexSource() {
        return vertexSource;
    }

    /**
     * Gives the source code of the built shader at the fragment stage.
     *
     * @return source code of the built shader at the fragment stage
     */
    public String getFragmentSource() {
        return fragmentSource;
    }

    /**
     * Builds this shader.
     */
    public void build() {
        initSources();
        initProgram();
        initUniforms();
        initSamplers();
        initUniformBuffers();

        if (!GL.validateProgram(program)) {
            throw new EngineException(
                "Failed to initialize shaders.\n" +
                "Program: " + GL.getProgramInfo(program) + "\n" +
                "Vertex: " + GL.getShaderInfo(vShader) + "\n" +
                "Fragment: " + GL.getShaderInfo(fShader));
        }

        GL.freeShader(program, vShader);
        GL.freeShader(program, fShader);
    }

    /**
     * Frees all registered components.
     */
    public void free() {
        inputs.clear();
        outputs.clear();
        varyings.clear();
        uniforms.clear();
        constants.clear();
        samplers.clear();
        functions.clear();
        structures.clear();
        executables.clear();
        uniformBuffers.clear();
        definitions.clear();
    }

    /**
     * Builds the shader sources from the registered components.
     */
    private void initSources() {
        String version = buildVersion();
        String define = buildDefines();
        String input = buildInputs();
        String output = buildOutputs();
        String uniform = buildUniforms();
        String constant = buildConstants();
        String structure = buildStructures();
        String uniformBuffer = buildUniformBuffers();
        String varyingsVertex = buildVaryingsVertex();
        String varyingsFragment = buildVaryingsFragment();
        String functionsVertex = buildFunctionsVertex();
        String functionsFragment = buildFunctionsFragment();
        String executablesVertex = buildExecutablesVertex();
        String executablesFragment = buildExecutablesFragment();

        vertexSource += version;
        vertexSource += define;
        vertexSource += input;
        vertexSource += varyingsVertex;
        vertexSource += structure;
        vertexSource += uniformBuffer;
        vertexSource += uniform;
        vertexSource += constant;
        vertexSource += functionsVertex;
        vertexSource += executablesVertex;
        vertexSource += "\n";

        fragmentSource += version;
        fragmentSource += define;
        fragmentSource += output;
        fragmentSource += varyingsFragment;
        fragmentSource += structure;
        fragmentSource += uniformBuffer;
        fragmentSource += uniform;
        fragmentSource += constant;
        fragmentSource += functionsFragment;
        fragmentSource += executablesFragment;
        fragmentSource += "\n";
    }

    private String buildVersion() {
        return "#version " + VERSION + "\n\n";
    }

    private String buildDefines() {
        String source = "";

        for (int i = 0; i < executables.size(); i++) {
            Executable exec = executables.get(i);
            exec.id = i;
            source += "#define " + exec.getIdentifier() + " " + i + "\n";
        }

        for (String key : definitions.keySet()) {
            source += "#define " + key + " " + definitions.get(key) + "\n";
        }

        return source + "\n";
    }

    private String buildInputs() {
        String source = "";

        for (int i = 0; i < inputs.size(); i++) {
            Variable input = inputs.get(i);
            source += buildComment(input, "");
            source += "layout (location = " + i + ") " + buildVariable(input);
        }

        return source + "\n";
    }

    private String buildOutputs() {
        String source = "";

        for (int i = 0; i < outputs.size(); i++) {
            Variable output = outputs.get(i);
            source += buildComment(output, "");
            source += "layout (location = " + i + ") " + buildVariable(output);
        }

        return source + "\n";
    }

    private String buildVaryingsVertex() {
        String source = "";

        if (!varyings.isEmpty()) {
            source += "out VARYING {\n";

            for (Variable varying : varyings) {
                source += buildComment(varying, "    ");

                source += "    " + varying.getType().getValue();

                if (varying.getSize() > 0) {
                    source += "[" + varying.getSize() + "]";
                }

                source += " " + varying.getIdentifier() + ";\n";
            }

            source += "} vertex;\n";
        }

        return source + "\n";
    }

    private String buildVaryingsFragment() {
        String source = "";

        if (!varyings.isEmpty()) {
            source += "in VARYING {\n";

            for (Variable varying : varyings) {
                source += buildComment(varying, "    ");

                source += "    " + varying.getType().getValue();

                if (varying.getSize() > 0) {
                    source += "[" + varying.getSize() + "]";
                }

                source += " " + varying.getIdentifier() + ";\n";
            }

            source += "} fragment;\n";
        }

        return source + "\n";
    }

    private String buildUniforms() {
        String source = "";

        for (Variable uniform : uniforms) {
            source += buildComment(uniform, "");
            source += buildVariable(uniform);
        }

        return source + "\n";
    }

    private String buildConstants() {
        String source = "";

        for (Variable constant : constants) {
            source += buildComment(constant, "");
            source += buildVariable(constant);
        }

        return source + "\n";
    }

    private String buildStructures() {
        String source = "";

        for (Structure structure : structures) {
            source += "struct " + structure.getIdentifier() + " {\n";

            for (Variable member : structure.getMembers()) {
                source += buildComment(member, "    ");
                source += "    " + buildVariable(member);
            }

            source += "};\n\n";
        }

        source += "};\n\n";

        return source + "\n";
    }

    private String buildUniformBuffers() {
        String source = "";

        for (UniformBuffer buffer : uniformBuffers) {
            source += "layout (std140) uniform " + buffer.getIdentifier() + " {\n";
            source += "    " + buffer.getStructID() + "[" + buffer.getBufferSize() + "] " + buffer.getBufferID() + ";\n";
            source += "};\n\n";
        }

        return source + "\n";
    }

    private String buildFunctionsVertex() {
        String source = "";

        for (Function function : functions) {
            if (function.getStage() == Stage.VERTEX) {
                source += buildComment(function);
                source += function + "\n";
            }
        }

        return source + "\n";
    }

    private String buildFunctionsFragment() {
        String source = "";

        for (Function function : functions) {
            if (function.getStage() == Stage.FRAGMENT) {
                source += buildComment(function);
                source += function + "\n";
            }
        }

        return source + "\n";
    }

    private String buildExecutablesVertex() {
        String source = "";

        source += "void main() {\n";

        if (!executables.isEmpty()) {
            source += "    switch(" + execUniform.getIdentifier() + ") {\n";

            for (Executable exec : executables) {
                if (exec.getVertexSource() == null) {
                    continue;
                }

                source += buildComment(exec);
                source += "        case " + exec.getIdentifier() + ": {\n";

                String[] src = exec.getVertexSource().split("\n");

                for (String s : src) {
                    source += "            " + s + "\n";
                }

                source += "            break;\n";
                source += "        }\n";
            }

            source += "    }\n";
        }

        source += "}";

        return source + "\n";
    }

    private String buildExecutablesFragment() {
        String source = "";

        source += "void main() {\n";

        if (!executables.isEmpty()) {
            source += "    switch(" + execUniform.getIdentifier() + ") {\n";

            for (Executable exec : executables) {
                if (exec.getFragmentSource() == null) {
                    continue;
                }

                source += buildComment(exec);
                source += "        case " + exec.getIdentifier() + ": {\n";

                String[] src = exec.getFragmentSource().split("\n");

                for (String s : src) {
                    source += "            " + s + "\n";
                }

                source += "            break;\n";
                source += "        }\n";
            }

            source += "    }\n";
        }

        source += "}";

        return source + "\n";
    }

    private String buildComment(Variable variable, String tabs) {
        String source = "";

        if (variable.getComment() != null && !variable.getComment().equals("")) {
            String[] lines = variable.getComment().split("\n");

            source += tabs + "/**\n";

            for (String line : lines) {
                source += tabs + " * " + line + "\n";
            }

            source += tabs + " */\n";
        }

        return source;
    }

    private String buildComment(Function function) {
        String source = "";

        if (function.getComment() != null && !function.getComment().equals("")) {
            String[] lines = function.getComment().split("\n");

            source += "/**\n";

            for (String line : lines) {
                source += " * " + line + "\n";
            }

            for (Variable param : function.getParameters()) {
                if (param.getComment() != null && !param.getComment().equals("")) {
                    source += " * \n";
                    source += " * @param " + param.getIdentifier() + " - ";

                    lines = param.getComment().split("\n");

                    source += lines[0] + "\n";

                    for (int i = 1; i < lines.length; i++) {
                        String line = lines[i];
                        source += " * " + line + "\n";
                    }
                }
            }

            source += " */\n";
        }

        return source;
    }

    private String buildComment(Executable executable) {
        String source = "";

        if (executable.getComment() != null && !executable.getComment().equals("")) {
            String[] lines = executable.getComment().split("\n");

            source += "        /**\n";

            for (String line : lines) {
                source += "         * " + line + "\n";
            }

            source += "         */\n";
        }

        return source;
    }

    private String buildVariable(Variable variable) {
        String source = "";

        for (Qualifier qualifier : variable.getQualifiers()) {
            source += qualifier + " ";
        }

        source += variable.getType().getValue();

        if (variable.getSize() > 0) {
            source += "[" + variable.getSize() + "]";
        }

        source += " " + variable.getIdentifier();

        if (variable.getValue() != null && !variable.getValue().equals("")) {
            source += " = " + variable.getType().getValue();

            if (variable.getSize() > 0) {
                source += "[]";
            }

            source += "(" + variable.getValue() + ")";
        }

        source += ";\n";

        return source;
    }

    private void initProgram() {
        program = GL.createProgram();
        vShader = GL.createVertexShader(vertexSource);
        fShader = GL.createFragmentShader(fragmentSource);

        GL.attachShader(program, vShader);
        GL.attachShader(program, fShader);

        GL.linkProgram(program);

        GL.bindProgram(program);
    }

    private void initUniforms() {
        for (Variable uniform : uniforms) {
            uniform.id = GL.getUniformLocation(program, uniform.getIdentifier());
        }
    }

    private void initUniformBuffers() {
        for (UniformBuffer buffer : uniformBuffers) {
            buffer.id = GL.createUniformBuffer(program, buffer.getBufferSize() * buffer.getDataSize() * 4, buffer.getIdentifier());
        }
    }

    private void initSamplers() {
        int index = 0;

        for (Sampler sampler : samplers) {
            index = sampler.initUnits(index, this);
        }
    }
}
