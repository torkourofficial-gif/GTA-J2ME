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

        public void animate(int pose, float phase) {
            float cycle = (float) Math.sin(phase);
            float leftLeg = pose == WALK ? cycle * 0.48f : (pose == JUMP ? 0.22f : 0.0f);
            float rightLeg = -leftLeg;
            float leftArm = pose == WALK ? -cycle * 0.34f : (pose == JUMP ? 0.0f : cycle * 0.04f);
            float rightArm = -leftArm;
            float leftKnee = pose == WALK ? Math.max(0.0f, (float) Math.sin(phase + 1.0f)) * 0.42f : (pose == JUMP ? 0.55f : 0.0f);
            float rightKnee = pose == WALK ? Math.max(0.0f, (float) Math.sin(phase + 4.14f)) * 0.42f : (pose == JUMP ? 0.55f : 0.0f);
            float leftLift = pose == JUMP ? -1.3f : 0.0f;
            float rightLift = pose == JUMP ? 1.3f : 0.0f;
            float sl = (float) Math.sin(leftLeg), cl = (float) Math.cos(leftLeg);
            float sr = (float) Math.sin(rightLeg), cr = (float) Math.cos(rightLeg);
            float sal = (float) Math.sin(leftArm), cal = (float) Math.cos(leftArm);
            float sar = (float) Math.sin(rightArm), car = (float) Math.cos(rightArm);
            float skl = (float) Math.sin(leftKnee), ckl = (float) Math.cos(leftKnee);
            float skr = (float) Math.sin(rightKnee), ckr = (float) Math.cos(rightKnee);
            float szl = (float) Math.sin(leftLift), czl = (float) Math.cos(leftLift);
            float szr = (float) Math.sin(rightLift), czr = (float) Math.cos(rightLift);
            for (int i = 0; i < ClaudeModelData.POSITIONS.length; i += 3) {
                float x = ClaudeModelData.POSITIONS[i] * POSITION_SCALE + BIAS_X;
                float y = ClaudeModelData.POSITIONS[i + 1] * POSITION_SCALE + BIAS_Y;
                float z = ClaudeModelData.POSITIONS[i + 2] * POSITION_SCALE + BIAS_Z;
                float absX = Math.abs(x);
                float lower = clamp((-y + 0.04f) / 0.25f);
                float center = clamp((0.29f - absX) / 0.13f);
                float split = clamp((x + 0.12f) / 0.24f);
                float legBase = lower * center;
                float lw = legBase * (1.0f - split);
                float rw = legBase * split;
                float kneeBase = clamp((-y - 0.47f) / 0.20f) * center;
                float lkw = kneeBase * (1.0f - split);
                float rkw = kneeBase * split;
                int armSide = ClaudeModelData.ARM_SIDE[i / 3];
                float armWeight = armSide == 0 ? 0.0f : clamp((absX - 0.15f) / 0.14f);
                float law = armSide < 0 ? armWeight : 0.0f;
                float raw = armSide > 0 ? armWeight : 0.0f;
                point[0] = x; point[1] = y; point[2] = z;
                rotateX(-0.12f, -0.06f, 0.0f, sl, cl, lw);
                rotateX(0.12f, -0.06f, 0.0f, sr, cr, rw);
                rotateX(-0.12f, -0.48f, 0.0f, skl, ckl, lkw);
                rotateX(0.12f, -0.48f, 0.0f, skr, ckr, rkw);
                rotateX(-0.19f, 0.42f, 0.04f, sal, cal, law);
                rotateX(0.19f, 0.42f, 0.04f, sar, car, raw);
                rotateZ(-0.19f, 0.42f, 0.04f, szl, czl, law * (pose == JUMP ? 1.0f : 0.0f));
                rotateZ(0.19f, 0.42f, 0.04f, szr, czr, raw * (pose == JUMP ? 1.0f : 0.0f));
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

        private float clamp(float value) {
            if (value < 0.0f) return 0.0f; if (value > 1.0f) return 1.0f; return value;
        }
        private short quantize(float value) {
            if (value < -32767.0f) value = -32767.0f; if (value > 32767.0f) value = 32767.0f;
            return (short) (value + (value >= 0.0f ? 0.5f : -0.5f));
        }
    }

