package core.importer.geometry;

import core.Geometry;
import core.math.EngineMath;
import core.math.Vector2;
import core.math.Vector3;
import core.math.Vector4;
import core.utility.EngineException;
import core.ShapeGeometry;
import core.importer.GeometryImporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Constructs a geometry out of a given .dae file.
 *
 * @author John Paul Quijano
 */
public class DAEGeometryImporter extends GeometryImporter {
    private boolean flipUp;
    private ArrayList<Integer> indices;
    private ArrayList<Vector3> coords;
    private ArrayList<Vector3> normals;
    private ArrayList<Vector4> colors;
    private ArrayList<Vector2> texCoords;
    private ArrayList<Vector3> sourceCoords;
    private ArrayList<Vector3> sourceNormals;
    private ArrayList<Vector4> sourceColors;
    private ArrayList<Vector2> sourceTexCoords;
    private ArrayList<Float> sourceWeights;
    private HashMap<String, Integer> entries;
    private HashMap<String, List<Integer>> entryMap;

    private int stride;
    private int coordOffset;
    private int normalOffset;
    private int colorOffset;
    private int texCoordOffset;
    private int jointOffset;
    private int weightOffset;
    private Document doc;
    private DocumentBuilder docBuilder;

    /**
     * Some modelers, like Blender, may have the z-axis as the up-vector. If flipUp is
     * set, the y and z axes are flipped.
     */
    public DAEGeometryImporter(boolean flipUp) {
        super("dae");

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new EngineException("Failed to create parser: " + ex.getMessage());
        }

        this.flipUp = flipUp;
        coordOffset = -1;
        normalOffset = -1;
        colorOffset = -1;
        texCoordOffset = -1;
        jointOffset = -1;
        weightOffset = -1;
        entries = new HashMap<>();
        entryMap = new HashMap<>();
        indices = new ArrayList<>();
        coords = new ArrayList<>();
        normals = new ArrayList<>();
        colors = new ArrayList<>();
        texCoords = new ArrayList<>();
        sourceCoords = new ArrayList<>();
        sourceNormals = new ArrayList<>();
        sourceColors = new ArrayList<>();
        sourceTexCoords = new ArrayList<>();
        sourceWeights = new ArrayList<>();
    }

    /**
     * Parses a .dae file to a geometry object.
     * <p>
     * Note: Only Geometry.Type.TRIS is supported at this time.
     */
    @Override
    protected ShapeGeometry process(String path) {
        entries.clear();
        entryMap.clear();
        indices.clear();
        coords.clear();
        normals.clear();
        colors.clear();
        texCoords.clear();
        sourceCoords.clear();
        sourceNormals.clear();
        sourceColors.clear();
        sourceTexCoords.clear();
        sourceWeights.clear();

        try {
            doc = docBuilder.parse(new FileInputStream(path));
        } catch (SAXException ex) {
            throw new EngineException("Failed to parse file: " + ex.getMessage());
        } catch (IOException ex) {
            throw new EngineException("Failed to read file: " + ex.getMessage());
        }

        Element root = doc.getDocumentElement();
        Element geometries = (Element) root.getElementsByTagName("library_geometries").item(0);
        Element controllers = (Element) root.getElementsByTagName("library_controllers").item(0);
        Element geometry = (Element) geometries.getElementsByTagName("geometry").item(0);
        Element controller = (Element) controllers.getElementsByTagName("controller").item(0);

        NodeList geomComponents = geometry.getElementsByTagName("mesh").item(0).getChildNodes();
        NodeList skinComponents = null;

        ShapeGeometry geom = null;

        root.normalize();

        if (controller != null) {
            skinComponents = controller.getElementsByTagName("skin").item(0).getChildNodes();
        }

        /**
         * Parse geometry data.
         */
        for (int i = 0; i < geomComponents.getLength(); i++) {
            Node component = geomComponents.item(i);
            String name = component.getNodeName();

            if (name.equals("source")) { /** parse vertex attributes */
                Element source = (Element) component;
                String id = source.getAttribute("id");

                if (id.contains("positions")) { /** parse coordinates */
                    String[] data = source.getElementsByTagName("float_array").item(0).getTextContent().split(" ");
                    parseVector3(data, sourceCoords);
                } else if (id.contains("normals")) { /** parse normals */
                    String[] data = source.getElementsByTagName("float_array").item(0).getTextContent().split(" ");
                    parseVector3(data, sourceNormals);
                } else if (id.contains("map")) { /** parse texture coordinates */
                    String[] data = source.getElementsByTagName("float_array").item(0).getTextContent().split(" ");
                    parseVector2(data, sourceTexCoords);
                } else if (id.contains("colors")) { /** parse colors */
                    String[] data = source.getElementsByTagName("float_array").item(0).getTextContent().split(" ");
                    parseVector4(data, sourceColors);
                }
            } else if (name.equals("polylist")) { /** parse indices and construct geometry */
                Element polylist = (Element) component;
                NodeList inputs = polylist.getElementsByTagName("input");
                String[] data = polylist.getElementsByTagName("p").item(0).getTextContent().split(" ");

                for (int j = 0; j < inputs.getLength(); j++) {
                    Element input = (Element) inputs.item(j);
                    String semantic = input.getAttribute("semantic");
                    String offset = input.getAttribute("offset");

                    switch (semantic) {
                        case "VERTEX":
                            coordOffset = Integer.valueOf(offset);
                            stride++;
                            break;
                        case "NORMAL":
                            normalOffset = Integer.valueOf(offset);
                            stride++;
                            break;
                        case "COLOR":
                            colorOffset = Integer.valueOf(offset);
                            stride++;
                            break;
                        case "TEXCOORD":
                            texCoordOffset = Integer.valueOf(offset);
                            stride++;
                            break;
                    }
                }

                geom = buildGeometry(data);
            }
        }

        stride = 0;

        /**
         * Parse skinning data.
         */
        if (skinComponents != null) {
            for (int i = 0; i < skinComponents.getLength(); i++) {
                Node component = skinComponents.item(i);
                String name = component.getNodeName();

                if (name.equals("source")) { /** parse skin attributes */
                    Element source = (Element) component;
                    String id = source.getAttribute("id");

                    if (id.contains("weights")) { /** parse vertex weights */
                        String[] data = source.getElementsByTagName("float_array").item(0).getTextContent().split(" ");
                        parseFloat(data, sourceWeights);
                    }
                } else if (name.equals("vertex_weights")) { /** build skin */
                    Element vertexWeights = (Element) component;
                    NodeList inputs = vertexWeights.getElementsByTagName("input");
                    String[] data = vertexWeights.getElementsByTagName("v").item(0).getTextContent().split(" ");
                    String[] count = vertexWeights.getElementsByTagName("vcount").item(0).getTextContent().split(" ");

                    for (int j = 0; j < inputs.getLength(); j++) {
                        Element input = (Element) inputs.item(j);
                        String semantic = input.getAttribute("semantic");
                        String offset = input.getAttribute("offset");

                        switch (semantic) {
                            case "JOINT":
                                jointOffset = Integer.valueOf(offset);
                                stride++;
                                break;
                            case "WEIGHT":
                                weightOffset = Integer.valueOf(offset);
                                stride++;
                                break;
                        }
                    }

                    buildSkin(geom, data, count);
                }
            }
        }

        return geom;
    }

    private ShapeGeometry buildGeometry(String[] data) {
        for (int i = 0; i < data.length; i += stride) {
            Vector3 c = sourceCoords.get(Integer.valueOf(data[i + coordOffset]));
            Vector3 n = null;
            Vector4 o = null;
            Vector2 t = null;
            String entry = "";

            entry += c;

            if (texCoordOffset > -1) {
                t = sourceTexCoords.get(Integer.valueOf(data[i + texCoordOffset]));
                entry += t;
            }

            if (normalOffset > -1) {
                n = sourceNormals.get(Integer.valueOf(data[i + normalOffset]));
                entry += n;
            }

            if (colorOffset > -1) {
                o = sourceColors.get(Integer.valueOf(data[i + colorOffset]));
                entry += o;
            }

            if (entries.containsKey(entry)) {
                int index = entries.get(entry);
                indices.add(index);
            } else {
                coords.add(new Vector3(c));

                if (n != null) {
                    normals.add(new Vector3(n));
                }

                if (o != null) {
                    colors.add(new Vector4(o));
                }

                if (t != null) {
                    texCoords.add(new Vector2(t));
                }

                int index = coords.size() - 1;

                indices.add(index);
                entries.put(entry, index);

                String key = c.toString();

                if (entryMap.containsKey(key)) {
                    entryMap.get(key).add(index);
                } else {
                    ArrayList<Integer> indexList = new ArrayList<>();
                    indexList.add(index);
                    entryMap.put(key, indexList);
                }
            }
        }

        ShapeGeometry geom = new ShapeGeometry(Geometry.Type.TRIS, coords.size(), indices.size());

        geom.setIndices(indices);
        geom.setCoordinates(coords);

        if (!normals.isEmpty()) {
            geom.setNormals(normals);
        }

        if (!colors.isEmpty()) {
            geom.setColors(colors);
        }

        if (!texCoords.isEmpty()) {
            geom.setTextureCoordinates(texCoords);
        }

        return geom;
    }

    private void buildSkin(ShapeGeometry geom, String[] data, String[] count) {
        int dataIndex = 0;
        int[] joints = new int[ShapeGeometry.JOINTS_PER_VERTEX];
        float[] weights = new float[ShapeGeometry.JOINTS_PER_VERTEX];

        for (int i = 0; i < sourceCoords.size(); i++) {
            float weight = 0;
            int numJoints = Integer.valueOf(count[i]);
            Vector3 coord = sourceCoords.get(i);
            List<Integer> indexList = entryMap.get(coord.toString());

            for (int j = 0; j < ShapeGeometry.JOINTS_PER_VERTEX; j++) {
                if (j < numJoints) {
                    int jointIndex = Integer.valueOf(data[dataIndex + jointOffset]);
                    int weightIndex = Integer.valueOf(data[dataIndex + weightOffset]);

                    joints[j] = jointIndex;
                    weights[j] = sourceWeights.get(weightIndex);
                    dataIndex += stride;
                } else {
                    joints[j] = -1;
                    weights[j] = 0f;
                }

                weight += weights[j];
            }

            /** recalculate index pointer for skipped items */
            if (numJoints > ShapeGeometry.JOINTS_PER_VERTEX) {
                dataIndex += stride * (numJoints - ShapeGeometry.JOINTS_PER_VERTEX);
            }

            /** normalize weights */
            if (weight != 1f) {
                for (int k = 0; k < ShapeGeometry.JOINTS_PER_VERTEX; k++) {
                    weights[k] /= weight;
                }
            }

            for (Integer coordIndex : indexList) {
                geom.setJoints(coordIndex, joints);
                geom.setWeights(coordIndex, weights);
            }
        }
    }

    private void parseFloat(String[] data, List<Float> output) {
        for (String datum : data) {
            output.add(Float.parseFloat(datum));
        }
    }

    private void parseVector2(String[] data, List<Vector2> output) {
        int count = 0;
        float x = 0f;
        float y = 0f;

        for (String datum : data) {
            if (count == 0) {
                x = Float.parseFloat(datum);
            } else if (count == 1) {
                y = Float.parseFloat(datum);
            }

            if (count == 1) {
                output.add(new Vector2(x, y));
                count = 0;
            } else {
                count++;
            }
        }
    }

    private void parseVector3(String[] data, List<Vector3> output) {
        int count = 0;
        float x = 0f;
        float y = 0f;
        float z = 0f;
        float rad = EngineMath.toRadians(-90);
        float sin = EngineMath.sin(rad);
        float cos = EngineMath.cos(rad);

        for (String datum : data) {
            if (count == 0) {
                x = Float.parseFloat(datum);
            } else if (count == 1) {
                y = Float.parseFloat(datum);
            } else if (count == 2) {
                z = Float.parseFloat(datum);
            }

            if (count == 2) {
                Vector3 vec = new Vector3(x, y, z);

                if (flipUp) {
                    vec.setY(cos * y - sin * z);
                    vec.setZ(sin * y + cos * z);
                }

                output.add(vec);

                count = 0;
            } else {
                count++;
            }
        }
    }

    private void parseVector4(String[] data, List<Vector4> output) {
        int count = 0;
        float x = 0f;
        float y = 0f;
        float z = 0f;

        for (String datum : data) {
            if (count == 0) {
                x = Float.parseFloat(datum);
            } else if (count == 1) {
                y = Float.parseFloat(datum);
            } else if (count == 2) {
                z = Float.parseFloat(datum);
            }

            if (count == 2) {
                output.add(new Vector4(x, y, z, 1f));
                count = 0;
            } else {
                count++;
            }
        }
    }
}
