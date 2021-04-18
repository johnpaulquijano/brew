package core.importer.animation;

import core.math.EngineMath;
import core.math.Matrix3;
import core.math.Matrix4;
import core.math.Vector3;
import core.utility.EngineException;
import core.utility.Pools;
import core.animation.Animation;
import core.animation.Frame;
import core.animation.Joint;
import core.importer.AnimationImporter;
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
import java.util.Arrays;
import java.util.HashMap;

/**
 * Constructs an animation out of a given .dae file.
 *
 * @author John Paul Quijano
 */
public class DAEAnimationImporter extends AnimationImporter {
    private boolean flipUp;
    private ArrayList<String> jointOrder;
    private ArrayList<Frame> keyFrames;
    private HashMap<String, String[]> transformsMap;
    private Joint bindPose;
    private Document doc;
    private DocumentBuilder docBuilder;

    /**
     * Some modelers such as Blender may have the z-axis as the up-vector. If flipUp is
     * set, the y and z axes are flipped.
     */
    public DAEAnimationImporter(boolean flipUp) {
        super("dae");

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new EngineException("Failed to create parser: " + ex.getMessage());
        }

        this.flipUp = flipUp;
        transformsMap = new HashMap<>();
        jointOrder = new ArrayList<>();
        keyFrames = new ArrayList<>();
    }

    /**
     * Parses a .dae file to an animation object.
     */
    @Override
    protected Animation process(String path) {
        transformsMap.clear();
        jointOrder.clear();
        keyFrames.clear();

        try {
            doc = docBuilder.parse(new FileInputStream(path));
        } catch (SAXException ex) {
            throw new EngineException("Failed to parse file: " + ex.getMessage());
        } catch (IOException ex) {
            throw new EngineException("Failed to read file: " + ex.getMessage());
        }

        float[] transform = new float[16];
        Animation animation = new Animation();
        Matrix3 rotation = Pools.Matrix3.get();
        Matrix4 correction = Pools.Matrix4.get();
        Matrix4 matrix = Pools.Matrix4.get();
        NodeList boneComponents = null;
        NodeList poseComponents = null;
        NodeList skinComponents = null;
        Element root = doc.getDocumentElement();
        Element libraryVisualScenes = (Element) root.getElementsByTagName("library_visual_scenes").item(0);
        Element libraryAnimations = (Element) root.getElementsByTagName("library_animations").item(0);
        Element libraryControllers = (Element) root.getElementsByTagName("library_controllers").item(0);
        Element visualScene = (Element) libraryVisualScenes.getElementsByTagName("visual_scene").item(0);
        Element controller = (Element) libraryControllers.getElementsByTagName("controller").item(0);

        rotation.fromAngleAxis(EngineMath.toRadians(90f), Vector3.UNIT_X);
        correction.set(Matrix4.IDENTITY).set(rotation);

        root.normalize();

        if (visualScene != null) {
            NodeList nodes = visualScene.getElementsByTagName("node");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element node = (Element) nodes.item(i);

                if (node.getAttribute("id").equals("Armature")) {
                    boneComponents = node.getChildNodes();
                }
            }
        }

        if (libraryAnimations != null) {
            poseComponents = libraryAnimations.getElementsByTagName("animation");
        }

        if (controller != null) {
            skinComponents = controller.getElementsByTagName("skin").item(0).getChildNodes();
        }

        /** parse joint index order */
        if (skinComponents != null) {
            for (int i = 0; i < skinComponents.getLength(); i++) {
                Node component = skinComponents.item(i);
                String name = component.getNodeName();

                if (name.equals("source")) {
                    Element source = (Element) component;

                    if (source.getAttribute("id").contains("joints")) {
                        String[] data = source.getElementsByTagName("Name_array").item(0).getTextContent().split(" ");
                        jointOrder.addAll(Arrays.asList(data));
                    }
                }
            }
        }

        /** construct bind pose */
        if (boneComponents != null) {
            for (int i = 0; i < boneComponents.getLength(); i++) {
                Node component = boneComponents.item(i);
                String name = component.getNodeName();

                if (name.equals("node")) {
                    bindPose = parseBonesHierarchy((Element) component, null, correction);
                    bindPose.setOrder(jointOrder);
                    bindPose.collapse();
                    bindPose.cascade();
                    bindPose.invert();
                }
            }
        }

        /** construct animation */
        if (poseComponents != null) {
            for (int i = 0; i < poseComponents.getLength(); i++) {
                Element joint = (Element) poseComponents.item(i);
                NodeList sources = joint.getElementsByTagName("source");
                String name = ((Element) joint.getElementsByTagName("channel").item(0)).getAttribute("target").split("/")[0];

                for (int j = 0; j < sources.getLength(); j++) {
                    Element source = (Element) sources.item(j);
                    String id = source.getAttribute("id");

                    if (id.endsWith("matrix-input")) { /** parse animation times */
                        String[] times = source.getElementsByTagName("float_array").item(0).getTextContent().split(" ");

                        if (keyFrames.isEmpty()) {
                            for (String time : times) {
                                Frame keyFrame = new Frame();
                                keyFrame.setTime(Float.valueOf(time));
                                keyFrames.add(keyFrame);
                            }
                        }
                    } else if (id.endsWith("matrix-output")) { /** parse pose transformations */
                        String[] transforms = source.getElementsByTagName("float_array").item(0).getTextContent().split(" ");
                        transformsMap.put(name, transforms);
                    }
                }
            }

            /** construct each keyframe's pose transformations */
            for (int i = 0; i < keyFrames.size(); i++) {
                int index = i * 16;
                Frame keyFrame = keyFrames.get(i);
                Joint pose = bindPose.deepCopy().collapse();

                for (int j = 0; j < pose.numJoints(); j++) {
                    Joint joint = pose.getJoint(j);
                    String[] transforms = transformsMap.get(joint.getName());

                    for (int k = 0; k < 16; k++) {
                        transform[k] = Float.valueOf(transforms[index + k]);
                    }

                    matrix.set(
                            transform[0], transform[1], transform[2], transform[3],
                            transform[4], transform[5], transform[6], transform[7],
                            transform[8], transform[9], transform[10], transform[11],
                            transform[12], transform[13], transform[14], transform[15]
                    ).transpose();

                    if (flipUp && joint.isRoot()) {
                        matrix.multiply(correction);
                    }

                    joint.setTransform(matrix);
                }

                keyFrame.setPose(pose.cascade());
            }

            animation.addKeyFrames(keyFrames);
        }

        animation.setBind(bindPose);

        Pools.Matrix3.put(rotation);
        Pools.Matrix4.put(correction);
        Pools.Matrix4.put(matrix);

        return animation;
    }

    private Joint parseBonesHierarchy(Element element, Joint parent, Matrix4 correction) {
        NodeList children = element.getChildNodes();
        String name = element.getAttribute("id");
        String[] transform = element.getElementsByTagName("matrix").item(0).getTextContent().split(" ");

        Joint joint = new Joint();

        Matrix4 matrix = Pools.Matrix4.get();

        matrix.set(
                Float.valueOf(transform[0]), Float.valueOf(transform[1]), Float.valueOf(transform[2]), Float.valueOf(transform[3]),
                Float.valueOf(transform[4]), Float.valueOf(transform[5]), Float.valueOf(transform[6]), Float.valueOf(transform[7]),
                Float.valueOf(transform[8]), Float.valueOf(transform[9]), Float.valueOf(transform[10]), Float.valueOf(transform[11]),
                Float.valueOf(transform[12]), Float.valueOf(transform[13]), Float.valueOf(transform[14]), Float.valueOf(transform[15])
        ).transpose();

        if (parent != null) {
            parent.addChild(joint);
        } else if (flipUp) {
            matrix.multiply(correction);
        }

        joint.setName(name);
        joint.setTransform(matrix);

        Pools.Matrix4.put(matrix);

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equals("node")) {
                parseBonesHierarchy((Element) children.item(i), joint, correction);
            }
        }

        return joint;
    }
}
