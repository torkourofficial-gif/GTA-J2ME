package gta;

import javax.microedition.m3g.Appearance;
import javax.microedition.m3g.Mesh;
import javax.microedition.m3g.TriangleStripArray;
import javax.microedition.m3g.VertexArray;
import javax.microedition.m3g.VertexBuffer;

/**
 * Small procedural geometry factory. Coordinates are authored in metres.
 * Positions are stored as signed 16-bit millimetres for compact M3G buffers.
 */
final class ModelFactory {
    private ModelFactory() {}

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

        int[] strips = new int[] {4,4,4,4,4,4};
        TriangleStripArray triangles = new TriangleStripArray(0, strips);

        // No material/lighting is needed for these flat-color prototype meshes.
        // A default vertex-buffer color keeps the geometry opaque and cheap.
        vertices.setDefaultColor(color | 0xFF000000);
        Appearance appearance = new Appearance();

        return new Mesh(vertices, triangles, appearance);
    }

    private static short mm(float value) {
        int v = (int)(value * 1000.0f);
        if (v > 32767) v = 32767;
        if (v < -32768) v = -32768;
        return (short)v;
    }
}
