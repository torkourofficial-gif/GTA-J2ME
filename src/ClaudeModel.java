package gta3me;

import javax.microedition.lcdui.Image;
import javax.microedition.m3g.Appearance;
import javax.microedition.m3g.Image2D;
import javax.microedition.m3g.Material;
import javax.microedition.m3g.Mesh;
import javax.microedition.m3g.Texture2D;
import javax.microedition.m3g.TriangleStripArray;
import javax.microedition.m3g.VertexArray;
import javax.microedition.m3g.VertexBuffer;

/** Generated from the Claude OBJ. Do not edit the mesh arrays by hand. */
public final class ClaudeModel {
    public static final int IDLE = 0;
    public static final int WALK = 1;
    public static final int JUMP = 2;
    public static final int LAND = 3;
    public static final int RUN = 4;
    public static final int SPRINT = 5;
    public static final int START_WALK = 6;
    public static final int RUN_STOP_LEFT = 7;
    public static final int RUN_STOP_RIGHT = 8;
    public static final int LANDING = 9;
    public static final int BACKWARD = 10;

    // Sixteen samples per loop (plus the matching end sample) give the gait
    // detailed timing for heel strike, loading, toe-off, flight and recovery.
    private static final float[] WALK_HIP = new float[] {-0.30f, -0.28f, -0.24f, -0.14f, -0.02f, 0.10f, 0.20f, 0.27f, 0.30f, 0.29f, 0.24f, 0.14f, 0.02f, -0.10f, -0.20f, -0.27f, -0.30f};
    private static final float[] RUN_HIP = new float[] {-0.62f, -0.58f, -0.48f, -0.31f, -0.10f, 0.14f, 0.38f, 0.55f, 0.62f, 0.60f, 0.48f, 0.31f, 0.10f, -0.14f, -0.38f, -0.55f, -0.62f};
    private static final float[] SPRINT_HIP = new float[] {-0.78f, -0.73f, -0.60f, -0.39f, -0.12f, 0.18f, 0.48f, 0.69f, 0.78f, 0.75f, 0.60f, 0.39f, 0.12f, -0.18f, -0.48f, -0.69f, -0.78f};
    private static final float[] WALK_KNEE = new float[] {0.04f, 0.06f, 0.10f, 0.25f, 0.38f, 0.34f, 0.22f, 0.10f, 0.05f, 0.05f, 0.08f, 0.12f, 0.12f, 0.14f, 0.18f, 0.12f, 0.04f};
    private static final float[] RUN_KNEE = new float[] {0.10f, 0.12f, 0.18f, 0.52f, 0.82f, 0.72f, 0.46f, 0.20f, 0.10f, 0.10f, 0.14f, 0.20f, 0.26f, 0.30f, 0.38f, 0.22f, 0.10f};
    private static final float[] SPRINT_KNEE = new float[] {0.14f, 0.17f, 0.24f, 0.66f, 1.02f, 0.88f, 0.58f, 0.26f, 0.12f, 0.12f, 0.17f, 0.25f, 0.32f, 0.38f, 0.46f, 0.27f, 0.14f};
    private static final float[] WALK_ARM = new float[] {0.22f, 0.21f, 0.18f, 0.10f, 0.02f, -0.08f, -0.16f, -0.21f, -0.22f, -0.21f, -0.18f, -0.10f, -0.02f, 0.08f, 0.16f, 0.21f, 0.22f};
    private static final float[] RUN_ARM = new float[] {0.48f, 0.45f, 0.38f, 0.23f, 0.08f, -0.13f, -0.34f, -0.45f, -0.48f, -0.46f, -0.38f, -0.23f, -0.08f, 0.13f, 0.34f, 0.45f, 0.48f};
    private static final float[] SPRINT_ARM = new float[] {0.60f, 0.56f, 0.48f, 0.29f, 0.10f, -0.17f, -0.44f, -0.56f, -0.60f, -0.57f, -0.48f, -0.29f, -0.10f, 0.17f, 0.44f, 0.56f, 0.60f};
    private static final float[] BACKWARD_HIP = new float[] {-0.19f, -0.18f, -0.15f, -0.09f, -0.02f, 0.06f, 0.12f, 0.17f, 0.19f, 0.18f, 0.15f, 0.09f, 0.02f, -0.06f, -0.12f, -0.17f, -0.19f};
    private static final float[] BACKWARD_KNEE = new float[] {0.12f, 0.13f, 0.16f, 0.21f, 0.24f, 0.22f, 0.18f, 0.13f, 0.10f, 0.11f, 0.14f, 0.18f, 0.20f, 0.19f, 0.18f, 0.15f, 0.12f};
    private static final float[] BACKWARD_ARM = new float[] {-0.13f, -0.12f, -0.10f, -0.06f, -0.02f, 0.04f, 0.08f, 0.12f, 0.13f, 0.12f, 0.10f, 0.06f, 0.02f, -0.04f, -0.08f, -0.12f, -0.13f};
    private static final float[] ELBOW_KEYS = new float[] {0.10f, 0.12f, 0.16f, 0.24f, 0.30f, 0.28f, 0.22f, 0.14f, 0.10f, 0.10f, 0.13f, 0.18f, 0.24f, 0.22f, 0.18f, 0.13f, 0.10f};
    private static final float[] RUN_ELBOW_KEYS = new float[] {0.42f, 0.45f, 0.50f, 0.57f, 0.62f, 0.60f, 0.54f, 0.46f, 0.40f, 0.41f, 0.46f, 0.52f, 0.58f, 0.56f, 0.50f, 0.45f, 0.42f};
    private static final float[] SPRINT_ELBOW_KEYS = new float[] {0.52f, 0.55f, 0.60f, 0.68f, 0.72f, 0.70f, 0.64f, 0.55f, 0.48f, 0.49f, 0.54f, 0.62f, 0.68f, 0.66f, 0.60f, 0.55f, 0.52f};
    private static final float[] WALK_SHOULDER = new float[] {0.04f, 0.05f, 0.07f, 0.05f, 0.02f, -0.02f, -0.05f, -0.06f, -0.04f, -0.05f, -0.07f, -0.05f, -0.02f, 0.02f, 0.05f, 0.06f, 0.04f};
    private static final float[] RUN_SHOULDER = new float[] {0.12f, 0.14f, 0.16f, 0.13f, 0.08f, -0.02f, -0.12f, -0.15f, -0.12f, -0.14f, -0.16f, -0.13f, -0.08f, 0.02f, 0.12f, 0.15f, 0.12f};
    private static final float[] SPRINT_SHOULDER = new float[] {0.18f, 0.20f, 0.22f, 0.18f, 0.12f, -0.04f, -0.18f, -0.21f, -0.18f, -0.20f, -0.22f, -0.18f, -0.12f, 0.04f, 0.18f, 0.21f, 0.18f};
    private static final float[] WALK_WRIST = new float[] {0.02f, 0.03f, 0.04f, 0.06f, 0.08f, 0.07f, 0.04f, 0.00f, -0.02f, -0.03f, -0.04f, -0.06f, -0.08f, -0.07f, -0.04f, 0.00f, 0.02f};
    private static final float[] RUN_WRIST = new float[] {0.08f, 0.09f, 0.12f, 0.15f, 0.18f, 0.16f, 0.10f, 0.02f, -0.08f, -0.09f, -0.12f, -0.15f, -0.18f, -0.16f, -0.10f, -0.02f, 0.08f};
    private static final float[] SPRINT_WRIST = new float[] {0.12f, 0.14f, 0.16f, 0.19f, 0.22f, 0.19f, 0.12f, 0.02f, -0.12f, -0.14f, -0.16f, -0.19f, -0.22f, -0.19f, -0.12f, -0.02f, 0.12f};
    private static final float[] WALK_ANKLE = new float[] {0.02f, -0.02f, -0.08f, -0.13f, -0.16f, -0.04f, 0.08f, 0.16f, 0.18f, 0.15f, 0.10f, 0.02f, -0.04f, -0.08f, -0.10f, -0.04f, 0.02f};
    private static final float[] RUN_ANKLE = new float[] {0.10f, 0.02f, -0.12f, -0.24f, -0.30f, -0.06f, 0.18f, 0.30f, 0.34f, 0.30f, 0.18f, 0.02f, -0.08f, -0.14f, -0.18f, -0.02f, 0.10f};
    private static final float[] SPRINT_ANKLE = new float[] {0.14f, 0.04f, -0.16f, -0.30f, -0.38f, -0.08f, 0.22f, 0.38f, 0.42f, 0.37f, 0.22f, 0.04f, -0.10f, -0.17f, -0.22f, -0.04f, 0.14f};
    private static final float[] START_KEYS = new float[] {0.0f, 0.10f, 0.24f, 0.40f, 0.60f, 0.82f, 1.0f};
    private static final float[] START_HIP = new float[] {0.0f, -0.06f, -0.18f, -0.32f, -0.38f, -0.28f, -0.20f};
    private static final float[] START_KNEE = new float[] {0.0f, 0.04f, 0.16f, 0.38f, 0.52f, 0.24f, 0.06f};
    private static final float[] START_ARM = new float[] {0.0f, 0.06f, 0.16f, 0.28f, 0.34f, 0.22f, 0.10f};
    private static final float[] STOP_KEYS = new float[] {0.0f, 0.10f, 0.22f, 0.36f, 0.50f, 0.64f, 0.78f, 0.90f, 1.0f};
    private static final float[] STOP_HIP = new float[] {0.0f, 0.16f, 0.36f, 0.54f, 0.60f, 0.42f, 0.22f, 0.06f, 0.0f};
    private static final float[] STOP_KNEE = new float[] {0.08f, 0.16f, 0.34f, 0.62f, 0.88f, 0.70f, 0.42f, 0.16f, 0.0f};
    private static final float[] STOP_ARM = new float[] {0.0f, -0.10f, -0.24f, -0.40f, -0.52f, -0.38f, -0.20f, -0.06f, 0.0f};
    private static final float[] STOP_LEAN = new float[] {0.08f, 0.14f, 0.24f, 0.36f, 0.44f, 0.34f, 0.20f, 0.06f, 0.0f};
    private static final float[] LAND_KEYS = new float[] {0.0f, 0.12f, 0.28f, 0.48f, 0.68f, 0.86f, 1.0f};
    private static final float[] LAND_KNEE = new float[] {0.02f, 0.26f, 0.72f, 0.82f, 0.54f, 0.20f, 0.0f};
    private static final float[] LAND_ARM = new float[] {0.0f, -0.10f, -0.24f, -0.28f, -0.18f, -0.06f, 0.0f};
    private static final float[] LAND_LEAN = new float[] {0.0f, 0.08f, 0.20f, 0.24f, 0.16f, 0.06f, 0.0f};
    private static final int VERTEX_COUNT = 719;
    private static final float POSITION_SCALE = 3.67573e-05f;
    private static final float BIAS_X = -0.00545f;
    private static final float BIAS_Y = -0.0793525f;
    private static final float BIAS_Z = -0.016872f;
    private ClaudeModel() {}

    public static Rig create(Image diffuseTexture) {
        VertexArray positions = new VertexArray(719, 3, 2);
        positions.set(0, 719, ClaudeModelData.POSITIONS);
        VertexArray texCoords = new VertexArray(719, 2, 2);
        texCoords.set(0, 719, ClaudeModelData.TEXCOORDS);
        VertexArray normals = new VertexArray(719, 3, 2);
        normals.set(0, 719, ClaudeModelData.NORMALS);
        VertexBuffer vertices = new VertexBuffer();
        vertices.setPositions(positions, POSITION_SCALE, new float[] {BIAS_X, BIAS_Y, BIAS_Z});
        vertices.setTexCoords(0, texCoords, 5.93869747e-05f, null);
        vertices.setNormals(normals);

        Appearance appearance = new Appearance();
        Material material = new Material();
        material.setColor(Material.EMISSIVE, 0xFFFFFFFF);
        material.setColor(Material.SPECULAR, 0xFFFFFFFF);
        material.setShininess(0.0f);
        appearance.setMaterial(material);
        if (diffuseTexture != null) {
            Image2D image = new Image2D(Image2D.RGBA, diffuseTexture);
            Texture2D texture = new Texture2D(image);
            texture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
            texture.setWrapping(Texture2D.WRAP_REPEAT, Texture2D.WRAP_REPEAT);
            appearance.setTexture(0, texture);
        }

        int[] stripLengths = new int[770];
        for (int i = 0; i < stripLengths.length; i++) {
            stripLengths[i] = 3;
        }
        Mesh mesh = new Mesh(vertices, new TriangleStripArray(ClaudeModelData.INDICES, stripLengths), appearance);
        return new Rig(mesh, positions);
    }

    public static final class Rig {
        private final Mesh mesh;
        private final VertexArray positionArray;
        private final short[] deformed = new short[ClaudeModelData.POSITIONS.length];
        private final float[] point = new float[3];

        private Rig(Mesh mesh, VertexArray positionArray) {
            this.mesh = mesh;
            this.positionArray = positionArray;
        }
        public Mesh getMesh() { return mesh; }

        public void animate(int pose, float phase, float gaitBlend, float speed, int transition, float transitionTime) {
            float movement = clamp(gaitBlend);
            boolean moving = pose == WALK || pose == RUN || pose == SPRINT || pose == BACKWARD;
            float cycle = phase / 6.2831853f;
            while (cycle >= 1.0f) cycle -= 1.0f;
            while (cycle < 0.0f) cycle += 1.0f;

            // GTA III blends WALK into RUN by movement speed, then layers RUNFAST on top.
            float runMix = clamp((speed - 0.85f) / 1.15f);
            float sprintMix = clamp((speed - 2.25f) / 1.05f);
            float leftWalkHip = sampleCycle(WALK_HIP, cycle);
            float rightWalkHip = sampleCycle(WALK_HIP, cycle + 0.5f);
            float leftRunHip = sampleCycle(RUN_HIP, cycle);
            float rightRunHip = sampleCycle(RUN_HIP, cycle + 0.5f);
            float leftSprintHip = sampleCycle(SPRINT_HIP, cycle);
            float rightSprintHip = sampleCycle(SPRINT_HIP, cycle + 0.5f);
            float leftWalkKnee = sampleCycle(WALK_KNEE, cycle);
            float rightWalkKnee = sampleCycle(WALK_KNEE, cycle + 0.5f);
            float leftRunKnee = sampleCycle(RUN_KNEE, cycle);
            float rightRunKnee = sampleCycle(RUN_KNEE, cycle + 0.5f);
            float leftSprintKnee = sampleCycle(SPRINT_KNEE, cycle);
            float rightSprintKnee = sampleCycle(SPRINT_KNEE, cycle + 0.5f);
            float leftWalkArm = sampleCycle(WALK_ARM, cycle);
            float rightWalkArm = sampleCycle(WALK_ARM, cycle + 0.5f);
            float leftRunArm = sampleCycle(RUN_ARM, cycle);
            float rightRunArm = sampleCycle(RUN_ARM, cycle + 0.5f);
            float leftSprintArm = sampleCycle(SPRINT_ARM, cycle);
            float rightSprintArm = sampleCycle(SPRINT_ARM, cycle + 0.5f);
            if (pose == BACKWARD) {
                leftWalkHip = sampleCycle(BACKWARD_HIP, cycle);
                rightWalkHip = sampleCycle(BACKWARD_HIP, cycle + 0.5f);
                leftWalkKnee = sampleCycle(BACKWARD_KNEE, cycle);
                rightWalkKnee = sampleCycle(BACKWARD_KNEE, cycle + 0.5f);
                leftWalkArm = sampleCycle(BACKWARD_ARM, cycle);
                rightWalkArm = sampleCycle(BACKWARD_ARM, cycle + 0.5f);
                runMix = 0.0f;
                sprintMix = 0.0f;
            }

            float leftHip = moving ? (leftWalkHip + (leftRunHip - leftWalkHip) * runMix
                    + (leftSprintHip - leftRunHip) * sprintMix) * movement : 0.0f;
            float rightHip = moving ? (rightWalkHip + (rightRunHip - rightWalkHip) * runMix
                    + (rightSprintHip - rightRunHip) * sprintMix) * movement : 0.0f;
            float leftKnee = moving ? (leftWalkKnee + (leftRunKnee - leftWalkKnee) * runMix
                    + (leftSprintKnee - leftRunKnee) * sprintMix) * movement : 0.0f;
            float rightKnee = moving ? (rightWalkKnee + (rightRunKnee - rightWalkKnee) * runMix
                    + (rightSprintKnee - rightRunKnee) * sprintMix) * movement : 0.0f;
            float leftArm = moving ? -(leftWalkArm + (leftRunArm - leftWalkArm) * runMix
                    + (leftSprintArm - leftRunArm) * sprintMix) * movement - 0.05f * movement : 0.0f;
            float rightArm = moving ? -(rightWalkArm + (rightRunArm - rightWalkArm) * runMix
                    + (rightSprintArm - rightRunArm) * sprintMix) * movement - 0.05f * movement : 0.0f;
            float leftElbow = moving ? (sampleCycle(ELBOW_KEYS, cycle)
                    + (sampleCycle(RUN_ELBOW_KEYS, cycle) - sampleCycle(ELBOW_KEYS, cycle)) * runMix
                    + (sampleCycle(SPRINT_ELBOW_KEYS, cycle) - sampleCycle(RUN_ELBOW_KEYS, cycle)) * sprintMix) * movement : 0.04f;
            float rightElbow = moving ? (sampleCycle(ELBOW_KEYS, cycle + 0.5f)
                    + (sampleCycle(RUN_ELBOW_KEYS, cycle + 0.5f) - sampleCycle(ELBOW_KEYS, cycle + 0.5f)) * runMix
                    + (sampleCycle(SPRINT_ELBOW_KEYS, cycle + 0.5f) - sampleCycle(RUN_ELBOW_KEYS, cycle + 0.5f)) * sprintMix) * movement : 0.04f;
            float leftShoulder = moving ? (sampleCycle(WALK_SHOULDER, cycle)
                    + (sampleCycle(RUN_SHOULDER, cycle) - sampleCycle(WALK_SHOULDER, cycle)) * runMix
                    + (sampleCycle(SPRINT_SHOULDER, cycle) - sampleCycle(RUN_SHOULDER, cycle)) * sprintMix) * movement : 0.0f;
            float rightShoulder = moving ? (sampleCycle(WALK_SHOULDER, cycle + 0.5f)
                    + (sampleCycle(RUN_SHOULDER, cycle + 0.5f) - sampleCycle(WALK_SHOULDER, cycle + 0.5f)) * runMix
                    + (sampleCycle(SPRINT_SHOULDER, cycle + 0.5f) - sampleCycle(RUN_SHOULDER, cycle + 0.5f)) * sprintMix) * movement : 0.0f;
            float leftWrist = moving ? (sampleCycle(WALK_WRIST, cycle)
                    + (sampleCycle(RUN_WRIST, cycle) - sampleCycle(WALK_WRIST, cycle)) * runMix
                    + (sampleCycle(SPRINT_WRIST, cycle) - sampleCycle(RUN_WRIST, cycle)) * sprintMix) * movement : 0.0f;
            float rightWrist = moving ? (sampleCycle(WALK_WRIST, cycle + 0.5f)
                    + (sampleCycle(RUN_WRIST, cycle + 0.5f) - sampleCycle(WALK_WRIST, cycle + 0.5f)) * runMix
                    + (sampleCycle(SPRINT_WRIST, cycle + 0.5f) - sampleCycle(RUN_WRIST, cycle + 0.5f)) * sprintMix) * movement : 0.0f;
            float leftFoot = moving ? (sampleCycle(WALK_ANKLE, cycle)
                    + (sampleCycle(RUN_ANKLE, cycle) - sampleCycle(WALK_ANKLE, cycle)) * runMix
                    + (sampleCycle(SPRINT_ANKLE, cycle) - sampleCycle(RUN_ANKLE, cycle)) * sprintMix) * movement : 0.0f;
            float rightFoot = moving ? (sampleCycle(WALK_ANKLE, cycle + 0.5f)
                    + (sampleCycle(RUN_ANKLE, cycle + 0.5f) - sampleCycle(WALK_ANKLE, cycle + 0.5f)) * runMix
                    + (sampleCycle(SPRINT_ANKLE, cycle + 0.5f) - sampleCycle(RUN_ANKLE, cycle + 0.5f)) * sprintMix) * movement : 0.0f;
            if (pose == BACKWARD) {
                leftElbow = 0.08f * movement;
                rightElbow = 0.08f * movement;
                leftShoulder = 0.0f;
                rightShoulder = 0.0f;
                leftWrist = 0.0f;
                rightWrist = 0.0f;
            }

            float torsoLean = moving ? (pose == SPRINT ? 0.28f : (pose == RUN ? 0.22f
                    : (pose == BACKWARD ? -0.025f : 0.06f))) * movement : 0.0f;
            // Counter-rotation follows the actual left/right leg separation,
            // so the torso reacts more strongly to a running stride than a walk.
            float strideDifference = leftHip - rightHip;
            float torsoTwist = moving ? -strideDifference * 0.075f : 0.0f;
            float pelvisRoll = moving ? strideDifference * 0.028f : 0.0f;
            float neckCounter = -torsoTwist * 0.55f;
            float chestLean = torsoLean * 0.58f;
            float chestTwist = torsoTwist * 0.62f;
            float pelvisShift = moving ? (float) Math.sin(phase * 2.0f) * 0.014f * movement : 0.0f;
            float bodyCompression = moving ? (pose == WALK
                    ? (float) Math.sin(phase * 2.0f) * 0.004f
                    : Math.max(0.0f, (float) Math.sin(phase * 2.0f)) * (pose == SPRINT ? 0.014f : 0.010f)) : 0.0f;
            float shoulderLift = moving ? (float) Math.sin(phase * 2.0f) * (pose == SPRINT ? 0.035f : 0.018f) * movement : 0.0f;

            // GTA III uses a short STARTWALK clip before the repeating walk/run cycles.
            if (transition == START_WALK) {
                float t = clamp(transitionTime);
                float startHip = sampleClip(START_KEYS, START_HIP, t);
                float startKnee = sampleClip(START_KEYS, START_KNEE, t);
                float startArm = sampleClip(START_KEYS, START_ARM, t);
                float cycleMix = smooth(t);
                leftHip = startHip * (1.0f - cycleMix) + leftHip * cycleMix;
                rightHip = -startHip * (1.0f - cycleMix) + rightHip * cycleMix;
                leftKnee = startKnee * (1.0f - cycleMix) + leftKnee * cycleMix;
                rightKnee = startKnee * 0.55f * (1.0f - cycleMix) + rightKnee * cycleMix;
                leftArm = -startArm * (1.0f - cycleMix) + leftArm * cycleMix;
                rightArm = startArm * (1.0f - cycleMix) + rightArm * cycleMix;
                leftElbow = (0.10f + startKnee * 0.25f) * (1.0f - cycleMix) + leftElbow * cycleMix;
                rightElbow = (0.10f + startKnee * 0.25f) * (1.0f - cycleMix) + rightElbow * cycleMix;
                torsoLean = 0.10f * (1.0f - cycleMix) + torsoLean * cycleMix;
            } else if (transition == RUN_STOP_LEFT || transition == RUN_STOP_RIGHT) {
                // Two mirrored stop clips plant a different foot, then absorb momentum.
                float t = clamp(transitionTime);
                float stopHip = sampleClip(STOP_KEYS, STOP_HIP, t);
                float stopKnee = sampleClip(STOP_KEYS, STOP_KNEE, t);
                float stopArm = sampleClip(STOP_KEYS, STOP_ARM, t);
                float stopLean = sampleClip(STOP_KEYS, STOP_LEAN, t);
                float stopMix = smooth(t);
                float side = transition == RUN_STOP_LEFT ? 1.0f : -1.0f;
                leftHip = leftHip * (1.0f - stopMix) + stopHip * side * stopMix;
                rightHip = rightHip * (1.0f - stopMix) - stopHip * side * stopMix;
                leftKnee = leftKnee * (1.0f - stopMix) + stopKnee * (side > 0.0f ? 1.0f : 0.55f) * stopMix;
                rightKnee = rightKnee * (1.0f - stopMix) + stopKnee * (side < 0.0f ? 1.0f : 0.55f) * stopMix;
                leftArm = leftArm * (1.0f - stopMix) + stopArm * stopMix;
                rightArm = rightArm * (1.0f - stopMix) + stopArm * stopMix;
                leftElbow = leftElbow * (1.0f - stopMix) + (0.18f + stopKnee * 0.18f) * stopMix;
                rightElbow = rightElbow * (1.0f - stopMix) + (0.18f + stopKnee * 0.18f) * stopMix;
                torsoLean = torsoLean * (1.0f - stopMix) + stopLean * stopMix;
            }

            if (pose == JUMP) {
                leftHip = -0.22f; rightHip = 0.30f;
                leftKnee = 0.58f; rightKnee = 0.48f;
                leftArm = -0.78f; rightArm = -0.78f;
                leftElbow = 0.22f; rightElbow = 0.22f;
                torsoLean = -0.12f;
            } else if (pose == LAND) {
                float t = transition == LANDING ? clamp(transitionTime) : 1.0f;
                float landKnee = sampleClip(LAND_KEYS, LAND_KNEE, t);
                float landArm = sampleClip(LAND_KEYS, LAND_ARM, t);
                leftHip = -0.08f * (1.0f - smooth(t));
                rightHip = leftHip;
                leftKnee = landKnee; rightKnee = landKnee;
                leftArm = landArm; rightArm = landArm;
                leftElbow = 0.16f + landKnee * 0.18f;
                rightElbow = leftElbow;
                torsoLean = sampleClip(LAND_KEYS, LAND_LEAN, t);
            }

            // Small breathing and balance motion replaces a perfectly frozen idle pose.
            if (!moving && transition == 0 && pose != JUMP && pose != LAND) {
                float breath = (float) Math.sin(phase * 0.55f);
                torsoLean = breath * 0.008f;
                leftArm = breath * 0.018f;
                rightArm = -breath * 0.018f;
            }

            float leftAnkle = moving ? leftFoot - leftHip * 0.10f - leftKnee * 0.22f
                    : (transition != 0 ? -leftHip * 0.18f - leftKnee * 0.34f
                    : (pose == LAND ? -0.12f : (pose == JUMP ? 0.12f : 0.0f)));
            float rightAnkle = moving ? rightFoot - rightHip * 0.10f - rightKnee * 0.22f
                    : (transition != 0 ? -rightHip * 0.18f - rightKnee * 0.34f
                    : (pose == LAND ? -0.12f : (pose == JUMP ? 0.12f : 0.0f)));
            float headCounter = -torsoLean * 0.30f;
            float headTurn = neckCounter + (moving ? sampleCycle(WALK_HIP, cycle) * 0.035f : 0.0f);

            float sl = (float) Math.sin(leftHip), cl = (float) Math.cos(leftHip);
            float sr = (float) Math.sin(rightHip), cr = (float) Math.cos(rightHip);
            float sal = (float) Math.sin(leftArm), cal = (float) Math.cos(leftArm);
            float sar = (float) Math.sin(rightArm), car = (float) Math.cos(rightArm);
            float skl = (float) Math.sin(leftKnee), ckl = (float) Math.cos(leftKnee);
            float skr = (float) Math.sin(rightKnee), ckr = (float) Math.cos(rightKnee);
            float sel = (float) Math.sin(leftElbow), cel = (float) Math.cos(leftElbow);
            float ser = (float) Math.sin(rightElbow), cer = (float) Math.cos(rightElbow);
            float saln = (float) Math.sin(leftAnkle), caln = (float) Math.cos(leftAnkle);
            float sran = (float) Math.sin(rightAnkle), cran = (float) Math.cos(rightAnkle);
            float st = (float) Math.sin(torsoLean), ct = (float) Math.cos(torsoLean);
            float stw = (float) Math.sin(torsoTwist), ctw = (float) Math.cos(torsoTwist);
            float spr = (float) Math.sin(pelvisRoll), cpr = (float) Math.cos(pelvisRoll);
            float sh = (float) Math.sin(headCounter), ch = (float) Math.cos(headCounter);
            float sht = (float) Math.sin(headTurn), cht = (float) Math.cos(headTurn);
            float scl = (float) Math.sin(chestLean), ccl = (float) Math.cos(chestLean);
            float sct = (float) Math.sin(chestTwist), cct = (float) Math.cos(chestTwist);
            float ssl = (float) Math.sin(leftShoulder + shoulderLift), csl = (float) Math.cos(leftShoulder + shoulderLift);
            float ssr = (float) Math.sin(rightShoulder - shoulderLift), csr = (float) Math.cos(rightShoulder - shoulderLift);
            float swl = (float) Math.sin(leftWrist), cwl = (float) Math.cos(leftWrist);
            float swr = (float) Math.sin(rightWrist), cwr = (float) Math.cos(rightWrist);
            float jumpLeftArmSin = (float) Math.sin(-0.65f), jumpLeftArmCos = (float) Math.cos(-0.65f);
            float jumpRightArmSin = (float) Math.sin(0.65f), jumpRightArmCos = (float) Math.cos(0.65f);

            for (int i = 0; i < ClaudeModelData.POSITIONS.length; i += 3) {
                float x = ClaudeModelData.POSITIONS[i] * POSITION_SCALE + BIAS_X;
                float y = ClaudeModelData.POSITIONS[i + 1] * POSITION_SCALE + BIAS_Y;
                float z = ClaudeModelData.POSITIONS[i + 2] * POSITION_SCALE + BIAS_Z;
                float absX = Math.abs(x);
                // Keep each leg's influence centered on its own side of the
                // pelvis. A narrow smooth seam prevents the two leg weights
                // from bleeding across the crotch and inner thighs.
                float legEnvelope = smooth(clamp((0.27f - absX) / 0.13f));
                float split = smooth(clamp((x + 0.045f) / 0.09f));
                float legBase = smooth(clamp((-y - 0.02f) / 0.22f)) * legEnvelope;
                float lw = legBase * (1.0f - split), rw = legBase * split;
                // Lower-leg and shoe weights are nested lower down, so knee
                // and ankle rotations cannot pull the upper trouser mesh.
                float kneeBase = smooth(clamp((-y - 0.42f) / 0.20f)) * legEnvelope;
                float lkw = kneeBase * (1.0f - split), rkw = kneeBase * split;
                float footBase = smooth(clamp((-y - 0.64f) / 0.12f)) * legEnvelope;
                float lfw = footBase * (1.0f - split), rfw = footBase * split;
                int armSide = ClaudeModelData.ARM_SIDE[i / 3];
                float armWeight = armSide == 0 ? 0.0f : clamp((absX - 0.15f) / 0.14f);
                float law = armSide < 0 ? armWeight : 0.0f;
                float raw = armSide > 0 ? armWeight : 0.0f;
                float elbowRegion = clamp((0.39f - y) / 0.20f);
                float wristRegion = clamp((0.22f - y) / 0.12f);
                float shoulderRegion = clamp((0.48f - y) / 0.14f);
                float torsoWeight = clamp((0.36f - absX) / 0.19f)
                        * clamp((y + 0.04f) / 0.20f) * clamp((0.72f - y) / 0.18f);
                float chestWeight = clamp((0.34f - absX) / 0.20f)
                        * clamp((y - 0.30f) / 0.12f) * clamp((0.68f - y) / 0.16f);
                float pelvisWeight = clamp((0.27f - absX) / 0.16f)
                        * clamp((0.20f - y) / 0.17f) * clamp((y + 0.16f) / 0.18f);
                float headWeight = clamp((y - 0.58f) / 0.17f) * clamp((0.23f - absX) / 0.14f);
                point[0] = x; point[1] = y; point[2] = z;
                // Small side-to-side weight transfer and vertical compression
                // keep the pelvis from behaving like a rigid pivot.
                point[0] += pelvisShift * pelvisWeight;
                point[1] += bodyCompression * (torsoWeight + pelvisWeight * 0.65f);

                rotateX(-0.12f, -0.06f, 0.0f, sl, cl, lw);
                rotateX(0.12f, -0.06f, 0.0f, sr, cr, rw);
                rotateX(-0.12f, -0.48f, 0.0f, skl, ckl, lkw);
                rotateX(0.12f, -0.48f, 0.0f, skr, ckr, rkw);
                rotateX(-0.12f, -0.68f, 0.0f, saln, caln, lfw);
                rotateX(0.12f, -0.68f, 0.0f, sran, cran, rfw);
                rotateX(0.0f, 0.16f, 0.0f, st, ct, torsoWeight);
                rotateY(0.0f, 0.16f, 0.0f, stw, ctw, torsoWeight);
                rotateX(0.0f, 0.42f, 0.0f, scl, ccl, chestWeight);
                rotateY(0.0f, 0.42f, 0.0f, sct, cct, chestWeight);
                rotateZ(0.0f, -0.02f, 0.0f, spr, cpr, pelvisWeight);
                // Clavicle/shoulder, upper arm, elbow, forearm and wrist are
                // separate deformation zones rather than one rigid arm swing.
                rotateZ(-0.19f, 0.42f, 0.04f, ssl, csl, law * shoulderRegion);
                rotateZ(0.19f, 0.42f, 0.04f, ssr, csr, raw * shoulderRegion);
                rotateX(-0.19f, 0.42f, 0.04f, sal, cal, law);
                rotateX(0.19f, 0.42f, 0.04f, sar, car, raw);
                rotateX(-0.25f, 0.20f, 0.04f, sel, cel, law * elbowRegion);
                rotateX(0.25f, 0.20f, 0.04f, ser, cer, raw * elbowRegion);
                rotateX(-0.25f, 0.055f, 0.04f, swl, cwl, law * wristRegion);
                rotateX(0.25f, 0.055f, 0.04f, swr, cwr, raw * wristRegion);
                rotateZ(-0.19f, 0.42f, 0.04f, jumpLeftArmSin, jumpLeftArmCos, law * (pose == JUMP ? 1.0f : 0.0f));
                rotateZ(0.19f, 0.42f, 0.04f, jumpRightArmSin, jumpRightArmCos, raw * (pose == JUMP ? 1.0f : 0.0f));
                rotateX(0.0f, 0.68f, 0.0f, sh, ch, headWeight);
                rotateY(0.0f, 0.61f, 0.0f, sht, cht, headWeight);

                deformed[i] = quantize((point[0] - BIAS_X) / POSITION_SCALE);
                deformed[i + 1] = quantize((point[1] - BIAS_Y) / POSITION_SCALE);
                deformed[i + 2] = quantize((point[2] - BIAS_Z) / POSITION_SCALE);
            }
            positionArray.set(0, VERTEX_COUNT, deformed);
        }

        private float sampleCycle(float[] values, float phase) {
            while (phase >= 1.0f) phase -= 1.0f;
            while (phase < 0.0f) phase += 1.0f;
            int segments = values.length - 1;
            float scaled = phase * segments;
            int index = (int) scaled;
            if (index >= segments) index = segments - 1;
            float t = scaled - index;
            int previous = index == 0 ? segments - 1 : index - 1;
            int next = index + 2 > segments ? 1 : index + 2;
            float p0 = values[index];
            float p1 = values[index + 1];
            float tangent0 = (p1 - values[previous]) * 0.5f;
            float tangent1 = (values[next] - p0) * 0.5f;
            float t2 = t * t;
            float t3 = t2 * t;
            return (2.0f * t3 - 3.0f * t2 + 1.0f) * p0
                    + (t3 - 2.0f * t2 + t) * tangent0
                    + (-2.0f * t3 + 3.0f * t2) * p1
                    + (t3 - t2) * tangent1;
        }

        private float sampleClip(float[] times, float[] values, float time) {
            if (time <= times[0]) return values[0];
            int last = times.length - 1;
            if (time >= times[last]) return values[last];
            for (int i = 0; i < last; i++) {
                if (time <= times[i + 1]) {
                    float duration = times[i + 1] - times[i];
                    float t = (time - times[i]) / duration;
                    float p0 = values[i], p1 = values[i + 1];
                    float tangent0 = 0.0f, tangent1 = 0.0f;
                    if (i > 0) {
                        tangent0 = (values[i + 1] - values[i - 1])
                                / (times[i + 1] - times[i - 1]) * duration;
                    }
                    if (i + 2 <= last) {
                        tangent1 = (values[i + 2] - values[i])
                                / (times[i + 2] - times[i]) * duration;
                    }
                    float t2 = t * t, t3 = t2 * t;
                    return (2.0f * t3 - 3.0f * t2 + 1.0f) * p0
                            + (t3 - 2.0f * t2 + t) * tangent0
                            + (-2.0f * t3 + 3.0f * t2) * p1
                            + (t3 - t2) * tangent1;
                }
            }
            return values[last];
        }

        private float smooth(float value) {
            if (value < 0.0f) value = 0.0f;
            if (value > 1.0f) value = 1.0f;
            return value * value * (3.0f - 2.0f * value);
        }

        private void rotateX(float px, float py, float pz, float s, float c, float weight) {
            if (weight <= 0.0f) return;
            float y = point[1], z = point[2];
            float ry = py + (y - py) * c - (z - pz) * s;
            float rz = pz + (y - py) * s + (z - pz) * c;
            point[1] = y + (ry - y) * weight; point[2] = z + (rz - z) * weight;
        }

        private void rotateZ(float px, float py, float pz, float s, float c, float weight) {
            if (weight <= 0.0f) return;
            float x = point[0], y = point[1];
            float rx = px + (x - px) * c - (y - py) * s;
            float ry = py + (x - px) * s + (y - py) * c;
            point[0] = x + (rx - x) * weight; point[1] = y + (ry - y) * weight;
        }

        private void rotateY(float px, float py, float pz, float s, float c, float weight) {
            if (weight <= 0.0f) return;
            float x = point[0], z = point[2];
            float rx = px + (x - px) * c + (z - pz) * s;
            float rz = pz - (x - px) * s + (z - pz) * c;
            point[0] = x + (rx - x) * weight; point[2] = z + (rz - z) * weight;
        }

        private float clamp(float value) {
            if (value < 0.0f) return 0.0f; if (value > 1.0f) return 1.0f; return value;
        }
        private short quantize(float value) {
            if (value < -32767.0f) value = -32767.0f; if (value > 32767.0f) value = 32767.0f;
            return (short) (value + (value >= 0.0f ? 0.5f : -0.5f));
        }
    }

}
