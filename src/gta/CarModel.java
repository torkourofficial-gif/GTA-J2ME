package gta;

import javax.microedition.m3g.Group;
import javax.microedition.m3g.Mesh;

/** Procedural sedan with visible cabin, seats, dashboard and an animated driver door. */
final class CarModel {
    final Group root = new Group();
    private final Group driverDoor = new Group();
    private final Group passengerDoor = new Group();

    CarModel() {
        this(0xB72E32);
    }

    CarModel(int bodyColor) {
        // Lower body, hood and trunk.
        addBox(root, 1.62f, 0.42f, 2.75f, 0.0f, 0.48f, 0.0f, bodyColor);
        addBox(root, 1.58f, 0.18f, 0.72f, 0.0f, 0.80f, -1.02f, bodyColor);
        addBox(root, 1.58f, 0.18f, 0.58f, 0.0f, 0.80f, 1.06f, bodyColor);

        // Cabin frame and windows. The open middle leaves the interior visible.
        addBox(root, 1.35f, 0.10f, 1.40f, 0.0f, 1.39f, -0.12f, bodyColor);
        addBox(root, 0.10f, 0.58f, 1.35f, -0.66f, 1.08f, -0.12f, bodyColor);
        addBox(root, 0.10f, 0.58f, 1.35f, 0.66f, 1.08f, -0.12f, bodyColor);
        addBox(root, 1.18f, 0.42f, 0.045f, 0.0f, 1.08f, -0.79f, 0x79AFC1);
        addBox(root, 1.18f, 0.42f, 0.045f, 0.0f, 1.08f, 0.55f, 0x79AFC1);

        // Cabin floor, seats, dashboard, steering wheel and center console.
        addBox(root, 1.22f, 0.10f, 1.30f, 0.0f, 0.76f, -0.10f, 0x292727);
        addBox(root, 0.48f, 0.18f, 0.48f, -0.34f, 0.91f, -0.36f, 0x393331);
        addBox(root, 0.48f, 0.18f, 0.48f, 0.34f, 0.91f, -0.36f, 0x393331);
        addBox(root, 0.48f, 0.44f, 0.14f, -0.34f, 1.12f, -0.55f, 0x413A36);
        addBox(root, 0.48f, 0.44f, 0.14f, 0.34f, 1.12f, -0.55f, 0x413A36);
        addBox(root, 1.20f, 0.18f, 0.18f, 0.0f, 1.02f, -0.78f, 0x242424);
        addBox(root, 0.10f, 0.10f, 0.10f, -0.42f, 1.04f, -0.89f, 0xD5D0C4);
        addBox(root, 0.30f, 0.06f, 0.30f, -0.34f, 1.17f, -0.60f, 0x202020);

        // Bumpers, grille and lamps.
        addBox(root, 1.48f, 0.13f, 0.20f, 0.0f, 0.39f, -1.43f, 0x292929);
        addBox(root, 1.48f, 0.13f, 0.20f, 0.0f, 0.39f, 1.43f, 0x292929);
        addBox(root, 0.48f, 0.13f, 0.035f, 0.0f, 0.52f, -1.40f, 0x333333);
        addBox(root, 0.20f, 0.15f, 0.08f, -0.53f, 0.55f, -1.42f, 0xFFF0B0);
        addBox(root, 0.20f, 0.15f, 0.08f, 0.53f, 0.55f, -1.42f, 0xFFF0B0);
        addBox(root, 0.18f, 0.14f, 0.08f, -0.53f, 0.55f, 1.42f, 0xB82020);
        addBox(root, 0.18f, 0.14f, 0.08f, 0.53f, 0.55f, 1.42f, 0xB82020);

        // Four low-poly wheels with simple hubcaps.
        addWheel(-0.82f, 0.32f, -0.88f);
        addWheel(0.82f, 0.32f, -0.88f);
        addWheel(-0.82f, 0.32f, 0.88f);
        addWheel(0.82f, 0.32f, 0.88f);

        // Both front doors are separate hinged assemblies. Their panels sit
        // just outside the body shell to avoid coplanar/z-fighting surfaces.
        driverDoor.setTranslation(-0.84f, 0.0f, -0.70f);
        addBox(driverDoor, 0.065f, 0.46f, 0.78f, -0.025f, 0.78f, 0.39f, bodyColor);
        addBox(driverDoor, 0.035f, 0.31f, 0.66f, -0.070f, 1.15f, 0.39f, 0x79AFC1);
        addBox(driverDoor, 0.08f, 0.035f, 0.12f, -0.055f, 0.92f, 0.42f, 0xD6C9B4);
        root.addChild(driverDoor);

        passengerDoor.setTranslation(0.84f, 0.0f, -0.70f);
        addBox(passengerDoor, 0.065f, 0.46f, 0.78f, 0.025f, 0.78f, 0.39f, bodyColor);
        addBox(passengerDoor, 0.035f, 0.31f, 0.66f, 0.070f, 1.15f, 0.39f, 0x79AFC1);
        addBox(passengerDoor, 0.08f, 0.035f, 0.12f, 0.055f, 0.92f, 0.42f, 0xD6C9B4);
        root.addChild(passengerDoor);
        addBox(root, 0.16f, 0.12f, 0.16f, -0.82f, 1.24f, -0.50f, bodyColor);
        addBox(root, 0.16f, 0.12f, 0.16f, 0.82f, 1.24f, -0.50f, bodyColor);
    }

    private void addWheel(float x, float y, float z) {
        addBox(root, 0.22f, 0.48f, 0.58f, x, y, z, 0x171717);
        addBox(root, 0.235f, 0.20f, 0.20f, x + (x < 0 ? 0.12f : -0.12f),
               y, z, 0xA7A7A7);
    }

    private static void addBox(Group parent, float w, float h, float d,
                               float x, float y, float z, int color) {
        Mesh mesh = ModelFactory.box(w, h, d, color);
        mesh.setTranslation(x, y, z);
        parent.addChild(mesh);
    }

    void setDoorOpen(float degrees, boolean driverSide) {
        if (degrees < 0.0f) degrees = 0.0f;
        if (degrees > 72.0f) degrees = 72.0f;
        driverDoor.setOrientation(driverSide ? -degrees : 0.0f, 0.0f, 1.0f, 0.0f);
        passengerDoor.setOrientation(driverSide ? 0.0f : degrees, 0.0f, 1.0f, 0.0f);
    }

    void setPosition(float x, float y, float z) {
        root.setTranslation(x, y, z);
    }

    void setHeading(float degrees) {
        root.setOrientation(degrees, 0.0f, 1.0f, 0.0f);
    }
}
