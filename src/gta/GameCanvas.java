package gta;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.m3g.Background;
import javax.microedition.m3g.Camera;
import javax.microedition.m3g.Graphics3D;
import javax.microedition.m3g.Fog;
import javax.microedition.m3g.Group;
import javax.microedition.m3g.Mesh;
import javax.microedition.m3g.Transform;
import javax.microedition.m3g.World;

/**
 * First playable scene: small test block, one sedan and a procedural CJ.
 * Numeric keys move/action; the D-pad orbits the third-person camera.
 */
final class GameCanvas extends Canvas implements Runnable {
    private static final int FORWARD = 1;
    private static final int BACK = 2;
    private static final int LEFT = 4;
    private static final int RIGHT = 8;
    private static final int CROUCH = 16;
    private static final int SPRINT = 32;

    private static final int CAM_UP = 1;
    private static final int CAM_DOWN = 2;
    private static final int CAM_LEFT = 4;
    private static final int CAM_RIGHT = 8;

    private Thread loop;
    private volatile boolean running;
    private volatile boolean paused;
    private volatile int keys;
    private volatile int cameraKeys;

    private World world;
    private Group scenery;
    private Group regionalRoads;
    private Group[] regionChunks = new Group[6];
    private Group[][] cityChunks = new Group[CITY_BLOCKS][CITY_BLOCKS];
    private int activeChunkX = -99, activeChunkZ = -99;
    private CJModel cj;
    private CJModel secondProtagonist;
    private boolean luciaActive;
    private boolean characterSwitching;
    private float characterSwitchTime;
    private float switchFromX, switchFromZ, switchToX, switchToZ;
    private boolean switchApplied;
    private CarModel car;
    private MotorcycleModel playerBike;
    private MotorcycleModel[] trafficBikes = new MotorcycleModel[3];
    private float[] bikeTrafficX = new float[3], bikeTrafficZ = new float[3];
    private float[] bikeTrafficHeading = new float[3], bikeTrafficSpeed = new float[3];
    private boolean[] bikeTrafficActive = new boolean[3];
    private boolean inBike;
    private int bikeSourceIndex = -1;
    private float bikeX = -3.0f, bikeZ = 0.0f, bikeHeading, bikeSpeed, bikeSteering;
    private float bikeMountTimer, bikeMountStartX, bikeMountStartZ;
    private boolean jetpackOwned, jetpackThrust;
    private float jetpackFlamePhase;
    private Group jetpackVisual;
    private Group jetpackFlames;
    private int cheatProgress;
    private long cheatLastKeyTime;
    private static final int[] JETPACK_CHEAT = {3,3,3,8,8,2,2,5,5};
    private CarModel[] policeCars = new CarModel[3];
    private float[] policeX = new float[3], policeZ = new float[3];
    private boolean[] policeActive = new boolean[3];
    private int wantedLevel;
    private float wantedDecay, arrestTimer;
    private Image wantedStarOn, wantedStarOff;
    private Camera camera;
    private static final int PED_COUNT = 28;
    private static final int TRAFFIC_COUNT = 12;
    private PedestrianModel[] pedestrians = new PedestrianModel[PED_COUNT];
    private boolean[] pedActive = new boolean[PED_COUNT];
    private int[] pedState = new int[PED_COUNT]; // 0 wander, 1 flee, 2 confront, 3 down
    private float[] pedX = new float[PED_COUNT], pedZ = new float[PED_COUNT];
    private float[] pedHeading = new float[PED_COUNT], pedTimer = new float[PED_COUNT];
    private float[] pedPhase = new float[PED_COUNT], pedAttackCooldown = new float[PED_COUNT];
    private int nextPedVariant = 0;
    private CarModel[] trafficCars = new CarModel[TRAFFIC_COUNT];
    private float[] trafficX = new float[TRAFFIC_COUNT], trafficZ = new float[TRAFFIC_COUNT];
    private float[] trafficHeading = new float[TRAFFIC_COUNT], trafficSpeed = new float[TRAFFIC_COUNT];
    private float[] trafficCruiseSpeed = new float[TRAFFIC_COUNT];
    private float[] trafficStop = new float[TRAFFIC_COUNT], trafficHitCooldown = new float[TRAFFIC_COUNT];
    private float[] trafficLaneX = new float[TRAFFIC_COUNT], trafficLaneCooldown = new float[TRAFFIC_COUNT];
    private float playerRagdollTime, playerRagdollY, playerRagdollVX, playerRagdollVY, playerRagdollVZ;
    private float playerRagdollRoll, playerRagdollPitch, playerRagdollRollSpeed, playerRagdollPitchSpeed;
    private float playerCollisionCooldown;
    private float[] pedRagdollVX = new float[PED_COUNT], pedRagdollVY = new float[PED_COUNT];
    private float[] pedRagdollY = new float[PED_COUNT];
    private float[] pedRagdollVZ = new float[PED_COUNT];
    private float[] pedRagdollRoll = new float[PED_COUNT], pedRagdollPitch = new float[PED_COUNT];
    private float[] pedRagdollRollSpeed = new float[PED_COUNT], pedRagdollPitchSpeed = new float[PED_COUNT];
    private int trafficHijackIndex = -1;
    private float trafficHijackTime = 0.0f;
    private boolean trafficDriverEjected = false;
    private int randomSeed = 173;
    private float playerHitStun = 0.0f;
    private Transform cameraTransform;

    private float playerX = 0.0f;
    private float playerZ = 3.0f;
    private float playerY = 0.0f;
    private float playerHeading = 0.0f;
    private float playerVelocityX = 0.0f;
    private float playerVelocityZ = 0.0f;
    private float cameraYaw = 0.0f;
    private float cameraPitch = 13.0f;
    private float walkPhase = 0.0f;
    private float jumpVelocity = 0.0f;
    private float punchTimer = 0.0f;
    private float carSpeed = 0.0f;
    private float carSteering = 0.0f;
    private long lastForwardTap = 0L;
    private long lastBackTap = 0L;
    private float carHeading = 0.0f;
    private float carX = 2.5f;
    private float carZ = 0.0f;
    private boolean inCar = false;
    private int carTransition = 0;
    private float carTransitionTime = 0.0f;
    private float transitionStartX, transitionStartZ, transitionEndX, transitionEndZ;
    private boolean transitionDriverSide = true;
    private static final float CAR_TRANSITION_LENGTH = 0.9f;
    private static final int CITY_BLOCKS = 10;
    private static final float CITY_SPACING = 28.0f;
    private static final float CITY_LIMIT = 560.0f;
    private float[] buildingX = new float[180];
    private float[] buildingZ = new float[180];
    private float[] buildingW = new float[180];
    private float[] buildingD = new float[180];
    private int buildingCount = 0;
    private boolean reverseFacing = false;
    private boolean backwardFacingActive = false;
    private float reverseTargetHeading = 0.0f;
    private boolean initialized = false;
    private String status = "2/4/6/8 MOVE  3 CAR";
    private String fatalError;
    private String debugStage = "starting";
    private int diagnosticPage = 0;

    GameCanvas() {
        setFullScreenMode(true);
        loadWantedStarImages();
        try {
            initializeScene();
            initialized = true;
        } catch (Throwable error) {
            fatalError = error.toString();
            debugStage = "scene initialization";
            status = "M3G INIT ERROR";
        }
    }

    private void loadWantedStarImages() {
        try {
            Image source = Image.createImage("/gta/stars.png");
            int sw = source.getWidth();
            int sh = source.getHeight();
            int size = 12;
            int[] pixels = new int[sw * sh];
            source.getRGB(pixels, 0, sw, 0, 0, sw, sh);
            int[] scaled = new int[size * size];
            for (int y = 0; y < size; y++) {
                int sy = y * sh / size;
                for (int x = 0; x < size; x++) {
                    int sx = x * sw / size;
                    scaled[y * size + x] = pixels[sy * sw + sx];
                }
            }
            wantedStarOn = Image.createRGBImage(scaled, size, size, true);
            int[] gray = new int[scaled.length];
            for (int i = 0; i < scaled.length; i++) {
                int pixel = scaled[i];
                int alpha = pixel & 0xFF000000;
                int red = (pixel >> 16) & 255;
                int green = (pixel >> 8) & 255;
                int blue = pixel & 255;
                int light = (red * 30 + green * 59 + blue * 11) / 100;
                gray[i] = alpha | (light << 16) | (light << 8) | light;
            }
            wantedStarOff = Image.createRGBImage(gray, size, size, true);
        } catch (Throwable ignored) {
            wantedStarOn = null;
            wantedStarOff = null;
        }
    }

    private void initializeScene() {
        debugStage = "create M3G world";
        world = new World();
        debugStage = "create background";
        Background sky = new Background();
        sky.setColor(0x87B6D8);
        sky.setColorClearEnable(true);
        world.setBackground(sky);

        debugStage = "create fog";
        Fog fog = new Fog();
        fog.setColor(0x87B6D8);
        fog.setLinear(95.0f, 285.0f);
        ModelFactory.setFog(fog);
        debugStage = "create scenery group";
        scenery = new Group();
        world.addChild(scenery);

        // The road grid spans eleven blocks in each direction. Tile the
        // ground across that full footprint so no road or block hangs over
        // the edge of the visible floor.
        debugStage = "create ground tiles";
        for (int tx = -5; tx <= 5; tx++) {
            for (int tz = -5; tz <= 5; tz++) {
                float tileX = tx * CITY_SPACING;
                float tileZ = tz * CITY_SPACING;
                addTexturedBox(scenery, CITY_SPACING, 0.18f, CITY_SPACING,
                              tileX, -0.12f, tileZ, 0xFFFFFF, 2);
            }
        }
        debugStage = "create road meshes";
        for (int i = -5; i <= 5; i++) {
            float road = i * CITY_SPACING;
            for (int segment = -5; segment <= 5; segment++) {
                float along = segment * CITY_SPACING;
                addTexturedBox(scenery, 7.5f, 0.025f, CITY_SPACING,
                              road, -0.015f, along, 0xFFFFFF, 1);
                addTexturedBox(scenery, CITY_SPACING, 0.025f, 7.5f,
                              along, -0.014f, road, 0xFFFFFF, 1);
            }
        }

        // Vice City-inspired districts: pastel oceanfront hotels, a denser
        // downtown strip, and lower residential/commercial blocks inland.
        // Buildings remain simple boxes so the scene stays suitable for J2ME.
        debugStage = "create city buildings";
        buildingCount = 0;
        for (int bz = 0; bz < CITY_BLOCKS; bz++) {
            for (int bx = 0; bx < CITY_BLOCKS; bx++) {
                Group chunk = new Group();
                cityChunks[bx][bz] = chunk;
                scenery.addChild(chunk);
                float x = (bx - (CITY_BLOCKS - 1) * 0.5f) * CITY_SPACING;
                float z = (bz - (CITY_BLOCKS - 1) * 0.5f) * CITY_SPACING;
                int seed = bx * 17 + bz * 31 + bx * bz * 3;
                int district = bz < 3 ? 0 : (bz < 7 ? 1 : 2);
                int type = Math.abs(seed) % 8;
                float width = 10.5f + (Math.abs(seed / 3) % 5) * 1.15f;
                float depth = 10.5f + (Math.abs(seed / 7) % 5) * 1.05f;
                float height;
                if (district == 0) height = 12.0f + (type % 4) * 4.0f;
                else if (district == 1) height = 9.0f + (type % 6) * 3.0f;
                else height = 4.5f + (type % 4) * 2.0f;
                x += ((seed % 3) - 1) * 1.0f;
                z += (((seed / 2) % 3) - 1) * 1.0f;

                int color;
                switch (type) {
                    case 0: color = 0xF0B6A1; break; // coral
                    case 1: color = 0xF0D6A0; break; // warm cream
                    case 2: color = 0x91C9C2; break; // seafoam
                    case 3: color = 0xE8A9B8; break; // pink
                    case 4: color = 0xA8B8D8; break; // faded blue
                    case 5: color = 0xE8D9C4; break; // art-deco ivory
                    case 6: color = 0xD49B78; break; // peach
                    default: color = 0xB8C99A; break; // pale green
                }

                boolean park = (Math.abs(seed) % 13) == 0;
                if (park) {
                    addBox(chunk, 16.0f, 0.12f, 16.0f, x, 0.02f, z, 0x54865B);
                    addBox(chunk, 1.4f, 0.08f, 14.0f, x, 0.09f, z, 0xD4C5A5);
                    addBox(chunk, 14.0f, 0.08f, 1.4f, x, 0.10f, z, 0xD4C5A5);
                    addPalmTree(chunk, x - 5.0f, z - 5.0f);
                    addPalmTree(chunk, x + 5.0f, z - 5.0f);
                    addTree(chunk, x + 5.0f, z + 5.0f);
                } else {
                    // Main facade and a flat roof/parapet, classic Miami skyline.
                    addTexturedBox(chunk, width, height, depth, x, height * 0.5f, z,
                                  color, 4);
                    int trim = district == 0 ? 0xF7E7D0 : 0x786B67;
                    addBox(chunk, width + 0.28f, 0.28f, depth + 0.28f,
                           x, height + 0.14f, z, trim);

                    // Art-deco vertical fins and rooftop crown on selected towers.
                    if (district == 0 || type == 2 || type == 5) {
                        addBox(chunk, 0.28f, height + 0.45f, 0.32f,
                               x - width * 0.34f, height * 0.5f, z + depth * 0.5f + 0.17f,
                               0xF8E9D5);
                        addBox(chunk, 0.28f, height + 0.45f, 0.32f,
                               x + width * 0.34f, height * 0.5f, z + depth * 0.5f + 0.17f,
                               0xF8E9D5);
                        if (district == 0) {
                            addBox(chunk, width * 0.42f, 0.65f, depth * 0.72f,
                                   x, height + 0.58f, z, color);
                        }
                    }

                    // Ground-floor shopfronts, balconies/windows on only some
                    // blocks to create a varied street rhythm without excess meshes.
                    addBox(chunk, 1.25f, 1.55f, 0.09f, x, 0.78f,
                           z + depth * 0.5f + 0.06f, 0x493D3B);
                    if ((type & 1) == 0) {
                        addBox(chunk, 1.05f, 1.05f, 0.08f, x - 2.7f, 2.0f,
                               z + depth * 0.5f + 0.07f, 0x78AFC0);
                        addBox(chunk, 1.05f, 1.05f, 0.08f, x + 2.7f, 2.0f,
                               z + depth * 0.5f + 0.07f, 0x78AFC0);
                    }
                    if (district == 1 && type == 3) {
                        addBox(chunk, width * 0.72f, 0.22f, 0.45f,
                               x, 3.2f, z + depth * 0.5f + 0.22f, 0xF4D58A);
                    }
                }

                if (!park) {
                    buildingX[buildingCount] = x;
                    buildingZ[buildingCount] = z;
                    buildingW[buildingCount] = width;
                    buildingD[buildingCount] = depth;
                    buildingCount++;
                }
            }
        }

        debugStage = "create Leonida regions";
        createLeonidaRegions();

        debugStage = "create CJ model";
        cj = new CJModel(false);
        cj.setPosition(playerX, playerY, playerZ);
        world.addChild(cj.root);
        initializeJetpackVisual();
        secondProtagonist = new CJModel(true);
        secondProtagonist.setPosition(playerX, playerY, playerZ);
        secondProtagonist.setVisible(false);
        world.addChild(secondProtagonist.root);

        debugStage = "create player car";
        car = new CarModel();
        car.setPosition(carX, 0.0f, carZ);
        world.addChild(car.root);
        playerBike = new MotorcycleModel(0xD16A32);
        playerBike.setPosition(bikeX, 0.0f, bikeZ);
        world.addChild(playerBike.root);
        initializeMotorcycles();
        initializePolice();

        debugStage = "initialize pedestrians";
        initializePedestrians();
        debugStage = "initialize traffic";
        initializeTraffic();

        debugStage = "create camera";
        camera = new Camera();
        camera.setPerspective(62.0f, 1.0f, 0.25f, 120.0f);
        cameraTransform = new Transform();
        world.addChild(camera);
        world.setActiveCamera(camera);

        debugStage = "initial camera setup";
        updateCamera();
        debugStage = "scene ready";
    }

    private void updateCityChunks() {
        float cityDistance2 = playerX * playerX + playerZ * playerZ;
        boolean inCity = cityDistance2 < 205.0f * 205.0f;
        int cx = (int)((playerX / CITY_SPACING) + CITY_BLOCKS * 0.5f);
        int cz = (int)((playerZ / CITY_SPACING) + CITY_BLOCKS * 0.5f);
        if (cx < 0) cx = 0; else if (cx >= CITY_BLOCKS) cx = CITY_BLOCKS - 1;
        if (cz < 0) cz = 0; else if (cz >= CITY_BLOCKS) cz = CITY_BLOCKS - 1;
        activeChunkX = cx; activeChunkZ = cz;
        for (int x = 0; x < CITY_BLOCKS; x++) {
            for (int z = 0; z < CITY_BLOCKS; z++) {
                int dx = x - cx; if (dx < 0) dx = -dx;
                int dz = z - cz; if (dz < 0) dz = -dz;
                cityChunks[x][z].setRenderingEnable(inCity && dx <= 2 && dz <= 2);
            }
        }
        if (regionChunks[0] != null) {
            float[] rx = {0.0f, 20.0f, -245.0f, -350.0f, -55.0f, 175.0f};
            float[] rz = {0.0f, 390.0f, 225.0f, -45.0f, -285.0f, -445.0f};
            for (int i = 0; i < regionChunks.length; i++) {
                float dx = playerX - rx[i], dz = playerZ - rz[i];
                regionChunks[i].setRenderingEnable(dx * dx + dz * dz < 245.0f * 245.0f);
            }
        }
    }

    private void createLeonidaRegions() {
        // Ocean under the entire reduced state map.
        addBox(scenery, 1120.0f, 0.22f, 1120.0f, 0.0f, -0.42f, 0.0f, 0x397F9B);

        regionalRoads = new Group();
        scenery.addChild(regionalRoads);

        // Compact regional road network. The coordinates are a scaled,
        // gameplay-friendly interpretation of the community Leonida map.
        addTexturedBox(regionalRoads, 9.0f, 0.04f, 270.0f,
                       18.0f, -0.015f, 330.0f, 0xFFFFFF, 1); // Vice City to Keys
        addTexturedBox(regionalRoads, 185.0f, 0.04f, 8.0f,
                       -245.0f, -0.015f, -45.0f, 0xFFFFFF, 1); // Port Gellhorn
        addTexturedBox(regionalRoads, 8.0f, 0.04f, 160.0f,
                       -245.0f, -0.015f, 135.0f, 0xFFFFFF, 1); // Port to Grassrivers
        addTexturedBox(regionalRoads, 9.0f, 0.04f, 175.0f,
                       -55.0f, -0.015f, -205.0f, 0xFFFFFF, 1); // Vice to Ambrosia
        addTexturedBox(regionalRoads, 8.0f, 0.04f, 150.0f,
                       70.0f, -0.015f, -365.0f, 0xFFFFFF, 1); // Ambrosia to Kalaga
        addTexturedBox(regionalRoads, 170.0f, 0.04f, 8.0f,
                       -165.0f, -0.015f, 205.0f, 0xFFFFFF, 1); // Grassrivers access
        addTexturedBox(regionalRoads, 8.0f, 0.04f, 115.0f,
                       -80.0f, -0.015f, 145.0f, 0xFFFFFF, 1); // City southwest connector
        addTexturedBox(regionalRoads, 150.0f, 0.04f, 8.0f,
                       100.0f, -0.015f, -300.0f, 0xFFFFFF, 1);

        // Vice City: existing dense downtown and Art Deco coast remain the
        // central/eastern urban anchor of Leonida.
        regionChunks[0] = new Group();
        scenery.addChild(regionChunks[0]);
        addBox(regionChunks[0], 45.0f, 0.12f, 100.0f, 151.0f, -0.02f, -5.0f, 0xD9C6A0);
        for (int i = 0; i < 3; i++) addPalmTree(regionChunks[0], 137.0f + i * 5.0f, -42.0f + i * 18.0f);
        addBox(regionChunks[0], 3.0f, 7.0f, 2.0f, 164.0f, 3.5f, -24.0f, 0xE9D7B9);
        addBox(regionChunks[0], 3.0f, 9.0f, 2.0f, 164.0f, 4.5f, 3.0f, 0xE9D7B9);
        addBox(regionChunks[0], 3.0f, 6.0f, 2.0f, 164.0f, 3.0f, 29.0f, 0xE9D7B9);

        // Leonida Keys: a thin chain of small islands linked by causeways.
        regionChunks[1] = new Group(); scenery.addChild(regionChunks[1]);
        for (int i = 0; i < 4; i++) {
            float x = 12.0f + (i % 2) * 18.0f;
            float z = 330.0f + i * 32.0f;
            addBox(regionChunks[1], 34.0f, 0.28f, 21.0f, x, -0.20f, z, 0xCBBE91);
            addBox(regionChunks[1], 7.0f, 0.05f, 23.0f, x, -0.02f, z - 12.0f, 0x777C78);
            if ((i & 1) == 0) addPalmTree(regionChunks[1], x - 8.0f, z - 2.0f);
            if (i == 1 || i == 4) {
                addBox(regionChunks[1], 12.0f, 2.5f, 8.0f, x, 1.25f, z, 0xE9D1A4);
                addBox(regionChunks[1], 13.0f, 0.35f, 9.0f, x, 2.65f, z, 0xA45C42);
            }
        }
        addBox(regionChunks[1], 4.0f, 0.12f, 120.0f, 20.0f, 0.03f, 390.0f, 0x686D70);

        // Grassrivers: wetlands, shallow channels, mangroves and stilt huts.
        regionChunks[2] = new Group(); scenery.addChild(regionChunks[2]);
        addBox(regionChunks[2], 170.0f, 0.18f, 130.0f, -245.0f, -0.24f, 225.0f, 0x617F54);
        addBox(regionChunks[2], 28.0f, 0.10f, 145.0f, -270.0f, -0.05f, 225.0f, 0x397F9B);
        addBox(regionChunks[2], 18.0f, 0.10f, 95.0f, -205.0f, -0.04f, 225.0f, 0x397F9B);
        for (int i = 0; i < 4; i++) {
            float x = -310.0f + (i % 2) * 90.0f;
            float z = 185.0f + (i / 2) * 75.0f;
            addTree(regionChunks[2], x, z);
        }
        for (int i = 0; i < 2; i++) {
            float x = -300.0f + i * 72.0f;
            addBox(regionChunks[2], 9.0f, 2.0f, 8.0f, x, 1.25f, 260.0f, 0xA98B65);
            addBox(regionChunks[2], 10.0f, 0.35f, 9.0f, x, 2.4f, 260.0f, 0x76513C);
            addBox(regionChunks[2], 0.35f, 1.8f, 0.35f, x - 3.5f, 0.8f, 256.5f, 0x735A3D);
            addBox(regionChunks[2], 0.35f, 1.8f, 0.35f, x + 3.5f, 0.8f, 256.5f, 0x735A3D);
        }

        // Port Gellhorn: faded Gulf coast resort, motel blocks and marina.
        regionChunks[3] = new Group(); scenery.addChild(regionChunks[3]);
        addBox(regionChunks[3], 135.0f, 0.16f, 100.0f, -350.0f, -0.22f, -45.0f, 0xCBBE91);
        for (int i = 0; i < 3; i++) {
            float x = -385.0f + i * 32.0f;
            addBox(regionChunks[3], 20.0f, 5.0f, 13.0f, x, 2.5f, -48.0f, 0xD4B18B);
            addBox(regionChunks[3], 21.0f, 0.35f, 14.0f, x, 5.1f, -48.0f, 0x9C5E4B);
            addBox(regionChunks[3], 4.0f, 2.0f, 0.12f, x, 2.0f, -41.4f, 0x83AFC0);
        }
        addBox(regionChunks[3], 42.0f, 0.16f, 8.0f, -360.0f, 0.02f, 18.0f, 0x795B43);
        for (int i = 0; i < 3; i++)
            addBox(regionChunks[3], 1.0f, 0.10f, 20.0f, -375.0f + i * 14.0f, 0.10f, 31.0f, 0x795B43);

        // Ambrosia: central sugar country, flat fields and the refinery.
        regionChunks[4] = new Group(); scenery.addChild(regionChunks[4]);
        addBox(regionChunks[4], 145.0f, 0.16f, 110.0f, -55.0f, -0.22f, -285.0f, 0x9EAC68);
        for (int i = 0; i < 3; i++)
            addBox(regionChunks[4], 22.0f, 0.05f, 78.0f, -90.0f + i * 36.0f, -0.10f, -285.0f, 0xB6B77A);
        addBox(regionChunks[4], 34.0f, 13.0f, 27.0f, -20.0f, 6.5f, -285.0f, 0xA5A39A);
        addBox(regionChunks[4], 10.0f, 22.0f, 10.0f, -36.0f, 11.0f, -285.0f, 0x888D8A);
        addBox(regionChunks[4], 10.0f, 18.0f, 10.0f, -4.0f, 9.0f, -285.0f, 0x888D8A);
        addBox(regionChunks[4], 3.0f, 28.0f, 3.0f, -20.0f, 14.0f, -300.0f, 0x777F80);
        addBox(regionChunks[4], 3.0f, 24.0f, 3.0f, -7.0f, 12.0f, -300.0f, 0x777F80);
        for (int i = 0; i < 3; i++)
            addBox(regionChunks[4], 0.6f, 0.6f, 30.0f, 5.0f + i * 5.0f, 8.0f, -285.0f, 0xB7B8B2);

        // Mount Kalaga: northern wooded highlands, ridges and pine silhouettes.
        regionChunks[5] = new Group(); scenery.addChild(regionChunks[5]);
        addBox(regionChunks[5], 150.0f, 0.22f, 125.0f, 175.0f, -0.18f, -445.0f, 0x607C4B);
        addBox(regionChunks[5], 65.0f, 8.0f, 45.0f, 150.0f, 3.8f, -465.0f, 0x71865B);
        addBox(regionChunks[5], 42.0f, 12.0f, 35.0f, 205.0f, 5.8f, -420.0f, 0x667D53);
        for (int i = 0; i < 4; i++) {
            float x = 120.0f + (i % 2) * 48.0f;
            float z = -485.0f + (i / 2) * 42.0f;
            addTree(regionChunks[5], x, z);
        }
    }

    private void addPalmTree(Group parent, float x, float z) {
        addBox(parent, 0.34f, 3.6f, 0.34f, x, 1.8f, z, 0x8B6240);
        // Four broad fronds form a readable low-poly palm silhouette.
        addBox(parent, 2.8f, 0.16f, 0.42f, x, 3.55f, z, 0x397B4D);
        addBox(parent, 0.42f, 0.16f, 2.8f, x, 3.55f, z, 0x438653);
        addBox(parent, 2.1f, 0.14f, 0.34f, x, 3.25f, z, 0x4A8A50);
        addBox(parent, 0.34f, 0.14f, 2.1f, x, 3.25f, z, 0x4A8A50);
    }

    private void addTree(Group parent, float x, float z) {
        addBox(parent, 0.42f, 2.1f, 0.42f, x, 1.05f, z, 0x68462E);
        addBox(parent, 2.0f, 1.7f, 2.0f, x, 2.35f, z, 0x397044);
        addBox(parent, 1.35f, 1.25f, 1.35f, x, 3.05f, z, 0x4D8650);
    }

    private void addBuilding(float x, float z, float width, float depth, int color) {
        addBox(scenery, width, 3.2f, depth, x, 1.6f, z, color);
        addBox(scenery, width + 0.18f, 0.22f, depth + 0.18f,
               x, 3.28f, z, 0x665044);
        addBox(scenery, 0.85f, 1.25f, 0.08f, x, 0.64f, z + depth * 0.5f + 0.045f, 0x49372D);
        addBox(scenery, 0.75f, 0.75f, 0.07f, x - 1.0f, 1.65f,
               z + depth * 0.5f + 0.05f, 0x6C9BAA);
        addBox(scenery, 0.75f, 0.75f, 0.07f, x + 1.0f, 1.65f,
               z + depth * 0.5f + 0.05f, 0x6C9BAA);
    }

    private void addTexturedBox(Group parent, float w, float h, float d,
                                  float x, float y, float z, int color, int textureKind) {
        debugStage = "create procedural texture mesh";
        javax.microedition.m3g.Mesh mesh =
            ModelFactory.texturedBox(w, h, d, color, textureKind);
        mesh.setTranslation(x, y, z);
        parent.addChild(mesh);
    }

    private void addBox(Group parent, float w, float h, float d,
                        float x, float y, float z, int color) {
        debugStage = "create box mesh";
        javax.microedition.m3g.Mesh mesh = ModelFactory.box(w, h, d, color);
        mesh.setTranslation(x, y, z);
        parent.addChild(mesh);
    }

    synchronized void start() {
        if (!initialized) return;
        if (running) {
            paused = false;
            return;
        }
        paused = false;
        running = true;
        loop = new Thread(this);
        loop.start();
    }

    synchronized void pause() {
        paused = true;
    }

    synchronized void stop() {
        running = false;
        if (loop != null) {
            try { loop.join(); } catch (InterruptedException ignored) {}
            loop = null;
        }
    }

    public void run() {
        while (running) {
            if (!paused && fatalError == null) {
                try {
                    debugStage = "game update";
                    update(0.04f);
                    repaint();
                } catch (Throwable error) {
                    fatalError = error.toString();
                    debugStage = "update: " + debugStage;
                    diagnosticPage = 0;
                    running = false;
                    repaint();
                }
            }
            try { Thread.sleep(40); } catch (InterruptedException ignored) {}
        }
    }

    private void update(float dt) {
        debugStage = "camera input";
        updateCameraInput(dt);
        if (characterSwitching) { updateCharacterSwitch(dt); updateCamera(); return; }
        debugStage = "city chunk visibility";
        updateCityChunks();
        if (carTransition == 3) {
            debugStage = "traffic hijack";
            updateTrafficHijack(dt); updateCamera(); return;
        }
        if (carTransition != 0) {
            debugStage = "car transition";
            updateCarTransition(dt); updateCamera(); return;
        }
        boolean moving = false;
        if (playerHitStun > 0.0f) playerHitStun -= dt;
        if (playerCollisionCooldown > 0.0f) playerCollisionCooldown -= dt;

        if (inCar) {
            if ((keys & FORWARD) != 0) carSpeed += 3.2f * dt;
            if ((keys & BACK) != 0) carSpeed -= 3.0f * dt;
            float steeringInput = ((keys & LEFT) != 0 ? 1.0f : 0.0f) -
                                  ((keys & RIGHT) != 0 ? 1.0f : 0.0f);
            float steeringTarget = steeringInput * 28.0f;
            carSteering += (steeringTarget - carSteering) * Math.min(1.0f, 7.0f * dt);
            carSpeed *= 0.985f;
            if (carSpeed > 8.0f) carSpeed = 8.0f;
            if (carSpeed < -3.0f) carSpeed = -3.0f;
            // The car model's front points along local -Z. Under M3G's
            // positive Y rotation that forward vector turns toward world -X,
            // so left steering must produce positive yaw.
            float steeringRad = carSteering * 0.0174532925f;
            float yawRate = (float)Math.tan(steeringRad) * carSpeed / 2.45f;
            carHeading = normalizeAngle(carHeading + yawRate * 57.29578f * dt);
            float radians = carHeading * 0.0174532925f;
            // Keep translation aligned with the model's rotated local -Z.
            float moveX = -(float)Math.sin(radians) * carSpeed * dt;
            float moveZ = -(float)Math.cos(radians) * carSpeed * dt;
            float nextX = carX + moveX;
            float nextZ = carZ + moveZ;
            boolean hitX = carBlocked(nextX, carZ);
            boolean hitZ = carBlocked(carX, nextZ);

            if (!hitX) carX = nextX;
            if (!hitZ) carZ = nextZ;
            if (hitX || hitZ) {
                // A low-speed bump settles the car; a fast impact produces
                // a short, damped rebound instead of pinning it to the wall.
                float impactSpeed = Math.abs(carSpeed);
                float rebound = impactSpeed > 5.0f ? 0.34f :
                                (impactSpeed > 2.2f ? 0.20f :
                                (impactSpeed > 0.15f ? 0.18f : 0.0f));
                carSpeed = -carSpeed * rebound;
                carSteering *= 0.45f;
                if (hitX && hitZ) {
                    // A head-on impact loses more energy than a glancing one.
                    carSpeed *= 0.72f;
                }
            }
            car.setPosition(carX, 0.0f, carZ);
            car.setHeading(carHeading);
            playerX = carX;
            playerZ = carZ;
            playerHeading = carHeading;
            playerVelocityX = 0.0f;
            playerVelocityZ = 0.0f;
        } else if (inBike) {
            if ((keys & FORWARD) != 0) bikeSpeed += 4.0f * dt;
            if ((keys & BACK) != 0) bikeSpeed -= 3.4f * dt;
            float steer = ((keys & LEFT) != 0 ? 1.0f : 0.0f) -
                          ((keys & RIGHT) != 0 ? 1.0f : 0.0f);
            bikeSteering += (steer * 32.0f - bikeSteering) * Math.min(1.0f, 6.0f * dt);
            bikeSpeed *= 0.989f;
            if (bikeSpeed > 14.0f) bikeSpeed = 14.0f;
            if (bikeSpeed < -3.0f) bikeSpeed = -3.0f;
            bikeHeading = normalizeAngle(bikeHeading +
                (float)Math.tan(bikeSteering * 0.0174532925f) *
                bikeSpeed / 1.65f * 57.29578f * dt);
            float ba = bikeHeading * 0.0174532925f;
            float nx = bikeX - (float)Math.sin(ba) * bikeSpeed * dt;
            float nz = bikeZ - (float)Math.cos(ba) * bikeSpeed * dt;
            boolean bx = vehicleBlocked(nx, bikeZ, 0.82f);
            boolean bz = vehicleBlocked(bikeX, nz, 0.82f);
            if (!bx) bikeX = nx;
            if (!bz) bikeZ = nz;
            if (bx || bz) bikeSpeed *= -0.18f;
            playerBike.setPosition(bikeX, 0.0f, bikeZ);
            playerBike.setHeading(bikeHeading);
            float leanTarget = -bikeSteering * Math.min(1.0f, Math.abs(bikeSpeed) / 5.0f) * 0.48f;
            playerBike.setLean(leanTarget);
            float seatAngle = bikeHeading * 0.0174532925f;
            float seatX = bikeX + (float)Math.sin(seatAngle) * 0.18f;
            float seatZ = bikeZ - (float)Math.cos(seatAngle) * 0.18f;
            if (bikeMountTimer > 0.0f) {
                bikeMountTimer -= dt;
                if (bikeMountTimer < 0.0f) bikeMountTimer = 0.0f;
                float t = 1.0f - bikeMountTimer / 0.62f;
                playerX = bikeMountStartX + (seatX - bikeMountStartX) * t;
                playerZ = bikeMountStartZ + (seatZ - bikeMountStartZ) * t;
            } else {
                playerX = seatX; playerZ = seatZ;
            }
            playerY = 0.12f;
            playerHeading = bikeHeading;
            playerVelocityX = playerVelocityZ = 0.0f;
        } else if (playerRagdollTime > 0.0f) {
            updatePlayerRagdoll(dt);
            moving = false;
        } else {
            float forward = 0.0f;
            float side = 0.0f;
            if ((keys & FORWARD) != 0) forward += 1.0f;
            if ((keys & BACK) != 0) forward -= 1.0f;
            if ((keys & RIGHT) != 0) side += 1.0f;
            if ((keys & LEFT) != 0) side -= 1.0f;

            // 4/6 turn CJ. Holding 8 makes him turn around and run in the
            // direction he is now facing, rather than moonwalking backwards.
            if (side != 0.0f) {
                playerHeading = normalizeAngle(playerHeading - side * 150.0f * dt);
            }
            if ((keys & BACK) != 0 && !backwardFacingActive && !reverseFacing) {
                // Pressing 8 from a forward stance turns CJ around once.
                reverseFacing = true;
                backwardFacingActive = true;
                reverseTargetHeading = normalizeAngle(playerHeading + 180.0f);
            } else if ((keys & FORWARD) != 0 && backwardFacingActive && !reverseFacing) {
                // Pressing 2 while CJ is in the backward-facing stance makes
                // him turn back toward his original forward direction.
                reverseFacing = true;
                backwardFacingActive = false;
                reverseTargetHeading = normalizeAngle(playerHeading + 180.0f);
            }
            if (reverseFacing) {
                float difference = normalizeAngle(reverseTargetHeading - playerHeading);
                float maxTurn = 300.0f * dt;
                if (difference > maxTurn) difference = maxTurn;
                else if (difference < -maxTurn) difference = -maxTurn;
                if (Math.abs(difference) <= maxTurn) {
                    playerHeading = normalizeAngle(reverseTargetHeading);
                    reverseFacing = false;
                } else {
                    playerHeading = normalizeAngle(playerHeading + difference);
                }
            }

            float inputLength = (float)Math.sqrt(forward * forward + side * side);
            if (inputLength > 1.0f) inputLength = 1.0f;

            float angle = playerHeading * 0.0174532925f;
            float targetSpeed = ((keys & BACK) != 0 || (keys & FORWARD) != 0) ?
                                (((keys & SPRINT) != 0) ? 5.2f : 3.0f) : 0.0f;
            // M3G's positive Y rotation turns the model's local -Z forward
            // axis toward -X. Keep movement in that exact same local-forward
            // direction, otherwise CJ appears to face one way and walk another.
            float targetVX = -(float)Math.sin(angle) * targetSpeed;
            float targetVZ = -(float)Math.cos(angle) * targetSpeed;

            // Gradual acceleration and deceleration.
            float response = inputLength > 0.01f ? 12.0f : 15.0f;
            float blend = response * dt;
            if (blend > 1.0f) blend = 1.0f;
            playerVelocityX += (targetVX - playerVelocityX) * blend;
            playerVelocityZ += (targetVZ - playerVelocityZ) * blend;

            float nextPlayerX = playerX + playerVelocityX * dt;
            float nextPlayerZ = playerZ + playerVelocityZ * dt;
            if (!playerBlocked(nextPlayerX, playerZ)) playerX = nextPlayerX;
            else playerVelocityX = 0.0f;
            if (!playerBlocked(playerX, nextPlayerZ)) playerZ = nextPlayerZ;
            else playerVelocityZ = 0.0f;

            float speed = (float)Math.sqrt(playerVelocityX * playerVelocityX +
                                           playerVelocityZ * playerVelocityZ);
            moving = speed > 0.12f;
            if (moving || side != 0.0f) {
                walkPhase += dt * (7.0f + speed * 2.0f + (side != 0.0f ? 2.0f : 0.0f));
            }

            if (inputLength < 0.01f && speed < 0.035f) {
                playerVelocityX = 0.0f;
                playerVelocityZ = 0.0f;
            }

            if (jetpackOwned && !inBike && !inCar && playerRagdollTime <= 0.0f) {
                if (jetpackThrust) {
                    playerY += 7.0f * dt;
                    if (playerY > 18.0f) playerY = 18.0f;
                    jumpVelocity = 0.0f;
                } else if ((keys & CROUCH) != 0) {
                    playerY -= 5.0f * dt;
                    if (playerY < 0.0f) playerY = 0.0f;
                    jumpVelocity = 0.0f;
                } else if (playerY > 0.0f) {
                    playerY -= 1.35f * dt;
                    if (playerY < 0.0f) playerY = 0.0f;
                    jumpVelocity = 0.0f;
                }
            } else if (jumpVelocity != 0.0f || playerY > 0.0f) {
                playerY += jumpVelocity * dt;
                jumpVelocity -= 9.8f * dt;
                if (playerY <= 0.0f) {
                    playerY = 0.0f;
                    jumpVelocity = 0.0f;
                }
            }
        }

        debugStage = "traffic update";
        updateTraffic(dt);
        updateMotorcycles(dt);
        updatePolice(dt);
        debugStage = "pedestrian update";
        updatePedestrians(dt);
        if (punchTimer > 0.0f) punchTimer -= dt;
        if (punchTimer < 0.0f) punchTimer = 0.0f;
        float punchAngle = punchTimer > 0.0f ? (float)Math.sin((0.35f - punchTimer) * 18.0f) * 75.0f : 0.0f;

        activeCharacter().setPosition(playerX, playerY, playerZ);
        activeCharacter().setHeading(playerHeading);
        updateJetpackVisual(dt);
        boolean crouching = inBike || (!inCar && (keys & CROUCH) != 0);
        boolean jumping = !inCar && (playerY > 0.001f || jumpVelocity > 0.0f);
        float turnAmount = !inCar ? (((keys & RIGHT) != 0 ? 1.0f : 0.0f) -
                                    ((keys & LEFT) != 0 ? 1.0f : 0.0f)) : 0.0f;
        if (playerRagdollTime > 0.0f) {
            activeCharacter().animateRagdoll(dt, playerRagdollRoll, playerRagdollPitch,
                              playerRagdollRollSpeed, playerRagdollPitchSpeed);
        } else {
            activeCharacter().animate(walkPhase, moving && !inCar, punchAngle,
                       false, jumping, crouching, turnAmount);
        }
        debugStage = "camera update";
        updateCamera();
    }

    private void updateCameraInput(float dt) {
        int held = cameraKeys;
        if ((held & CAM_LEFT) != 0) cameraYaw -= 115.0f * dt;
        if ((held & CAM_RIGHT) != 0) cameraYaw += 115.0f * dt;
        if ((held & CAM_UP) != 0) cameraPitch -= 75.0f * dt;
        if ((held & CAM_DOWN) != 0) cameraPitch += 75.0f * dt;

        if (cameraYaw > 180.0f) cameraYaw -= 360.0f;
        if (cameraYaw < -180.0f) cameraYaw += 360.0f;
        if (cameraPitch < -5.0f) cameraPitch = -5.0f;
        if (cameraPitch > 35.0f) cameraPitch = 35.0f;
    }

    // Java ME compatibility: atan2 approximation using only basic arithmetic.
    // Returns radians in the range [-PI, PI], without Math.atan/atan2.
    private double atan2Compat(double y, double x) {
        if (x == 0.0) {
            if (y > 0.0) return 1.5707963267948966;
            if (y < 0.0) return -1.5707963267948966;
            return 0.0;
        }

        double ay = y < 0.0 ? -y : y;
        double ax = x < 0.0 ? -x : x;
        double angle;
        if (ax >= ay) {
            double r = ay / ax;
            double r2 = r * r;
            // Minimax-style polynomial approximation for atan(r), 0..1.
            angle = r * (0.9998660 + r2 * (-0.3302995 +
                    r2 * (0.1801410 + r2 * (-0.0851330 + r2 * 0.0208351))));
        } else {
            double r = ax / ay;
            double r2 = r * r;
            double small = r * (0.9998660 + r2 * (-0.3302995 +
                    r2 * (0.1801410 + r2 * (-0.0851330 + r2 * 0.0208351))));
            angle = 1.5707963267948966 - small;
        }

        if (x < 0.0) angle = 3.141592653589793 - angle;
        if (y < 0.0) angle = -angle;
        return angle;
    }

    private float normalizeAngle(float angle) {
        while (angle > 180.0f) angle -= 360.0f;
        while (angle < -180.0f) angle += 360.0f;
        return angle;
    }


    private CJModel activeCharacter() {
        return luciaActive ? secondProtagonist : cj;
    }

    private void beginCharacterSwitch() {
        if (characterSwitching || inCar || inBike || carTransition != 0 || playerRagdollTime > 0.0f) return;
        characterSwitching = true;
        characterSwitchTime = 0.0f;
        switchApplied = false;
        switchFromX = playerX;
        switchFromZ = playerZ;
        switchToX = playerX > 0.0f ? -84.0f : 84.0f;
        switchToZ = playerZ > 0.0f ? -84.0f : 84.0f;
        status = "SWITCHING PROTAGONIST...";
        keys = 0;
        cameraKeys = 0;
    }

    private void updateCharacterSwitch(float dt) {
        characterSwitchTime += dt;
        if (characterSwitchTime >= 1.4f && !switchApplied) {
            switchApplied = true;
            luciaActive = !luciaActive;
            cj.setVisible(!luciaActive);
            secondProtagonist.setVisible(luciaActive);
            playerX = switchToX;
            playerZ = switchToZ;
            playerY = 0.0f;
            playerHeading = 0.0f;
            playerVelocityX = 0.0f;
            playerVelocityZ = 0.0f;
            jumpVelocity = 0.0f;
            if (luciaActive) {
                secondProtagonist.setPosition(playerX, playerY, playerZ);
                secondProtagonist.setHeading(playerHeading);
            } else {
                cj.setPosition(playerX, playerY, playerZ);
                cj.setHeading(playerHeading);
            }
            updateCityChunks();
        }
        if (characterSwitchTime >= 2.8f) {
            characterSwitchTime = 2.8f;
            characterSwitching = false;
            status = luciaActive ? "LUCIA" : "JASON";
        }
    }

    private void updateCamera() {
        float radians = cameraYaw * 0.0174532925f;
        float distance = inCar ? 6.5f : 5.2f;
        float eyeX, eyeY, eyeZ;
        if (characterSwitching) {
            float t = characterSwitchTime / 2.8f;
            float focusX = switchFromX + (switchToX - switchFromX) * t;
            float focusZ = switchFromZ + (switchToZ - switchFromZ) * t;
            float altitude = 3.0f + 82.0f * (float)Math.sin(t * 3.1415926f);
            eyeX = focusX;
            eyeZ = focusZ;
            eyeY = altitude;
            cameraTransform.setIdentity();
            cameraTransform.postRotate(-89.0f, 1.0f, 0.0f, 0.0f);
        } else {
            eyeX = playerX + (float)Math.sin(radians) * distance;
            eyeZ = playerZ + (float)Math.cos(radians) * distance;
            eyeY = playerY + 2.3f + cameraPitch * 0.035f;
            cameraTransform.setIdentity();
            cameraTransform.postRotate(cameraYaw, 0.0f, 1.0f, 0.0f);
            cameraTransform.postRotate(-cameraPitch, 1.0f, 0.0f, 0.0f);
        }
        camera.setTransform(cameraTransform);
        camera.setTranslation(eyeX, eyeY, eyeZ);
        float w = getWidth();
        float h = getHeight();
        if (w > 0 && h > 0) camera.setPerspective(62.0f, w / h, 0.25f, 160.0f);
    }

    private boolean handleJetpackCheatKey(int keyCode) {
        int digit = -1;
        if (keyCode >= '0' && keyCode <= '9') digit = keyCode - '0';
        long now = System.currentTimeMillis();
        if (cheatProgress > 0 && now - cheatLastKeyTime > 3500L) cheatProgress = 0;
        if (cheatProgress == 0 && digit != 3) return false;
        if (digit == JETPACK_CHEAT[cheatProgress]) {
            cheatProgress++;
            cheatLastKeyTime = now;
            if (cheatProgress >= JETPACK_CHEAT.length) {
                cheatProgress = 0;
                jetpackOwned = true;
                jetpackThrust = false;
                status = "JETPACK UNLOCKED!  5 FLY  9 DESCEND";
            }
            return true;
        }
        cheatProgress = digit == 3 ? 1 : 0;
        cheatLastKeyTime = now;
        return digit == 3;
    }

    private void initializeJetpackVisual() {
        jetpackVisual = new Group();
        Group pack = new Group();
        pack.addChild(ModelFactory.box(0.72f, 0.92f, 0.28f, 0x343A40));
        Mesh tankLeft = ModelFactory.box(0.22f, 0.82f, 0.32f, 0xAEB8BC);
        tankLeft.setTranslation(-0.27f, 0.02f, 0.0f);
        pack.addChild(tankLeft);
        Mesh tankRight = ModelFactory.box(0.22f, 0.82f, 0.32f, 0xAEB8BC);
        tankRight.setTranslation(0.27f, 0.02f, 0.0f);
        pack.addChild(tankRight);
        Mesh strap = ModelFactory.box(0.78f, 0.12f, 0.12f, 0x20252A);
        strap.setTranslation(0.0f, 0.25f, 0.18f);
        pack.addChild(strap);
        jetpackVisual.addChild(pack);
        jetpackFlames = new Group();
        Mesh flameL = ModelFactory.box(0.13f, 0.52f, 0.13f, 0x39BDE8);
        flameL.setTranslation(-0.25f, -0.68f, 0.0f);
        jetpackFlames.addChild(flameL);
        Mesh flameR = ModelFactory.box(0.13f, 0.52f, 0.13f, 0x39BDE8);
        flameR.setTranslation(0.25f, -0.68f, 0.0f);
        jetpackFlames.addChild(flameR);
        jetpackVisual.addChild(jetpackFlames);
        jetpackVisual.setRenderingEnable(false);
        world.addChild(jetpackVisual);
    }

    private void updateJetpackVisual(float dt) {
        if (jetpackVisual == null) return;
        boolean visible = jetpackOwned && !inCar && !inBike &&
                          playerRagdollTime <= 0.0f;
        jetpackVisual.setRenderingEnable(visible);
        if (!visible) return;
        jetpackVisual.setTranslation(playerX, playerY + 1.0f, playerZ);
        Transform jetTransform = new Transform();
        jetTransform.setIdentity();
        jetTransform.postRotate(playerHeading, 0.0f, 1.0f, 0.0f);
        jetpackVisual.setTransform(jetTransform);
        jetpackFlamePhase += dt;
        jetpackFlames.setRenderingEnable(jetpackThrust);
    }

    private void jump() {
        if (!inCar && playerY <= 0.001f && jumpVelocity == 0.0f) {
            jumpVelocity = 5.0f;
        }
    }

    private void punch() {
        if (!inCar) {
            punchTimer = 0.35f;
            hitPedestrian();
        }
    }

    private void toggleCar() {
        if (carTransition != 0) return;
        if (inBike) {
            inBike = false;
            bikeSpeed = 0.0f;
            playerBike.setVisible(bikeSourceIndex < 0);
            if (bikeSourceIndex >= 0) {
                bikeTrafficX[bikeSourceIndex] = bikeX;
                bikeTrafficZ[bikeSourceIndex] = bikeZ;
                bikeTrafficHeading[bikeSourceIndex] = bikeHeading;
                trafficBikes[bikeSourceIndex].setPosition(bikeX, 0.0f, bikeZ);
                trafficBikes[bikeSourceIndex].setHeading(bikeHeading);
                bikeTrafficActive[bikeSourceIndex] = true;
                trafficBikes[bikeSourceIndex].setVisible(true);
            }
            bikeSourceIndex = -1;
            float exitAngle = bikeHeading * 0.0174532925f;
            playerX = bikeX + (float)Math.cos(exitAngle);
            playerZ = bikeZ - (float)Math.sin(exitAngle);
            playerY = 0.0f;
            playerHeading = bikeHeading;
            bikeMountTimer = 0.0f;
            activeCharacter().setVisible(true);
            activeCharacter().setPosition(playerX, playerY, playerZ);
            status = "EXITED MOTORCYCLE";
            return;
        }
        int nearbyBike = findNearbyMotorcycle();
        if (!inCar && nearbyBike != -2) {
            if (nearbyBike >= 0) {
                addWanted(1);
                bikeSourceIndex = nearbyBike;
                bikeX = bikeTrafficX[nearbyBike];
                bikeZ = bikeTrafficZ[nearbyBike];
                bikeHeading = bikeTrafficHeading[nearbyBike];
                bikeTrafficActive[nearbyBike] = false;
                trafficBikes[nearbyBike].setVisible(false);
            } else {
                bikeSourceIndex = -1;
            }
            inBike = true;
            bikeSpeed = 0.0f;
            bikeMountTimer = 0.62f;
            bikeMountStartX = playerX;
            bikeMountStartZ = playerZ;
            playerBike.setVisible(true);
            playerBike.setPosition(bikeX, 0.0f, bikeZ);
            playerBike.setHeading(bikeHeading);
            playerBike.setLean(0.0f);
            playerX = bikeMountStartX; playerZ = bikeMountStartZ;
            playerHeading = bikeHeading;
            activeCharacter().setVisible(true);
            status = "GETTING ON MOTORCYCLE...";
            return;
        }
        if (inCar) {
            beginCarTransition(false);
            return;
        }
        int stoppedCar = findStoppableTrafficCar();
        if (stoppedCar >= 0) {
            beginTrafficHijack(stoppedCar);
            return;
        }
        float dx = playerX - carX;
        float dz = playerZ - carZ;
        if (dx * dx + dz * dz < 12.0f) beginCarTransition(true);
        else status = "GET CLOSER TO THE CAR";
    }

    private void beginCarTransition(boolean entering) {
        carTransition = entering ? 1 : 2;
        carTransitionTime = 0.0f;
        carSpeed = 0.0f;
        playerVelocityX = 0.0f;
        playerVelocityZ = 0.0f;

        // The driver seat and the standing point are expressed in car-local
        // coordinates, then transformed by the sedan's current heading.
        if (entering) {
            float headingRad = carHeading * 0.0174532925f;
            float relX = playerX - carX;
            float relZ = playerZ - carZ;
            float localSideX = relX * (float)Math.cos(headingRad) -
                               relZ * (float)Math.sin(headingRad);
            transitionDriverSide = localSideX <= 0.0f;
        }
        float sideSign = transitionDriverSide ? -1.0f : 1.0f;
        float seatX = sideSign * 0.32f;
        float seatZ = -0.28f;
        float outsideX = sideSign * 1.28f;
        float outsideZ = -0.25f;
        if (entering) {
            transitionStartX = playerX;
            transitionStartZ = playerZ;
            worldPoint(outsideX, outsideZ, true);
            transitionStartX = playerX;
            transitionStartZ = playerZ;
            worldPoint(seatX, seatZ, false);
            transitionEndX = playerX;
            transitionEndZ = playerZ;
            playerX = transitionStartX;
            playerZ = transitionStartZ;
            playerHeading = normalizeAngle(carHeading + 90.0f);
            status = "ENTERING VEHICLE...";
        } else {
            worldPoint(seatX, seatZ, false);
            transitionStartX = playerX;
            transitionStartZ = playerZ;
            worldPoint(outsideX, outsideZ, true);
            transitionEndX = playerX;
            transitionEndZ = playerZ;
            playerX = transitionStartX;
            playerZ = transitionStartZ;
            playerHeading = normalizeAngle(carHeading + 90.0f);
            status = "EXITING VEHICLE...";
        }
        playerY = 0.0f;
        activeCharacter().setPosition(playerX, playerY, playerZ);
        activeCharacter().setHeading(playerHeading);
        activeCharacter().setVisible(true);
        activeCharacter().setEntryPose(entering ? 0.0f : 1.0f, entering);
        car.setDoorOpen(0.0f, transitionDriverSide);
    }

    /** Convert a car-local X/Z point to world space and store it in playerX/Z. */
    private void worldPoint(float localX, float localZ, boolean outside) {
        float a = carHeading * 0.0174532925f;
        playerX = carX + localX * (float)Math.cos(a) + localZ * (float)Math.sin(a);
        playerZ = carZ - localX * (float)Math.sin(a) + localZ * (float)Math.cos(a);
    }

    private void updateCarTransition(float dt) {
        carTransitionTime += dt;
        float t = carTransitionTime / 1.45f;
        if (t > 1.0f) t = 1.0f;
        float smooth = t * t * (3.0f - 2.0f * t);

        // Door opens first, CJ moves through the doorway, then the door closes.
        float door;
        if (t < 0.22f) door = smooth / (0.22f * 0.22f * (3.0f - 2.0f * 0.22f));
        else if (t < 0.78f) door = 1.0f;
        else {
            float close = (t - 0.78f) / 0.22f;
            if (close > 1.0f) close = 1.0f;
            door = 1.0f - close * close * (3.0f - 2.0f * close);
        }
        if (door < 0.0f) door = 0.0f;
        if (door > 1.0f) door = 1.0f;
        car.setDoorOpen(72.0f * door, transitionDriverSide);

        float moveT = (t - 0.18f) / 0.64f;
        if (moveT < 0.0f) moveT = 0.0f;
        if (moveT > 1.0f) moveT = 1.0f;
        float moveSmooth = moveT * moveT * (3.0f - 2.0f * moveT);
        playerX = transitionStartX + (transitionEndX - transitionStartX) * moveSmooth;
        playerZ = transitionStartZ + (transitionEndZ - transitionStartZ) * moveSmooth;
        activeCharacter().setPosition(playerX, playerY, playerZ);
        activeCharacter().setHeading(normalizeAngle(carHeading + 90.0f));
        float pose = carTransition == 1 ? moveSmooth : 1.0f - moveSmooth;
        activeCharacter().setEntryPose(pose, carTransition == 1);

        if (t >= 1.0f) {
            car.setDoorOpen(0.0f, transitionDriverSide);
            activeCharacter().setEntryPose(0.0f, carTransition == 1);
            if (carTransition == 1) {
                inCar = true;
                activeCharacter().setVisible(false);
                playerX = carX;
                playerZ = carZ;
                status = "DRIVING  |  3 EXIT";
            } else {
                inCar = false;
                activeCharacter().setVisible(true);
                playerX = transitionEndX;
                playerZ = transitionEndZ;
                activeCharacter().setPosition(playerX, playerY, playerZ);
                status = "ON FOOT  |  3 ENTER CAR";
            }
            carTransition = 0;
        }
    }


    private int randomInt(int bound) {
        randomSeed = (randomSeed * 1103515245 + 12345) & 0x7fffffff;
        return bound <= 0 ? 0 : randomSeed % bound;
    }

    private void initializePedestrians() {
        // Twenty-eight lightweight active pedestrians; their palette index cycles through
        // twenty distinct clothing/skin combinations.
        float[] px = {-5.2f, 5.2f, -5.2f, 5.2f, -5.2f, 5.2f, -5.2f, 5.2f, -5.2f, 5.2f,
                      -5.2f, 5.2f, -5.2f, 5.2f, -5.2f, 5.2f, -5.2f, 5.2f, -5.2f, 5.2f,
                      22.8f, 33.2f, 22.8f, 33.2f, -33.2f, -22.8f, -33.2f, -22.8f};
        float[] pz = {-70.0f, -70.0f, -56.0f, -56.0f, -42.0f, -42.0f, -28.0f, -28.0f, -14.0f, -14.0f,
                       0.0f, 0.0f, 14.0f, 14.0f, 28.0f, 28.0f, 42.0f, 42.0f, 56.0f, 56.0f,
                      -63.0f, -63.0f, -35.0f, -35.0f, -7.0f, -7.0f, 21.0f, 21.0f};
        for (int i = 0; i < px.length && i < PED_COUNT; i++) {
            debugStage = "create pedestrian " + i;
            pedestrians[i] = new PedestrianModel(i);
            world.addChild(pedestrians[i].root);
            pedActive[i] = true;
            pedX[i] = px[i]; pedZ[i] = pz[i];
            pedHeading[i] = (i * 47) % 360;
            pedTimer[i] = 1.0f + (i % 4) * 0.7f;
            pedPhase[i] = i * 0.73f;
            pedAttackCooldown[i] = 1.0f + (i % 3);
            pedState[i] = 0;
            pedestrians[i].setPosition(pedX[i], 0.0f, pedZ[i]);
            pedestrians[i].setHeading(pedHeading[i]);
        }
        nextPedVariant = PED_COUNT;
    }

    private void initializeTraffic() {
        // Twelve low-poly traffic vehicles spread across the main avenues.
        float[] xs = {-30.0f, -26.0f, -30.0f, -2.0f, 2.0f, -2.0f, 26.0f, 30.0f, 26.0f,
                      54.0f, -54.0f, 54.0f};
        float[] zs = {-68.0f, -20.0f, 38.0f, -58.0f, -8.0f, 52.0f, -42.0f, 14.0f, 68.0f,
                      -48.0f, 12.0f, 65.0f};
        float[] headings = {0.0f, 180.0f, 0.0f, 0.0f, 180.0f, 0.0f, 0.0f, 180.0f, 0.0f,
                            180.0f, 0.0f, 180.0f};
        int[] colors = {0xB72E32,0x315B91,0xD0B43B,0x39764B,0x777777,
                        0xD16A32,0x3D8587,0xB7B7B7,0x6B477E,0xE4E0D4,0x8D453D,0x386B78};
        for (int i = 0; i < xs.length && i < trafficCars.length; i++) {
            debugStage = "create traffic car " + i;
            trafficCars[i] = new CarModel(colors[i]);
            trafficX[i] = xs[i]; trafficZ[i] = zs[i];
            trafficLaneX[i] = xs[i];
            trafficLaneCooldown[i] = 1.0f + (i % 3) * 0.7f;
            trafficHeading[i] = headings[i];
            trafficSpeed[i] = 3.0f + (i % 4) * 0.42f;
            trafficCruiseSpeed[i] = trafficSpeed[i];
            trafficCars[i].setPosition(trafficX[i], 0.0f, trafficZ[i]);
            trafficCars[i].setHeading(trafficHeading[i]);
            world.addChild(trafficCars[i].root);
        }
    }

    private void updateTrafficLane(int index, float dt) {
        if (trafficLaneCooldown[index] > 0.0f) trafficLaneCooldown[index] -= dt;
        float dirZ = -(float)Math.cos(trafficHeading[index] * 0.0174532925f);
        float roadCenter = (int)(trafficLaneX[index] / 28.0f + 0.5f) * 28.0f;
        boolean blockedAhead = false;
        boolean changedLane = false;
        for (int j = 0; j < trafficCars.length; j++) {
            if (j == index || trafficCars[j] == null) continue;
            if (Math.abs(normalizeAngle(trafficHeading[j] - trafficHeading[index])) > 35.0f) continue;
            if (Math.abs(trafficX[j] - trafficX[index]) > 2.8f) continue;
            float gap = (trafficZ[j] - trafficZ[index]) * dirZ;
            if (gap > 0.0f && gap < 10.0f) { blockedAhead = true; break; }
        }
        if (blockedAhead && trafficLaneCooldown[index] <= 0.0f) {
            float candidate = trafficLaneX[index] + (trafficLaneX[index] < roadCenter ? 4.0f : -4.0f);
            boolean free = Math.abs(candidate - roadCenter) <= 3.0f;
            for (int j = 0; j < trafficCars.length && free; j++) {
                if (j == index || trafficCars[j] == null) continue;
                if (Math.abs(trafficX[j] - candidate) < 2.7f &&
                    Math.abs(trafficZ[j] - trafficZ[index]) < 9.0f) free = false;
            }
            if (free) {
                trafficLaneX[index] = candidate;
                trafficLaneCooldown[index] = 3.0f;
                changedLane = true;
            }
        } else if (!blockedAhead && trafficLaneCooldown[index] <= 0.0f) {
            trafficLaneX[index] += (roadCenter - trafficLaneX[index]) * 0.04f;
        }
        if (blockedAhead && !changedLane) {
            // No safe adjacent lane: slow down instead of rear-ending the car ahead.
            trafficSpeed[index] += (1.35f - trafficSpeed[index]) * Math.min(1.0f, 2.8f * dt);
        } else {
            trafficSpeed[index] += (trafficCruiseSpeed[index] - trafficSpeed[index]) *
                                   Math.min(1.0f, 0.8f * dt);
        }
        float dx = trafficLaneX[index] - trafficX[index];
        float step = 1.7f * dt;
        if (dx > step) dx = step;
        else if (dx < -step) dx = -step;
        trafficX[index] += dx;
    }

    private void beginPlayerRagdoll(float dirX, float sideX, float dirZ, float speed) {
        float strength = speed * 0.72f;
        if (strength < 2.0f) strength = 2.0f;
        if (strength > 8.5f) strength = 8.5f;
        playerRagdollTime = 2.1f;
        playerRagdollY = 0.12f;
        playerRagdollVX = dirX * strength + sideX * strength * 0.45f;
        playerRagdollVY = 2.2f + strength * 0.30f;
        playerRagdollVZ = dirZ * strength;
        playerRagdollRoll = playerRagdollPitch = 0.0f;
        playerRagdollRollSpeed = (randomInt(2) == 0 ? -1.0f : 1.0f) * (100.0f + strength * 15.0f);
        playerRagdollPitchSpeed = (randomInt(2) == 0 ? -1.0f : 1.0f) * (90.0f + strength * 12.0f);
        playerVelocityX = playerRagdollVX;
        playerVelocityZ = playerRagdollVZ;
        jumpVelocity = 0.0f;
        reverseFacing = false;
        keys = 0;
    }

    private void updatePlayerRagdoll(float dt) {
        playerRagdollTime -= dt;
        playerRagdollVY -= 9.8f * dt;

        // Resolve the horizontal movement one axis at a time. This preserves
        // motion parallel to a wall instead of freezing CJ or letting him pass
        // through the obstacle.
        float nextX = playerX + playerRagdollVX * dt;
        boolean hitX = playerRagdollBlocked(nextX, playerZ);
        if (!hitX) {
            playerX = nextX;
        } else {
            float impact = Math.abs(playerRagdollVX);
            playerRagdollVX = impact > 1.0f ? -playerRagdollVX * 0.16f : 0.0f;
            playerRagdollVZ *= 0.78f;
            playerRagdollRollSpeed *= 0.88f;
        }

        float nextZ = playerZ + playerRagdollVZ * dt;
        boolean hitZ = playerRagdollBlocked(playerX, nextZ);
        if (!hitZ) {
            playerZ = nextZ;
        } else {
            float impact = Math.abs(playerRagdollVZ);
            playerRagdollVZ = impact > 1.0f ? -playerRagdollVZ * 0.16f : 0.0f;
            playerRagdollVX *= 0.78f;
            playerRagdollPitchSpeed *= 0.88f;
        }
        if (hitX && hitZ) {
            playerRagdollVX *= 0.45f;
            playerRagdollVZ *= 0.45f;
            playerRagdollRollSpeed *= 0.72f;
            playerRagdollPitchSpeed *= 0.72f;
        }

        playerRagdollVX *= 0.965f;
        playerRagdollVZ *= 0.965f;
        playerRagdollRoll += playerRagdollRollSpeed * dt;
        playerRagdollPitch += playerRagdollPitchSpeed * dt;
        playerRagdollRollSpeed *= 0.965f;
        playerRagdollPitchSpeed *= 0.965f;
        playerRagdollY += playerRagdollVY * dt;
        if (playerRagdollY <= 0.0f) {
            playerRagdollY = 0.0f;
            if (playerRagdollVY < -1.0f) playerRagdollVY = -playerRagdollVY * 0.14f;
            else playerRagdollVY = 0.0f;
            playerRagdollRollSpeed *= 0.72f;
            playerRagdollPitchSpeed *= 0.72f;
        }
        playerY = playerRagdollY;
        if (playerRagdollTime <= 0.0f && playerRagdollY <= 0.02f) {
            playerRagdollTime = 0.0f;
            playerY = 0.0f;
            playerRagdollVX = playerRagdollVZ = 0.0f;
            playerRagdollRollSpeed = playerRagdollPitchSpeed = 0.0f;
            playerVelocityX = playerVelocityZ = 0.0f;
            status = "GETTING BACK UP...";
        }
    }

    private void updateTraffic(float dt) {
        for (int i = 0; i < trafficCars.length; i++) {
            if (trafficCars[i] == null) continue;
            float oldTrafficX = trafficX[i], oldTrafficZ = trafficZ[i];
            if (trafficHitCooldown[i] > 0.0f) trafficHitCooldown[i] -= dt;
            if (trafficStop[i] > 0.0f) {
                trafficStop[i] -= dt;
            } else {
                updateTrafficLane(i, dt);
                float a = trafficHeading[i] * 0.0174532925f;
                float nx = trafficX[i] - (float)Math.sin(a) * trafficSpeed[i] * dt;
                float nz = trafficZ[i] - (float)Math.cos(a) * trafficSpeed[i] * dt;
                trafficX[i] = nx; trafficZ[i] = nz;
                if (trafficZ[i] < -CITY_LIMIT + 6.0f) trafficHeading[i] = 180.0f;
                else if (trafficZ[i] > CITY_LIMIT - 6.0f) trafficHeading[i] = 0.0f;
            }
            // Traffic vehicles have solid body-to-body collision. A car
            // yields and brakes instead of passing through another vehicle.
            for (int j = 0; j < i; j++) {
                if (trafficCars[j] == null) continue;
                float cx = trafficX[i] - trafficX[j];
                float cz = trafficZ[i] - trafficZ[j];
                if (cx * cx + cz * cz < 5.8f) {
                    trafficX[i] = oldTrafficX;
                    trafficZ[i] = oldTrafficZ;
                    trafficSpeed[i] = 0.7f;
                    break;
                }
            }
            if (inCar) {
                float px = trafficX[i] - carX, pz = trafficZ[i] - carZ;
                if (px * px + pz * pz < 6.0f) {
                    trafficX[i] = oldTrafficX;
                    trafficZ[i] = oldTrafficZ;
                    trafficStop[i] = 0.7f;
                    carSpeed *= 0.82f;
                }
            }
            for (int k = 0; k < trafficBikes.length; k++) {
                if (!bikeTrafficActive[k]) continue;
                float bx = trafficX[i] - bikeTrafficX[k];
                float bz = trafficZ[i] - bikeTrafficZ[k];
                if (bx * bx + bz * bz < 3.0f) {
                    trafficX[i] = oldTrafficX;
                    trafficZ[i] = oldTrafficZ;
                    trafficSpeed[i] = 0.7f;
                    break;
                }
            }
            if (inBike) {
                float bx = trafficX[i] - bikeX, bz = trafficZ[i] - bikeZ;
                if (bx * bx + bz * bz < 4.2f) {
                    trafficX[i] = oldTrafficX;
                    trafficZ[i] = oldTrafficZ;
                    trafficStop[i] = 0.6f;
                    bikeSpeed *= 0.8f;
                }
            } else if (bikeSourceIndex < 0) {
                float bx = trafficX[i] - bikeX, bz = trafficZ[i] - bikeZ;
                if (bx * bx + bz * bz < 4.2f) {
                    trafficX[i] = oldTrafficX;
                    trafficZ[i] = oldTrafficZ;
                    trafficSpeed[i] = 0.7f;
                }
            }
            trafficCars[i].setPosition(trafficX[i], 0.0f, trafficZ[i]);
            trafficCars[i].setHeading(trafficHeading[i]);

            if (!inCar && playerRagdollTime <= 0.0f && playerCollisionCooldown <= 0.0f) {
                float dx = playerX - trafficX[i], dz = playerZ - trafficZ[i];
                float dist2 = dx * dx + dz * dz;
                if (dist2 < 3.8f && trafficSpeed[i] > 0.8f) {
                    float a = trafficHeading[i] * 0.0174532925f;
                    float fx = -(float)Math.sin(a), fz = -(float)Math.cos(a);
                    float sx = (float)Math.cos(a), sz = -(float)Math.sin(a);
                    float side = dx * sx + dz * sz;
                    float direction = (dx * fx + dz * fz) >= 0.0f ? 1.0f : -1.0f;
                    beginPlayerRagdoll(fx * direction, sx * (side >= 0.0f ? 0.45f : -0.45f),
                                       fz * direction, trafficSpeed[i]);
                    playerCollisionCooldown = 1.8f;
                    status = "HIT BY TRAFFIC!";
                }
            }
        }
    }

    private void updatePedestrians(float dt) {
        for (int i = 0; i < PED_COUNT; i++) {
            if (!pedActive[i]) continue;
            if (pedAttackCooldown[i] > 0.0f) pedAttackCooldown[i] -= dt;
            float dx = playerX - pedX[i], dz = playerZ - pedZ[i];
            float dist2 = dx * dx + dz * dz;
            if ((inCar || inBike) && pedState[i] != 3 &&
                ((inCar && Math.abs(carSpeed) > 2.2f) ||
                 (inBike && Math.abs(bikeSpeed) > 3.0f)) &&
                dist2 < (inCar ? 2.9f : 2.1f)) {
                float hitSpeed = inCar ? carSpeed : bikeSpeed;
                float heading = inCar ? carHeading : bikeHeading;
                float ha = heading * 0.0174532925f;
                beginPedRagdoll(i, -(float)Math.sin(ha) * hitSpeed * 0.42f,
                                1.8f, -(float)Math.cos(ha) * hitSpeed * 0.42f);
                addWanted(1);
                status = "WANTED: PEDESTRIAN COLLISION";
                continue;
            }
            if (pedState[i] == 3) {
                // Lightweight articulated ragdoll: integrate body motion and
                // angular momentum, then settle against the ground plane.
                pedRagdollVY[i] -= 9.8f * dt;
                pedX[i] += pedRagdollVX[i] * dt;
                pedZ[i] += pedRagdollVZ[i] * dt;
                pedRagdollVX[i] *= 0.985f;
                pedRagdollVZ[i] *= 0.985f;
                pedRagdollRoll[i] += pedRagdollRollSpeed[i] * dt;
                pedRagdollPitch[i] += pedRagdollPitchSpeed[i] * dt;
                pedRagdollRollSpeed[i] *= 0.97f;
                pedRagdollPitchSpeed[i] *= 0.97f;
                float ragY = pedRagdollY[i] + pedRagdollVY[i] * dt;
                pedRagdollY[i] = ragY;
                if (ragY <= 0.0f) {
                    pedRagdollY[i] = 0.0f;
                    if (pedRagdollVY[i] < -1.0f) {
                        pedRagdollVY[i] = -pedRagdollVY[i] * 0.16f;
                        pedRagdollRollSpeed[i] *= 0.55f;
                        pedRagdollPitchSpeed[i] *= 0.55f;
                    } else {
                        pedRagdollVY[i] = 0.0f;
                        pedRagdollRollSpeed[i] *= 0.82f;
                        pedRagdollPitchSpeed[i] *= 0.82f;
                    }
                }
                pedestrians[i].setPosition(pedX[i], pedRagdollY[i], pedZ[i]);
                pedestrians[i].setHeading(pedHeading[i]);
                pedestrians[i].animateRagdoll(pedRagdollRoll[i], pedRagdollPitch[i]);
                pedTimer[i] -= dt;
                if (pedTimer[i] <= 0.0f) {
                    pedActive[i] = false;
                    pedestrians[i].setVisible(false);
                }
                continue;
            }
            if (pedState[i] == 0 && dist2 < 49.0f) {
                if ((i % 4) == 0) pedState[i] = 2;
                else pedState[i] = 1;
                pedTimer[i] = 2.0f;
            }
            float speed = 0.0f;
            boolean running = false;
            if (pedState[i] == 1) {
                // Most pedestrians panic and flee from CJ.
                pedHeading[i] = (float)(atan2Compat(dx, dz) * 57.29578f);
                speed = 3.0f; running = true;
                pedTimer[i] -= dt;
                if (dist2 > 225.0f) { pedState[i] = 0; pedTimer[i] = 1.5f; }
            } else if (pedState[i] == 2) {
                // A few confront CJ and shove him if he stays close.
                pedHeading[i] = (float)(atan2Compat(-dx, -dz) * 57.29578f);
                speed = dist2 > 1.7f ? 1.65f : 0.0f;
                if (dist2 < 2.0f && pedAttackCooldown[i] <= 0.0f && !inCar) {
                    float inv = 1.0f / (float)Math.sqrt(Math.max(0.01f, dist2));
                    playerVelocityX += dx * inv * 2.0f;
                    playerVelocityZ += dz * inv * 2.0f;
                    playerHitStun = 0.25f;
                    pedAttackCooldown[i] = 2.0f;
                    status = "A PEDESTRIAN PUSHED YOU";
                }
                pedTimer[i] -= dt;
                if (dist2 > 100.0f) { pedState[i] = 0; pedTimer[i] = 2.0f; }
            } else {
                pedTimer[i] -= dt;
                if (pedTimer[i] <= 0.0f) {
                    pedHeading[i] += randomInt(181) - 90;
                    pedTimer[i] = 1.5f + randomInt(35) * 0.1f;
                }
                speed = 0.55f;
            }
            float a = pedHeading[i] * 0.0174532925f;
            float nx = pedX[i] - (float)Math.sin(a) * speed * dt;
            float nz = pedZ[i] - (float)Math.cos(a) * speed * dt;
            if (!playerBlocked(nx, nz)) { pedX[i] = nx; pedZ[i] = nz; }
            else { pedHeading[i] += 115.0f; pedTimer[i] = 0.8f; }
            pedPhase[i] += dt * (running ? 12.0f : 7.0f);
            pedestrians[i].setPosition(pedX[i], 0.0f, pedZ[i]);
            pedestrians[i].setHeading(pedHeading[i]);
            if (pedState[i] == 1) pedestrians[i].animateRunAway(pedPhase[i]);
            else pedestrians[i].animate(pedPhase[i], speed > 0.1f, running, false);
        }
    }

    private void hitPedestrian() {
        float best = 4.0f;
        int target = -1;
        float fx = -(float)Math.sin(playerHeading * 0.0174532925f);
        float fz = -(float)Math.cos(playerHeading * 0.0174532925f);
        for (int i = 0; i < PED_COUNT; i++) {
            if (!pedActive[i] || pedState[i] == 3) continue;
            float dx = pedX[i] - playerX, dz = pedZ[i] - playerZ;
            float d2 = dx * dx + dz * dz;
            if (d2 < best && dx * fx + dz * fz > 0.0f) { best = d2; target = i; }
        }
        if (target >= 0) {
            int i = target;
            if ((i % 4) == 0 && pedState[i] != 2) {
                pedState[i] = 1;
                pedTimer[i] = 4.0f;
            } else {
                beginPedRagdoll(i,
                    -(float)Math.sin(playerHeading * 0.0174532925f) * 2.2f,
                    2.8f,
                    -(float)Math.cos(playerHeading * 0.0174532925f) * 2.2f);
            }
        }
    }

    private void beginPedRagdoll(int i, float vx, float vy, float vz) {
        pedState[i] = 3;
        pedTimer[i] = 3.2f;
        pedRagdollY[i] = 0.08f;
        pedRagdollVX[i] = vx;
        pedRagdollVY[i] = vy;
        pedRagdollVZ[i] = vz;
        pedRagdollRoll[i] = 0.0f;
        pedRagdollPitch[i] = 0.0f;
        pedRagdollRollSpeed[i] = (randomInt(2) == 0 ? -1.0f : 1.0f) * (100.0f + randomInt(100));
        pedRagdollPitchSpeed[i] = (randomInt(2) == 0 ? -1.0f : 1.0f) * (70.0f + randomInt(90));
    }

    private int spawnPedestrian(float x, float z, float heading) {
        int slot = -1;
        for (int i = 0; i < PED_COUNT; i++) if (!pedActive[i]) { slot = i; break; }
        if (slot < 0) {
            float farthest = -1.0f;
            for (int i = 0; i < PED_COUNT; i++) {
                float dx = pedX[i] - playerX, dz = pedZ[i] - playerZ;
                float d = dx * dx + dz * dz;
                if (d > farthest && pedState[i] != 3) { farthest = d; slot = i; }
            }
        }
        if (slot < 0) return -1;
        // Recycle this slot with the next of twenty appearance presets.
        world.removeChild(pedestrians[slot].root);
        pedestrians[slot] = new PedestrianModel(nextPedVariant % 20);
        nextPedVariant++;
        world.addChild(pedestrians[slot].root);
        pedActive[slot] = true;
        pedState[slot] = 1;
        pedX[slot] = x; pedZ[slot] = z; pedHeading[slot] = heading;
        pedTimer[slot] = 4.0f; pedPhase[slot] = 0.0f;
        pedAttackCooldown[slot] = 2.0f;
        pedRagdollY[slot] = 0.0f;
        pedRagdollVX[slot] = pedRagdollVY[slot] = pedRagdollVZ[slot] = 0.0f;
        pedRagdollRoll[slot] = pedRagdollPitch[slot] = 0.0f;
        pedRagdollRollSpeed[slot] = pedRagdollPitchSpeed[slot] = 0.0f;
        pedestrians[slot].setPosition(x, 0.0f, z);
        pedestrians[slot].setHeading(heading);
        return slot;
    }

    private int findStoppableTrafficCar() {
        int best = -1;
        float bestDist = 6.5f;
        for (int i = 0; i < trafficCars.length; i++) {
            if (trafficCars[i] == null || trafficStop[i] <= 0.0f) continue;
            float dx = playerX - trafficX[i], dz = playerZ - trafficZ[i];
            float d = (float)Math.sqrt(dx * dx + dz * dz);
            if (d < bestDist) { bestDist = d; best = i; }
        }
        return best;
    }

    private void beginTrafficHijack(int index) {
        trafficHijackIndex = index;
        addWanted(1);
        trafficHijackTime = 0.0f;
        trafficDriverEjected = false;
        carTransition = 3;
        float dx = playerX - trafficX[index], dz = playerZ - trafficZ[index];
        float a = trafficHeading[index] * 0.0174532925f;
        float localSide = dx * (float)Math.cos(a) - dz * (float)Math.sin(a);
        transitionDriverSide = localSide <= 0.0f;
        status = "TAKING THE DRIVER'S SEAT...";
        playerVelocityX = 0.0f; playerVelocityZ = 0.0f;
    }

    private void updateTrafficHijack(float dt) {
        int i = trafficHijackIndex;
        if (i < 0 || i >= trafficCars.length || trafficCars[i] == null) {
            carTransition = 0; trafficHijackIndex = -1; return;
        }
        trafficHijackTime += dt;
        float t = trafficHijackTime;
        CarModel target = trafficCars[i];
        target.setDoorOpen(t < 1.15f ? 68.0f : 0.0f, transitionDriverSide);
        float side = transitionDriverSide ? -1.0f : 1.0f;
        float a = trafficHeading[i] * 0.0174532925f;
        float outsideX = trafficX[i] + side * 1.28f * (float)Math.cos(a) - 0.25f * (float)Math.sin(a);
        float outsideZ = trafficZ[i] - side * 1.28f * (float)Math.sin(a) - 0.25f * (float)Math.cos(a);
        activeCharacter().setVisible(true);
        activeCharacter().setPosition(outsideX, 0.0f, outsideZ);
        activeCharacter().setHeading(normalizeAngle(trafficHeading[i] + 90.0f));
        if (t > 0.25f && !trafficDriverEjected) {
            // The driver is converted into an ordinary fleeing pedestrian.
            float driverX = trafficX[i] + side * 2.0f * (float)Math.cos(a);
            float driverZ = trafficZ[i] - side * 2.0f * (float)Math.sin(a);
            int driver = spawnPedestrian(driverX, driverZ, trafficHeading[i] + 90.0f);
            if (driver >= 0) {
                // The former driver gets up immediately and runs away.
                pedState[driver] = 1;
                pedTimer[driver] = 8.0f;
                pedAttackCooldown[driver] = 4.0f;
            }
            trafficDriverEjected = true;
        }
        if (t < 0.65f) {
            activeCharacter().setEntryPose(Math.min(1.0f, t / 0.65f), false);
        } else {
            float enter = Math.min(1.0f, (t - 0.65f) / 0.75f);
            activeCharacter().setEntryPose(enter, true);
        }
        if (t >= 1.4f) {
            car = target;
            carX = trafficX[i]; carZ = trafficZ[i]; carHeading = trafficHeading[i];
            carSpeed = 0.0f; carSteering = 0.0f;
            trafficCars[i] = null;
            inCar = true;
            activeCharacter().setVisible(false);
            activeCharacter().setEntryPose(0.0f, true);
            playerX = carX; playerZ = carZ; playerHeading = carHeading;
            carTransition = 0; trafficHijackIndex = -1;
            status = "STOLEN CAR  |  3 EXIT";
        }
    }

    private boolean playerBlocked(float x, float z) {
        return playerBlockedRadius(x, z, 0.34f);
    }

    // The ragdoll uses a wider collision capsule than the upright character.
    // This keeps the torso from clipping into walls while it is lying sideways.
    private boolean playerRagdollBlocked(float x, float z) {
        return playerBlockedRadius(x, z, 0.58f);
    }

    private boolean playerBlockedRadius(float x, float z, float radius) {
        if (x < -CITY_LIMIT + radius || x > CITY_LIMIT - radius ||
            z < -CITY_LIMIT + radius || z > CITY_LIMIT - radius) return true;
        for (int i = 0; i < trafficCars.length; i++) {
            if (trafficCars[i] != null) {
                float tx = x - trafficX[i], tz = z - trafficZ[i];
                float carRadius = 1.35f + radius;
                if (tx * tx + tz * tz < carRadius * carRadius) return true;
            }
        }
        for (int i = 0; i < buildingCount; i++) {
            if (circleHitsRect(x, z, radius, buildingX[i], buildingZ[i],
                               buildingW[i], buildingD[i])) return true;
        }

        // The parked/player sedan is also solid during the ragdoll.
        float a = carHeading * 0.0174532925f;
        float dx = x - carX, dz = z - carZ;
        float lx = dx * (float)Math.cos(a) - dz * (float)Math.sin(a);
        float lz = dx * (float)Math.sin(a) + dz * (float)Math.cos(a);
        float ox = Math.max(Math.abs(lx) - (0.92f + radius), 0.0f);
        float oz = Math.max(Math.abs(lz) - (1.48f + radius), 0.0f);
        return ox * ox + oz * oz < radius * radius;
    }

    private void addWanted(int amount) {
        wantedLevel += amount;
        if (wantedLevel > 5) wantedLevel = 5;
        wantedDecay = 0.0f;
    }

    private int findNearbyMotorcycle() {
        float best2 = 25.0f;
        int result = -2;
        float dx = playerX - bikeX, dz = playerZ - bikeZ;
        if (!inBike && dx * dx + dz * dz < best2) {
            best2 = dx * dx + dz * dz;
            result = -1;
        }
        for (int i = 0; i < trafficBikes.length; i++) {
            if (!bikeTrafficActive[i]) continue;
            dx = playerX - bikeTrafficX[i]; dz = playerZ - bikeTrafficZ[i];
            float d2 = dx * dx + dz * dz;
            if (d2 < best2) { best2 = d2; result = i; }
        }
        return result;
    }

    private void initializeMotorcycles() {
        float[] xs = {-30.0f, 2.0f, 30.0f};
        float[] zs = {-24.0f, 32.0f, 76.0f};
        int[] colors = {0xD16A32, 0x315B91, 0x39764B};
        for (int i = 0; i < trafficBikes.length; i++) {
            trafficBikes[i] = new MotorcycleModel(colors[i]);
            bikeTrafficX[i] = xs[i]; bikeTrafficZ[i] = zs[i];
            bikeTrafficHeading[i] = i == 1 ? 180.0f : 0.0f;
            bikeTrafficSpeed[i] = 3.2f + i * 0.45f;
            bikeTrafficActive[i] = true;
            trafficBikes[i].setPosition(xs[i], 0.0f, zs[i]);
            trafficBikes[i].setHeading(bikeTrafficHeading[i]);
            world.addChild(trafficBikes[i].root);
        }
    }

    private void updateMotorcycles(float dt) {
        for (int i = 0; i < trafficBikes.length; i++) {
            if (!bikeTrafficActive[i]) continue;
            float a = bikeTrafficHeading[i] * 0.0174532925f;
            float nx = bikeTrafficX[i] - (float)Math.sin(a) * bikeTrafficSpeed[i] * dt;
            float nz = bikeTrafficZ[i] - (float)Math.cos(a) * bikeTrafficSpeed[i] * dt;
            if (!bikeBlocked(nx, nz, i)) {
                bikeTrafficX[i] = nx; bikeTrafficZ[i] = nz;
            } else {
                bikeTrafficHeading[i] = normalizeAngle(bikeTrafficHeading[i] + 180.0f);
            }
            if (bikeTrafficZ[i] < -CITY_LIMIT + 5.0f) bikeTrafficHeading[i] = 180.0f;
            else if (bikeTrafficZ[i] > CITY_LIMIT - 5.0f) bikeTrafficHeading[i] = 0.0f;
            trafficBikes[i].setPosition(bikeTrafficX[i], 0.0f, bikeTrafficZ[i]);
            trafficBikes[i].setHeading(bikeTrafficHeading[i]);
        }
    }

    private boolean bikeBlocked(float x, float z, int self) {
        if (x < -CITY_LIMIT + 1.0f || x > CITY_LIMIT - 1.0f ||
            z < -CITY_LIMIT + 1.0f || z > CITY_LIMIT - 1.0f) return true;
        for (int i = 0; i < buildingCount; i++)
            if (circleHitsRect(x, z, 0.55f, buildingX[i], buildingZ[i],
                               buildingW[i], buildingD[i])) return true;
        for (int i = 0; i < trafficCars.length; i++) {
            if (trafficCars[i] == null) continue;
            float dx = x - trafficX[i], dz = z - trafficZ[i];
            if (dx * dx + dz * dz < 2.5f) return true;
        }
        if (inCar) {
            float dx = x - carX, dz = z - carZ;
            if (dx * dx + dz * dz < 4.0f) return true;
        }
        for (int i = 0; i < trafficBikes.length; i++) {
            if (i == self || !bikeTrafficActive[i]) continue;
            float dx = x - bikeTrafficX[i], dz = z - bikeTrafficZ[i];
            if (dx * dx + dz * dz < 1.4f) return true;
        }
        if (!inBike && bikeSourceIndex < 0) {
            float dx = x - bikeX, dz = z - bikeZ;
            if (dx * dx + dz * dz < 1.4f) return true;
        }
        return false;
    }

    private boolean vehicleBlocked(float x, float z, float radius) {
        if (x < -CITY_LIMIT + 1.8f || x > CITY_LIMIT - 1.8f ||
            z < -CITY_LIMIT + 1.8f || z > CITY_LIMIT - 1.8f) return true;
        for (int i = 0; i < buildingCount; i++)
            if (circleHitsRect(x, z, radius, buildingX[i], buildingZ[i],
                               buildingW[i], buildingD[i])) return true;
        for (int i = 0; i < trafficCars.length; i++) {
            if (trafficCars[i] == null) continue;
            float dx = x - trafficX[i], dz = z - trafficZ[i];
            if (dx * dx + dz * dz < (radius + 1.15f) * (radius + 1.15f)) return true;
        }
        for (int i = 0; i < trafficBikes.length; i++) {
            if (!bikeTrafficActive[i]) continue;
            float dx = x - bikeTrafficX[i], dz = z - bikeTrafficZ[i];
            if (dx * dx + dz * dz < (radius + 0.65f) * (radius + 0.65f)) return true;
        }
        return false;
    }

    private void initializePolice() {
        for (int i = 0; i < policeCars.length; i++) {
            policeCars[i] = new CarModel(0x263C62);
            policeCars[i].setPosition(0.0f, -20.0f, 0.0f);
            policeCars[i].root.setRenderingEnable(false);
            policeActive[i] = false;
            world.addChild(policeCars[i].root);
        }
    }

    private void updatePolice(float dt) {
        if (wantedLevel <= 0) {
            arrestTimer = 0.0f;
            for (int i = 0; i < policeCars.length; i++) {
                policeActive[i] = false;
                policeCars[i].root.setRenderingEnable(false);
            }
            return;
        }
        int active = wantedLevel;
        if (active > policeCars.length) active = policeCars.length;
        boolean nearby = false;
        for (int i = 0; i < policeCars.length; i++) {
            if (i >= active) {
                policeActive[i] = false;
                policeCars[i].root.setRenderingEnable(false);
                continue;
            }
            if (!policeActive[i]) {
                float avenueX = (float)Math.floor(playerX / CITY_SPACING + 0.5f) * CITY_SPACING;
                policeX[i] = avenueX + (i - 1) * 2.0f;
                policeZ[i] = playerZ + (i % 2 == 0 ? -15.0f : 15.0f);
                policeActive[i] = true;
                policeCars[i].root.setRenderingEnable(true);
            }
            float dx = playerX - policeX[i], dz = playerZ - policeZ[i];
            float dist = (float)Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.01f) {
                float heading = (float)(atan2Compat(-dx, -dz) * 57.29578f);
                float speed = 3.0f + wantedLevel * 0.75f;
                float a = heading * 0.0174532925f;
                float nx = policeX[i] - (float)Math.sin(a) * speed * dt;
                float nz = policeZ[i] - (float)Math.cos(a) * speed * dt;
                if (!carBlocked(nx, nz)) { policeX[i] = nx; policeZ[i] = nz; }
                policeCars[i].setPosition(policeX[i], 0.0f, policeZ[i]);
                policeCars[i].setHeading(heading);
            }
            if (dist < 3.4f) nearby = true;
        }
        if (nearby && (!inCar || Math.abs(carSpeed) < 2.0f) &&
            (!inBike || Math.abs(bikeSpeed) < 2.0f)) {
            arrestTimer += dt;
            if (arrestTimer > 1.8f) {
                wantedLevel = 0; arrestTimer = 0.0f;
                if (inCar) { inCar = false; carSpeed = 0.0f; }
                if (inBike) {
                    inBike = false; bikeSpeed = 0.0f;
                    playerBike.setVisible(bikeSourceIndex < 0);
                    if (bikeSourceIndex >= 0) {
                        bikeTrafficX[bikeSourceIndex] = bikeX;
                        bikeTrafficZ[bikeSourceIndex] = bikeZ;
                        bikeTrafficHeading[bikeSourceIndex] = bikeHeading;
                        trafficBikes[bikeSourceIndex].setPosition(bikeX, 0.0f, bikeZ);
                        trafficBikes[bikeSourceIndex].setHeading(bikeHeading);
                        bikeTrafficActive[bikeSourceIndex] = true;
                        trafficBikes[bikeSourceIndex].setVisible(true);
                    }
                    bikeSourceIndex = -1;
                    activeCharacter().setVisible(true);
                }
                playerX = policeX[0] + 2.0f; playerZ = policeZ[0] + 2.0f;
                playerY = 0.0f; playerRagdollTime = 0.0f;
                activeCharacter().setVisible(true);
                activeCharacter().setPosition(playerX, playerY, playerZ);
                status = "BUSTED - ARRESTED";
                for (int j = 0; j < policeCars.length; j++) {
                    policeActive[j] = false;
                    policeCars[j].root.setRenderingEnable(false);
                }
            }
        } else {
            arrestTimer = 0.0f;
            wantedDecay += dt;
            if (!nearby && wantedDecay > 45.0f) {
                wantedLevel--; wantedDecay = 0.0f;
            }
        }
    }

    private boolean carBlocked(float x, float z) {
        if (x < -CITY_LIMIT + 2.0f || x > CITY_LIMIT - 2.0f ||
            z < -CITY_LIMIT + 2.0f || z > CITY_LIMIT - 2.0f) return true;
        for (int i = 0; i < buildingCount; i++) {
            if (circleHitsRect(x, z, 1.75f, buildingX[i], buildingZ[i],
                               buildingW[i], buildingD[i])) return true;
        }
        // NPC cars, motorcycles and the player's motorcycle are solid bodies.
        for (int i = 0; i < trafficCars.length; i++) {
            if (trafficCars[i] == null) continue;
            float dx = x - trafficX[i], dz = z - trafficZ[i];
            if (dx * dx + dz * dz < 7.0f) return true;
        }
        for (int i = 0; i < trafficBikes.length; i++) {
            if (!bikeTrafficActive[i]) continue;
            float dx = x - bikeTrafficX[i], dz = z - bikeTrafficZ[i];
            if (dx * dx + dz * dz < 4.0f) return true;
        }
        if (inBike) {
            float dx = x - bikeX, dz = z - bikeZ;
            if (dx * dx + dz * dz < 4.0f) return true;
        }
        return false;
    }

    private boolean circleHitsRect(float x,float z,float radius,
                                   float rx,float rz,float width,float depth) {
        float nx=x, nz=z;
        float minX=rx-width*0.5f, maxX=rx+width*0.5f;
        float minZ=rz-depth*0.5f, maxZ=rz+depth*0.5f;
        if(nx<minX)nx=minX; if(nx>maxX)nx=maxX;
        if(nz<minZ)nz=minZ; if(nz>maxZ)nz=maxZ;
        float dx=x-nx, dz=z-nz;
        return dx*dx+dz*dz<radius*radius;
    }

    protected void keyPressed(int keyCode) {
        if (fatalError != null) {
            if (keyCode == '2' || getGameAction(keyCode) == Canvas.DOWN) diagnosticPage++;
            else if (keyCode == '8' || getGameAction(keyCode) == Canvas.UP) {
                if (diagnosticPage > 0) diagnosticPage--;
            }
            repaint();
            return;
        }
        if (handleJetpackCheatKey(keyCode)) return;
        if (keyCode == '2') {
            if ((keys & FORWARD) == 0) {
                long now = System.currentTimeMillis();
                if (now - lastForwardTap <= 360L) keys |= SPRINT;
                lastForwardTap = now;
            }
            keys |= FORWARD;
        }
        else if (keyCode == '8') {
            if ((keys & BACK) == 0) {
                long now = System.currentTimeMillis();
                if (now - lastBackTap <= 360L) keys |= SPRINT;
                lastBackTap = now;
            }
            keys |= BACK;
        }
        else if (keyCode == '4') keys |= LEFT;
        else if (keyCode == '6') keys |= RIGHT;
        else if (keyCode == '9') keys |= CROUCH;
        else if (keyCode == '5') { if (jetpackOwned) jetpackThrust = true; else jump(); }
        else if (keyCode == '1') punch();
        else if (keyCode == '3') toggleCar();
        else if (keyCode == '*') beginCharacterSwitch();
        else {
            int action = getGameAction(keyCode);
            if (action == Canvas.UP) cameraKeys |= CAM_UP;
            else if (action == Canvas.DOWN) cameraKeys |= CAM_DOWN;
            else if (action == Canvas.LEFT) cameraKeys |= CAM_LEFT;
            else if (action == Canvas.RIGHT) cameraKeys |= CAM_RIGHT;
        }
    }

    protected void keyReleased(int keyCode) {
        if (keyCode == '2') { keys &= ~FORWARD; keys &= ~SPRINT; }
        else if (keyCode == '8') { keys &= ~BACK; keys &= ~SPRINT; }
        else if (keyCode == '4') keys &= ~LEFT;
        else if (keyCode == '6') keys &= ~RIGHT;
        else if (keyCode == '9') keys &= ~CROUCH;
        else if (keyCode == '5') jetpackThrust = false;
        else {
            int action = getGameAction(keyCode);
            if (action == Canvas.UP) cameraKeys &= ~CAM_UP;
            else if (action == Canvas.DOWN) cameraKeys &= ~CAM_DOWN;
            else if (action == Canvas.LEFT) cameraKeys &= ~CAM_LEFT;
            else if (action == Canvas.RIGHT) cameraKeys &= ~CAM_RIGHT;
        }
    }

    // Paginated exception display for the Java ME SDK emulator.
    private void drawDiagnostic(Graphics g) {
        String value = "EXCEPTION: " + fatalError + "\nSTAGE: " + debugStage;
        int charsPerLine = 28;
        int rowsPerPage = 8;
        int rows = 0;
        int col = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\n' || col >= charsPerLine) {
                rows++;
                col = 0;
                if (c == '\n') continue;
            }
            col++;
        }
        if (col > 0) rows++;
        int pages = (rows + rowsPerPage - 1) / rowsPerPage;
        if (pages < 1) pages = 1;
        if (diagnosticPage >= pages) diagnosticPage = pages - 1;
        int first = diagnosticPage * rowsPerPage;
        int row = 0;
        int start = 0;
        col = 0;
        g.setColor(0xFFFFFF);
        g.drawString("JAVA ME ERROR " + (diagnosticPage + 1) + "/" + pages,
                     3, 2, Graphics.TOP | Graphics.LEFT);
        for (int i = 0; i <= value.length(); i++) {
            boolean end = i == value.length();
            char c = end ? '\n' : value.charAt(i);
            if (c == '\n' || col >= charsPerLine || end) {
                if (row >= first && row < first + rowsPerPage) {
                    g.drawString(value.substring(start, i), 3,
                                 22 + (row - first) * 17,
                                 Graphics.TOP | Graphics.LEFT);
                }
                row++;
                if (c == '\n') start = i + 1;
                else start = i;
                col = 0;
                if (c != '\n' && !end) col = 1;
            } else {
                col++;
            }
        }
        g.drawString("2/DOWN NEXT  8/UP PREVIOUS",
                     3, getHeight() - 18, Graphics.TOP | Graphics.LEFT);
    }

    protected void paint(Graphics g) {
        g.setColor(0x87B6D8);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (fatalError != null) {
            drawDiagnostic(g);
            return;
        }
        if (!initialized) {
            g.setColor(0xFFFFFF);
            g.drawString(status, 3, 3, Graphics.TOP | Graphics.LEFT);
            return;
        }
        Graphics3D g3d = Graphics3D.getInstance();
        try {
            g3d.bindTarget(g);
            g3d.render(world);
        } catch (Throwable error) {
            fatalError = error.toString();
            debugStage = "3D rendering";
            diagnosticPage = 0;
            drawDiagnostic(g);
            return;
        } finally {
            try { g3d.releaseTarget(); } catch (Throwable ignored) {}
        }

        g.setColor(0x101010);
        g.fillRect(0, 0, getWidth(), 17);
        g.setColor(0xFFFFFF);
        g.drawString(status, 3, 1, Graphics.TOP | Graphics.LEFT);
        if (wantedStarOn != null && wantedStarOff != null) {
            int starX = getWidth() - 5 * 14 - 3;
            for (int wi = 0; wi < 5; wi++) {
                g.drawImage(wi < wantedLevel ? wantedStarOn : wantedStarOff,
                            starX + wi * 14, 2, Graphics.TOP | Graphics.LEFT);
            }
        } else if (wantedLevel > 0) {
            String stars = "";
            for (int wi = 0; wi < wantedLevel; wi++) stars += "*";
            g.drawString(stars, getWidth() - 42, 1, Graphics.TOP | Graphics.LEFT);
        }
        g.drawString("2/8 MOVE  4/6 TURN  * SWITCH  D-PAD CAMERA", 3,
                     getHeight() - 16, Graphics.TOP | Graphics.LEFT);
    }
}
