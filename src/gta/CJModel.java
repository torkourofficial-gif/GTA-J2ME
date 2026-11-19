package gta;

import javax.microedition.m3g.Group;
import javax.microedition.m3g.Mesh;

/** CJ is assembled from independently transformable low-poly body parts. */
final class CJModel {
    final Group root = new Group();
    final Group leftArm = new Group();
    final Group rightArm = new Group();
    final Group leftLeg = new Group();
    final Group rightLeg = new Group();
    private final Group upperBody = new Group();
    private float crouchBlend = 0.0f;
    private float jumpBlend = 0.0f;

    CJModel() {
        addBox(upperBody, 0.52f, 0.62f, 0.28f, 0.0f, 1.02f, 0.0f, 0xFF9A32); // shirt
        addBox(upperBody, 0.24f, 0.28f, 0.24f, 0.0f, 1.52f, 0.0f, 0xFF9B65); // head
        addBox(upperBody, 0.27f, 0.12f, 0.27f, 0.0f, 1.70f, 0.0f, 0x252525); // hair
        addBox(upperBody, 0.18f, 0.12f, 0.20f, 0.0f, 1.37f, 0.0f, 0xFF9B65); // neck
        root.addChild(upperBody);

        leftArm.setTranslation(-0.34f, 1.28f, 0.0f);
        rightArm.setTranslation(0.34f, 1.28f, 0.0f);
        addBox(leftArm, 0.17f, 0.52f, 0.18f, 0.0f, -0.25f, 0.0f, 0xFF9B65);
        addBox(rightArm, 0.17f, 0.52f, 0.18f, 0.0f, -0.25f, 0.0f, 0xFF9B65);
        upperBody.addChild(leftArm);
        upperBody.addChild(rightArm);

        leftLeg.setTranslation(-0.14f, 0.72f, 0.0f);
        rightLeg.setTranslation(0.14f, 0.72f, 0.0f);
        addBox(leftLeg, 0.21f, 0.66f, 0.23f, 0.0f, -0.31f, 0.0f, 0x303D83);
        addBox(rightLeg, 0.21f, 0.66f, 0.23f, 0.0f, -0.31f, 0.0f, 0x303D83);
        addBox(leftLeg, 0.23f, 0.12f, 0.34f, 0.0f, -0.64f, 0.035f, 0x252525);
        addBox(rightLeg, 0.23f, 0.12f, 0.34f, 0.0f, -0.64f, 0.035f, 0x252525);
        root.addChild(leftLeg);
        root.addChild(rightLeg);
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

    void animate(float walkPhase, boolean moving, float punchAngle,
                  boolean backward, boolean jumping, boolean crouching,
                  float turnAmount) {
        // Smoothly blend crouch/jump poses to avoid abrupt limb snapping.
        float targetCrouch = crouching ? 1.0f : 0.0f;
        float targetJump = jumping ? 1.0f : 0.0f;
        crouchBlend += (targetCrouch - crouchBlend) * 0.28f;
        jumpBlend += (targetJump - jumpBlend) * 0.30f;

        float direction = backward ? -1.0f : 1.0f;
        float swing = moving ? (float)Math.sin(walkPhase) * 30.0f * direction : 0.0f;
        float turnStep = turnAmount * (moving ? 12.0f : 22.0f);
        float crouchLeg = crouchBlend * 48.0f;
        float jumpLeg = jumpBlend * 22.0f;

        leftLeg.setOrientation(swing + crouchLeg + jumpLeg, 1.0f, 0.0f, 0.0f);
        rightLeg.setOrientation(-swing + crouchLeg + jumpLeg, 1.0f, 0.0f, 0.0f);

        float armSwing = -swing * 0.75f;
        leftArm.setOrientation(armSwing - turnStep - jumpBlend * 105.0f,
                               1.0f, 0.0f, 0.0f);
        rightArm.setOrientation(-armSwing - turnStep - punchAngle -
                                jumpBlend * 105.0f, 1.0f, 0.0f, 0.0f);

        // Keep the torso upright during reverse walking; only crouch/jump
        // change the upper-body height and pitch.
        upperBody.setTranslation(0.0f, -0.30f * crouchBlend + 0.04f * jumpBlend, 0.0f);
        upperBody.setOrientation(-8.0f * crouchBlend + 3.0f * jumpBlend,
                                 1.0f, 0.0f, 0.0f);
    }
}
