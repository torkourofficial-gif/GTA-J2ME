package gta3me;

import javax.microedition.m3g.Appearance;
import javax.microedition.m3g.Material;
import javax.microedition.m3g.Mesh;
import javax.microedition.m3g.PolygonMode;
import javax.microedition.m3g.TriangleStripArray;
import javax.microedition.m3g.VertexArray;
import javax.microedition.m3g.VertexBuffer;
import javax.microedition.m3g.World;

/** Small low-poly city block built from reusable M3G primitives. */
final class CityMap {
    private static final int ASPHALT = 0x333940;
    private static final int SIDEWALK = 0x777b78;
    private static final int WHITE = 0xd8d5c8;
    private static final int GLASS = 0x42616b;
    private static final int TREE = 0x285b36;
    private static final int TRUNK = 0x60452f;
    private static final float[][] BUILDINGS = {
        {-14,-10,4.3f,5.0f}, {-7,-10,5.2f,5.0f}, {1,-10,4.8f,5.0f},
        {9,-10,5.0f,5.0f}, {16,-10,4.0f,5.0f},
        {-14,10,4.6f,5.0f}, {-6,10,5.0f,5.0f}, {2,10,4.5f,5.0f},
        {10,10,5.0f,5.0f}, {17,10,3.5f,5.0f}
    };

    private CityMap() {
    }

    static void addTo(World world) {
        addBox(world, 0, -0.15f, 0, 40, 0.3f, 40, 0x687254); // ground
        addBox(world, 0, 0.015f, 0, 40, 0.03f, 6, ASPHALT); // main street
        addBox(world, 0, 0.015f, -7.1f, 40, 0.03f, 7.8f, 0x77765f); // plaza
        addBox(world, 0, 0.015f, 7.1f, 40, 0.03f, 7.8f, 0x77765f);
        addBox(world, 0, 0.09f, -3.75f, 40, 0.18f, 1.2f, SIDEWALK);
        addBox(world, 0, 0.09f, 3.75f, 40, 0.18f, 1.2f, SIDEWALK);

        // Dashed road markings.
        for (int x = -18; x <= 18; x += 4) {
            addBox(world, x, 0.035f, 0, 1.8f, 0.01f, 0.09f, WHITE);
        }

        addBuilding(world, -14, -10, 4.3f, 5.5f, 5.0f, 0x77756f);
        addBuilding(world, -7, -10, 5.2f, 7.0f, 5.0f, 0x8b735c);
        addBuilding(world, 1, -10, 4.8f, 4.2f, 5.0f, 0x777d7a);
        addBuilding(world, 9, -10, 5.0f, 6.1f, 5.0f, 0x786d61);
        addBuilding(world, 16, -10, 4.0f, 5.0f, 5.0f, 0x747a78);

        addBuilding(world, -14, 10, 4.6f, 5.2f, 5.0f, 0x81715f);
        addBuilding(world, -6, 10, 5.0f, 6.4f, 5.0f, 0x727a7b);
        addBuilding(world, 2, 10, 4.5f, 4.6f, 5.0f, 0x8b785e);
        addBuilding(world, 10, 10, 5.0f, 7.2f, 5.0f, 0x77736c);
        addBuilding(world, 17, 10, 3.5f, 4.5f, 5.0f, 0x7a7e77);

        addTree(world, -18, -4.2f);
        addTree(world, -10, -4.2f);
        addTree(world, 10, -4.2f);
        addTree(world, 18, -4.2f);
        addTree(world, -18, 4.2f);
        addTree(world, -10, 4.2f);
        addTree(world, 10, 4.2f);
        addTree(world, 18, 4.2f);

        addLamp(world, -15, -3.4f);
        addLamp(world, -5, -3.4f);
        addLamp(world, 5, 3.4f);
        addLamp(world, 15, 3.4f);

    }

    static boolean blocks(float x, float z) {
        for (int i = 0; i < BUILDINGS.length; i++) {
            float[] building = BUILDINGS[i];
            if (Math.abs(x - building[0]) < building[2] / 2.0f + 0.45f
                    && Math.abs(z - building[1]) < building[3] / 2.0f + 0.45f) {
                return true;
            }
        }
        return false;
    }

    private static void addBuilding(World world, float x, float z, float width,
                                    float height, float depth, int color) {
        addBox(world, x, height / 2.0f, z, width, height, depth, color);
        float faceZ = z < 0 ? z + depth / 2.0f + 0.035f : z - depth / 2.0f - 0.035f;
        float[] levels = {1.5f, 3.2f, 4.9f};
        for (int i = 0; i < levels.length; i++) {
            if (levels[i] < height - 0.5f) {
                addBox(world, x - width * 0.25f, levels[i], faceZ, 0.72f, 0.78f, 0.06f, GLASS);
                addBox(world, x + width * 0.25f, levels[i], faceZ, 0.72f, 0.78f, 0.06f, GLASS);
            }
        }
        addBox(world, x, 0.85f, faceZ, 0.8f, 1.65f, 0.08f, 0x493d32);
    }

    private static void addTree(World world, float x, float z) {
        addBox(world, x, 0.85f, z, 0.28f, 1.7f, 0.28f, TRUNK);
        addBox(world, x, 2.0f, z, 1.65f, 1.65f, 1.65f, TREE);
        addBox(world, x, 2.8f, z, 1.0f, 1.0f, 1.0f, 0x376d3c);
    }

    private static void addLamp(World world, float x, float z) {
        addBox(world, x, 1.45f, z, 0.12f, 2.9f, 0.12f, 0x41464a);
        addBox(world, x, 2.95f, z, 0.7f, 0.14f, 0.24f, 0xc9b77a);
    }

    private static void addBox(World world, float x, float y, float z,
                               float width, float height, float depth, int color) {
        Mesh box = createUnitBox(color);
        box.setScale(width, height, depth);
        box.setTranslation(x, y, z);
        world.addChild(box);
    }

    private static Mesh createUnitBox(int color) {
        short[] positions = {
            -1000,-1000,1000, 1000,-1000,1000, -1000,1000,1000, 1000,1000,1000,
             1000,-1000,-1000, -1000,-1000,-1000, 1000,1000,-1000, -1000,1000,-1000,
            -1000,-1000,-1000, -1000,-1000,1000, -1000,1000,-1000, -1000,1000,1000,
             1000,-1000,1000, 1000,-1000,-1000, 1000,1000,1000, 1000,1000,-1000,
            -1000,1000,1000, 1000,1000,1000, -1000,1000,-1000, 1000,1000,-1000,
            -1000,-1000,-1000, 1000,-1000,-1000, -1000,-1000,1000, 1000,-1000,1000
        };
        short[] normals = {
            0,0,32767, 0,0,32767, 0,0,32767, 0,0,32767,
            0,0,-32767, 0,0,-32767, 0,0,-32767, 0,0,-32767,
            -32767,0,0, -32767,0,0, -32767,0,0, -32767,0,0,
            32767,0,0, 32767,0,0, 32767,0,0, 32767,0,0,
            0,32767,0, 0,32767,0, 0,32767,0, 0,32767,0,
            0,-32767,0, 0,-32767,0, 0,-32767,0, 0,-32767,0
        };
        int[] stripLengths = {4,4,4,4,4,4};
        VertexArray positionArray = new VertexArray(24, 3, 2);
        positionArray.set(0, 24, positions);
        VertexArray normalArray = new VertexArray(24, 3, 2);
        normalArray.set(0, 24, normals);
        VertexBuffer vertices = new VertexBuffer();
        vertices.setPositions(positionArray, 0.0005f, null);
        vertices.setNormals(normalArray);

        Material material = new Material();
        material.setColor(Material.EMISSIVE, 0xFF000000 | color);
        Appearance appearance = new Appearance();
        appearance.setMaterial(material);
        PolygonMode polygonMode = new PolygonMode();
        polygonMode.setCulling(PolygonMode.CULL_NONE);
        appearance.setPolygonMode(polygonMode);
        return new Mesh(vertices, new TriangleStripArray(0, stripLengths), appearance);
    }
}
