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

    private static final float[] CYCLE_KEYS = new float[] {0.0f, 0.25f, 0.5f, 0.75f, 1.0f};
    private static final float[] WALK_HIP = new float[] {-0.30f, -0.02f, 0.30f, 0.02f, -0.30f};
    private static final float[] RUN_HIP = new float[] {-0.48f, -0.02f, 0.48f, 0.02f, -0.48f};
    private static final float[] SPRINT_HIP = new float[] {-0.62f, -0.02f, 0.62f, 0.02f, -0.62f};
    private static final float[] WALK_KNEE = new float[] {0.04f, 0.38f, 0.05f, 0.12f, 0.04f};
    private static final float[] RUN_KNEE = new float[] {0.06f, 0.52f, 0.06f, 0.15f, 0.06f};
    private static final float[] SPRINT_KNEE = new float[] {0.08f, 0.68f, 0.08f, 0.20f, 0.08f};
    private static final float[] WALK_ARM = new float[] {0.22f, 0.02f, -0.22f, -0.02f, 0.22f};
    private static final float[] RUN_ARM = new float[] {0.36f, 0.02f, -0.36f, -0.02f, 0.36f};
    private static final float[] SPRINT_ARM = new float[] {0.46f, 0.02f, -0.46f, -0.02f, 0.46f};
    private static final float[] ELBOW_KEYS = new float[] {0.10f, 0.30f, 0.10f, 0.24f, 0.10f};
    private static final float[] START_KEYS = new float[] {0.0f, 0.22f, 0.58f, 1.0f};
    private static final float[] START_HIP = new float[] {0.0f, -0.16f, -0.34f, -0.22f};
    private static final float[] START_KNEE = new float[] {0.0f, 0.10f, 0.44f, 0.08f};
    private static final float[] START_ARM = new float[] {0.0f, 0.12f, 0.28f, 0.12f};
    private static final float[] STOP_KEYS = new float[] {0.0f, 0.18f, 0.42f, 0.68f, 1.0f};
    private static final float[] STOP_HIP = new float[] {0.0f, 0.20f, 0.38f, 0.20f, 0.0f};
    private static final float[] STOP_KNEE = new float[] {0.08f, 0.18f, 0.64f, 0.30f, 0.0f};
    private static final float[] STOP_ARM = new float[] {0.0f, -0.18f, -0.34f, -0.12f, 0.0f};
    private static final float[] STOP_LEAN = new float[] {0.08f, 0.18f, 0.30f, 0.12f, 0.0f};
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
            boolean moving = pose == WALK || pose == RUN || pose == SPRINT;
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
            float leftElbow = moving ? sampleCycle(ELBOW_KEYS, cycle) * movement : 0.04f;
            float rightElbow = moving ? sampleCycle(ELBOW_KEYS, cycle + 0.5f) * movement : 0.04f;

            float leftAnkle = moving ? -leftHip * 0.18f - leftKnee * 0.34f : 0.0f;
            float rightAnkle = moving ? -rightHip * 0.18f - rightKnee * 0.34f : 0.0f;
            float torsoLean = moving ? (pose == SPRINT ? 0.20f : (pose == RUN ? 0.13f : 0.06f)) * movement : 0.0f;
            float torsoTwist = moving ? -sampleCycle(WALK_HIP, cycle) * 0.10f * movement : 0.0f;
            float pelvisRoll = moving ? sampleCycle(WALK_HIP, cycle) * 0.035f * movement : 0.0f;
            float headCounter = -torsoLean * 0.30f;

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
                leftHip = leftHip * (1.0f - stopMix) + stopHip * side * (1.0f - t) * stopMix;
                rightHip = rightHip * (1.0f - stopMix) - stopHip * side * (1.0f - t) * stopMix;
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
                leftHip = -0.08f; rightHip = -0.08f;
                leftKnee = 0.72f; rightKnee = 0.72f;
                leftArm = -0.18f; rightArm = -0.18f;
                leftElbow = 0.28f; rightElbow = 0.28f;
                torsoLean = 0.10f;
            }

            // Small breathing and balance motion replaces a perfectly frozen idle pose.
            if (!moving && transition == 0 && pose != JUMP && pose != LAND) {
                float breath = (float) Math.sin(phase * 0.55f);
                torsoLean = breath * 0.008f;
                leftArm = breath * 0.018f;
                rightArm = -breath * 0.018f;
            }

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
            float jumpLeftArmSin = (float) Math.sin(-0.65f), jumpLeftArmCos = (float) Math.cos(-0.65f);
            float jumpRightArmSin = (float) Math.sin(0.65f), jumpRightArmCos = (float) Math.cos(0.65f);

            for (int i = 0; i < ClaudeModelData.POSITIONS.length; i += 3) {
                float x = ClaudeModelData.POSITIONS[i] * POSITION_SCALE + BIAS_X;
                float y = ClaudeModelData.POSITIONS[i + 1] * POSITION_SCALE + BIAS_Y;
                float z = ClaudeModelData.POSITIONS[i + 2] * POSITION_SCALE + BIAS_Z;
                float absX = Math.abs(x);
                float center = clamp((0.29f - absX) / 0.13f);
                float split = clamp((x + 0.12f) / 0.24f);
                float legBase = clamp((-y + 0.04f) / 0.25f) * center;
                float lw = legBase * (1.0f - split), rw = legBase * split;
                float kneeBase = clamp((-y - 0.47f) / 0.20f) * center;
                float lkw = kneeBase * (1.0f - split), rkw = kneeBase * split;
                float footBase = clamp((-y - 0.66f) / 0.15f) * center;
                float lfw = footBase * (1.0f - split), rfw = footBase * split;
                int armSide = ClaudeModelData.ARM_SIDE[i / 3];
                float armWeight = armSide == 0 ? 0.0f : clamp((absX - 0.15f) / 0.14f);
                float law = armSide < 0 ? armWeight : 0.0f;
                float raw = armSide > 0 ? armWeight : 0.0f;
                float elbowRegion = clamp((0.39f - y) / 0.20f);
                float torsoWeight = clamp((0.36f - absX) / 0.19f)
                        * clamp((y + 0.04f) / 0.20f) * clamp((0.72f - y) / 0.18f);
                float pelvisWeight = clamp((0.27f - absX) / 0.16f)
                        * clamp((0.20f - y) / 0.17f) * clamp((y + 0.16f) / 0.18f);
                float headWeight = clamp((y - 0.58f) / 0.17f) * clamp((0.23f - absX) / 0.14f);
                point[0] = x; point[1] = y; point[2] = z;

                rotateX(-0.12f, -0.06f, 0.0f, sl, cl, lw);
                rotateX(0.12f, -0.06f, 0.0f, sr, cr, rw);
                rotateX(-0.12f, -0.48f, 0.0f, skl, ckl, lkw);
                rotateX(0.12f, -0.48f, 0.0f, skr, ckr, rkw);
                rotateX(-0.12f, -0.68f, 0.0f, saln, caln, lfw);
                rotateX(0.12f, -0.68f, 0.0f, sran, cran, rfw);
                rotateX(0.0f, 0.16f, 0.0f, st, ct, torsoWeight);
                rotateY(0.0f, 0.16f, 0.0f, stw, ctw, torsoWeight);
                rotateZ(0.0f, -0.02f, 0.0f, spr, cpr, pelvisWeight);
                rotateX(-0.19f, 0.42f, 0.04f, sal, cal, law);
                rotateX(0.19f, 0.42f, 0.04f, sar, car, raw);
                rotateX(-0.25f, 0.20f, 0.04f, sel, cel, law * elbowRegion);
                rotateX(0.25f, 0.20f, 0.04f, ser, cer, raw * elbowRegion);
                rotateZ(-0.19f, 0.42f, 0.04f, jumpLeftArmSin, jumpLeftArmCos, law * (pose == JUMP ? 1.0f : 0.0f));
                rotateZ(0.19f, 0.42f, 0.04f, jumpRightArmSin, jumpRightArmCos, raw * (pose == JUMP ? 1.0f : 0.0f));
                rotateX(0.0f, 0.68f, 0.0f, sh, ch, headWeight);

                deformed[i] = quantize((point[0] - BIAS_X) / POSITION_SCALE);
                deformed[i + 1] = quantize((point[1] - BIAS_Y) / POSITION_SCALE);
                deformed[i + 2] = quantize((point[2] - BIAS_Z) / POSITION_SCALE);
            }
            positionArray.set(0, VERTEX_COUNT, deformed);
        }

        private float sampleCycle(float[] values, float phase) {
            while (phase >= 1.0f) phase -= 1.0f;
            while (phase < 0.0f) phase += 1.0f;
            float scaled = phase * 4.0f;
            int index = (int) scaled;
            if (index > 3) index = 3;
            float t = scaled - index;
            int previous = index == 0 ? 3 : index - 1;
            int next = index + 2 > 4 ? 1 : index + 2;
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
                    float amount = (time - times[i]) / (times[i + 1] - times[i]);
                    amount = smooth(amount);
                    return values[i] + (values[i + 1] - values[i]) * amount;
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
