package core.importer.geometry;

import core.Geometry;
import core.math.Vector2;
import core.math.Vector3;
import core.utility.EngineException;
import core.ShapeGeometry;
import core.importer.GeometryImporter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Constructs a geometry out of a given .obj file.
 *
 * @author John Paul Quijano
 */
public class OBJGeometryImporter extends GeometryImporter {
    private HashMap<String, Integer> entries;
    private ArrayList<Integer> indices;
    private ArrayList<Vector3> coords;
    private ArrayList<Vector3> normals;
    private ArrayList<Vector2> texCoords;
    private ArrayList<Vector3> sourceCoords;
    private ArrayList<Vector3> sourceNormals;
    private ArrayList<Vector2> sourceTexCoords;

    public OBJGeometryImporter() {
        super("obj");

        entries = new HashMap<>();
        indices = new ArrayList<>();
        coords = new ArrayList<>();
        normals = new ArrayList<>();
        texCoords = new ArrayList<>();
        sourceCoords = new ArrayList<>();
        sourceNormals = new ArrayList<>();
        sourceTexCoords = new ArrayList<>();
    }

    /**
     * Parses a .obj file to a geometry object.
     * <p>
     * Note: Only Geometry.Type.TRIS is supported at this time.
     */
    @Override
    protected ShapeGeometry process(String path) {
        entries.clear();
        indices.clear();
        coords.clear();
        normals.clear();
        texCoords.clear();
        sourceCoords.clear();
        sourceNormals.clear();
        sourceTexCoords.clear();


        try {
            String line;
            InputStream stream = new FileInputStream(path);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                while ((line = reader.readLine()) != null) {
                    String[] source = line.split(" ");

                    switch (source[0]) {
                        case "v":
                            Vector3 coord = new Vector3();
                            coord.setX(Float.parseFloat(source[1]));
                            coord.setY(Float.parseFloat(source[2]));
                            coord.setZ(Float.parseFloat(source[3]));
                            sourceCoords.add(coord);
                            break;
                        case "vt":
                            Vector2 texCoord = new Vector2();
                            texCoord.setX(Float.parseFloat(source[1]));
                            texCoord.setY(Float.parseFloat(source[2]));
                            sourceTexCoords.add(texCoord);
                            break;
                        case "vn":
                            Vector3 normal = new Vector3();
                            normal.setX(Float.parseFloat(source[1]));
                            normal.setY(Float.parseFloat(source[2]));
                            normal.setZ(Float.parseFloat(source[3]));
                            sourceNormals.add(normal);
                            break;
                        case "f":
                            for (int i = 1; i <= 3; i++) {
                                String[] indexTrio = source[i].split("/");
                                Vector3 c = sourceCoords.get(Integer.parseInt(indexTrio[0]) - 1);
                                Vector2 t = null;
                                Vector3 n = null;
                                String entry = "";

                                entry += c;

                                if (!indexTrio[1].equals("")) {
                                    t = sourceTexCoords.get(Integer.parseInt(indexTrio[1]) - 1);
                                    entry += t;
                                }

                                if (!indexTrio[2].equals("")) {
                                    n = sourceNormals.get(Integer.parseInt(indexTrio[2]) - 1);
                                    entry += n;
                                }

                                if (entries.containsKey(entry)) {
                                    indices.add(entries.get(entry));
                                } else {
                                    coords.add(c);

                                    if (t != null) {
                                        texCoords.add(new Vector2(t));
                                    }

                                    if (n != null) {
                                        normals.add(new Vector3(n));
                                    }

                                    int index = coords.size() - 1;

                                    entries.put(entry, index);
                                    indices.add(index);
                                }
                            }
                    }
                }
            } catch (FileNotFoundException ex) {
                throw new EngineException("Cannot locate file: " + ex.getMessage());
            } catch (IOException ex) {
                throw new EngineException("Failed to close file: " + ex.getMessage());
            }
        } catch (FileNotFoundException ex) {
            throw new EngineException("Cannot locate file: " + ex.getMessage());
        }

        ShapeGeometry geometry = new ShapeGeometry(Geometry.Type.TRIS, coords.size(), indices.size());

        geometry.setIndices(indices);
        geometry.setCoordinates(coords);

        if (!texCoords.isEmpty()) {
            geometry.setTextureCoordinates(texCoords);
        }

        if (!normals.isEmpty()) {
            geometry.setNormals(normals);
        }

        return geometry;
    }
}
