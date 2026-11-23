#!/usr/bin/env python3
"""Generate a compact J2ME M3G mesh class from a triangulated Wavefront OBJ."""

from pathlib import Path
import base64
import sys


def read_obj(path):
    positions, texcoords, normals = [], [], []
    vertices, indices, faces, seen = [], [], [], {}
    for line_no, raw in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        fields = line.split()
        kind, values = fields[0], fields[1:]
        if kind == "v":
            positions.append(tuple(map(float, values[:3])))
        elif kind == "vt":
            texcoords.append(tuple(map(float, values[:2])))
        elif kind == "vn":
            normals.append(tuple(map(float, values[:3])))
        elif kind == "f":
            if len(values) != 3:
                raise ValueError("line %d: faces must be triangulated" % line_no)
            face = []
            for value in values:
                parts = value.split("/")
                if len(parts) != 3 or not all(parts):
                    raise ValueError("line %d: expected v/vt/vn indices" % line_no)
                key = tuple(int(part) - 1 for part in parts)
                face.append(key[0])
                if key not in seen:
                    seen[key] = len(vertices)
                    vertices.append(key)
                indices.append(seen[key])
            faces.append(face)
    if not vertices:
        raise ValueError("OBJ contains no faces")
    if len(vertices) > 65535:
        raise ValueError("M3G VertexArray limit exceeded")
    return positions, texcoords, normals, vertices, indices, faces


def arm_side_by_source_vertex(positions, faces):
    """Classify disconnected arm shells by their OBJ connectivity and center."""
    neighbors = [set() for _ in positions]
    for face in faces:
        for i, vertex in enumerate(face):
            other = face[(i + 1) % len(face)]
            neighbors[vertex].add(other)
            neighbors[other].add(vertex)

    arm_sides = [0] * len(positions)
    visited = set()
    for start in range(len(positions)):
        if start in visited:
            continue
        stack = [start]
        visited.add(start)
        component = []
        while stack:
            vertex = stack.pop()
            component.append(vertex)
            for other in neighbors[vertex]:
                if other not in visited:
                    visited.add(other)
                    stack.append(other)

        center_x = sum(positions[i][0] for i in component) / float(len(component))
        center_y = sum(positions[i][1] for i in component) / float(len(component))
        # In this OBJ, each arm is a separate shell outside the torso.
        if abs(center_x) > 0.24 and -0.18 < center_y < 0.50:
            side = -1 if center_x < 0.0 else 1
            for vertex in component:
                arm_sides[vertex] = side
    return arm_sides


def append_packed_data(source, name, values, kind, chunk_size=800):
    """Store compact packed arrays as short Base64 strings."""
    raw = bytearray()
    if kind == "byte":
        raw.extend(value & 0xff for value in values)
    else:
        signed = kind == "short"
        for value in values:
            raw.extend(int(value).to_bytes(2, "big", signed=signed))
    encoded = base64.b64encode(raw).decode("ascii")
    data_name = name + "_DATA"
    chunks = [encoded[i:i + chunk_size] for i in range(0, len(encoded), chunk_size)]
    source.append("    private static String[] " + data_name + " = new String[] {")
    for i, chunk in enumerate(chunks):
        source.append("        \"" + chunk + "\"" + ("," if i + 1 < len(chunks) else ""))
    source.append("    };")
    return data_name


def generate(obj_path, java_path):
    positions, texcoords, normals, vertices, indices, faces = read_obj(obj_path)
    source_arm_sides = arm_side_by_source_vertex(positions, faces)
    arm_sides = [source_arm_sides[vi] for vi, ti, ni in vertices]
    xyz, uv, nrm = [], [], []
    for vi, ti, ni in vertices:
        x, y, z = positions[vi]
        u, v = texcoords[ti]
        nx, ny, nz = normals[ni]
        xyz.extend((x, y, z))
        # OBJ/Blender uses a bottom-left texture origin; M3G uses top-left.
        uv.extend((u, 1.0 - v))
        nrm.extend((nx, ny, nz))

    mins = [min(xyz[i::3]) for i in range(3)]
    maxs = [max(xyz[i::3]) for i in range(3)]
    bias = [(mins[i] + maxs[i]) / 2.0 for i in range(3)]
    # Leave headroom in the short coordinates for CPU-skinned limb motion.
    scale = max(maxs[i] - mins[i] for i in range(3)) / 50000.0
    pshort = [max(-32767, min(32767, round((xyz[i] - bias[i % 3]) / scale))) for i in range(len(xyz))]
    uv_scale = max(abs(value) for value in uv) / 32767.0
    uvshort = [round(value / uv_scale) for value in uv]
    nshort = [max(-32767, min(32767, round(value * 32767.0))) for value in nrm]

    source = [
        "package gta3me;", "", "import javax.microedition.lcdui.Image;",
        "import javax.microedition.m3g.Appearance;", "import javax.microedition.m3g.Image2D;",
        "import javax.microedition.m3g.Material;", "import javax.microedition.m3g.Mesh;",
        "import javax.microedition.m3g.Texture2D;", "import javax.microedition.m3g.TriangleStripArray;",
        "import javax.microedition.m3g.VertexArray;", "import javax.microedition.m3g.VertexBuffer;", "",
        "/** Generated from the Claude OBJ. Do not edit the mesh arrays by hand. */",
        "public final class ClaudeModel {",
        "    public static final int IDLE = 0;", "    public static final int WALK = 1;", "    public static final int JUMP = 2;",
        "    private static final int VERTEX_COUNT = " + str(len(vertices)) + ";",
        "    private static final float POSITION_SCALE = " + ("%.10gf" % scale) + ";",
        "    private static final float BIAS_X = " + ("%.9gf" % bias[0]) + ";",
        "    private static final float BIAS_Y = " + ("%.9gf" % bias[1]) + ";",
        "    private static final float BIAS_Z = " + ("%.9gf" % bias[2]) + ";",
        "    private ClaudeModel() {}", "",
        "    public static Rig create(Image diffuseTexture) {",
        "        VertexArray positions = new VertexArray(" + str(len(vertices)) + ", 3, 2);",
        "        positions.set(0, " + str(len(vertices)) + ", POSITIONS);",
        "        VertexArray texCoords = new VertexArray(" + str(len(vertices)) + ", 2, 2);",
        "        texCoords.set(0, " + str(len(vertices)) + ", TEXCOORDS);",
        "        VertexArray normals = new VertexArray(" + str(len(vertices)) + ", 3, 2);",
        "        normals.set(0, " + str(len(vertices)) + ", NORMALS);",
        "        VertexBuffer vertices = new VertexBuffer();",
        "        vertices.setPositions(positions, POSITION_SCALE, new float[] {BIAS_X, BIAS_Y, BIAS_Z});",
        "        vertices.setTexCoords(0, texCoords, " + ("%.10gf" % uv_scale) + ", null);",
        "        vertices.setNormals(normals);", "",
        "        Appearance appearance = new Appearance();",
        "        Material material = new Material();",
        "        material.setColor(Material.EMISSIVE, 0xFFFFFFFF);",
        "        material.setColor(Material.SPECULAR, 0xFFFFFFFF);",
        "        material.setShininess(0.0f);",
        "        appearance.setMaterial(material);",
        "        if (diffuseTexture != null) {",
        "            Image2D image = new Image2D(Image2D.RGBA, diffuseTexture);",
        "            Texture2D texture = new Texture2D(image);",
        "            texture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);",
        "            texture.setWrapping(Texture2D.WRAP_REPEAT, Texture2D.WRAP_REPEAT);",
        "            appearance.setTexture(0, texture);", "        }", "",
        "        int[] stripLengths = new int[" + str(len(indices) // 3) + "];",
        "        for (int i = 0; i < stripLengths.length; i++) {",
        "            stripLengths[i] = 3;", "        }",
        "        Mesh mesh = new Mesh(vertices, new TriangleStripArray(INDICES, stripLengths), appearance);",
        "        return new Rig(mesh, positions);", "    }", "",
        "    public static final class Rig {",
        "        private final Mesh mesh;", "        private final VertexArray positionArray;",
        "        private final short[] deformed = new short[POSITIONS.length];",
        "        private final float[] point = new float[3];", "",
        "        private Rig(Mesh mesh, VertexArray positionArray) {",
        "            this.mesh = mesh;", "            this.positionArray = positionArray;", "        }",
        "        public Mesh getMesh() { return mesh; }", "",
        "        public void animate(int pose, float phase) {",
        "            float cycle = (float) Math.sin(phase);",
        "            float leftLeg = pose == WALK ? cycle * 0.48f : (pose == JUMP ? 0.22f : 0.0f);",
        "            float rightLeg = -leftLeg;",
        "            float leftArm = pose == WALK ? -cycle * 0.34f : (pose == JUMP ? 0.0f : cycle * 0.04f);",
        "            float rightArm = -leftArm;",
        "            float leftKnee = pose == WALK ? Math.max(0.0f, (float) Math.sin(phase + 1.0f)) * 0.42f : (pose == JUMP ? 0.55f : 0.0f);",
        "            float rightKnee = pose == WALK ? Math.max(0.0f, (float) Math.sin(phase + 4.14f)) * 0.42f : (pose == JUMP ? 0.55f : 0.0f);",
            "            float leftLift = pose == JUMP ? -1.3f : 0.0f;",
            "            float rightLift = pose == JUMP ? 1.3f : 0.0f;",
        "            float sl = (float) Math.sin(leftLeg), cl = (float) Math.cos(leftLeg);",
        "            float sr = (float) Math.sin(rightLeg), cr = (float) Math.cos(rightLeg);",
        "            float sal = (float) Math.sin(leftArm), cal = (float) Math.cos(leftArm);",
        "            float sar = (float) Math.sin(rightArm), car = (float) Math.cos(rightArm);",
        "            float skl = (float) Math.sin(leftKnee), ckl = (float) Math.cos(leftKnee);",
        "            float skr = (float) Math.sin(rightKnee), ckr = (float) Math.cos(rightKnee);",
        "            float szl = (float) Math.sin(leftLift), czl = (float) Math.cos(leftLift);",
        "            float szr = (float) Math.sin(rightLift), czr = (float) Math.cos(rightLift);",
        "            for (int i = 0; i < POSITIONS.length; i += 3) {",
        "                float x = POSITIONS[i] * POSITION_SCALE + BIAS_X;",
        "                float y = POSITIONS[i + 1] * POSITION_SCALE + BIAS_Y;",
        "                float z = POSITIONS[i + 2] * POSITION_SCALE + BIAS_Z;",
        "                float absX = Math.abs(x);",
        "                float lower = clamp((-y + 0.04f) / 0.25f);",
        "                float center = clamp((0.29f - absX) / 0.13f);",
        "                float split = clamp((x + 0.12f) / 0.24f);",
        "                float legBase = lower * center;",
        "                float lw = legBase * (1.0f - split);",
        "                float rw = legBase * split;",
        "                float kneeBase = clamp((-y - 0.47f) / 0.20f) * center;",
        "                float lkw = kneeBase * (1.0f - split);",
        "                float rkw = kneeBase * split;",
        # The connectivity mask keeps arm deformation out of the leg shells.
            "                int armSide = ARM_SIDE[i / 3];",
            "                float armWeight = armSide == 0 ? 0.0f : clamp((absX - 0.15f) / 0.14f);",
            "                float law = armSide < 0 ? armWeight : 0.0f;",
            "                float raw = armSide > 0 ? armWeight : 0.0f;",
        "                point[0] = x; point[1] = y; point[2] = z;",
        "                rotateX(-0.12f, -0.06f, 0.0f, sl, cl, lw);",
        "                rotateX(0.12f, -0.06f, 0.0f, sr, cr, rw);",
        "                rotateX(-0.12f, -0.48f, 0.0f, skl, ckl, lkw);",
        "                rotateX(0.12f, -0.48f, 0.0f, skr, ckr, rkw);",
        "                rotateX(-0.19f, 0.42f, 0.04f, sal, cal, law);",
        "                rotateX(0.19f, 0.42f, 0.04f, sar, car, raw);",
        "                rotateZ(-0.19f, 0.42f, 0.04f, szl, czl, law * (pose == JUMP ? 1.0f : 0.0f));",
        "                rotateZ(0.19f, 0.42f, 0.04f, szr, czr, raw * (pose == JUMP ? 1.0f : 0.0f));",
        "                deformed[i] = quantize((point[0] - BIAS_X) / POSITION_SCALE);",
        "                deformed[i + 1] = quantize((point[1] - BIAS_Y) / POSITION_SCALE);",
        "                deformed[i + 2] = quantize((point[2] - BIAS_Z) / POSITION_SCALE);",
        "            }",
        "            positionArray.set(0, VERTEX_COUNT, deformed);", "        }", "",
        "        private void rotateX(float px, float py, float pz, float s, float c, float weight) {",
        "            if (weight <= 0.0f) return;",
        "            float y = point[1], z = point[2];",
        "            float ry = py + (y - py) * c - (z - pz) * s;",
        "            float rz = pz + (y - py) * s + (z - pz) * c;",
        "            point[1] = y + (ry - y) * weight; point[2] = z + (rz - z) * weight;",
        "        }", "",
        "        private void rotateZ(float px, float py, float pz, float s, float c, float weight) {",
        "            if (weight <= 0.0f) return;",
        "            float x = point[0], y = point[1];",
        "            float rx = px + (x - px) * c - (y - py) * s;",
        "            float ry = py + (x - px) * s + (y - py) * c;",
        "            point[0] = x + (rx - x) * weight; point[1] = y + (ry - y) * weight;",
        "        }", "",
        "        private float clamp(float value) {",
        "            if (value < 0.0f) return 0.0f; if (value > 1.0f) return 1.0f; return value;", "        }",
        "        private short quantize(float value) {",
        "            if (value < -32767.0f) value = -32767.0f; if (value > 32767.0f) value = 32767.0f;",
        "            return (short) (value + (value >= 0.0f ? 0.5f : -0.5f));", "        }",
        "    }", ""
    ]

    data_source = [
        "package gta3me;", "", "/** Packed OBJ data. Generated; do not edit by hand. */",
        "final class ClaudeModelData {"
    ]
    position_data = append_packed_data(data_source, "POSITIONS", pshort, "short")
    texcoord_data = append_packed_data(data_source, "TEXCOORDS", uvshort, "short")
    normal_data = append_packed_data(data_source, "NORMALS", nshort, "short")
    arm_data = append_packed_data(data_source, "ARM_SIDE", arm_sides, "byte")
    index_data = append_packed_data(data_source, "INDICES", indices, "ushort")

    data_source.extend((
        '    private static final String BASE64_DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";',
        "    static final short[] POSITIONS = decodeShorts(" + position_data + ", " + str(len(pshort)) + ");",
        "    static final short[] TEXCOORDS = decodeShorts(" + texcoord_data + ", " + str(len(uvshort)) + ");",
        "    static final short[] NORMALS = decodeShorts(" + normal_data + ", " + str(len(nshort)) + ");",
        "    static final byte[] ARM_SIDE = decodeBytes(" + arm_data + ", " + str(len(arm_sides)) + ");",
        "    static final int[] INDICES = decodeUnsignedShorts(" + index_data + ", " + str(len(indices)) + ");",
        "    static { " + position_data + " = null; " + texcoord_data + " = null; " + normal_data + " = null; " + arm_data + " = null; " + index_data + " = null; }",
        "    private static byte[] decodeBase64(String[] chunks, int length) {",
        "        byte[] result = new byte[length]; int out = 0; int buffer = 0; int bits = 0;",
        "        for (int c = 0; c < chunks.length; c++) { String text = chunks[c];",
        "            for (int i = 0; i < text.length(); i++) { int value = BASE64_DIGITS.indexOf(text.charAt(i));",
        "                if (value >= 0) { buffer = (buffer << 6) | value; bits += 6;",
        "                    if (bits >= 8) { bits -= 8; result[out++] = (byte) ((buffer >> bits) & 255); buffer = bits == 0 ? 0 : buffer & ((1 << bits) - 1); }",
        "                }",
        "            }",
        "        } return result;",
        "    }",
        "    private static short[] decodeShorts(String[] chunks, int length) {",
        "        byte[] raw = decodeBase64(chunks, length * 2); short[] result = new short[length];",
        "        for (int i = 0; i < length; i++) result[i] = (short) (((raw[i * 2] & 255) << 8) | (raw[i * 2 + 1] & 255));",
        "        return result;",
        "    }",
        "    private static byte[] decodeBytes(String[] chunks, int length) {",
        "        return decodeBase64(chunks, length);",
        "    }",
        "    private static int[] decodeUnsignedShorts(String[] chunks, int length) {",
        "        byte[] raw = decodeBase64(chunks, length * 2); int[] result = new int[length];",
        "        for (int i = 0; i < length; i++) result[i] = ((raw[i * 2] & 255) << 8) | (raw[i * 2 + 1] & 255);",
        "        return result;",
        "    }",
        "}"
    ))
    java_path.parent.mkdir(parents=True, exist_ok=True)
    source.append("}")
    source_text = "\n".join(source)
    for name in ("POSITIONS", "TEXCOORDS", "NORMALS", "ARM_SIDE", "INDICES"):
        source_text = source_text.replace(name, "ClaudeModelData." + name)
    java_path.write_text(source_text + "\n", encoding="utf-8")
    data_path = java_path.with_name("ClaudeModelData.java")
    data_path.write_text("\n".join(data_source) + "\n", encoding="utf-8")
    print("Generated %s (%d vertices, %d triangles)" % (java_path, len(vertices), len(indices) // 3))
    print("Generated %s" % data_path)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise SystemExit("usage: obj_to_java.py model.obj ClaudeModel.java")
    generate(Path(sys.argv[1]), Path(sys.argv[2]))
