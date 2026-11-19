package gta;

import javax.microedition.m3g.Group;
import javax.microedition.m3g.Mesh;

/** Compact low-poly motorcycle for the Java ME traffic and player vehicle system. */
final class MotorcycleModel {
    final Group root = new Group();

    MotorcycleModel(int bodyColor) {
        // Two wheels, narrow chassis, tank, seat and handlebar.
        addWheel(0.0f, 0.34f, -0.82f);
        addWheel(0.0f, 0.34f, 0.82f);
        addBox(0.16f, 0.20f, 1.15f, 0.0f, 0.60f, 0.0f, 0x343434);
        addBox(0.43f, 0.34f, 0.58f, 0.0f, 0.88f, -0.12f, bodyColor);
        addBox(0.38f, 0.13f, 0.55f, 0.0f, 1.02f, 0.43f, 0x242424);
        addBox(0.52f, 0.08f, 0.20f, 0.0f, 1.13f, -0.63f, 0x333333);
        addBox(0.62f, 0.07f, 0.12f, 0.0f, 1.20f, -0.70f, 0xA8A8A8);
        addBox(0.20f, 0.18f, 0.12f, 0.0f, 0.88f, -0.77f, 0xFFF0B0);
        addBox(0.24f, 0.13f, 0.12f, 0.0f, 0.72f, 0.78f, 0xB82020);
        addBox(0.09f, 0.45f, 0.10f, -0.18f, 0.72f, -0.28f, 0x777777);
        addBox(0.09f, 0.45f, 0.10f, 0.18f, 0.72f, -0.28f, 0x777777);
        addBox(0.09f, 0.08f, 0.36f, -0.28f, 1.18f, -0.70f, 0x242424);
        addBox(0.09f, 0.08f, 0.36f, 0.28f, 1.18f, -0.70f, 0x242424);
    }

    private void addWheel(float x, float y, float z) {
        Mesh tire = ModelFactory.ellipsoid(0.22f, 0.58f, 0.58f, 0x171717);
        tire.setTranslation(x, y, z);
        root.addChild(tire);
        Mesh hub = ModelFactory.ellipsoid(0.24f, 0.25f, 0.25f, 0xA7A7A7);
        hub.setTranslation(x, y, z);
        root.addChild(hub);
    }

    private void addBox(float w, float h, float d, float x, float y, float z, int color) {
        Mesh mesh = ModelFactory.box(w, h, d, color);
        mesh.setTranslation(x, y, z);
        root.addChild(mesh);
    }

    void setPosition(float x, float y, float z) { root.setTranslation(x, y, z); }
    void setHeading(float degrees) { root.setOrientation(degrees, 0.0f, 1.0f, 0.0f); }
    void setVisible(boolean visible) { root.setRenderingEnable(visible); }
}
