YªçŠx-®éÜj×¢ëiºÚ+Š§j[h‘éÜ¢éí×O<ÓÔèµ©hºÚn¶X§zÍûpackage gta3me;

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
        positions.set(0, 719, POSITIONS);
        VertexArray texCoords = new VertexArray(719, 2, 2);
        texCoords.set(0, 719, TEXCOORDS);
        VertexArray normals = new VertexArray(719, 3, 2);
        normals.set(0, 719, NORMALS);
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
        Mesh mesh = new Mesh(vertices, new TriangleStripArray(INDICES, stripLengths), appearance);
        return new Rig(mesh, positions);
    }

    public static final class Rig {
        private final Mesh mesh;
        private final VertexArray positionArray;
        private final short[] deformed = new short[POSITIONS.length];
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
            for (int i = 0; i < POSITIONS.length; i += 3) {
                float x = POSITIONS[i] * POSITION_SCALE + BIAS_X;
                float y = POSITIONS[i + 1] * POSITION_SCALE + BIAS_Y;
                float z = POSITIONS[i + 2] * POSITION_SCALE + BIAS_Z;
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
                int armSide = ARM_SIDE[i / 3];
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

    private static final short[] POSITIONS = createPOSITIONS();
    private static short[] createPOSITIONS() {
        short[] values = new short[2157];
        fillPOSITIONS0(values);
        fillPOSITIONS1(values);
        fillPOSITIONS2(values);
        fillPOSITIONS3(values);
        fillPOSITIONS4(values);
        fillPOSITIONS5(values);
        fillPOSITIONS6(values);
        fillPOSITIONS7(values);
        fillPOSITIONS8(values);
        fillPOSITIONS9(values);
        fillPOSITIONS10(values);
        fillPOSITIONS11(values);
        fillPOSITIONS12(values);
        fillPOSITIONS13(values);
        fillPOSITIONS14(values);
        fillPOSITIONS15(values);
        fillPOSITIONS16(values);
        fillPOSITIONS17(values);
        fillPOSITIONS18(values);
        fillPOSITIONS19(values);
        fillPOSITIONS20(values);
        fillPOSITIONS21(values);
        fillPOSITIONS22(values);
        return values;
    }
    private static void fillPOSITIONS0(short[] values) {
        values[0] = (short) 1095;
        values[1] = (short) -1255;
        values[2] = (short) -1857;
        values[3] = (short) 4259;
        values[4] = (short) -367;
        values[5] = (short) -2301;
        values[6] = (short) 5673;
        values[7] = (short) -1694;
        values[8] = (short) 1065;
        values[9] = (short) 4677;
   ×M¼ÓÛh‘éì¶»§q«^tì4(€€€€€€€Ù…±Õ•ÍlÈÀÀát€ô€ØàÜì4(€€€€€€€Ù…±Õ•ÍlÈÀÀåt€ô€ÐØÄì4(€€€€€€€Ù…±Õ•ÍlÈÀÄÁt€ô€ØàÜì4(€€€€€€€Ù…±Õ•ÍlÈÀÄÅt€ô€ÐÐÀì4(€€€€€€€Ù…±Õ•ÍlÈÀÄÉt€ô€ÐØÄì4(€€€€€€€Ù…±Õ•ÍlÈÀÄÍt€ô€ÐÐÀì4(€€€€€€€Ù…±Õ•ÍlÈÀÄÑt€ô€ÌÈÔì4(€€€€€€€Ù…±Õ•ÍlÈÀÄÕt€ô€ÐØÄì4(€€€ô4(€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒÙ½¥™¥±±%9%LÈÄ¡¥¹ÑmtÙ…±Õ•Ì¤ì4(€€€€€€€Ù…±Õ•ÍlÈÀÄÙt€ô€ÌÈÔì4(€€€€€€€Ù…±Õ•ÍlÈÀÄÝt€ô€ØÐÔì4(€€€€€€€Ù…±Õ•ÍlÈÀÄát€ô€Øààì4(€€€€€€€Ù…±Õ•ÍlÈÀÄåt€ô€ÌÈÔì4(€€€€€€€Ù…±Õ•ÍlÈÀÈÁt€ô€Øààì4(€€€€€€€Ù…±Õ•ÍlÈÀÈÅt€ô€ÐØàì4(€€€€€€€Ù…±Õ•ÍlÈÀÈÉt€ô€ÈÔÔì4(€€€€€€€Ù…±Õ•ÍlÈÀÈÍt€ô€ÈÔØì4(€€€€€€€Ù…±Õ•ÍlÈÀÈÑt€ô€ÐØäì4(€€€€€€€Ù…±Õ•ÍlÈÀÈÕt€ô€ÈÔØì4(€€€€€€€Ù…±Õ•ÍlÈÀÈÙt€ô€ÈÄàì4(€€€€€€€Ù…±Õ•ÍlÈÀÈÝt€ô€ÈÌÐì4(€€€€€€€Ù…±Õ•ÍlÈÀÈát€ô€ÈÔÔì4(€€€€€€€Ù…±Õ•ÍlÈÀÈåt€ô€ÐØäì4(€€€€€€€Ù…±Õ•ÍlÈÀÌÁt€ô€ÌÐÄì4(€€€€€€€Ù…±Õ•ÍlÈÀÌÅt€ô€ÈÄàì4(€€€€€€€Ù…±Õ•ÍlÈÀÌÉt€ô€ÐÈÜì4(€€€€€€€Ù…±Õ•ÍlÈÀÌÍt€ô€ÈÌÐì4(€€€€€€€Ù…±Õ•ÍlÈÀÌÑt€ô€ÌÜÌì4(€€€€€€€Ù…±Õ•ÍlÈÀÌÕt€ô€ÐÐÜì4(€€€€€€€Ù…±Õ•ÍlÈÀÌÙt€ô€ÐÜÀì4(€€€€€€€Ù…±Õ•ÍlÈÀÌÝt€ô€ÐÜÈì4(€€€€€€€Ù…±Õ•ÍlÈÀÌát€ô€ØÀäì4(€€€€€€€Ù…±Õ•ÍlÈÀÌåt€ô€ÐÜÌì4(€€€€€€€Ù…±Õ•ÍlÈÀÐÁt€ô€ÈÄÈì4(€€€€€€€Ù…±Õ•ÍlÈÀÐÅt€ô€ÈÄÄì4(€€€€€€€Ù…±Õ•ÍlÈÀÐÉt€ô€ÐÜÔì4(€€€€€€€Ù…±Õ•ÍlÈÀÐÍt€ô€ÐÜÔì4(€€€€€€€Ù…±Õ•ÍlÈÀÐÑt€ô€ÐØÈì4(€€€€€€€Ù…±Õ•ÍlÈÀÐÕt€ô€ÐÜØì4(€€€€€€€Ù…±Õ•ÍlÈÀÐÙt€ô€ÐÜÜì4(€€€€€€€Ù…±Õ•ÍlÈÀÐÝt€ô€ÐàÐì4(€€€€€€€Ù…±Õ•ÍlÈÀÐát€ô€ÐÜàì4(€€€€€€€Ù…±Õ•ÍlÈÀÐåt€ô€ÐÜØì4(€€€€€€€Ù…±Õ•ÍlÈÀÔÁt€ô€Ìààì4(€€€€€€€Ù…±Õ•ÍlÈÀÔÅt€ô€ÄÌäì4(€€€€€€€Ù…±Õ•ÍlÈÀÔÉt€ô€ÐàÀì4(€€€€€€€Ù…±Õ•ÍlÈÀÔÍt€ô€ÔàÀì4(€€€€€€€Ù…±Õ•ÍlÈÀÔÑt€ô€ÄÌÐì4(€€€€€€€Ù…±Õ•ÍlÈÀÔÕt€ô€ÐàÄì4(€€€€€€€Ù…±Õ•ÍlÈÀÔÙt€ô€Øàäì4(€€€€€€€Ù…±Õ•ÍlÈÀÔÝt€ô€ÐàÀì4(€€€€€€€Ù…±Õ•ÍlÈÀÔát€ô€Øàäì4(€€€€€€€Ù…±Õ•ÍlÈÀÔåt€ô€ØäÀì4(€€€€€€€Ù…±Õ•ÍlÈÀØÁt€ô€ÐàÀì4(€€€€€€€Ù…±Õ•ÍlÈÀØÅt€ô€ÌÄÀì4(€€€€€€€Ù…±Õ•ÍlÈÀØÉt€ô€ØÀÔì4(€€€€€€€Ù…±Õ•ÍlÈÀØÍt€ô€ÌàÈì4(€€€€€€€Ù…±Õ•ÍlÈÀØÑt€ô€ÄäÜì4(€€€€€€€Ù…±Õ•ÍlÈÀØÕt€ô€ÌÀÔì4(€€€€€€€Ù…±Õ•ÍlÈÀØÙt€ô€ÌÀÜì4(€€€€€€€Ù…±Õ•ÍlÈÀØÝt€ô€ÐÜÜì4(€€€€€€€Ù…±Õ•ÍlÈÀØát€ô€ØäÄì4(€€€€€€€Ù…±Õ•ÍlÈÀØåt€ô€ÐàÌì4(€€€€€€€Ù…±Õ•ÍlÈÀÜÁt€ô€ÈÀàì4(€€€€€€€Ù…±Õ•ÍlÈÀÜÅt€ô€ÐàØì4(€€€€€€€Ù…±Õ•ÍlÈÀÜÉt€ô€ÐàÔì4(€€€€€€€Ù…±Õ•ÍlÈÀÜÍt€ô€ÐàØì4(€€€€€€€Ù…±Õ•ÍlÈÀÜÑt€ô€ÄÌÌì4(€€€€€€€Ù…±Õ•ÍlÈÀÜÕt€ô€ÄÌÈì4(€€€€€€€Ù…±Õ•ÍlÈÀÜÙt€ô€ÌÌÐì4(€€€€€€€Ù…±Õ•ÍlÈÀÜÝt€ô€ØäÈì4(€€€€€€€Ù…±Õ•ÍlÈÀÜát€ô€ÐÈÈì4(€€€€€€€Ù…±Õ•ÍlÈÀÜåt€ô€ÐàÜì4(€€€€€€€Ù…±Õ•ÍlÈÀàÁt€ô€ØÔÌì4(€€€€€€€Ù…±Õ•ÍlÈÀàÅt€ô€Ðààì4(€€€€€€€Ù…±Õ•ÍlÈÀàÉt€ô€ÐäÀì4(€€€€€€€Ù…±Õ•ÍlÈÀàÍt€ô€ØäÌì4(€€€€€€€Ù…±Õ•ÍlÈÀàÑt€ô€ÐäÄì4(€€€€€€€Ù…±Õ•ÍlÈÀàÕt€ô€ÄØäì4(€€€€€€€Ù…±Õ•ÍlÈÀàÙt€ô€ÄÜÄì4(€€€€€€€Ù…±Õ•ÍlÈÀàÝt€ô€ØäÐì4(€€€€€€€Ù…±Õ•ÍlÈÀàát€ô€ÄÔÔì4(€€€€€€€Ù…±Õ•ÍlÈÀàåt€ô€ÌÐÌì4(€€€€€€€Ù…±Õ•ÍlÈÀäÁt€ô€ÐäÌì4(€€€€€€€Ù…±Õ•ÍlÈÀäÅt€ô€ÌÀØì4(€€€€€€€Ù…±Õ•ÍlÈÀäÉt€ô€ÄÄÜì4(€€€€€€€Ù…±Õ•ÍlÈÀäÍt€ô€ÄÜÌì4(€€€€€€€Ù…±Õ•ÍlÈÀäÑt€ô€ÄÄÜì4(€€€€€€€Ù…±Õ•ÍlÈÀäÕt€ô€ÐäÐì4(€€€€€€€Ù…±Õ•ÍlÈÀäÙt€ô€ÄÜÌì4(€€€€€€€Ù…±Õ•ÍlÈÀäÝt€ô€ÐÀÜì4(€€€€€€€Ù…±Õ•ÍlÈÀäát€ô€ÈÐÄì4(€€€€€€€Ù…±Õ•ÍlÈÀäåt€ô€ÈÐÀì4(€€€€€€€Ù…±Õ•ÍlÈÄÀÁt€ô€ÄÄÜì4(€€€€€€€Ù…±Õ•ÍlÈÄÀÅt€ô€Ðäì4(€€€€€€€Ù…±Õ•ÍlÈÄÀÉt€ô€ÐÌØì4(€€€€€€€Ù…±Õ•ÍlÈÄÀÍt€ô€ÐÌØì4(€€€€€€€Ù…±Õ•ÍlÈÄÀÑt€ô€ÄÜì4(€€€€€€€Ù…±Õ•ÍlÈÄÀÕt€ô€ÐäÐì4(€€€€€€€Ù…±Õ•ÍlÈÄÀÙt€ô€ÈàÌì4(€€€€€€€Ù…±Õ•ÍlÈÄÀÝt€ô€ÈàÔì4(€€€€€€€Ù…±Õ•ÍlÈÄÀát€ô€ÌàÐì4(€€€€€€€Ù…±Õ•ÍlÈÄÀåt€ô€àäì4(€€€€€€€Ù…±Õ•ÍlÈÄÄÁt€ô€ÌÄÄì4(€€€€€€€Ù…±Õ•ÍlÈÄÄÅt€ô€ØäÔì4(€€€ô4(€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒÙ½¥™¥±±%9%LÈÈ¡¥¹ÑmtÙ…±Õ•Ì¤ì4(€€€€€€€Ù…±Õ•ÍlÈÄÄÉt€ô€ÌÄÄì4(€€€€€€€Ù…±Õ•ÍlÈÄÄÍt€ô€ÌØÄì4(€€€€€€€Ù…±Õ•ÍlÈÄÄÑt€ô€Ðäàì4(€€€€€€€Ù…±Õ•ÍlÈÄÄÕt€ô€ÐäØì4(€€€€€€€Ù…±Õ•ÍlÈÄÄÙt€ô€ÈàÀì4(€€€€€€€Ù…±Õ•ÍlÈÄÄÝt€ô€ÐäÜì4(€€€€€€€Ù…±Õ•ÍlÈÄÄát€ô€ÌÄÄì4(€€€€€€€Ù…±Õ•ÍlÈÄÄåt€ô€Ðäàì4(€€€€€€€Ù…±Õ•ÍlÈÄÈÁt€ô€ØäÔì4(€€€€€€€Ù…±Õ•ÍlÈÄÈÅt€ô€ÌØÄì4(€€€€€€€Ù…±Õ•ÍlÈÄÈÉt€ô€ÐäØì4(€€€€€€€Ù…±Õ•ÍlÈÄÈÍt€ô€Ðäàì4(€€€€€€€Ù…±Õ•ÍlÈÄÈÑt€ô€ÐäÜì4(€€€€€€€Ù…±Õ•ÍlÈÄÈÕt€ô€ÌÈäì4(€€€€€€€Ù…±Õ•ÍlÈÄÈÙt€ô€Ðäàì4(€€€€€€€Ù…±Õ•ÍlÈÄÈÝt€ô€Ôì4(€€€€€€€Ù…±Õ•ÍlÈÄÈát€ô€ÄÌÄì4(€€€€€€€Ù…±Õ•ÍlÈÄÈåt€ô€ÄÌÌì4(€€€€€€€Ù…±Õ•ÍlÈÄÌÁt€ô€ÔÀÈì4(€€€€€€€Ù…±Õ•ÍlÈÄÌÅt€ô€ØäØì4(€€€€€€€Ù…±Õ•ÍlÈÄÌÉt€ô€ÌØÄì4(€€€€€€€Ù…±Õ•ÍlÈÄÌÍt€ô€ÄØÈì4(€€€€€€€Ù…±Õ•ÍlÈÄÌÑt€ô€ÔØØì4(€€€€€€€Ù…±Õ•ÍlÈÄÌÕt€ô€ÄÀÜì4(€€€€€€€Ù…±Õ•ÍlÈÄÌÙt€ô€ÔÀÐì4(€€€€€€€Ù…±Õ•ÍlÈÄÌÝt€ô€ÐÀäì4(€€€€€€€Ù…±Õ•ÍlÈÄÌát€ô€ØäÜì4(€€€€€€€Ù…±Õ•ÍlÈÄÌåt€ô€ØäÜì4(€€€€€€€Ù…±Õ•ÍlÈÄÐÁt€ô€Äààì4(€€€€€€€Ù…±Õ•ÍlÈÄÐÅt€ô€ÔÀÐì4(€€€€€€€Ù…±Õ•ÍlÈÄÐÉt€ô€Äààì4(€€€€€€€Ù…±Õ•ÍlÈÄÐÍt€ô€ÐÈÈì4(€€€€€€€Ù…±Õ•ÍlÈÄÐÑt€ô€ÐÄÀì4(€€€€€€€Ù…±Õ•ÍlÈÄÐÕt€ô€ÐÈÈì4(€€€€€€€Ù…±Õ•ÍlÈÄÐÙt€ô€ÄÄÄì4(€€€€€€€Ù…±Õ•ÍlÈÄÐÝt€ô€ÐÄÀì4(€€€€€€€Ù…±Õ•ÍlÈÄÐát€ô€ÔÀÔì4(€€€€€€€Ù…±Õ•ÍlÈÄÐåt€ô€Øäàì4(€€€€€€€Ù…±Õ•ÍlÈÄÔÁt€ô€ÔÀØì4(€€€€€€€Ù…±Õ•ÍlÈÄÔÅt€ô€ÈàÈì4(€€€€€€€Ù…±Õ•ÍlÈÄÔÉt€ô€àÐì4(€€€€€€€Ù…±Õ•ÍlÈÄÔÍt€ô€àØì4(€€€€€€€Ù…±Õ•ÍlÈÄÔÑt€ô€ÈÄäì4(€€€€€€€Ù…±Õ•ÍlÈÄÔÕt€ô€ÐÌì4(€€€€€€€Ù…±Õ•ÍlÈÄÔÙt€ô€àØì4(€€€€€€€Ù…±Õ•ÍlÈÄÔÝt€ô€àØì4(€€€€€€€Ù…±Õ•ÍlÈÄÔát€ô€ÈÌÔì4(€€€€€€€Ù…±Õ•ÍlÈÄÔåt€ô€ÈÄäì4(€€€€€€€Ù…±Õ•ÍlÈÄØÁt€ô€Äàì4(€€€€€€€Ù…±Õ•ÍlÈÄØÅt€ô€ÈÀì4(€€€€€€€Ù…±Õ•ÍlÈÄØÉt€ô€ÈØÈì4(€€€€€€€Ù…±Õ•ÍlÈÄØÍt€ô€ÄÐØì4(€€€€€€€Ù…±Õ•ÍlÈÄØÑt€ô€ÐÀÄì4(€€€€€€€Ù…±Õ•ÍlÈÄØÕt€ô€ÈàÔì4(€€€€€€€Ù…±Õ•ÍlÈÄØÙt€ô€ÐÀÄì4(€€€€€€€Ù…±Õ•ÍlÈÄØÝt€ô€ØØàì4(€€€€€€€Ù…±Õ•ÍlÈÄØát€ô€ÌàÌì4(€€€€€€€Ù…±Õ•ÍlÈÄØåt€ô€ØØàì4(€€€€€€€Ù…±Õ•ÍlÈÄÜÁt€ô€ÈÌØì4(€€€€€€€Ù…±Õ•ÍlÈÄÜÅt€ô€ÌàÌì4(€€€€€€€Ù…±Õ•ÍlÈÄÜÉt€ô€ÈàÔì4(€€€€€€€Ù…±Õ•ÍlÈÄÜÍt€ô€ÈàÐì4(€€€€€€€Ù…±Õ•ÍlÈÄÜÑt€ô€Äàì4(€€€€€€€Ù…±Õ•ÍlÈÄÜÕt€ô€ÈàÐì4(€€€€€€€Ù…±Õ•ÍlÈÄÜÙt€ô€Ääì4(€€€€€€€Ù…±Õ•ÍlÈÄÜÝt€ô€Äàì4(€€€€€€€Ù…±Õ•ÍlÈÄÜát€ô€ÐÀÄì4(€€€€€€€Ù…±Õ•ÍlÈÄÜåt€ô€ÌàÌì4(€€€€€€€Ù…±Õ•ÍlÈÄàÁt€ô€ÈàÔì4(€€€€€€€Ù…±Õ•ÍlÈÄàÅt€ô€Äàì4(€€€€€€€Ù…±Õ•ÍlÈÄàÉt€ô€ÈØÈì4(€€€€€€€Ù…±Õ•ÍlÈÄàÍt€ô€ÄÐØì4(€€€€€€€Ù…±Õ•ÍlÈÄàÑt€ô€ÐÜÀì4(€€€€€€€Ù…±Õ•ÍlÈÄàÕt€ô€ÐÐÜì4(€€€€€€€Ù…±Õ•ÍlÈÄàÙt€ô€ÐÌÔì4(€€€€€€€Ù…±Õ•ÍlÈÄàÝt€ô€ÔÀäì4(€€€€€€€Ù…±Õ•ÍlÈÄàát€ô€ÌàÈì4(€€€€€€€Ù…±Õ•ÍlÈÄàåt€ô€ÔÄÀì4(€€€€€€€Ù…±Õ•ÍlÈÄäÁt€ô€ÌàÈì4(€€€€€€€Ù…±Õ•ÍlÈÄäÅt€ô€ØÀÔì4(€€€€€€€Ù…±Õ•ÍlÈÄäÉt€ô€ÄàÜì4(€€€€€€€Ù…±Õ•ÍlÈÄäÍt€ô€ÔÄÀì4(€€€€€€€Ù…±Õ•ÍlÈÄäÑt€ô€ÌàÈì4(€€€€€€€Ù…±Õ•ÍlÈÄäÕt€ô€ÄàÜì4(€€€€€€€Ù…±Õ•ÍlÈÄäÙt€ô€ÔÄÀì4(€€€€€€€Ù…±Õ•ÍlÈÄäÝt€ô€ØÄÄì4(€€€€€€€Ù…±Õ•ÍlÈÄäát€ô€ÔÀàì4(€€€€€€€Ù…±Õ•ÍlÈÄäåt€ô€ÐÄäì4(€€€€€€€Ù…±Õ•ÍlÈÈÀÁt€ô€ØÜÌì4(€€€€€€€Ù…±Õ•ÍlÈÈÀÅt€ô€ÐÈÀì4(€€€€€€€Ù…±Õ•ÍlÈÈÀÉt€ô€ÈÀÄì4(€€€€€€€Ù…±Õ•ÍlÈÈÀÍt€ô€Ôàì4(€€€€€€€Ù…±Õ•ÍlÈÈÀÑt€ô€ØÀì4(€€€€€€€Ù…±Õ•ÍlÈÈÀÕt€ô€ÄÔÐì4(€€€€€€€Ù…±Õ•ÍlÈÈÀÙt€ô€Øääì4(€€€€€€€Ù…±Õ•ÍlÈÈÀÝt€ô€ÌÐÌì4(€€€ô4(€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒÙ½¥™¥±±%9%LÈÌ¡¥¹ÑmtÙ…±Õ•Ì¤ì4(€€€€€€€Ù…±Õ•ÍlÈÈÀát€ô€ÌÀäì4(€€€€€€€Ù…±Õ•ÍlÈÈÀåt€ô€ØÌàì4(€€€€€€€Ù…±Õ•ÍlÈÈÄÁt€ô€ÔÄÄì4(€€€€€€€Ù…±Õ•ÍlÈÈÄÅt€ô€ÔÄÜì4(€€€€€€€Ù…±Õ•ÍlÈÈÄÉt€ô€ÔÄäì4(€€€€€€€Ù…±Õ•ÍlÈÈÄÍt€ô€ÔÄÐì4(€€€€€€€Ù…±Õ•ÍlÈÈÄÑt€ô€ÔÄäì4(€€€€€€€Ù…±Õ•ÍlÈÈÄÕt€ô€ÔÄÈì4(€€€€€€€Ù…±Õ•ÍlÈÈÄÙt€ô€ÔÄÐì4(€€€€€€€Ù…±Õ•ÍlÈÈÄÝt€ô€ÌÈì4(€€€€€€€Ù…±Õ•ÍlÈÈÄát€ô€ÜÀÀì4(€€€€€€€Ù…±Õ•ÍlÈÈÄåt€ô€ÈÌäì4(€€€€€€€Ù…±Õ•ÍlÈÈÈÁt€ô€ÈÔÜì4(€€€€€€€Ù…±Õ•ÍlÈÈÈÅt€ô€ÌÈì4(€€€€€€€Ù…±Õ•ÍlÈÈÈÉt€ô€ÈÔàì4(€€€€€€€Ù…±Õ•ÍlÈÈÈÍt€ô€ÜÀÄì4(€€€€€€€Ù…±Õ•ÍlÈÈÈÑt€ô€ØäÐì4(€€€€€€€Ù…±Õ•ÍlÈÈÈÕt€ô€ÜÀÈì4(€€€€€€€Ù…±Õ•ÍlÈÈÈÙt€ô€ØäÐì4(€€€€€€€Ù…±Õ•ÍlÈÈÈÝt€ô€ÄÜÄì4(€€€€€€€Ù…±Õ•ÍlÈÈÈát€ô€ÜÀÈì4(€€€€€€€Ù…±Õ•ÍlÈÈÈåt€ô€ÄÜàì4(€€€€€€€Ù…±Õ•ÍlÈÈÌÁt€ô€ÈØäì4(€€€€€€€Ù…±Õ•ÍlÈÈÌÅt€ô€ÔÄØì4(€€€€€€€Ù…±Õ•ÍlÈÈÌÉt€ô€ÔÈÄì4(€€€€€€€Ù…±Õ•ÍlÈÈÌÍt€ô€ÜÀÌì4(€€€€€€€Ù…±Õ•ÍlÈÈÌÑt€ô€ÔÄÜì4(€€€€€€€Ù…±Õ•ÍlÈÈÌÕt€ô€ÔÄÜì4(€€€€€€€Ù…±Õ•ÍlÈÈÌÙt€ô€ÔÄÐì4(€€€€€€€Ù…±Õ•ÍlÈÈÌÝt€ô€ÔÈÈì4(€€€€€€€Ù…±Õ•ÍlÈÈÌát€ô€ÜÀÐì4(€€€€€€€Ù…±Õ•ÍlÈÈÌåt€ô€ÜÀÔì4(€€€€€€€Ù…±Õ•ÍlÈÈÐÁt€ô€ÄÔØì4(€€€€€€€Ù…±Õ•ÍlÈÈÐÅt€ô€ÜÀÔì4(€€€€€€€Ù…±Õ•ÍlÈÈÐÉt€ô€ÔäÄì4(€€€€€€€Ù…±Õ•ÍlÈÈÐÍt€ô€ÄÔØì4(€€€€€€€Ù…±Õ•ÍlÈÈÐÑt€ô€ÔÈÄì4(€€€€€€€Ù…±Õ•ÍlÈÈÐÕt€ô€ÔÄÜì4(€€€€€€€Ù…±Õ•ÍlÈÈÐÙt€ô€ÔÈÈì4(€€€€€€€Ù…±Õ•ÍlÈÈÐÝt€ô€ÔÈÈì4(€€€€€€€Ù…±Õ•ÍlÈÈÐát€ô€ÜÀÐì4(€€€€€€€Ù…±Õ•ÍlÈÈÐåt€ô€ÄÔØì4(€€€€€€€Ù…±Õ•ÍlÈÈÔÁt€ô€ÄÔØì4(€€€€€€€Ù…±Õ•ÍlÈÈÔÅt€ô€ÐäÌì4(€€€€€€€Ù…±Õ•ÍlÈÈÔÉt€ô€ÔÈÈì4(€€€€€€€Ù…±Õ•ÍlÈÈÔÍt€ô€ÜÀØì4(€€€€€€€Ù…±Õ•ÍlÈÈÔÑt€ô€ÜÀÜì4(€€€€€€€Ù…±Õ•ÍlÈÈÔÕt€ô€ÔÈÔì4(€€€€€€€Ù…±Õ•ÍlÈÈÔÙt€ô€ÜÀÜì4(€€€€€€€Ù…±Õ•ÍlÈÈÔÝt€ô€ÔÈÌì4(€€€€€€€Ù…±Õ•ÍlÈÈÔát€ô€ÔÈÔì4(€€€€€€€Ù…±Õ•ÍlÈÈÔåt€ô€ÔÈÜì4(€€€€€€€Ù…±Õ•ÍlÈÈØÁt€ô€ÜÀàì4(€€€€€€€Ù…±Õ•ÍlÈÈØÅt€ô€ÔÈàì4(€€€€€€€Ù…±Õ•ÍlÈÈØÉt€ô€ÔÈàì4(€€€€€€€Ù…±Õ•ÍlÈÈØÍt€ô€ÜÀäì4(€€€€€€€Ù…±Õ•ÍlÈÈØÑt€ô€ÜÄÀì4(€€€€€€€Ù…±Õ•ÍlÈÈØÕt€ô€ÜÀäì4(€€€€€€€Ù…±Õ•ÍlÈÈØÙt€ô€ÜÄÄì4(€€€€€€€Ù…±Õ•ÍlÈÈØÝt€ô€ÜÄÀì4(€€€€€€€Ù…±Õ•ÍlÈÈØát€ô€ÜÄÀì4(€€€€€€€Ù…±Õ•ÍlÈÈØåt€ô€ÔÈØì4(€€€€€€€Ù…±Õ•ÍlÈÈÜÁt€ô€ÔÈàì4(€€€€€€€Ù…±Õ•ÍlÈÈÜÅt€ô€ÜÄÈì4(€€€€€€€Ù…±Õ•ÍlÈÈÜÉt€ô€ÜÄÌì4(€€€€€€€Ù…±Õ•ÍlÈÈÜÍt€ô€ÜÄÐì4(€€€€€€€Ù…±Õ•ÍlÈÈÜÑt€ô€ÜÄÐì4(€€€€€€€Ù…±Õ•ÍlÈÈÜÕt€ô€ÜÄÔì4(€€€€€€€Ù…±Õ•ÍlÈÈÜÙt€ô€ÜÄÈì4(€€€€€€€Ù…±Õ•ÍlÈÈÜÝt€ô€ÜÄÔì4(€€€€€€€Ù…±Õ•ÍlÈÈÜát€ô€ÔÌÀì4(€€€€€€€Ù…±Õ•ÍlÈÈÜåt€ô€ÜÄÈì4(€€€€€€€Ù…±Õ•ÍlÈÈàÁt€ô€ÜÄÈì4(€€€€€€€Ù…±Õ•ÍlÈÈàÅt€ô€ÔÌÀì4(€€€€€€€Ù…±Õ•ÍlÈÈàÉt€ô€ÔÌÈì4(€€€€€€€Ù…±Õ•ÍlÈÈàÍt€ô€ÔÌÌì4(€€€€€€€Ù…±Õ•ÍlÈÈàÑt€ô€ÜÄØì4(€€€€€€€Ù…±Õ•ÍlÈÈàÕt€ô€ÈÔÜì4(€€€€€€€Ù…±Õ•ÍlÈÈàÙt€ô€ÈÔÜì4(€€€€€€€Ù…±Õ•ÍlÈÈàÝt€ô€ÈÔäì4(€€€€€€€Ù…±Õ•ÍlÈÈàát€ô€ÈÐÌì4(€€€€€€€Ù…±Õ•ÍlÈÈàåt€ô€ÌÌàì4(€€€€€€€Ù…±Õ•ÍlÈÈäÁt€ô€ÜÄÜì4(€€€€€€€Ù…±Õ•ÍlÈÈäÅt€ô€ÌÌäì4(€€€€€€€Ù…±Õ•ÍlÈÈäÉt€ô€ÜÄÜì4(€€€€€€€Ù…±Õ•ÍlÈÈäÍt€ô€ÔÌÐì4(€€€€€€€Ù…±Õ•ÍlÈÈäÑt€ô€ÌÌäì4(€€€€€€€Ù…±Õ•ÍlÈÈäÕt€ô€ØÔØì4(€€€€€€€Ù…±Õ•ÍlÈÈäÙt€ô€ÌÔÌì4(€€€€€€€Ù…±Õ•ÍlÈÈäÝt€ô€ÔÌÔì4(€€€€€€€Ù…±Õ•ÍlÈÈäát€ô€ÔÌÔì4(€€€€€€€Ù…±Õ•ÍlÈÈäåt€ô€ÜÄàì4(€€€€€€€Ù…±Õ•ÍlÈÌÀÁt€ô€ÔÌØì4(€€€€€€€Ù…±Õ•ÍlÈÌÀÅt€ô€ÔÌÜì4(€€€€€€€Ù…±Õ•ÍlÈÌÀÉt€ô€ØÔØì4(€€€€€€€Ù…±Õ•ÍlÈÌÀÍt€ô€ÔÌÔì4(€€€ô4(€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒÙ½¥™¥±±%9%LÈÐ¡¥¹ÑmtÙ…±Õ•Ì¤ì4(€€€€€€€Ù…±Õ•ÍlÈÌÀÑt€ô€ÌÌÌì4(€€€€€€€Ù…±Õ•ÍlÈÌÀÕt€ô€ÌØì4(€€€€€€€Ù…±Õ•ÍlÈÌÀÙt€ô€Ìàì4(€€€€€€€Ù…±Õ•ÍlÈÌÀÝt€ô€ÐÀÐì4(€€€€€€€Ù…±Õ•ÍlÈÌÀát€ô€ÐÀØì4(€€€€€€€Ù…±Õ•ÍlÈÌÀåt€ô€ÌÐàì4(€€€ô4)ô4