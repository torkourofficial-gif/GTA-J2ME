package gta;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.m3g.Background;
import javax.microedition.m3g.Camera;
import javax.microedition.m3g.Graphics3D;
import javax.microedition.m3g.Group;
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
    private CJModel cj;
    private CarModel car;
    private Camera camera;
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
    private float carHeading = 0.0f;
    private float carX = 2.5f;
    private float carZ = 0.0f;
    private boolean inCar = false;
    private boolean initialized = false;
    private String status = "2/4/6/8 MOVE  3 CAR";

    GameCanvas() {
        setFullScreenMode(true);
        try {
            initializeScene();
            initialized = true;
        } catch (Throwable error) {
            status = "M3G INIT ERROR: " + error.toString();
        }
    }

    private void initializeScene() {
        world = new World();
        Background sky = new Background();
        sky.setColor(0x87B6D8);
        sky.setColorClearEnable(true);
        world.setBackground(sky);
        scenery = new Group();
        world.addChild(scenery);

        addBox(scenery, 42.0f, 0.18f, 42.0f, 0.0f, -0.12f, 0.0f, 0x547B48);
        addBox(scenery, 5.2f, 0.025f, 42.0f, 0.0f, -0.015f, 0.0f, 0x454545);
        addBox(scenery, 0.12f, 0.03f, 42.0f, 0.0f, 0.005f, 0.0f, 0xE5D9A0);

        // A compact starter block: buildings frame the test road.
        addBuilding(-6.0f, -5.0f, 3.8f, 5.0f, 0xC49A68);
        addBuilding(6.0f, -7.0f, 4.2f, 6.0f, 0xB7A07A);
        addBuilding(-6.0f, 5.0f, 4.0f, 4.0f, 0x9D806A);
        addBuilding(6.0f, 6.0f, 3.5f, 5.0f, 0xC4B28B);

        // Simple sidewalk props and street lamps.
        addBox(scenery, 1.8f, 0.7f, 1.8f, -3.7f, 0.35f, -4.0f, 0x426E3C);
        addBox(scenery, 0.10f, 2.8f, 0.10f, -3.7f, 1.4f, -4.0f, 0x555555);
        addBox(scenery, 0.9f, 0.12f, 0.45f, -3.7f, 2.8f, -4.0f, 0xD8D2A0);

        cj = new CJModel();
        cj.setPosition(playerX, playerY, playerZ);
        world.addChild(cj.root);

        car = new CarModel();
        car.setPosition(carX, 0.0f, carZ);
        world.addChild(car.root);

        camera = new Camera();
        camera.setPerspective(62.0f, 1.0f, 0.1f, 90.0f);
        cameraTransform = new Transform();
        world.addChild(camera);
        world.setActiveCamera(camera);

        updateCamera();
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

    private void addBox(Group parent, float w, float h, float d,
                        float x, float y, float z, int color) {
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
            if (!paused) {
                update(0.04f);
                repaint();
            }
            try { Thread.sleep(40); } catch (InterruptedException ignored) {}
        }
    }

    private void update(float dt) {
        updateCameraInput(dt);
        boolean moving = false;

        if (inCar) {
            if ((keys & FORWARD) != 0) carSpeed += 3.2f * dt;
            if ((keys & BACK) != 0) carSpeed -= 3.0f * dt;
            if ((keys & LEFT) != 0) carHeading -= 75.0f * dt;
            if ((keys & RIGHT) != 0) carHeading += 75.0f * dt;
            carSpeed *= 0.985f;
            if (carSpeed > 8.0f) carSpeed = 8.0f;
            if (carSpeed < -3.0f) carSpeed = -3.0f;
            float radians = carHeading * 0.0174532925f;
            carX += (float)Math.sin(radians) * carSpeed * dt;
            carZ -= (float)Math.cos(radians) * carSpeed * dt;
            car.setPosition(carX, 0.0f, carZ);
            car.setHeading(carHeading);
            playerX = carX;
            playerZ = carZ;
            playerHeading = carHeading;
            playerVelocityX = 0.0f;
            playerVelocityZ = 0.0f;
        } else {
            float forward = 0.0f;
            float side = 0.0f;
            if ((keys & FORWARD) != 0) forward += 1.0f;
            if ((keys & BACK) != 0) forward -= 1.0f;
            if ((keys & RIGHT) != 0) side += 1.0f;
            if ((keys & LEFT) != 0) side -= 1.0f;

            // 4/6 turn CJ; 2/8 move along his facing direction (SA-like).
            // Turning is independent of forward/back input, so reverse movement
            // never snaps the character around or makes the body appear tilted.
            if (side != 0.0f) {
                playerHeading = normalizeAngle(playerHeading + side * 150.0f * dt);
            }

            float inputLength = (float)Math.sqrt(forward * forward + side * side);
            if (inputLength > 1.0f) inputLength = 1.0f;

            float angle = playerHeading * 0.0174532925f;
            float targetSpeed = forward * 3.0f;
            float targetVX = (float)Math.sin(angle) * targetSpeed;
            float targetVZ = -(float)Math.cos(angle) * targetSpeed;

            // Gradual acceleration and deceleration.
            float response = inputLength > 0.01f ? 12.0f : 15.0f;
            float blend = response * dt;
            if (blend > 1.0f) blend = 1.0f;
            playerVelocityX += (targetVX - playerVelocityX) * blend;
            playerVelocityZ += (targetVZ - playerVelocityZ) * blend;

            playerX += playerVelocityX * dt;
            playerZ += playerVelocityZ * dt;

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

            if (jumpVelocity != 0.0f || playerY > 0.0f) {
                playerY += jumpVelocity * dt;
                jumpVelocity -= 9.8f * dt;
                if (playerY <= 0.0f) {
                    playerY = 0.0f;
                    jumpVelocity = 0.0f;
                }
            }
        }

        if (punchTimer > 0.0f) punchTimer -= dt;
        if (punchTimer < 0.0f) punchTimer = 0.0f;
        float punchAngle = punchTimer > 0.0f ? (float)Math.sin((0.35f - punchTimer) * 18.0f) * 75.0f : 0.0f;

        cj.setPosition(playerX, playerY, playerZ);
        cj.setHeading(playerHeading);
        boolean crouching = !inCar && (keys & CROUCH) != 0;
        boolean jumping = !inCar && (playerY > 0.001f || jumpVelocity > 0.0f);
        float turnAmount = !inCar ? (((keys & RIGHT) != 0 ? 1.0f : 0.0f) -
                                    ((keys & LEFT) != 0 ? 1.0f : 0.0f)) : 0.0f;
        cj.animate(walkPhase, moving && !inCar, punchAngle,
                   (keys & BACK) != 0, jumping, crouching, turnAmount);
        updateCamera();
    }

    // CLDC-friendly atan2 approximation; avoids Math.atan2 on limited runtimes.
    private float angleDegrees(float y, float x) {
        float ax = x < 0.0f ? -x : x;
        float ay = y < 0.0f ? -y : y;
        float angle;
        if (ax + ay < 0.0001f) return playerHeading;
        if (ax >= ay) {
            float r = ay / ax;
            angle = r * (45.0f - 14.0f * r);
        } else {
            float r = ax / ay;
            angle = 90.0f - r * (45.0f - 14.0f * r);
        }
        if (x < 0.0f) angle = 180.0f - angle;
        if (y < 0.0f) angle = -angle;
        return angle;
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

    private float normalizeAngle(float angle) {
        while (angle > 180.0f) angle -= 360.0f;
        while (angle < -180.0f) angle += 360.0f;
        return angle;
    }

    private float turnTowards(float current, float target, float maxStep) {
        float difference = target - current;
        while (difference > 180.0f) difference -= 360.0f;
        while (difference < -180.0f) difference += 360.0f;
        if (difference > maxStep) difference = maxStep;
        else if (difference < -maxStep) difference = -maxStep;
        current += difference;
        if (current > 180.0f) current -= 360.0f;
        if (current < -180.0f) current += 360.0f;
        return current;
    }

    private void updateCamera() {
        float radians = cameraYaw * 0.0174532925f;
        float distance = inCar ? 6.5f : 5.2f;
        float targetY = inCar ? 1.1f : 1.15f;
        float eyeX = playerX + (float)Math.sin(radians) * distance;
        float eyeZ = playerZ + (float)Math.cos(radians) * distance;
        float eyeY = playerY + 2.3f + cameraPitch * 0.035f;
        // M3G Transform has no lookAt helper: compose yaw and pitch directly.
        cameraTransform.setIdentity();
        cameraTransform.postRotate(cameraYaw, 0.0f, 1.0f, 0.0f);
        cameraTransform.postRotate(-cameraPitch, 1.0f, 0.0f, 0.0f);
        camera.setTransform(cameraTransform);
        camera.setTranslation(eyeX, eyeY, eyeZ);
        float w = getWidth();
        float h = getHeight();
        if (w > 0 && h > 0) camera.setPerspective(62.0f, w / h, 0.1f, 90.0f);
    }

    private void jump() {
        if (!inCar && playerY <= 0.001f && jumpVelocity == 0.0f) {
            jumpVelocity = 5.0f;
        }
    }

    private void punch() {
        if (!inCar) punchTimer = 0.35f;
    }

    private void toggleCar() {
        if (inCar) {
            inCar = false;
            cj.setVisible(true);
            playerX = carX + 1.6f;
            playerZ = carZ;
            playerY = 0.0f;
            playerVelocityX = 0.0f;
            playerVelocityZ = 0.0f;
            carSpeed = 0.0f;
            status = "ON FOOT  |  3 ENTER CAR";
            return;
        }
        float dx = playerX - carX;
        float dz = playerZ - carZ;
        if (dx * dx + dz * dz < 12.0f) {
            inCar = true;
            cj.setVisible(false);
            playerX = carX;
            playerZ = carZ;
            playerY = 0.0f;
            playerVelocityX = 0.0f;
            playerVelocityZ = 0.0f;
            status = "DRIVING  |  3 EXIT";
        } else {
            status = "GET CLOSER TO THE CAR";
        }
    }

    protected void keyPressed(int keyCode) {
        if (keyCode == '2') keys |= FORWARD;
        else if (keyCode == '8') keys |= BACK;
        else if (keyCode == '4') keys |= LEFT;
        else if (keyCode == '6') keys |= RIGHT;
        else if (keyCode == '9') keys |= CROUCH;
        else if (keyCode == '5') jump();
        else if (keyCode == '1') punch();
        else if (keyCode == '3') toggleCar();
        else {
            int action = getGameAction(keyCode);
            if (action == Canvas.UP) cameraKeys |= CAM_UP;
            else if (action == Canvas.DOWN) cameraKeys |= CAM_DOWN;
            else if (action == Canvas.LEFT) cameraKeys |= CAM_LEFT;
            else if (action == Canvas.RIGHT) cameraKeys |= CAM_RIGHT;
        }
    }

    protected void keyReleased(int keyCode) {
        if (keyCode == '2') keys &= ~FORWARD;
        else if (keyCode == '8') keys &= ~BACK;
        else if (keyCode == '4') keys &= ~LEFT;
        else if (keyCode == '6') keys &= ~RIGHT;
        else if (keyCode == '9') keys &= ~CROUCH;
        else {
            int action = getGameAction(keyCode);
            if (action == Canvas.UP) cameraKeys &= ~CAM_UP;
            else if (action == Canvas.DOWN) cameraKeys &= ~CAM_DOWN;
            else if (action == Canvas.LEFT) cameraKeys &= ~CAM_LEFT;
            else if (action == Canvas.RIGHT) cameraKeys &= ~CAM_RIGHT;
        }
    }

    protected void paint(Graphics g) {
        g.setColor(0x87B6D8);
        g.fillRect(0, 0, getWidth(), getHeight());
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
            g.setColor(0xFFFFFF);
            g.drawString("3D RENDER ERROR", 3, 3, Graphics.TOP | Graphics.LEFT);
            g.drawString(error.toString(), 3, 20, Graphics.TOP | Graphics.LEFT);
        } finally {
            try { g3d.releaseTarget(); } catch (Throwable ignored) {}
        }

        g.setColor(0x101010);
        g.fillRect(0, 0, getWidth(), 17);
        g.setColor(0xFFFFFF);
        g.drawString(status, 3, 1, Graphics.TOP | Graphics.LEFT);
        g.drawString("2/8 MOVE  4/6 STRAFE  D-PAD CAMERA", 3,
                     getHeight() - 16, Graphics.TOP | Graphics.LEFT);
    }
}
