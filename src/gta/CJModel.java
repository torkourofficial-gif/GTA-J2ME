package gta;

import javax.microedition.m3g.Group;
import javax.microedition.m3g.Mesh;

/**
 * Procedural low-poly CJ. Separate pivots for shoulders, elbows, hips and knees
 * keep the model light enough for small Java ME/M3G devices.
 */
final class CJModel {
    final Group root = new Group();
    final Group leftArm = new Group();
    final Group rightArm = new Group();
    final Group leftLeg = new Group();
    final Group rightLeg = new Group();

    private final Group upperBody = new Group();
    private final Group leftForearm = new Group();
    private final Group rightForearm = new Group();
    private final Group leftShin = new Group();
    private final Group rightShin = new Group();

    private float crouchBlend;
    private float jumpBlend;
    private float gaitBlend;
    private float turnBlend;

    // CJ's palette: white tank, blue loose jeans, dark sneakers, warm skin.
    private static final int SKIN = 0xA96543;
    private static final int SKIN_LIGHT = 0xC17B55;
    private static final int SKIN_DARK = 0x75432F;
    private static final int SHIRT = 0xE9E9E2;
    private static final int SHIRT_SHADE = 0xBFC2C0;
    private static final int JEANS = 0x344D78;
    private static final int JEANS_LIGHT = 0x526B91;
    private static final int JEANS_DARK = 0x253653;
    private static final int SHOE = 0x292725;

    CJModel() {
        // Waist, belt and denim waistband.
        addBox(root, 0.43f, 0.20f, 0.29f, 0.0f, 0.82f, 0.0f, JEANS);
        addBox(root, 0.45f, 0.045f, 0.30f, 0.0f, 0.92f, 0.0f, 0xB6A17A);
        addBox(root, 0.07f, 0.055f, 0.025f, 0.0f, 0.92f, -0.16f, 0xC7B77D);

        // Tank top and its shoulder straps, with a visible skin neckline.
        addOval(upperBody, 0.48f, 0.54f, 0.27f, 0.0f, 1.19f, 0.0f, SHIRT);
        addBox(upperBody, 0.15f, 0.34f, 0.29f, -0.16f, 1.30f, 0.0f, SHIRT);
        addBox(upperBody, 0.15f, 0.34f, 0.29f, 0.16f, 1.30f, 0.0f, SHIRT);
        addBox(upperBody, 0.18f, 0.15f, 0.18f, 0.0f, 1.52f, 0.0f, SKIN);
        addOval(upperBody, 0.22f, 0.29f, 0.22f, 0.0f, 1.72f, 0.0f, SKIN);
        // Ears, nose, brows and eyes face local -Z.
        addBox(upperBody, 0.045f, 0.09f, 0.055f, -0.125f, 1.73f, -0.005f, SKIN_LIGHT);
        addBox(upperBody, 0.045f, 0.09f, 0.055f, 0.125f, 1.73f, -0.005f, SKIN_LIGHT);
        addBox(upperBody, 0.045f, 0.075f, 0.045f, 0.0f, 1.69f, -0.125f, SKIN_LIGHT);
        addBox(upperBody, 0.055f, 0.018f, 0.018f, -0.055f, 1.77f, -0.116f, 0x29201C);
        addBox(upperBody, 0.055f, 0.018f, 0.018f, 0.055f, 1.77f, -0.116f, 0x29201C);
        addBox(upperBody, 0.027f, 0.025f, 0.012f, -0.052f, 1.735f, -0.122f, 0x171717);
        addBox(upperBody, 0.027f, 0.025f, 0.012f, 0.052f, 1.735f, -0.122f, 0x171717);
        addBox(upperBody, 0.13f, 0.045f, 0.23f, 0.0f, 1.87f, 0.0f, 0x24201D);
        addBox(upperBody, 0.23f, 0.065f, 0.22f, 0.0f, 1.82f, 0.0f, 0x30251F);
        root.addChild(upperBody);

        // Shoulder pivots. Forearms and hands rotate independently at elbows.
        leftArm.setTranslation(-0.29f, 1.43f, 0.0f);
        rightArm.setTranslation(0.29f, 1.43f, 0.0f);
        addOval(leftArm, 0.17f, 0.36f, 0.18f, 0.0f, -0.17f, 0.0f, SKIN);
        addOval(rightArm, 0.17f, 0.36f, 0.18f, 0.0f, -0.17f, 0.0f, SKIN);
        leftForearm.setTranslation(0.0f, -0.34f, 0.0f);
        rightForearm.setTranslation(0.0f, -0.34f, 0.0f);
        addOval(leftForearm, 0.145f, 0.34f, 0.15f, 0.0f, -0.16f, 0.0f, SKIN_LIGHT);
        addOval(rightForearm, 0.145f, 0.34f, 0.15f, 0.0f, -0.16f, 0.0f, SKIN_LIGHT);
        addBox(leftForearm, 0.13f, 0.12f, 0.15f, 0.0f, -0.37f, -0.005f, SKIN);
        addBox(rightForearm, 0.13f, 0.12f, 0.15f, 0.0f, -0.37f, -0.005f, SKIN);
        leftArm.addChild(leftForearm);
        rightArm.addChild(rightForearm);
        upperBody.addChild(leftArm);
        upperBody.addChild(rightArm);

        // Hip and knee pivots create a proper two-part leg silhouette.
        leftLeg.setTranslation(-0.13f, 0.79f, 0.0f);
        rightLeg.setTranslation(0.13f, 0.79f, 0.0f);
        addOval(leftLeg, 0.205f, 0.43f, 0.235f, 0.0f, -0.22f, 0.0f, JEANS);
        addOval(rightLeg, 0.205f, 0.43f, 0.235f, 0.0f, -0.22f, 0.0f, JEANS);
        // Denim seams/highlights add shape without expensive textures.
        addBox(leftLeg, 0.025f, 0.36f, 0.012f, -0.075f, -0.22f, -0.123f, JEANS_LIGHT);
        addBox(rightLeg, 0.025f, 0.36f, 0.012f, 0.075f, -0.22f, -0.123f, JEANS_LIGHT);
        leftShin.setTranslation(0.0f, -0.42f, 0.0f);
        rightShin.setTranslation(0.0f, -0.42f, 0.0f);
        addOval(leftShin, 0.19f, 0.43f, 0.22f, 0.0f, -0.21f, 0.0f, JEANS);
        addOval(rightShin, 0.19f, 0.43f, 0.22f, 0.0f, -0.21f, 0.0f, JEANS);
        addBox(leftShin, 0.025f, 0.31f, 0.012f, -0.055f, -0.20f, -0.115f, JEANS_LIGHT);
        addBox(rightShin, 0.025f, 0.31f, 0.012f, 0.055f, -0.20f, -0.115f, JEANS_LIGHT);
        addBox(leftShin, 0.22f, 0.13f, 0.34f, 0.0f, -0.46f, -0.045f, SHOE);
        addBox(rightShin, 0.22f, 0.13f, 0.34f, 0.0f, -0.46f, -0.045f, SHOE);
        addBox(leftShin, 0.22f, 0.025f, 0.32f, 0.0f, -0.525f, -0.045f, 0x151515);
        addBox(rightShin, 0.22f, 0.025f, 0.32f, 0.0f, -0.525f, -0.045f, 0x151515);
        leftLeg.addChild(leftShin);
        rightLeg.addChild(rightShin);
        root.addChild(leftLeg);
        root.addChild(rightLeg);
    }


    private static void addOval(Group parent, float w, float h, float d,
                                float x, float y, float z, int color) {
        Mesh mesh = ModelFactory.ellipsoid(w, h, d, color);
        mesh.setTranslation(x, y, z);
        parent.addChild(mesh);
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

    void setVisible(boolean visible) {
        root.setRenderingEnable(visible);
    }

    /** Temporary leaning/stepping pose used while entering or leaving the sedan. */
    void setEntryPose(float amount, boolean entering) {
        if (amount < 0.0f) amount = 0.0f;
        if (amount > 1.0f) amount = 1.0f;
        float sign = entering ? 1.0f : -1.0f;
        // Lower the torso below the roofline and fold the legs into the cabin.
        // This makes CJ visibly occupy the seat instead of disappearing outside.
        upperBody.setTranslation(0.0f, -0.48f * amount, 0.0f);
        upperBody.setOrientation(-12.0f * amount, 0.0f, 0.0f, 1.0f);
        leftArm.setOrientation(-62.0f * amount * sign, 1.0f, 0.0f, 0.0f);
        rightArm.setOrientation(-28.0f * amount * sign, 1.0f, 0.0f, 0.0f);
        leftForearm.setOrientation(-35.0f * amount, 1.0f, 0.0f, 0.0f);
        rightForearm.setOrientation(-18.0f * amount, 1.0f, 0.0f, 0.0f);
        leftLeg.setOrientation(78.0f * amount, 1.0f, 0.0f, 0.0f);
        rightLeg.setOrientation(72.0f * amount, 1.0f, 0.0f, 0.0f);
        leftShin.setOrientation(-72.0f * amount, 1.0f, 0.0f, 0.0f);
        rightShin.setOrientation(-68.0f * amount, 1.0f, 0.0f, 0.0f);
    }

    void animate(float walkPhase, boolean moving, float punchAngle,
                 boolean backward, boolean jumping, boolean crouching,
                 float turnAmount) {
        float targetCrouch = crouching ? 1.0f : 0.0f;
        float targetJump = jumping ? 1.0f : 0.0f;
        float targetWalk = moving ? 1.0f : 0.0f;
        float targetTurn = turnAmount;
        crouchBlend += (targetCrouch - crouchBlend) * 0.20f;
        jumpBlend += (targetJump - jumpBlend) * 0.22f;
        gaitBlend += (targetWalk - gaitBlend) * 0.24f;
        turnBlend += (targetTurn - turnBlend) * 0.20f;

        float phase = walkPhase;
        float stride = (float)Math.sin(phase) * 27.0f * gaitBlend;
        float opposite = (float)Math.sin(phase + 3.1415926f) * 27.0f * gaitBlend;
        float kneeLeft = Math.max(0.0f, (float)Math.sin(phase + 0.65f)) * 28.0f * gaitBlend;
        float kneeRight = Math.max(0.0f, (float)Math.sin(phase + 3.79f)) * 28.0f * gaitBlend;
        if (backward) {
            stride = -stride;
            opposite = -opposite;
        }

        float crouch = crouchBlend;
        float jump = jumpBlend;
        leftLeg.setOrientation(stride + crouch * 25.0f + jump * 20.0f, 1, 0, 0);
        rightLeg.setOrientation(opposite + crouch * 25.0f + jump * 20.0f, 1, 0, 0);
        leftShin.setOrientation(-kneeLeft - crouch * 48.0f - jump * 15.0f, 1, 0, 0);
        rightShin.setOrientation(-kneeRight - crouch * 48.0f - jump * 15.0f, 1, 0, 0);

        float armSwing = -stride * 0.68f;
        float otherArm = -opposite * 0.68f;
        leftArm.setOrientation(armSwing - turnBlend * 8.0f - jump * 78.0f, 1, 0, 0);
        rightArm.setOrientation(otherArm - turnBlend * 8.0f - punchAngle -
                                jump * 78.0f, 1, 0, 0);
        leftForearm.setOrientation(-crouch * 35.0f - jump * 20.0f, 1, 0, 0);
        rightForearm.setOrientation(-crouch * 35.0f - jump * 20.0f, 1, 0, 0);

        // Small hip sway and torso counterbalance make gait feel less robotic.
        leftLeg.setTranslation(-0.13f - (float)Math.sin(phase) * 0.018f * gaitBlend,
                               0.79f - crouch * 0.12f, 0.0f);
        rightLeg.setTranslation(0.13f - (float)Math.sin(phase) * 0.018f * gaitBlend,
                                0.79f - crouch * 0.12f, 0.0f);
        upperBody.setTranslation(0.0f, -0.27f * crouch + 0.055f * jump +
                                 (float)Math.sin(phase * 2.0f) * 0.018f * gaitBlend, 0.0f);
        upperBody.setOrientation(-7.0f * crouch + 3.0f * jump,
                                 0.0f, 0.0f, 1.0f);
    }
}
