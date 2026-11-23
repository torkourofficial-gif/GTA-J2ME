#!/usr/bin/env python3
"""Generate a compact J2ME M3G mesh class from a triangulated Wavefront OBJ."""

from pathlib import Path
import sys


def read_obj(path):
    positions, texcoords, normals = [], [], []
    vertices, indices, seen = [], [], {}
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
            for value in values:
                parts = value.split("/")
                if len(parts) != 3 or not all(parts):
                    raise ValueError("line %d: expected v/vt/vn indices" % line_no)
                key = tuple(int(part) - 1 for part in parts)
                if key not in seen:
                    seen[key] = len(vertices)
                    vertices.append(key)
                indices.append(seen[key])
    if not vertices:
        raise ValueError("OBJ contains no faces")
    if len(vertices) > 65535:
        raise ValueError("M3G VertexArray limit exceeded")
    return positions, texcoords, normals, vertices, indices


def generate(obj_path, java_path):
    positions, texcoords, normals, vertices, indices = read_obj(obj_path)
    xyz, uv, nrm = [], [], []
    for vi, ti, ni in vertices:
        x, y, z = positions[vi]
        u, v = texcoords[ti]
        nx, ny, nz = normals[ni]
        xyz.extend((x, y, z))
        uv.extend((u, v))
        nrm.extend((nx, ny, nz))

    mins = [min(xyz[i::3]) for i in range(3)]
    maxs = [max(xyz[i::3]) for i in range(3)]
    bias = [(mins[i] + maxs[i]) / 2.0 for i in range(3)]
    scale = max(maxs[i] - mins[i] for i in range(3)) / 65534.0
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
        "public final class ClaudeModel {", "    private ClaudeModel() {}", "",
        "    public static Mesh create(Image diffuseTexture) {",
        "        VertexArray positions = new VertexArray(" + str(len(vertices)) + ", 3, 2);",
        "        positions.set(0, " + str(len(vertices)) + ", POSITIONS);",
        "        VertexArray texCoords = new VertexArray(" + str(len(vertices)) + ", 2, 2);",
        "        texCoords.set(0, " + str(len(vertices)) + ", TEXCOORDS);",
        "        VertexArray normals = new VertexArray(" + str(len(vertices)) + ", 3, 2);",
        "        normals.set(0, " + str(len(vertices)) + ", NORMALS);",
        "        VertexBuffer vertices = new VertexBuffer();",
        "        vertices.setPositions(positions, " + ("%.10g" % scale) + "f, new float[] {" + ", ".join("%.9gf" % x for x in bias) + "});",
        "        vertices.setTexCoords(0, texCoords, " + ("%.10gf" % uv_scale) + ", null);",
        "        vertices.setNormals(normals);", "",
        "        Appearance appearance = new Appearance();",
        "        Material material = new Material();",
        "        material.setColor(Material.AMBIENT, 0xFFFFFFFF);",
        "        material.setColor(Material.DIFFUSE, 0xFFFFFFFF);",
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
        "        return new Mesh(vertices, new TriangleStripArray(INDICES, stripLengths), new Appearance[] { appearance });",
        "    }", ""
    ]

    source.append("    private static final short[] POSITIONS = new short[] {")
    source.extend("        " + ", ".join(map(str, pshort[i:i + 18])) + ("," if i + 18 < len(pshort) else "") for i in range(0, len(pshort), 18))
    source.append("    };")
    source.append("    private static final short[] TEXCOORDS = new short[] {")
    source.extend("        " + ", ".join(map(str, uvshort[i:i + 18])) + ("," if i + 18 < len(uvshort) else "") for i in range(0, len(uvshort), 18))
    source.append("    };")
    source.append("    private static final short[] NORMALS = new short[] {")
    source.extend("        " + ", ".join(map(str, nshort[i:i + 18])) + ("," if i + 18 < len(nshort) else "") for i in range(0, len(nshort), 18))
    source.append("    };")
    source.append("    private static final int[] INDICES = new int[] {")
    source.extend("        " + ", ".join(map(str, indices[i:i + 18])) + ("," if i + 18 < len(indices) else "") for i in range(0, len(indices), 18))
    source.extend(("    };", "}"))
    java_path.parent.mkdir(parents=True, exist_ok=True)
    java_path.write_text("\n".join(source) + "\n", encoding="utf-8")
    print("Generated %s (%d vertices, %d triangles)" % (java_path, len(vertices), len(indices) // 3))


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise SystemExit("usage: obj_to_java.py model.obj ClaudeModel.java")
    generate(Path(sys.argv[1]), Path(sys.argv[2]))
