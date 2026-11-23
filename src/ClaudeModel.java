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

        public void animate(int pose, float phase, float gaitBlend) {
            float movement = clamp(gaitBlend);
            boolean moving = pose == WALK || pose == RUN || pose == SPRINT;
            float idleSway = (float) Math.sin(phase * 0.55f);
            float gait = (float) Math.sin(phase);
            float stride = pose == SPRINT ? 0.62f : (pose == RUN ? 0.51f : 0.39f);
            float kneeStrength = pose == SPRINT ? 0.66f : (pose == RUN ? 0.54f : 0.40f);
            float armStrength = pose == SPRINT ? 0.72f : (pose == RUN ? 0.62f : 0.52f);

            // Alternating hip swing, with knee flex during the forward recovery phase.
            float leftHip = moving ? gait * stride * movement : idleSway * 0.012f;
            float rightHip = -leftHip;
            float leftKnee = moving ? Math.max(0.0f, (float) Math.sin(phase + 0.72f)) * kneeStrength * movement
                    : (pose == JUMP ? 0.58f : (pose == LAND ? 0.72f : 0.0f));
            float rightKnee = moving ? Math.max(0.0f, (float) Math.sin(phase + 3.86f)) * kneeStrength * movement
                    : (pose == JUMP ? 0.48f : (pose == LAND ? 0.72f : 0.0f));

            // Arms counter-swing against the legs; elbows stay flexed instead of swinging as rigid sticks.
            float leftArm = moving ? -leftHip * armStrength / stride - 0.06f * movement : idleSway * 0.025f;
            float rightArm = moving ? -rightHip * armStrength / stride - 0.06f * movement : -idleSway * 0.025f;
            float leftElbow = moving ? 0.12f + Math.max(0.0f, (float) Math.sin(phase + 1.2f)) * 0.24f * movement : 0.04f;
            float rightElbow = moving ? 0.12f + Math.max(0.0f, (float) Math.sin(phase + 4.34f)) * 0.24f * movement : 0.04f;

            float leftAnkle = moving ? -leftHip * 0.22f - leftKnee * 0.30f : (pose == LAND ? -0.16f : 0.0f);
            float rightAnkle = moving ? -rightHip * 0.22f - rightKnee * 0.30f : (pose == LAND ? -0.16f : 0.0f);
            float torsoLean = moving ? (pose == SPRINT ? 0.19f : (pose == RUN ? 0.13f : 0.07f)) * movement
                    : (pose == JUMP ? -0.12f : (pose == LAND ? 0.10f : 0.0f));
            float torsoTwist = moving ? -gait * 0.055f * movement : idleSway * 0.008f;
            float pelvisRoll = moving ? gait * 0.025f * movement : idleSway * 0.006f;
            float headCounter = -torsoLean * 0.28f;

            if (pose == JUMP) {
                leftHip = -0.22f;
                rightHip = 0.30f;
                leftArm = -0.78f;
                rightArm = -0.78f;
                leftElbow = 0.22f;
                rightElbow = 0.22f;
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
                float lw = legBase * (1.0f - split);
                float rw = legBase * split;
                float kneeBase = clamp((-y - 0.47f) / 0.20f) * center;
                float lkw = kneeBase * (1.0f - split);
                float rkw = kneeBase * split;
                float footBase = clamp((-y - 0.66f) / 0.15f) * center;
                float lfw = footBase * (1.0f - split);
                float rfw = footBase * split;

                int armSide = ClaudeModelData.ARM_SIDE[i / 3];
                float armWeight = armSide == 0 ? 0.0f : clamp((absX - 0.15f) / 0.14f);
                float law = armSide < 0 ? armWeight : 0.0f;
                float raw = armSide > 0 ? armWeight : 0.0f;
                float elbowRegion = clamp((0.39f - y) / 0.20f);
                float torsoWeight = clamp((0.36f - absX) / 0.19f)
                        * clamp((y + 0.04f) / 0.20f) * clamp((0.72f - y) / 0.18f);
                float pelvisWeight = clamp((0.27f - absX) / 0.16f)
                        * clamp((0.20f - y) / 0.17f) * clamp((y + 0.16f) / 0.18f);
                float headWeight = clamp((y - 0.58f) / 0.17f)
                        * clamp((0.23f - absX) / 0.14f);

                point[0] = x; point[1] = y; point[2] = z;

                // Lower body: hips, knees and ankle/foot articulation.
                rotateX(-0.12f, -0.06f, 0.0f, sl, cl, lw);
                rotateX(0.12f, -0.06f, 0.0f, sr, cr, rw);
                rotateX(-0.12f, -0.48f, 0.0f, skl, ckl, lkw);
                rotateX(0.12f, -0.48f, 0.0f, skr, ckr, rkw);
                rotateX(-0.12f, -0.68f, 0.0f, saln, caln, lfw);
                rotateX(0.12f, -0.68f, 0.0f, sran, cran, rfw);

                // Upper body counter-rotates slightly to keep the run from looking mechanical.
                rotateX(0.0f, 0.16f, 0.0f, st, ct, torsoWeight);
                rotateY(0.0f, 0.16f, 0.0f, stw, ctw, torsoWeight);
                rotateZ(0.0f, -0.02f, 0.0f, spr, cpr, pelvisWeight);

                // Shoulder swing and a small elbow bend add a second joint to each arm.
                rotateX(-0.19f, 0.42f, 0.04f, sal, cal, law);
                rotateX(0.19f, 0.42f, 0.04f, sar, car, raw);
                rotateX(-0.25f, 0.20f, 0.04f, sel, cel, law * elbowRegion);
                rotateX(0.25f, 0.20f, 0.04f, ser, cer, raw * elbowRegion);
                rotateZ(-0.19f, 0.42f, 0.04f, jumpLeftArmSin, jumpLeftArmCos, law * (pose == JUMP ? 1.0f : 0.0f));
                rotateZ(0.19f, 0.42f, 0.04f, jumpRightArmSin, jumpRightArmCos, raw * (pose == JUMP ? 1.0f : 0.0f));

                // The head compensates for the torso lean, keeping the gaze more level.
                rotateX(0.0f, 0.68f, 0.0f, sh, ch, headWeight);

                deformed[i] = quantize((point[0] - BIAS_X) / POSITION_SCALE);
                deformed[i + 1] = quantize((point[1] - BIAS_Y) / POSITION_SCALE);
                deformed[i + 2] = quantize((point[2] - BIAS_Z) / POSITION_SCALE);
            }
            positionArray.set(0, VERTEX_COUNT, deformed);
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
