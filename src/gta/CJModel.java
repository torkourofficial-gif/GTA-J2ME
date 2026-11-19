package gta;

import javax.microedition.m3g.Group;
import javax.microedition.m3g.Mesh;

/**
 * Procedural low-poly CJ. Separate pivots for shoulders, elbows, hips and knees
 * keep the model light enough for small Java ME/M3G devices.
 */
final class CJModel {
    final Group root = new Group();
    private final Group body = new Group();
    final Group leftArm = new Group();
    final Group rightArm = new Group();
    final Group leftLeg = new Group();
    final Group rightLeg = new Group();

    private final Group upperBody = new Group();
    private final Group leftForearm = new Group();
    private final Group rightForearm = new Group();
    private final Group leftShin = new Group();
    private final Group rightShin = new Group();

    private final boolean lucia;
    private final int shirtColor;
    private final int pantsColor;
    private final int skinColor;
    private final int skinLightColor;
    private float crouchBlend;
    private float jumpBlend;
    private float gaitBlend;
    private float turnBlend;

    // Articulated ragdoll state: angle and angular velocity for torso,
    // shoulders, elbows, hips and knees. Angles are stored in radians.
    private final float[] ragAngle = new float[9];
    private final float[] ragVelocity = new float[9];
    private boolean ragdollActive;

    // CJ's palette: white tank, blue loose jeans, dark sneakers, warm skin.
    private static final int SKIN = 0xBD896F;
    private static final int SKIN_LIGHT = 0xD6A58A;
    private static final int SKIN_DARK = 0x75432F;
    private static final int SHIRT = 0xE9E9E2;
    private static final int SHIRT_SHADE = 0xBFC2C0;
    private static final int JEANS = 0x344D78;
    private static final int JEANS_LIGHT = 0x526B91;
    private static final int JEANS_DARK = 0x253653;
    private static final int SHOE = 0x292725;

    CJModel() { this(false); }

    CJModel(boolean lucia) {
        this.lucia = lucia;
        shirtColor = lucia ? 0xD96F91 : 0xD8D6D0;
        pantsColor = lucia ? 0x456C9A : 0x555B50;
        skinColor = lucia ? 0xA96F53 : SKIN;
        skinLightColor = lucia ? 0xC58B6C : skinLightColor;
        // Waist, belt and denim waistband.
        addBox(body, 0.43f, 0.20f, 0.29f, 0.0f, 0.82f, 0.0f, pantsColor);
        addBox(body, 0.45f, 0.045f, 0.30f, 0.0f, 0.92f, 0.0f, 0xB6A17A);
        addBox(body, 0.07f, 0.055f, 0.025f, 0.0f, 0.92f, -0.16f, 0xC7B77D);

        if (lucia) {
            addOval(upperBody, 0.27f, 0.34f, 0.24f, 0.0f, 1.73f, 0.055f, 0x211B20);
            addOval(upperBody, 0.12f, 0.38f, 0.13f, 0.15f, 1.58f, 0.13f, 0x211B20);
        } else {
            addBox(upperBody, 0.28f, 0.09f, 0.25f, 0.0f, 1.88f, 0.015f, 0x8D3438);
            addBox(upperBody, 0.31f, 0.035f, 0.19f, 0.0f, 1.84f, -0.12f, 0xA84449);
        }
        // Tank top and shoulder straps.
        addTexturedOval(upperBody, 0.48f, 0.54f, 0.27f, 0.0f, 1.19f, 0.0f, shirtColor, 7);
        addBox(upperBody, 0.15f, 0.34f, 0.29f, -0.16f, 1.30f, 0.0f, shirtColor);
        addBox(upperBody, 0.15f, 0.34f, 0.29f, 0.16f, 1.30f, 0.0f, shirtColor);
        addBox(upperBody, 0.18f, 0.15f, 0.18f, 0.0f, 1.52f, 0.0f, skinColor);
        addTexturedOval(upperBody, 0.22f, 0.29f, 0.22f, 0.0f, 1.72f, 0.0f, skinColor, 5);
        // Ears, nose, brows and eyes face local -Z.
        addBox(upperBody, 0.045f, 0.09f, 0.055f, -0.125f, 1.73f, -0.005f, skinLightColor);
        addBox(upperBody, 0.045f, 0.09f, 0.055f, 0.125f, 1.73f, -0.005f, skinLightColor);
        addBox(upperBody, 0.045f, 0.075f, 0.045f, 0.0f, 1.69f, -0.125f, skinLightColor);
        addBox(upperBody, 0.055f, 0.018f, 0.018f, -0.055f, 1.77f, -0.116f, 0x29201C);
        addBox(upperBody, 0.055f, 0.018f, 0.018f, 0.055f, 1.77f, -0.116f, 0x29201C);
        addBox(upperBody, 0.027f, 0.025f, 0.012f, -0.052f, 1.735f, -0.122f, 0x171717);
        addBox(upperBody, 0.027f, 0.025f, 0.012f, 0.052f, 1.735f, -0.122f, 0x171717);
        addBox(upperBody, 0.13f, 0.045f, 0.23f, 0.0f, 1.87f, 0.0f, 0x24201D);
        addBox(upperBody, 0.23f, 0.065f, 0.22f, 0.0f, 1.82f, 0.0f, 0x30251F);
        body.addChild(upperBody);

        // Shoulder pivots. Forearms and hands rotate independently at elbows.
        leftArm.setTranslation(-0.29f, 1.43f, 0.0f);
        rightArm.setTranslation(0.29f, 1.43f, 0.0f);
        addTexturedOval(leftArm, 0.17f, 0.36f, 0.18f, 0.0f, -0.17f, 0.0f, skinColor, 5);
        addTexturedOval(rightArm, 0.17f, 0.36f, 0.18f, 0.0f, -0.17f, 0.0f, skinColor, 5);
        if (lucia) {
            addOval(leftArm, 0.178f, 0.23f, 0.185f, 0.0f, -0.12f, 0.0f, shirtColor);
            addOval(rightArm, 0.178f, 0.23f, 0.185f, 0.0f, -0.12f, 0.0f, shirtColor);
        }
        leftForearm.setTranslation(0.0f, -0.34f, 0.0f);
        rightForearm.setTranslation(0.0f, -0.34f, 0.0f);
        addTexturedOval(leftForearm, 0.145f, 0.34f, 0.15f, 0.0f, -0.16f, 0.0f, skinLightColor, 5);
        addTexturedOval(rightForearm, 0.145f, 0.34f, 0.15f, 0.0f, -0.16f, 0.0f, skinLightColor, 5);
        if (lucia) {
            addOval(leftForearm, 0.15f, 0.20f, 0.155f, 0.0f, -0.12f, 0.0f, shirtColor);
            addOval(rightForearm, 0.15f, 0.20f, 0.155f, 0.0f, -0.12f, 0.0f, shirtColor);
        }
        addBox(leftForearm, 0.13f, 0.12f, 0.15f, 0.0f, -0.37f, -0.005f, skinColor);
        addBox(rightForearm, 0.13f, 0.12f, 0.15f, 0.0f, -0.37f, -0.005f, skinColor);
        leftArm.addChild(leftForearm);
        rightArm.addChild(rightForearm);
        upperBody.addChild(leftArm);
        upperBody.addChild(rightArm);

        // Hip and knee pivots create a proper two-part leg silhouette.
        leftLeg.setTranslation(-0.13f, 0.79f, 0.0f);
        rightLeg.setTranslation(0.13f, 0.79f, 0.0f);
        addTexturedOval(leftLeg, 0.205f, 0.43f, 0.235f, 0.0f, -0.22f, 0.0f, pantsColor, 6);
        addTexturedOval(rightLeg, 0.205f, 0.43f, 0.235f, 0.0f, -0.22f, 0.0f, pantsColor, 6);
        // Denim seams/highlights add shape without expensive textures.
        addBox(leftLeg, 0.025f, 0.36f, 0.012f, -0.075f, -0.22f, -0.123f, JEANS_LIGHT);
        addBox(rightLeg, 0.025f, 0.36f, 0.012f, 0.075f, -0.22f, -0.123f, JEANS_LIGHT);
        leftShin.setTranslation(0.0f, -0.42f, 0.0f);
        rightShin.setTranslation(0.0f, -0.42f, 0.0f);
        addTexturedOval(leftShin, 0.19f, 0.43f, 0.22f, 0.0f, -0.21f, 0.0f, pantsColor, 6);
        addTexturedOval(rightShin, 0.19f, 0.43f, 0.22f, 0.0f, -0.21f, 0.0f, pantsColor, 6);
        addBox(leftShin, 0.025f, 0.31f, 0.012f, -0.055f, -0.20f, -0.115f, JEANS_LIGHT);
        addBox(rightShin, 0.025f, 0.31f, 0.012f, 0.055f, -0.20f, -0.115f, JEANS_LIGHT);
        addBox(leftShin, 0.22f, 0.13f, 0.34f, 0.0f, -0.46f, -0.045f, SHOE);
        addBox(rightShin, 0.22f, 0.13f, 0.34f, 0.0f, -0.46f, -0.045f, SHOE);
        addBox(leftShin, 0.22f, 0.025f, 0.32f, 0.0f, -0.525f, -0.045f, 0x151515);
        addBox(rightShin, 0.22f, 0.025f, 0.32f, 0.0f, -0.525f, -0.045f, 0x151515);
        leftLeg.addChild(leftShin);
        rightLeg.addChild(rightShin);
        body.addChild(leftLeg);
        body.addChild(rightLeg);
        root.addChild(body);
    }


    private static void addOval(Group parent, float w, float h, float d,
                                float x, float y, float z, int color) {
        Mesh mesh = ModelFactory.ellipsoid(w, h, d, color);
        mesh.setTranslation(x, y, z);
        parent.addChild(mesh);
    }

    private static void addTexturedOval(Group parent, float w, float h, float d,
                                        float x, float y, float z,
                                        int color, int textureKind) {
        Mesh mesh = ModelFactory.texturedEllipsoid(w, h, d, color, textureKind);
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
        body.setOrientation(0.0f, 0.0f, 1.0f, 0.0f);
        if (amount < 0.0f) amount = 0.0f;
        if (amount > 1.0f) amount = 1.0f;
        float sign = entering ? 1.0f : -1.0f;
        // Lower the torso below the roofline and fold the legs into the cabin.
        // This makes CJ visibly occupy the seat instead of disappearing outside.
        upperBody.setTranslation(0.0f, -0.48f * amount, 0.0f);
        upperBody.setOrientation(-12.0f * amount, 0.0f, 0.0f, 1.0f);
        leftArm.setOrientation(-48.0f * amount * sign, 1.0f, 0.0f, 0.0f);
        rightArm.setOrientation(-38.0f * amount * sign, 1.0f, 0.0f, 0.0f);
        leftForearm.setOrientation(-52.0f * amount, 1.0f, 0.0f, 0.0f);
        rightForearm.setOrientation(-42.0f * amount, 1.0f, 0.0f, 0.0f);
        leftLeg.setOrientation(78.0f * amount, 1.0f, 0.0f, 0.0f);
        rightLeg.setOrientation(72.0f * amount, 1.0f, 0.0f, 0.0f);
        leftShin.setOrientation(-72.0f * amount, 1.0f, 0.0f, 0.0f);
        rightShin.setOrientation(-68.0f * amount, 1.0f, 0.0f, 0.0f);
    }

    /**
     * Lightweight articulated rigid-body approximation for Java ME.
     * Every limb is a child of its shoulder/hip/elbow/knee pivot, so joint
     * transforms preserve attachment. Each joint integrates angular momentum
     * under gravity, damping, impact impulse and hard angular limits.
     */
    void animateRagdoll(float dt, float roll, float pitch,
                        float rollSpeed, float pitchSpeed) {
        if (dt < 0.001f) dt = 0.001f;
        if (dt > 0.08f) dt = 0.08f;
        if (!ragdollActive) {
            ragdollActive = true;
            for (int i = 0; i < ragAngle.length; i++) {
                ragAngle[i] = 0.0f;
                ragVelocity[i] = 0.0f;
            }
            // Impact angular momentum is transferred into the connected joints.
            ragVelocity[0] = pitchSpeed * 0.0065f;
            ragVelocity[1] = rollSpeed * 0.0018f;
            ragVelocity[2] = -rollSpeed * 0.0018f;
            ragVelocity[3] = pitchSpeed * 0.0012f;
            ragVelocity[4] = -pitchSpeed * 0.0012f;
            ragVelocity[5] = -pitchSpeed * 0.0015f;
            ragVelocity[6] = pitchSpeed * 0.0015f;
            ragVelocity[7] = pitchSpeed * 0.0010f;
            ragVelocity[8] = -pitchSpeed * 0.0010f;
        }

        // Body is the pelvis/root rigid segment; its angular state is supplied
        // by the translational collision solver in GameCanvas.
        float tilt = (float)Math.sqrt(roll * roll + pitch * pitch);
        if (tilt > 82.0f) tilt = 82.0f;
        if (tilt > 0.01f) {
            float inv = 1.0f / (float)Math.sqrt(roll * roll + pitch * pitch);
            body.setOrientation(tilt, pitch * inv, 0.0f, roll * inv);
        } else {
            body.setOrientation(0.0f, 0.0f, 1.0f, 0.0f);
        }

        // Pendulum-like joint dynamics. The gravity term makes unsupported
        // limbs fall; angular damping dissipates energy; limits keep anatomy
        // connected and prevent joints from folding through the body.
        float gravity = 7.8f;
        float damping = 1.65f;
        for (int i = 0; i < ragAngle.length; i++) {
            float torque = -gravity * (float)Math.sin(ragAngle[i]);
            // The torso and limbs inherit acceleration from the tumbling body.
            if (i == 0) torque += pitchSpeed * 0.0009f;
            else if (i == 1 || i == 2) torque += rollSpeed * 0.00035f;
            ragVelocity[i] += (torque - damping * ragVelocity[i]) * dt;
            ragAngle[i] += ragVelocity[i] * dt;
        }

        // Soft anatomical limits become stiff restoring constraints at the
        // extremes, rather than allowing a limb to detach or flip endlessly.
        constrainJoint(0, -0.85f, 1.35f);
        constrainJoint(1, -2.15f, 2.15f);
        constrainJoint(2, -2.15f, 2.15f);
        constrainJoint(3, -1.75f, 1.75f);
        constrainJoint(4, -1.75f, 1.75f);
        constrainJoint(5, -1.25f, 1.55f);
        constrainJoint(6, -1.25f, 1.55f);
        constrainJoint(7, -0.15f, 2.25f);
        constrainJoint(8, -0.15f, 2.25f);

        float d = 57.29578f;
        upperBody.setTranslation(0.0f, 0.0f, 0.0f);
        upperBody.setOrientation(ragAngle[0] * d, 1.0f, 0.0f, 0.0f);
        leftArm.setOrientation(ragAngle[1] * d, 1.0f, 0.0f, 0.0f);
        rightArm.setOrientation(ragAngle[2] * d, 1.0f, 0.0f, 0.0f);
        leftForearm.setOrientation(ragAngle[3] * d, 1.0f, 0.0f, 0.0f);
        rightForearm.setOrientation(ragAngle[4] * d, 1.0f, 0.0f, 0.0f);
        leftLeg.setOrientation(ragAngle[5] * d, 1.0f, 0.0f, 0.0f);
        rightLeg.setOrientation(ragAngle[6] * d, 1.0f, 0.0f, 0.0f);
        leftShin.setOrientation(ragAngle[7] * d, 1.0f, 0.0f, 0.0f);
        rightShin.setOrientation(ragAngle[8] * d, 1.0f, 0.0f, 0.0f);
    }

    private void constrainJoint(int index, float minimum, float maximum) {
        if (ragAngle[index] < minimum) {
            ragAngle[index] = minimum;
            if (ragVelocity[index] < 0.0f) ragVelocity[index] *= -0.18f;
        } else if (ragAngle[index] > maximum) {
            ragAngle[index] = maximum;
            if (ragVelocity[index] > 0.0f) ragVelocity[index] *= -0.18f;
        }
    }

    void endRagdoll() {
        ragdollActive = false;
        for (int i = 0; i < ragAngle.length; i++) {
            ragAngle[i] = 0.0f;
            ragVelocity[i] = 0.0f;
        }
    }

    void animate(float walkPhase, boolean moving, float punchAngle,
                 boolean backward, boolean jumping, boolean crouching,
                 float turnAmount) {
        endRagdoll();
        body.setOrientation(0.0f, 0.0f, 1.0f, 0.0f);
        upperBody.setTranslation(0.0f, 0.0f, 0.0f);
        float targetCrouch = crouching ? 1.0f : 0.0f;
        float targetJump = jumping ? 1.0f : 0.0f;
        float targetWalk = moving ? 1.0f : 0.0f;
        float targetTurn = turnAmount;
        crouchBlend += (targetCrouch - crouchBlend) * 0.20f;
        jumpBlend += (targetJump - jumpBlend) * 0.22f;
        gaitBlend += (targetWalk - gaitBlend) * 0.24f;
        turnBlend += (targetTurn - turnBlend) * 0.20f;

        float phase = walkPhase;
        float stride = (float)Math.sin(phase) * 23.0f * gaitBlend;
        float opposite = (float)Math.sin(phase + 3.1415926f) * 23.0f * gaitBlend;
        float kneeLeft = Math.max(0.0f, (float)Math.sin(phase + 0.65f)) * 28.0f * gaitBlend;
        float kneeRight = Math.max(0.0f, (float)Math.sin(phase + 3.79f)) * 28.0f * gaitBlend;
        if (backward) {
            stride = -stride;
            opposite = -opposite;
        }

        float crouch = crouchBlend;
        float jump = jumpBlend;
        leftLeg.setOrientation(stride + crouch * 25.0f - jump * 12.0f, 1, 0, 0);
        rightLeg.setOrientation(opposite + crouch * 25.0f - jump * 12.0f, 1, 0, 0);
        leftShin.setOrientation(-kneeLeft - crouch * 48.0f - jump * 28.0f, 1, 0, 0);
        rightShin.setOrientation(-kneeRight - crouch * 48.0f - jump * 28.0f, 1, 0, 0);

        float armSwing = -stride * 0.68f;
        float otherArm = -opposite * 0.68f;
        leftArm.setOrientation(armSwing - turnBlend * 8.0f + jump * 78.0f, 1, 0, 0);
        rightArm.setOrientation(otherArm - turnBlend * 8.0f - punchAngle +
                                jump * 78.0f, 1, 0, 0);
        leftForearm.setOrientation(-crouch * 35.0f + jump * 24.0f, 1, 0, 0);
        rightForearm.setOrientation(-crouch * 35.0f + jump * 24.0f, 1, 0, 0);

        // Small hip sway and torso counterbalance make gait feel less robotic.
        leftLeg.setTranslation(-0.13f - (float)Math.sin(phase) * 0.018f * gaitBlend,
                               0.79f - crouch * 0.12f, 0.0f);
        rightLeg.setTranslation(0.13f - (float)Math.sin(phase) * 0.018f * gaitBlend,
                                0.79f - crouch * 0.12f, 0.0f);
        upperBody.setTranslation(0.0f, -0.27f * crouch + 0.055f * jump +
                                 (float)Math.sin(phase * 2.0f) * 0.018f * gaitBlend, 0.0f);
        upperBody.setOrientation(-7.0f * crouch + 3.0f * jump +
                                 (float)Math.sin(phase) * 1.8f * gaitBlend,
                                 0.0f, 0.0f, 1.0f);
        leftForearm.setOrientation(-crouch * 35.0f + jump * 24.0f +
                                   Math.max(0.0f, stride) * 0.16f, 1, 0, 0);
        rightForearm.setOrientation(-crouch * 35.0f + jump * 24.0f +
                                    Math.max(0.0f, opposite) * 0.16f, 1, 0, 0);
    }
}
