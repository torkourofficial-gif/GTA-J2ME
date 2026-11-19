package gta;

import javax.microedition.m3g.Group;
import javax.microedition.m3g.Mesh;

/** Compact low-poly pedestrian; twenty palette presets keep silhouettes readable. */
final class PedestrianModel {
    final Group root = new Group();
    private final Group torso = new Group();
    private final Group leftArm = new Group();
    private final Group rightArm = new Group();
    private final Group leftLeg = new Group();
    private final Group rightLeg = new Group();

    private static final int[] SHIRTS = {
        0x315C9A,0xA33C32,0x39764B,0xD0B43B,0x6B477E,
        0xD16A32,0x3D8587,0x777777,0xB7B7B7,0x27364E,
        0x8B3E67,0x6B6B35,0x4B6C38,0x8A593A,0x4A4A55,
        0xB04D3D,0x466A9B,0x8D7B55,0x36706A,0x6E536F
    };
    private static final int[] PANTS = {
        0x303C55,0x292929,0x4A5B72,0x565044,0x343434,
        0x263B63,0x4C443D,0x303A3A,0x555555,0x242B35,
        0x493447,0x41412D,0x35452F,0x514032,0x303038,
        0x3E3E49,0x2F4968,0x4B493D,0x2D4745,0x403546
    };
    private static final int[] SKINS = {
        0xA96543,0xC17B55,0x87543D,0xD09A72,0x754B37,
        0xB77B55,0x9B6247,0xD0A17E,0x704A38,0xB98565
    };

    PedestrianModel(int variant) {
        int v = variant % 20;
        int skin = SKINS[v % SKINS.length];
        addBox(root, 0.39f, 0.18f, 0.25f, 0, 0.86f, 0, PANTS[v]);
        addBox(torso, 0.43f, 0.57f, 0.27f, 0, 1.25f, 0, SHIRTS[v]);
        Mesh head = ModelFactory.ellipsoid(0.25f, 0.29f, 0.23f, skin);
        head.setTranslation(0, 1.70f, 0);
        torso.addChild(head);
        root.addChild(torso);

        leftArm.setTranslation(-0.27f, 1.43f, 0);
        rightArm.setTranslation(0.27f, 1.43f, 0);
        addBox(leftArm, 0.14f, 0.48f, 0.16f, 0, -0.23f, 0, skin);
        addBox(rightArm, 0.14f, 0.48f, 0.16f, 0, -0.23f, 0, skin);
        torso.addChild(leftArm);
        torso.addChild(rightArm);

        leftLeg.setTranslation(-0.12f, 0.78f, 0);
        rightLeg.setTranslation(0.12f, 0.78f, 0);
        addBox(leftLeg, 0.17f, 0.54f, 0.20f, 0, -0.27f, 0, PANTS[v]);
        addBox(rightLeg, 0.17f, 0.54f, 0.20f, 0, -0.27f, 0, PANTS[v]);
        root.addChild(leftLeg);
        root.addChild(rightLeg);
    }

    private static void addBox(Group parent, float w, float h, float d,
                               float x, float y, float z, int color) {
        Mesh mesh = ModelFactory.box(w, h, d, color);
        mesh.setTranslation(x, y, z);
        parent.addChild(mesh);
    }

    void setPosition(float x, float y, float z) { root.setTranslation(x, y, z); }
    void setHeading(float degrees) { root.setOrientation(degrees, 0, 1, 0); }
    void setVisible(boolean visible) { root.setRenderingEnable(visible); }

    void animate(float phase, boolean moving, boolean running, boolean down) {
        float swing = moving ? (float)Math.sin(phase) * (running ? 34.0f : 22.0f) : 0.0f;
        float crouch = down ? 72.0f : 0.0f;
        leftLeg.setOrientation(swing + crouch, 1, 0, 0);
        rightLeg.setOrientation(-swing + crouch, 1, 0, 0);
        leftArm.setOrientation(-swing * 0.7f, 1, 0, 0);
        rightArm.setOrientation(swing * 0.7f, 1, 0, 0);
        torso.setOrientation(down ? 65.0f : (moving ? (float)Math.sin(phase * 2.0f) * 2.0f : 0.0f),
                             1, 0, 0);
    }

    // A cheap four-part ragdoll pose suitable for low-memory Java ME devices.
    // The body segments remain separate M3G groups, so each can rotate freely.
    void animateRunAway(float phase) {
        float swing = (float)Math.sin(phase) * 34.0f;
        leftLeg.setOrientation(swing, 1, 0, 0);
        rightLeg.setOrientation(-swing, 1, 0, 0);
        leftArm.setOrientation(-swing * 0.75f, 1, 0, 0);
        rightArm.setOrientation(swing * 0.75f, 1, 0, 0);
        torso.setOrientation(-5.0f, 1, 0, 0);
    }

    void animateRagdoll(float roll, float pitch) {
        torso.setOrientation(72.0f + pitch * 0.35f, 1.0f, 0.0f, 0.0f);
        leftArm.setOrientation(-38.0f + roll * 0.55f, 0.0f, 0.0f, 1.0f);
        rightArm.setOrientation(42.0f - roll * 0.55f, 0.0f, 0.0f, 1.0f);
        leftLeg.setOrientation(68.0f + pitch * 0.25f, 1.0f, 0.0f, 0.0f);
        rightLeg.setOrientation(48.0f - pitch * 0.25f, 1.0f, 0.0f, 0.0f);
    }
}
