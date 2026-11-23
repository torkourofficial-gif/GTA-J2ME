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
    private static final float WALK_SPEED = 2.3f;
    private static final float GRAVITY = 10.5f;

    private final Group actor = new Group();
    private final Node model;
    private final Camera camera;
    private final Transform chaseRotation = new Transform();
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;
    private boolean jumpRequested;
    private float x;
    private float z;
    private float yaw;
    private float jumpHeight;
    private float verticalSpeed;
    private float animationTime;
    private int state = IDLE;

    PlayerController(World world, Node model, Camera camera) {
        this.model = model;
        this.camera = camera;
        actor.addChild(model);
        world.addChild(actor);
        chaseRotation.setIdentity();
        chaseRotation.postRotate(10.0f, 1.0f, 0.0f, 0.0f);
        camera.setTransform(chaseRotation);
        updateTransforms();
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
            }
        }

        if (jumpHeight > 0.0f) {
            state = JUMP;
        } else if (moving) {
            state = WALK;
        } else {
            state = IDLE;
        }
        animationTime += elapsed * (state == WALK ? 11.0f : 2.5f);
        updateTransforms();
    }

    private void updateTransforms() {
        actor.setTranslation(x, 1.0f + jumpHeight, z);
        actor.setOrientation(yaw, 0.0f, 1.0f, 0.0f);

        float bob;
        float lean;
        float scaleY;
        if (state == WALK) {
            float step = (float) Math.sin(animationTime);
            float bounce = (float) Math.abs(step);
            bob = bounce * 0.11f;
            lean = step * 9.0f;
            scaleY = 1.0f + bounce * 0.06f;
        } else if (state == JUMP) {
            bob = 0.0f;
            lean = -10.0f;
            scaleY = verticalSpeed > 0.0f ? 1.08f : 0.94f;
        } else {
            float breath = (float) Math.sin(animationTime);
            bob = breath * 0.025f;
            lean = (float) Math.sin(animationTime * 0.5f) * 1.5f;
            scaleY = 1.0f + breath * 0.025f;
        }
        model.setTranslation(0.0f, bob, 0.0f);
        model.setOrientation(lean, 0.0f, 0.0f, 1.0f);
        model.setScale(1.0f, scaleY, 1.0f);

        // Lower shoulder-height chase camera; it stays put vertically during jumps.
        camera.setTranslation(x, 2.0f, z + 4.5f);
    }

    String getStateName() {
        if (state == WALK) return "WALK";
        if (state == JUMP) return "JUMP";
        return "IDLE";
    }

    void clearInput() {
        up = down = left = right = jumpRequested = false;
    }
}
