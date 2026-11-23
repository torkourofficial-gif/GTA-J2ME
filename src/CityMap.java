package gta3me;

import javax.microedition.m3g.Appearance;
import javax.microedition.m3g.Material;
import javax.microedition.m3g.Mesh;
import javax.microedition.m3g.PolygonMode;
import javax.microedition.m3g.TriangleStripArray;
import javax.microedition.m3g.VertexArray;
import javax.microedition.m3g.VertexBuffer;
import javax.microedition.m3g.World;
import javax.microedition.m3g.Texture2D;

/** Small low-poly city block built from reusable M3G primitives. */
final class CityMap {
    private static final int ASPHALT = 0x333940;
    private static final int SIDEWALK = 0x777b78;
    private static final int WHITE = 0xd8d5c8;
    private static final int GLASS = 0x42616b;
    private static final int TREE = 0x285b36;
    private static final int TRUNK = 0x60452f;
    private static Texture2D asphaltTexture;
    private static Texture2D concreteTexture;
    private static Texture2D grassTexture;
    private static Texture2D glassTexture;
    private static Texture2D barkTexture;
    private static Texture2D leavesTexture;
    private static Texture2D metalTexture;
    private static float[][] CITY_BUILDINGS;

    private CityMap() {
    }

    static void addTo(World world) {
        asphaltTexture = ProceduralTextures.createTexture(ProceduralTextures.ASPHALT, ASPHALT, 71);
        concreteTexture = ProceduralTextures.createTexture(ProceduralTextures.CONCRETE, SIDEWALK, 83);
        grassTexture = ProceduralTextures.createTexture(ProceduralTextures.GRASS, 0x687254, 97);
        glassTexture = ProceduralTextures.createTexture(ProceduralTextures.GLASS, GLASS, 109);
        barkTexture = ProceduralTextures.createTexture(ProceduralTextures.BARK, TRUNK, 131);
        leavesTexture = ProceduralTextures.createTexture(ProceduralTextures.LEAVES, TREE, 149);
        metalTexture = ProceduralTextures.createTexture(ProceduralTextures.METAL, 0x41464a, 163);

        // Nine connected districts, each 40 units wide. Streets form a
        // continuous grid and keep every building reachable on foot.
        addTexturedBox(world, 0, -0.15f, 0, 120, 0.3f, 120, 0x687254, grassTexture, 2.5f);
        for (int road = -60; road <= 60; road += 40) {
            addTexturedBox(world, 0, 0.015f, road, 120, 0.03f, 7.0f, ASPHALT, asphaltTexture, 1.5f);
            addTexturedBox(world, road, 0.015f, 0, 7.0f, 0.03f, 120, ASPHALT, asphaltTexture, 1.5f);
            for (int side = -1; side <= 1; side += 2) {
                addTexturedBox(world, 0, 0.09f, road + side * 4.4f, 120, 0.18f, 1.5f,
                        SIDEWALK, concreteTexture, 1.4f);
                addTexturedBox(world, road + side * 4.4f, 0.09f, 0, 1.5f, 0.18f, 120,
                        SIDEWALK, concreteTexture, 1.4f);
            }
        }

        CITY_BUILDINGS = new float[36][5];
        int buildingIndex = 0;
        int district = 0;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                float centerX = dx * 40.0f;
                float centerZ = dz * 40.0f;
                for (int sz = -1; sz <= 1; sz += 2) {
                    for (int sx = -1; sx <= 1; sx += 2) {
                        float bx = centerX + sx * 10.5f;
                        float bz = centerZ + sz * 10.5f;
                        float width = 7.0f + ((district + buildingIndex) % 3) * 0.8f;
                        float depth = 7.2f + ((district * 2 + buildingIndex) % 3) * 0.7f;
                        float height = 5.0f + ((district * 3 + buildingIndex * 5) % 8) * 1.05f;
                        int color = buildingColor(buildingIndex + district * 3);
                        CITY_BUILDINGS[buildingIndex][0] = bx;
                        CITY_BUILDINGS[buildingIndex][1] = bz;
                        CITY_BUILDINGS[buildingIndex][2] = width;
                        CITY_BUILDINGS[buildingIndex][3] = depth;
                        CITY_BUILDINGS[buildingIndex][4] = height;
                        addBuilding(world, bx, bz, width, height, depth, color);
                        buildingIndex++;
                    }
                }
                district++;
            }
        }

        // Trees are placed along sidewalks rather than in the middle of roads.
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                float cx = dx * 40.0f;
                float cz = dz * 40.0f;
                addTree(world, cx - 4.9f, cz - 10.0f, 0);
                addTree(world, cx + 4.9f, cz + 10.0f, 1);
            }
        }

        for (int road = -60; road <= 60; road += 40) {
            for (int dash = -13; dash <= 13; dash += 4) {
                addBox(world, dash, 0.035f, road, 1.8f, 0.01f, 0.09f, WHITE);
                addBox(world, road, 0.035f, dash, 0.09f, 0.01f, 1.8f, WHITE);
            }
            for (int side = -1; side <= 1; side += 2) {
                addLamp(world, road + side * 5.2f, road + side * 5.2f);
            }
        }

        // A few recognizable street props around the starting district.
        addCar(world, -3.4f, -1.9f, 0x6b2925);
        addCar(world, 8.0f, 1.9f, 0x485c70);
        addBench(world, -3.0f, -6.0f);
        addBench(world, 4.0f, 6.0f);
        addTrashBin(world, 2.5f, -5.6f);
        addTrashBin(world, -6.0f, 5.7f);
        addHydrant(world, 12.2f, -4.9f);
        addHydrant(world, -12.2f, 4.9f);
    }

    private static int buildingColor(int index) {
        int[] colors = {0x77756f, 0x8b735c, 0x777d7a, 0x786d61, 0x747a78,
                0x81715f, 0x727a7b, 0x8b785e, 0x77736c, 0x7a7e77};
        return colors[index % colors.length];
    }

    static boolean blocks(float x, float z) {
        if (CITY_BUILDINGS == null) return false;
        for (int i = 0; i < CITY_BUILDINGS.length; i++) {
            float[] building = CITY_BUILDINGS[i];
            if (Math.abs(x - building[0]) < building[2] / 2.0f + 0.55f
                    && Math.abs(z - building[1]) < building[3] / 2.0f + 0.55f) {
                return true;
            }
        }
        return false;
    }

    private static void addBuilding(World world, float x, float z, float width,
                                    float height, float depth, int color) {
        Texture2D facade = ProceduralTextures.createTexture(ProceduralTextures.BRICK,
                color, (int) (x * 67.0f + z * 43.0f + height * 19.0f));
        addTexturedBox(world, x, height / 2.0f, z, width, height, depth, color, facade, 0.85f);
        float faceZ = z < 0 ? z + depth / 2.0f + 0.035f : z - depth / 2.0f - 0.035f;
        int floors = (int) (height / 1.7f);
        if (floors > 4) floors = 4;
        for (int floor = 0; floor < floors; floor++) {
            float level = 1.25f + floor * 1.7f;
            if (level < height - 0.35f) {
                float windowWidth = width > 7.5f ? 0.88f : 0.72f;
                addTexturedBox(world, x - width * 0.25f, level, faceZ,
                        windowWidth, 0.88f, 0.06f, GLASS, glassTexture, 0.7f);
                addTexturedBox(world, x + width * 0.25f, level, faceZ,
                        windowWidth, 0.88f, 0.06f, GLASS, glassTexture, 0.7f);
            }
        }
        addTexturedBox(world, x, 0.85f, faceZ, 0.8f, 1.65f, 0.08f, 0x493d32,
                ProceduralTextures.createTexture(ProceduralTextures.BARK, 0x493d32, (int) x + 211), 0.9f);
        addBox(world, x, height + 0.12f, z, width + 0.12f, 0.24f, depth + 0.12f, 0x6b6962);
    }

    private static void addTree(World world, float x, float z, int variant) {
        float height = 2.6f + variant * 0.35f;
        addTexturedBox(world, x, height * 0.48f, z, 0.22f, height, 0.22f,
                TRUNK, barkTexture, 0.55f);
        // Visible branch structure breaks the old single-cube silhouette.
        addBranch(world, x, height * 0.66f, z, -0.52f, 0.58f, -0.08f);
        addBranch(world, x, height * 0.72f, z, 0.48f, 0.62f, 0.10f);
        addBranch(world, x, height * 0.78f, z, 0.08f, 0.58f, 0.48f);
        addBranch(world, x, height * 0.70f, z, -0.10f, 0.52f, -0.45f);
        // Overlapping low-poly ellipsoids create an irregular leafy crown.
        addLeafCluster(world, x, height * 0.88f, z, 0.72f, 0.72f, 0.68f, TREE);
        addLeafCluster(world, x - 0.48f, height * 0.80f, z + 0.10f, 0.56f, 0.62f, 0.58f, 0x356b3c);
        addLeafCluster(world, x + 0.45f, height * 0.82f, z - 0.08f, 0.58f, 0.64f, 0.56f, 0x477944);
        addLeafCluster(world, x + 0.04f, height * 1.08f, z + 0.34f, 0.48f, 0.52f, 0.48f, 0x3b713d);
    }

    private static void addBranch(World world, float x, float y, float z,
                                  float offsetX, float rise, float offsetZ) {
        float length = (float) Math.sqrt(offsetX * offsetX + rise * rise + offsetZ * offsetZ);
        Mesh branch = createUnitBox(TRUNK, barkTexture, 0.10f, length, 0.10f, 0.55f);
        branch.setScale(0.10f, length, 0.10f);
        branch.setTranslation(x + offsetX * 0.48f, y + rise * 0.48f, z + offsetZ * 0.48f);
        branch.setOrientation(offsetX * 28.0f, 0.0f, 0.0f, 1.0f);
        world.addChild(branch);
    }

    private static void addLeafCluster(World world, float x, float y, float z,
                                       float sx, float sy, float sz, int color) {
        Material material = new Material();
        material.setColor(Material.EMISSIVE, 0xFF000000 | color);
        Appearance appearance = new Appearance();
        appearance.setMaterial(material);
        appearance.setTexture(0, leavesTexture);
        PolygonMode mode = new PolygonMode();
        mode.setCulling(PolygonMode.CULL_NONE);
        appearance.setPolygonMode(mode);
        Mesh leaves = createLeafMesh(appearance);
        leaves.setScale(sx, sy, sz);
        leaves.setTranslation(x, y, z);
        world.addChild(leaves);
    }

    /**
     * Compact octahedral leaf volume built only from standard JSR-184
     * VertexArray and TriangleStripArray classes. Several overlapping,
     * differently scaled volumes give the crown a rounded, irregular shape.
     */
    private static Mesh createLeafMesh(Appearance appearance) {
        short[] positions = {
             0, 1000,    0,
             0,-1000,    0,
         -1000,    0,    0,
          1000,    0,    0,
             0,    0, 1000,
             0,    0,-1000
        };
        int[] indices = {
            0, 4, 3, 0, 3, 5, 0, 5, 2, 0, 2, 4,
            1, 3, 4, 1, 5, 3, 1, 2, 5, 1, 4, 2
        };
        int[] stripLengths = {3,3,3,3,3,3,3,3};
        VertexArray positionArray = new VertexArray(6, 3, 2);
        positionArray.set(0, 6, positions);
        VertexBuffer vertices = new VertexBuffer();
        vertices.setPositions(positionArray, 0.0005f, null);
        return new Mesh(vertices, new TriangleStripArray(indices, stripLengths), appearance);
    }

    private static void addLamp(World world, float x, float z) {
        addTexturedBox(world, x, 1.45f, z, 0.12f, 2.9f, 0.12f, 0x41464a, metalTexture, 1.0f);
        addTexturedBox(world, x, 2.95f, z, 0.7f, 0.14f, 0.24f, 0xc9b77a, glassTexture, 0.8f);
    }

    private static void addBench(World world, float x, float z) {
        addTexturedBox(world, x, 0.62f, z, 1.55f, 0.12f, 0.48f, 0x76583b,
                ProceduralTextures.createTexture(ProceduralTextures.BARK, 0x76583b, (int) (x * 17 + z)), 0.7f);
        addTexturedBox(world, x, 1.02f, z + 0.20f, 1.55f, 0.64f, 0.10f, 0x76583b,
                ProceduralTextures.createTexture(ProceduralTextures.BARK, 0x76583b, (int) (z * 23 - x)), 0.7f);
        addTexturedBox(world, x - 0.58f, 0.32f, z, 0.10f, 0.60f, 0.10f, 0x41464a, metalTexture, 0.7f);
        addTexturedBox(world, x + 0.58f, 0.32f, z, 0.10f, 0.60f, 0.10f, 0x41464a, metalTexture, 0.7f);
    }

    private static void addTrashBin(World world, float x, float z) {
        addTexturedBox(world, x, 0.48f, z, 0.48f, 0.86f, 0.48f, 0x36504c,
                ProceduralTextures.createTexture(ProceduralTextures.METAL, 0x36504c, (int) (x * 31 + z)), 0.6f);
        addBox(world, x, 0.94f, z, 0.56f, 0.08f, 0.56f, 0x272d30);
    }

    private static void addHydrant(World world, float x, float z) {
        addTexturedBox(world, x, 0.42f, z, 0.32f, 0.72f, 0.32f, 0x9d3429,
                ProceduralTextures.createTexture(ProceduralTextures.METAL, 0x9d3429, (int) (x * 9 + z * 13)), 0.6f);
        addBox(world, x, 0.72f, z, 0.56f, 0.16f, 0.24f, 0x9d3429);
        addBox(world, x, 0.82f, z, 0.24f, 0.12f, 0.24f, 0x9d3429);
    }

    private static void addCar(World world, float x, float z, int color) {
        addTexturedBox(world, x, 0.55f, z, 2.4f, 0.62f, 1.15f, color,
                ProceduralTextures.createTexture(ProceduralTextures.METAL, color, (int) (x * 47 + z * 29)), 0.8f);
        addTexturedBox(world, x, 0.98f, z, 1.28f, 0.46f, 0.92f, 0x536b78, glassTexture, 0.7f);
        for (int side = -1; side <= 1; side += 2) {
            for (int axle = -1; axle <= 1; axle += 2) {
                addTexturedBox(world, x + axle * 0.72f, 0.28f, z + side * 0.53f,
                        0.48f, 0.44f, 0.18f, 0x24282b, metalTexture, 0.5f);
            }
            addBox(world, x + side * 1.14f, 0.56f, z - 0.35f, 0.08f, 0.16f, 0.20f, 0xd5c087);
            addBox(world, x + side * 1.14f, 0.56f, z + 0.35f, 0.08f, 0.16f, 0.20f, 0x9b332b);
        }
    }

    private static void addBox(World world, float x, float y, float z,
                               float width, float height, float depth, int color) {
        addBox(world, x, y, z, width, height, depth, color, null, 1.0f);
    }

    private static void addTexturedBox(World world, float x, float y, float z,
                                       float width, float height, float depth, int color,
                                       Texture2D texture, float tileSize) {
        addBox(world, x, y, z, width, height, depth, color, texture, tileSize);
    }

    private static void addBox(World world, float x, float y, float z,
                               float width, float height, float depth, int color,
                               Texture2D texture, float tileSize) {
        Mesh box = createUnitBox(color, texture, width, height, depth, tileSize);
        box.setScale(width, height, depth);
        box.setTranslation(x, y, z);
        world.addChild(box);
    }

    private static Mesh createUnitBox(int color, Texture2D texture,
                                      float width, float height, float depth, float tileSize) {
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

        if (texture != null) {
            short[] uv = createTexCoords(width, height, depth, tileSize);
            VertexArray texCoordArray = new VertexArray(24, 2, 2);
            texCoordArray.set(0, 24, uv);
            vertices.setTexCoords(0, texCoordArray, 0.001f, null);
        }

        Material material = new Material();
        material.setColor(Material.EMISSIVE, texture == null ? 0xFF000000 | color : 0xFFFFFFFF);
        Appearance appearance = new Appearance();
        appearance.setMaterial(material);
        if (texture != null) appearance.setTexture(0, texture);
        PolygonMode polygonMode = new PolygonMode();
        polygonMode.setCulling(PolygonMode.CULL_NONE);
        appearance.setPolygonMode(polygonMode);
        return new Mesh(vertices, new TriangleStripArray(0, stripLengths), appearance);
    }

    private static short[] createTexCoords(float width, float height, float depth, float tileSize) {
        short[] uv = new short[48];
        float frontU = width / tileSize * 1000.0f;
        float sideU = depth / tileSize * 1000.0f;
        float verticalV = height / tileSize * 1000.0f;
        float topV = depth / tileSize * 1000.0f;
        setFaceUV(uv, 0, frontU, verticalV);
        setFaceUV(uv, 1, frontU, verticalV);
        setFaceUV(uv, 2, sideU, verticalV);
        setFaceUV(uv, 3, sideU, verticalV);
        setFaceUV(uv, 4, frontU, topV);
        setFaceUV(uv, 5, frontU, topV);
        return uv;
    }

    private static void setFaceUV(short[] uv, int face, float u, float v) {
        int i = face * 8;
        uv[i] = 0; uv[i + 1] = 0;
        uv[i + 2] = (short) u; uv[i + 3] = 0;
        uv[i + 4] = 0; uv[i + 5] = (short) v;
        uv[i + 6] = (short) u; uv[i + 7] = (short) v;
    }
}
