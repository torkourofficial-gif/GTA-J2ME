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
    private int carTransition = 0;
    private float carTransitionTime = 0.0f;
    private float transitionStartX, transitionStartZ, transitionEndX, transitionEndZ;
    private boolean transitionDriverSide = true;
    private static final float CAR_TRANSITION_LENGTH = 0.9f;
    private boolean reverseFacing = false;
    private float reverseTargetHeading = 0.0f;
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
        camera.setPerspective(62.0f, 1.0f, 0.25f, 90.0f);
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
        if (carTransition != 0) { updateCarTransition(dt); updateCamera(); return; }
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
            float nextX = carX + (float)Math.sin(radians) * carSpeed * dt;
            float nextZ = carZ - (float)Math.cos(radians) * carSpeed * dt;
            if (!carBlocked(nextX, carZ)) carX = nextX; else carSpeed = 0.0f;
            if (!carBlocked(carX, nextZ)) carZ = nextZ; else carSpeed = 0.0f;
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

            // 4/6 turn CJ. Holding 8 makes him turn around and run in the
            // direction he is now facing, rather than moonwalking backwards.
            if (side != 0.0f) {
                playerHeading = normalizeAngle(playerHeading - side * 150.0f * dt);
            }
            if ((keys & BACK) != 0 && !reverseFacing) {
                reverseFacing = true;
                reverseTargetHeading = normalizeAngle(playerHeading + 180.0f);
            } else if ((keys & BACK) == 0) {
                reverseFacing = false;
            }
            if (reverseFacing) {
                float difference = normalizeAngle(reverseTargetHeading - playerHeading);
                float maxTurn = 300.0f * dt;
                if (difference > maxTurn) difference = maxTurn;
                else if (difference < -maxTurn) difference = -maxTurn;
                playerHeading = normalizeAngle(playerHeading + difference);
            }

            float inputLength = (float)Math.sqrt(forward * forward + side * side);
            if (inputLength > 1.0f) inputLength = 1.0f;

            float angle = playerHeading * 0.0174532925f;
            float targetSpeed = ((keys & BACK) != 0 || (keys & FORWARD) != 0) ? 3.0f : 0.0f;
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
                   false, jumping, crouching, turnAmount);
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

    private float normalizeAngle(float angle) {
        while (angle > 180.0f) angle -= 360.0f;
        while (angle < -180.0f) angle += 360.0f;
        return angle;
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
        if (w > 0 && h > 0) camera.setPerspective(62.0f, w / h, 0.25f, 90.0f);
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
        if (carTransition != 0) return;
        if (inCar) {
            beginCarTransition(false);
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
        cj.setPosition(playerX, playerY, playerZ);
        cj.setHeading(playerHeading);
        cj.setVisible(true);
        cj.setEntryPose(entering ? 0.0f : 1.0f, entering);
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
        cj.setPosition(playerX, playerY, playerZ);
        cj.setHeading(normalizeAngle(carHeading + 90.0f));
        float pose = carTransition == 1 ? moveSmooth : 1.0f - moveSmooth;
        cj.setEntryPose(pose, carTransition == 1);

        if (t >= 1.0f) {
            car.setDoorOpen(0.0f, transitionDriverSide);
            cj.setEntryPose(0.0f, carTransition == 1);
            if (carTransition == 1) {
                inCar = true;
                cj.setVisible(false);
                playerX = carX;
                playerZ = carZ;
                status = "DRIVING  |  3 EXIT";
            } else {
                inCar = false;
                cj.setVisible(true);
                playerX = transitionEndX;
                playerZ = transitionEndZ;
                cj.setPosition(playerX, playerY, playerZ);
                status = "ON FOOT  |  3 ENTER CAR";
            }
            carTransition = 0;
        }
    }

    private boolean playerBlocked(float x, float z) {
        if (x < -20.5f || x > 20.5f || z < -20.5f || z > 20.5f) return true;
        if (circleHitsRect(x,z,0.34f,-6.0f,-5.0f,3.8f,5.0f)) return true;
        if (circleHitsRect(x,z,0.34f,6.0f,-7.0f,4.2f,6.0f)) return true;
        if (circleHitsRect(x,z,0.34f,-6.0f,5.0f,4.0f,4.0f)) return true;
        if (circleHitsRect(x,z,0.34f,6.0f,6.0f,3.5f,5.0f)) return true;
        if (circleHitsRect(x,z,0.34f,-3.7f,-4.0f,1.8f,1.8f)) return true;

        float a = carHeading * 0.0174532925f;
        float dx = x - carX, dz = z - carZ;
        float lx = dx * (float)Math.cos(a) - dz * (float)Math.sin(a);
        float lz = dx * (float)Math.sin(a) + dz * (float)Math.cos(a);
        float ox = Math.max(Math.abs(lx) - 0.92f, 0.0f);
        float oz = Math.max(Math.abs(lz) - 1.48f, 0.0f);
        return ox * ox + oz * oz < 0.34f * 0.34f;
    }

    private boolean carBlocked(float x, float z) {
        if (x < -18.0f || x > 18.0f || z < -18.0f || z > 18.0f) return true;
        if (circleHitsRect(x,z,1.45f,-6.0f,-5.0f,3.8f,5.0f)) return true;
        if (circleHitsRect(x,z,0.85f,6.0f,-7.0f,4.2f,6.0f)) return true;
        if (circleHitsRect(x,z,0.85f,-6.0f,5.0f,4.0f,4.0f)) return true;
        if (circleHitsRect(x,z,0.85f,6.0f,6.0f,3.5f,5.0f)) return true;
        if (circleHitsRect(x,z,0.85f,-3.7f,-4.0f,1.8f,1.8f)) return true;
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
        else if (keyCode == '8') { keys &= ~BACK; reverseFacing = false; }
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
        g.drawString("2/8 MOVE  4/6 TURN  9 CROUCH  D-PAD CAMERA", 3,
                     getHeight() - 16, Graphics.TOP | Graphics.LEFT);
    }
}
