package gta;

import javax.microedition.m3g.Appearance;
import javax.microedition.m3g.Fog;
import javax.microedition.m3g.Mesh;
import javax.microedition.m3g.TriangleStripArray;
import javax.microedition.m3g.VertexArray;
import javax.microedition.m3g.VertexBuffer;
import javax.microedition.m3g.Image2D;
import javax.microedition.m3g.Texture2D;

/**
 * Small procedural geometry factory. Coordinates are authored in metres.
 * Positions are stored as signed 16-bit millimetres for compact M3G buffers.
 */
final class ModelFactory {
    private static Fog sceneFog;

    private ModelFactory() {}

    static void setFog(Fog fog) {
        sceneFog = fog;
    }

    private static Appearance createAppearance() {
        Appearance appearance = new Appearance();
        if (sceneFog != null) appearance.setFog(sceneFog);
        return appearance;
    }

    private static Texture2D[] proceduralTextures = new Texture2D[9];

    // Tiny 16x16 RGB textures are generated in memory: no PNG decoding,
    // resource files or external assets are needed on the MIDP device.
    private static Texture2D getTexture(int kind) {
        if (kind < 0 || kind >= proceduralTextures.length) kind = 0;
        if (proceduralTextures[kind] != null) return proceduralTextures[kind];
        int size = kind == 7 ? 32 : 16;
        byte[] pixels = new byte[size * size * 3];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int r = 120, g = 120, b = 120;
                int noise = ((x * 17 + y * 29 + x * y * 3) & 7) - 3;
                if (kind == 0) {
                    // Stucco facade with regularly spaced blue-green windows.
                    boolean window = (x >= 3 && x <= 5 || x >= 10 && x <= 12) &&
                                     (y == 3 || y == 4 || y == 9 || y == 10);
                    if (window) { r = 75; g = 132; b = 151; }
                    else if (y == 7 || y == 15) { r = 238; g = 220; b = 190; }
                    else { r = 222; g = 190; b = 164; }
                } else if (kind == 1) {
                    // Asphalt grain with subtle aggregate flecks.
                    r = 57 + noise * 2; g = 62 + noise * 2; b = 66 + noise * 2;
                    if (((x * 5 + y * 3) & 15) == 0) { r += 12; g += 12; b += 12; }
                } else if (kind == 2) {
                    // Warm concrete sidewalk / sandy paving.
                    r = 181 + noise * 2; g = 166 + noise * 2; b = 143 + noise * 2;
                    if ((x + y * 2) % 9 == 0) { r -= 13; g -= 12; b -= 10; }
                } else if (kind == 3) {
                    // Subtle roof / weathered concrete grain.
                    r = 132 + noise * 2; g = 124 + noise * 2; b = 111 + noise * 2;
                    if ((x * 3 + y * 5) % 13 == 0) { r += 15; g += 13; b += 10; }
                } else if (kind == 4) {
                    // Pale Miami concrete: fine pores, faint formwork seams,
                    // small aggregate and restrained weather stains.
                    r = 184 + noise * 2; g = 177 + noise * 2; b = 163 + noise * 2;
                    if (y == 0 || y == 8) { r -= 12; g -= 12; b -= 11; }
                    if (((x * 11 + y * 7) % 23) == 0) { r -= 25; g -= 24; b -= 22; }
                    if ((x == 4 || x == 12) && y > 2 && y < 7) {
                        r -= 7; g -= 7; b -= 6;
                    }
                } else if (kind == 5) {
                    // Nearly neutral warm skin grain. The mesh vertex color
                    // supplies each protagonist's actual skin tone; this
                    // texture must not force every character toward CJ's tone.
                    r = 250 + noise; g = 246 + noise; b = 240 + noise;
                    if ((x * 3 + y * 5) % 19 == 0) { r -= 5; g -= 4; b -= 3; }
                } else if (kind == 6) {
                    // Blue denim: diagonal twill-like variation and seams.
                    r = 48 + noise * 2; g = 70 + noise * 2; b = 111 + noise * 3;
                    if (((x + y * 2) % 7) == 0) { r += 8; g += 10; b += 15; }
                    if (x == 0 || x == 15) { r -= 5; g -= 6; b -= 8; }
                } else if (kind == 7) {
                    // Jason's Leonida souvenir tank: ivory cotton, sunset stripes,
                    // dark palm silhouette and a tiny block-letter LEONIDA mark.
                    r = 226 + noise * 2; g = 225 + noise * 2; b = 211 + noise * 2;
                    if (x >= 17 && x <= 30 && y >= 14 && y <= 27) {
                        if (y == 15 || y == 18 || y == 21 || y == 24) {
                            r = 214; g = 111; b = 78;
                        }
                    }
                    // Palm trunk and fronds.
                    if ((x == 23 && y >= 15 && y <= 27) ||
                        (x == 22 && y == 24) || (x == 21 && y == 22) ||
                        (x == 24 && y == 20) || (x == 25 && y == 18) ||
                        (x >= 19 && x <= 22 && y == 15) ||
                        (x >= 21 && x <= 24 && y == 13) ||
                        (x >= 24 && x <= 27 && y == 14) ||
                        (x >= 25 && x <= 28 && y == 16)) {
                        r = 48; g = 66; b = 51;
                    }
                    // Small black-green LEONIDA lettering, centered above artwork.
                    if (y >= 7 && y <= 11) {
                        int lx = x - 17;
                        if (lx >= 0 && lx < 14) {
                            int ch = lx / 2;
                            int col = (lx % 2) + (ch % 2);
                            if (ch < 7 && ((y == 7 && (ch == 0 || ch == 1 || ch == 2 || ch == 3 || ch == 4 || ch == 5 || ch == 6)) ||
                                (y == 8 && ch == 0) || (y == 9 && ch == 0) ||
                                (y == 10 && ch == 0) || (y == 11 && (ch == 0 || ch == 1 || ch == 2 || ch == 3 || ch == 4 || ch == 5 || ch == 6))) &&
                                col == 0) { r = 54; g = 69; b = 55; }
                        }
                    }
                } else {
                    // Plain cotton weave for Lucia's pink top.
                    r = 224 + noise * 2; g = 225 + noise * 2; b = 218 + noise * 2;
                    if ((x + y) % 9 == 0) { r -= 7; g -= 7; b -= 7; }
                }
                int p = (y * size + x) * 3;
                pixels[p] = (byte)r; pixels[p + 1] = (byte)g; pixels[p + 2] = (byte)b;
            }
        }
        Image2D image = new Image2D(Image2D.RGB, size, size, pixels);
        Texture2D texture = new Texture2D(image);
        texture.setFiltering(Texture2D.FILTER_NEAREST, Texture2D.FILTER_NEAREST);
        texture.setWrapping(Texture2D.WRAP_REPEAT, Texture2D.WRAP_REPEAT);
        proceduralTextures[kind] = texture;
        return texture;
    }

    static Mesh texturedBox(float width, float height, float depth,
                            int color, int textureKind) {
        Mesh mesh = box(width, height, depth, color);
        Appearance appearance = createAppearance();
        appearance.setTexture(0, getTexture(textureKind));
        // Texture color is modulated with the material tint.
        mesh.setAppearance(0, appearance);
        return mesh;
    }

    static Mesh texturedEllipsoid(float width, float height, float depth,
                                  int color, int textureKind) {
        Mesh mesh = ellipsoid(width, height, depth, color);
        Appearance appearance = createAppearance();
        appearance.setTexture(0, getTexture(textureKind));
        mesh.setAppearance(0, appearance);
        return mesh;
    }

    static Mesh box(float width, float height, float depth, int color) {
        float x = width * 0.5f;
        float y = height * 0.5f;
        float z = depth * 0.5f;

        // Four independent vertices per face give clean face normals later
        // if lighting is enabled. The first prototype uses flat materials.
        short[] p = new short[] {
            // front
            mm(-x),mm(-y),mm(z), mm(x),mm(-y),mm(z),
            mm(-x),mm(y),mm(z), mm(x),mm(y),mm(z),
            // back
            mm(x),mm(-y),mm(-z), mm(-x),mm(-y),mm(-z),
            mm(x),mm(y),mm(-z), mm(-x),mm(y),mm(-z),
            // left
            mm(-x),mm(-y),mm(-z), mm(-x),mm(-y),mm(z),
            mm(-x),mm(y),mm(-z), mm(-x),mm(y),mm(z),
            // right
            mm(x),mm(-y),mm(z), mm(x),mm(-y),mm(-z),
            mm(x),mm(y),mm(z), mm(x),mm(y),mm(-z),
            // top
            mm(-x),mm(y),mm(z), mm(x),mm(y),mm(z),
            mm(-x),mm(y),mm(-z), mm(x),mm(y),mm(-z),
            // bottom
            mm(-x),mm(-y),mm(-z), mm(x),mm(-y),mm(-z),
            mm(-x),mm(-y),mm(z), mm(x),mm(-y),mm(z)
        };

        VertexArray positions = new VertexArray(24, 3, 2);
        positions.set(0, 24, p);

        VertexBuffer vertices = new VertexBuffer();
        vertices.setPositions(positions, 0.001f, null);
        short[] normalsData = new short[] {
            0,0,1000, 0,0,1000, 0,0,1000, 0,0,1000,
            0,0,-1000, 0,0,-1000, 0,0,-1000, 0,0,-1000,
            -1000,0,0, -1000,0,0, -1000,0,0, -1000,0,0,
            1000,0,0, 1000,0,0, 1000,0,0, 1000,0,0,
            0,1000,0, 0,1000,0, 0,1000,0, 0,1000,0,
            0,-1000,0, 0,-1000,0, 0,-1000,0, 0,-1000,0
        };
        VertexArray normals = new VertexArray(24, 3, 2);
        normals.set(0, 24, normalsData);
        vertices.setNormals(normals);
        short[] uv = new short[] {
            0,0, 1000,0, 0,1000, 1000,1000,
            0,0, 1000,0, 0,1000, 1000,1000,
            0,0, 1000,0, 0,1000, 1000,1000,
            0,0, 1000,0, 0,1000, 1000,1000,
            0,0, 1000,0, 0,1000, 1000,1000,
            0,0, 1000,0, 0,1000, 1000,1000
        };
        VertexArray texCoords = new VertexArray(24, 2, 2);
        texCoords.set(0, 24, uv);
        vertices.setTexCoords(0, texCoords, 0.001f, null);

        int[] strips = new int[] {4,4,4,4,4,4};
        TriangleStripArray triangles = new TriangleStripArray(0, strips);

        // Baked tropical daylight: vertex colors provide ambient fill plus
        // a warm upper-left sun direction. This works on textured and plain
        // meshes without M3G Light/Material state (fragile on older JBed).
        int baseR = (color >> 16) & 255;
        int baseG = (color >> 8) & 255;
        int baseB = color & 255;
        int[] faceLight = {218, 218, 148, 148, 255, 105};
        byte[] faceColors = new byte[24 * 3];
        for (int face = 0; face < 6; face++) {
            int shade = faceLight[face];
            for (int vertex = 0; vertex < 4; vertex++) {
                int ci = (face * 4 + vertex) * 3;
                faceColors[ci] = (byte)(baseR * shade / 255);
                faceColors[ci + 1] = (byte)(baseG * shade / 255);
                faceColors[ci + 2] = (byte)(baseB * shade / 255);
            }
        }
        VertexArray colors = new VertexArray(24, 3, 1);
        colors.set(0, 24, faceColors);
        vertices.setColors(colors, 1.0f / 255.0f, null);
        Appearance appearance = createAppearance();

        return new Mesh(vertices, triangles, appearance);
    }


    /** Low-poly ellipsoid for organic body parts; uses compact triangle strips. */
    static Mesh ellipsoid(float width, float height, float depth, int color) {
        final int rings = 7;
        final int segments = 8;
        int stripVertices = (segments + 1) * 2;
        short[] p = new short[rings * stripVertices * 3];
        int out = 0;
        for (int r = 0; r < rings; r++) {
            for (int s = 0; s <= segments; s++) {
                double lon = Math.PI * 2.0 * s / segments;
                for (int side = 0; side < 2; side++) {
                    int row = r + side;
                    double lat = -Math.PI * 0.5 + Math.PI * row / rings;
                    float radius = (float)Math.cos(lat);
                    p[out++] = mm((float)Math.cos(lon) * radius * width * 0.5f);
                    p[out++] = mm((float)Math.sin(lat) * height * 0.5f);
                    p[out++] = mm((float)Math.sin(lon) * radius * depth * 0.5f);
                }
            }
        }
        int vertexCount = rings * stripVertices;
        VertexArray positions = new VertexArray(vertexCount, 3, 2);
        positions.set(0, vertexCount, p);
        VertexBuffer vertices = new VertexBuffer();
        vertices.setPositions(positions, 0.001f, null);
        short[] normalData = new short[vertexCount * 3];
        int normalOut = 0;
        for (int r = 0; r < rings; r++) {
            for (int s = 0; s <= segments; s++) {
                double lon = Math.PI * 2.0 * s / segments;
                for (int side = 0; side < 2; side++) {
                    int row = r + side;
                    double lat = -Math.PI * 0.5 + Math.PI * row / rings;
                    normalData[normalOut++] = (short)((float)Math.cos(lon) * (float)Math.cos(lat) * 1000.0f);
                    normalData[normalOut++] = (short)((float)Math.sin(lat) * 1000.0f);
                    normalData[normalOut++] = (short)((float)Math.sin(lon) * (float)Math.cos(lat) * 1000.0f);
                }
            }
        }
        VertexArray normals = new VertexArray(vertexCount, 3, 2);
        normals.set(0, vertexCount, normalData);
        vertices.setNormals(normals);
        short[] uv = new short[vertexCount * 2];
        int uvOut = 0;
        for (int r = 0; r < rings; r++) {
            for (int s = 0; s <= segments; s++) {
                uv[uvOut++] = (short)(s * 1000 / segments);
                uv[uvOut++] = (short)(r * 1000 / rings);
                uv[uvOut++] = (short)(s * 1000 / segments);
                uv[uvOut++] = (short)((r + 1) * 1000 / rings);
            }
        }
        VertexArray texCoords = new VertexArray(vertexCount, 2, 2);
        texCoords.set(0, vertexCount, uv);
        vertices.setTexCoords(0, texCoords, 0.001f, null);
        int[] strips = new int[rings];
        for (int r = 0; r < rings; r++) strips[r] = stripVertices;
        TriangleStripArray triangles = new TriangleStripArray(0, strips);
        // Per-vertex diffuse shading follows the ellipsoid normals. A broad
        // ambient term keeps character silhouettes readable on small screens.
        int baseR = (color >> 16) & 255;
        int baseG = (color >> 8) & 255;
        int baseB = color & 255;
        byte[] vertexColors = new byte[vertexCount * 3];
        for (int i = 0; i < vertexCount; i++) {
            float nx = normalData[i * 3] / 1000.0f;
            float ny = normalData[i * 3 + 1] / 1000.0f;
            float nz = normalData[i * 3 + 2] / 1000.0f;
            float diffuse = nx * -0.35f + ny * 0.82f + nz * -0.45f;
            if (diffuse < 0.0f) diffuse = 0.0f;
            float light = 0.48f + diffuse * 0.52f;
            if (light > 1.0f) light = 1.0f;
            int ci = i * 3;
            vertexColors[ci] = (byte)(baseR * light);
            vertexColors[ci + 1] = (byte)(baseG * light);
            vertexColors[ci + 2] = (byte)(baseB * light);
        }
        VertexArray colors = new VertexArray(vertexCount, 3, 1);
        colors.set(0, vertexCount, vertexColors);
        vertices.setColors(colors, 1.0f / 255.0f, null);
        return new Mesh(vertices, triangles, createAppearance());
    }

    private static short mm(float value) {
        int v = (int)(value * 1000.0f);
        if (v > 32767) v = 32767;
        if (v < -32768) v = -32768;
        return (short)v;
    }
}
