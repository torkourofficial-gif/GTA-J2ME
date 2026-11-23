package gta3me;

import javax.microedition.lcdui.Canvas;
import javax.microedition.m3g.Camera;
import javax.microedition.m3g.Group;
import javax.microedition.m3g.Node;
import javax.microedition.m3g.Transform;
import javax.microedition.m3g.World;

/** Keyboard movement, simple jump physics, follow camera, and procedural animation. */
final class PlayerController {
    private static final int IDLE = 0;
    private static final int WALK = 1;
    private static final int JUMP = 2;
    private static final int LAND = 3;
    private static final float WALK_SPEED = 2.3f;
    private static final float GRAVITY = 10.5f;
    private static final float CAMERA_TURN_SPEED = 120.0f;
    private static final float CAMERA_TILT_SPEED = 45.0f;
    // This OBJ faces opposite the controller's forward direction (+Z).
    private static final float MODEL_FORWARD_OFFSET = 180.0f;

    private final Group actor = new Group();
    private final ClaudeModel.Rig rig;
    private final Node model;
    private final Camera camera;
    private final Transform chaseRotation = new Transform();
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;
    private boolean cameraUp;
    private boolean cameraDown;
    private boolean cameraLeft;
    private boolean cameraRight;
    private boolean jumpRequested;
    private float x;
    private float z;
    private float yaw;
    private float cameraYaw;
    private float cameraPitch = 5.0f;
    private float cameraDistance = 4.5f;
    private float jumpHeight;
    private float verticalSpeed;
    private float landingTime;
    private float animationTime;
    private int state = IDLE;

    PlayerController(World world, ClaudeModel.Rig rig, Camera camera) {
        this.rig = rig;
        this.model = rig.getMesh();
        this.camera = camera;
        actor.addChild(model);
        world.addChild(actor);
        updateCameraRotation();
        updateTransforms();
    }

    boolean adjustCamera(int keyCode) {
        if (keyCode == Canvas.KEY_NUM1) {
            cameraDistance -= 0.5f;
            if (cameraDistance < 2.5f) cameraDistance = 2.5f;
        } else if (keyCode == Canvas.KEY_NUM3) {
            cameraDistance += 0.5f;
            if (cameraDistance > 8.0f) cameraDistance = 8.0f;
        } else {
            return false;
        }
        updateCameraRotation();
        updateTransforms();
        return true;
    }

    void setCameraDirection(int gameAction, boolean pressed) {
        if (gameAction == Canvas.UP) {
            cameraUp = pressed;
        } else if (gameAction == Canvas.DOWN) {
            cameraDown = pressed;
        } else if (gameAction == Canvas.LEFT) {
            cameraLeft = pressed;
        } else if (gameAction == Canvas.RIGHT) {
            cameraRight = pressed;
        }
    }

    private void updateCameraRotation() {
        chaseRotation.setIdentity();
        chaseRotation.postRotate(cameraYaw, 0.0f, 1.0f, 0.0f);
        chaseRotation.postRotate(-cameraPitch, 1.0f, 0.0f, 0.0f);
        camera.setTransform(chaseRotation);
    }

    void setDirection(int gameAction, boolean pressed) {
        if (gameAction == Canvas.UP) {
            up = pressed;
        } else if (gameAction == Canvas.DOWN) {
            down = pressed;
        } else if (gameAction == Canvas.LEFT) {
            left = pressed;
        } else if (gameAction == Canvas.RIGHT) {
            right = pressed;
        }
    }

    void requestJump() {
        if (jumpHeight <= 0.0f && verticalSpeed <= 0.0f) {
            jumpRequested = true;
        }
    }

    void update(float elapsed) {
        if (elapsed < 0.0f) elapsed = 0.0f;
        if (elapsed > 0.1f) elapsed = 0.1f;

        updateCameraInput(elapsed);

        float moveX = (right ? 1.0f : 0.0f) - (left ? 1.0f : 0.0f);
        float moveZ = (down ? 1.0f : 0.0f) - (up ? 1.0f : 0.0f);
        boolean moving = moveX != 0.0f || moveZ != 0.0f;
        if (moving) {
            float length = (float) Math.sqrt(moveX * moveX + moveZ * moveZ);
            moveX /= length;
            moveZ /= length;
            float nextX = x + moveX * WALK_SPEED * elapsed;
            float nextZ = z + moveZ * WALK_SPEED * elapsed;
            if (nextX < -18.5f) nextX = -18.5f;
            if (nextX > 18.5f) nextX = 18.5f;
            if (nextZ < -18.5f) nextZ = -18.5f;
            if (nextZ > 18.5f) nextZ = 18.5f;
            if (!CityMap.blocks(nextX, z)) x = nextX;
            if (!CityMap.blocks(x, nextZ)) z = nextZ;
            if (moveX > 0.0f) {
                yaw = moveZ < 0.0f ? 135.0f : (moveZ > 0.0f ? 45.0f : 90.0f);
            } else if (moveX < 0.0f) {
                yaw = moveZ < 0.0f ? -135.0f : (moveZ > 0.0f ? -45.0f : -90.0f);
            } else if (moveZ < 0.0f) {
                yaw = 180.0f;
            } else if (moveZ > 0.0f) {
                yaw = 0.0f;
            }
        }

        if (jumpRequested && jumpHeight <= 0.0f) {
            verticalSpeed = 4.8f;
        }
        jumpRequested = false;
        if (verticalSpeed > 0.0f || jumpHeight > 0.0f) {
            jumpHeight += verticalSpeed * elapsed;
            verticalSpeed -= GRAVITY * elapsed;
            if (jumpHeight <= 0.0f) {
                jumpHeight = 0.0f;
                verticalSpeed = 0.0f;
                landingTime = 0.22f;
            }
        } else if (landingTime > 0.0f) {
            landingTime -= elapsed;
            if (landingTime < 0.0f) landingTime = 0.0f;
        }

        if (jumpHeight > 0.0f) {
            state = JUMP;
        } else if (landingTime > 0.0f) {
            state = LAND;
        } else if (moving) {
            state = WALK;
        } else {
            state = IDLE;
        }
        animationTime += elapsed * (state == WALK ? 10.5f : (state == IDLE ? 1.8f : 5.0f));
        rig.animate(state, animationTime);
        updateTransforms();
    }

    private void updateTransforms() {
        float scaleY;
        if (state == WALK) {
            float step = (float) Math.sin(animationTime);
            float bounce = (float) Math.abs(step);
            scaleY = 1.0f + bounce * 0.025f;
        } else if (state == JUMP) {
            scaleY = verticalSpeed > 0.0f ? 1.04f : 0.96f;
        } else if (state == LAND) {
            float recovery = landingTime / 0.22f;
            scaleY = 0.92f + recovery * 0.08f;
        } else {
            float breath = (float) Math.sin(animationTime);
            scaleY = 1.0f + breath * 0.012f;
        }
        actor.setTranslation(x, 1.0f + jumpHeight + (1.0f - scaleY) * 0.45f, z);
        actor.setOrientation(yaw + MODEL_FORWARD_OFFSET, 0.0f, 1.0f, 0.0f);

        float bob;
        float lean;
        if (state == WALK) {
            float step = (float) Math.sin(animationTime);
            float bounce = (float) Math.abs(step);
            bob = bounce * 0.045f;
            lean = step * 4.0f;
        } else if (state == JUMP) {
            bob = 0.0f;
            lean = -10.0f;
        } else if (state == LAND) {
            bob = 0.0f;
            lean = 2.0f;
        } else {
            float breath = (float) Math.sin(animationTime);
            bob = breath * 0.012f;
            lean = (float) Math.sin(animationTime * 0.5f) * 0.7f;
        }
        model.setTranslation(0.0f, bob, 0.0f);
        model.setOrientation(lean, 0.0f, 0.0f, 1.0f);
        model.setScale(1.0f, scaleY, 1.0f);

        // Lower shoulder-height chase camera; it stays put vertically during jumps.
        float orbitRadians = cameraYaw * 0.017453292f;
        float cameraX = x + (float) Math.sin(orbitRadians) * cameraDistance;
        float cameraZ = z + (float) Math.cos(orbitRadians) * cameraDistance;
        camera.setTranslation(cameraX, 1.9f, cameraZ);
    }

    private void updateCameraInput(float elapsed) {
        boolean changed = false;
        float turn = CAMERA_TURN_SPEED * elapsed;
        if (cameraLeft) { cameraYaw -= turn; changed = true; }
        if (cameraRight) { cameraYaw += turn; changed = true; }
        float tilt = CAMERA_TILT_SPEED * elapsed;
        if (cameraUp) { cameraPitch -= tilt; changed = true; }
        if (cameraDown) { cameraPitch += tilt; changed = true; }
        if (cameraPitch < 0.0f) cameraPitch = 0.0f;
        if (cameraPitch > 35.0f) cameraPitch = 35.0f;
        if (changed) updateCameraRotation();
    }

    String getStateName() {
        if (state == WALK) return "WALK";
        if (state == JUMP) return "JUMP";
        if (state == LAND) return "LAND";
        return "IDLE";
    }

    void clearInput() {
        up = down = left = right = jumpRequested = false;
        cameraUp = cameraDown = cameraLeft = cameraRight = false;
    }
}
