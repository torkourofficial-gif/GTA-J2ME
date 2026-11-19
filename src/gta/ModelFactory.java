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

    private static Texture2D[] proceduralTextures = new Texture2D[4];

    // Tiny 16x16 RGB textures are generated in memory: no PNG decoding,
    // resource files or external assets are needed on the MIDP device.
    private static Texture2D getTexture(int kind) {
        if (kind < 0 || kind >= proceduralTextures.length) kind = 0;
        if (proceduralTextures[kind] != null) return proceduralTextures[kind];
        byte[] pixels = new byte[16 * 16 * 3];
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
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
                } else {
                    // Subtle roof / weathered concrete grain.
                    r = 132 + noise * 2; g = 124 + noise * 2; b = 111 + noise * 2;
                    if ((x * 3 + y * 5) % 13 == 0) { r += 15; g += 13; b += 10; }
                }
                int p = (y * 16 + x) * 3;
                pixels[p] = (byte)r; pixels[p + 1] = (byte)g; pixels[p + 2] = (byte)b;
            }
        }
        Image2D image = new Image2D(Image2D.RGB, 16, 16, pixels);
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

        // No material/lighting is needed for these flat-color prototype meshes.
        // A default vertex-buffer color keeps the geometry opaque and cheap.
        vertices.setDefaultColor(color | 0xFF000000);
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
        int[] strips = new int[rings];
        for (int r = 0; r < rings; r++) strips[r] = stripVertices;
        TriangleStripArray triangles = new TriangleStripArray(0, strips);
        vertices.setDefaultColor(color | 0xFF000000);
        return new Mesh(vertices, triangles, createAppearance());
    }

    private static short mm(float value) {
        int v = (int)(value * 1000.0f);
        if (v > 32767) v = 32767;
        if (v < -32768) v = -32768;
        return (short)v;
    }
}
