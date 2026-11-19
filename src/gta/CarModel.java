package gta;

import javax.microedition.m3g.Group;
import javax.microedition.m3g.Mesh;

/** One compact procedural sedan used for the first driving prototype. */
final class CarModel {
    final Group root = new Group();

    CarModel() {
        addBox(root, 1.55f, 0.42f, 2.65f, 0.0f, 0.48f, 0.0f, 0xB72E32);
        addBox(root, 1.28f, 0.48f, 1.30f, 0.0f, 0.91f, -0.12f, 0xA8C8D8);
        addBox(root, 1.38f, 0.12f, 0.24f, 0.0f, 0.64f, 1.20f, 0x252525);
        addBox(root, 1.38f, 0.12f, 0.24f, 0.0f, 0.64f, -1.20f, 0x252525);

        // Wheels are deliberately simple blocks for this first milestone.
        addBox(root, 0.22f, 0.48f, 0.58f, -0.78f, 0.32f, 0.82f, 0x171717);
        addBox(root, 0.22f, 0.48f, 0.58f, 0.78f, 0.32f, 0.82f, 0x171717);
        addBox(root, 0.22f, 0.48f, 0.58f, -0.78f, 0.32f, -0.82f, 0x171717);
        addBox(root, 0.22f, 0.48f, 0.58f, 0.78f, 0.32f, -0.82f, 0x171717);
        addBox(root, 0.18f, 0.16f, 0.10f, -0.48f, 0.48f, 1.34f, 0xFFF0B0);
        addBox(root, 0.18f, 0.16f, 0.10f, 0.48f, 0.48f, 1.34f, 0xFFF0B0);
    }

    private static void addBox(Group parent, float w, float h, float d,
                               float x, float y, float z, int color) {
        Mesh mesh = ModelFactory.box(w, h, d, color);
        mesh.setTranslation(x, y, z);
        parent.addChild(mesh);
    }

    void setPosition(float x, float y, float z) {
        root.setTranslation(x, y, z);
    }

    void setHeading(float degrees) {
        root.setOrientation(degrees, 0.0f, 1.0f, 0.0f);
    }
}
