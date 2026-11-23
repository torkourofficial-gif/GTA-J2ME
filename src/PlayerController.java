package gta3me;

import javax.microedition.lcdui.Canvas;
import javax.microedition.m3g.Camera;
import javax.microedition.m3g.Group;
import javax.microedition.m3g.Node;
import javax.microedition.m3g.Transform;
import javax.microedition.m3g.World;

/** Keyboard movement, GTA-style acceleration, turning, sprinting and follow camera. */
final class PlayerController {
    private static final int IDLE = 0;
    private static final int WALK = 1;
    private static final int JUMP = 2;
    private static final int LAND = 3;
    private static final int RUN = 4;
    private static final int SPRINT = 5;

    private static final float RUN_SPEED = 2.3f;
    private static final float SPRINT_SPEED = 3.4f;
    private static final float ACCELERATION = 6.5f;
    private static final float BRAKING = 8.0f;
    private static final float TURN_RATE = 540.0f;
    private static final float MAX_STAMINA = 8.0f;
    private static final float STAMINA_DRAIN = 1.0f;
    private static final float STAMINA_RECOVERY = 0.55f;
    private static final float GRAVITY = 10.5f;
    private static final float CAMERA_TURN_SPEED = 120.0f;
    private static final float CAMERA_TILT_SPEED = 45.0f;
    // This OBJ faces opposite the controller's forward direction (+Z).
    private static final float MODEL_FORWARD_OFFSET = 180.0f;
    private static final float RAD_TO_DEG = 57.2957795f;

    private final Group actor = new Group();
    private final ClaudeModel.Rig rig;
    private final Node model;
    private final Camera camera;
    private final Transform chaseRotation = new Transform();
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;
    private boolean sprint;
    private boolean lastSprinting;
    private boolean sprintStopPending;
    private boolean cameraUp;
    private boolean cameraDown;
    private boolean cameraLeft;
    private boolean cameraRight;
    private boolean jumpRequested;
    private float x;
    private float z;
    private float velocityX;
    private float velocityZ;
    private float yaw;
    private float cameraYaw;
    private float cameraPitch = 5.0f;
    private float cameraDistance = 4.5f;
    private float jumpHeight;
    private float verticalSpeed;
    private float landingTime;
    private float animationTime;
    private float gaitBlend;
    private int animationTransition;
    private float transitionTime;
    private float lastHorizontalSpeed;
    private float stamina = MAX_STAMINA;
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

    void setSprint(boolean pressed) {
        sprint = pressed;
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

        float inputX = (right ? 1.0f : 0.0f) - (left ? 1.0f : 0.0f);
        float inputZ = (down ? 1.0f : 0.0f) - (up ? 1.0f : 0.0f);
        boolean hasInput = inputX != 0.0f || inputZ != 0.0f;

        if (hasInput) {
            float inputLength = (float) Math.sqrt(inputX * inputX + inputZ * inputZ);
            inputX /= inputLength;
            inputZ /= inputLength;

            // Convert the keyboard direction from camera space to world space.
            float cameraRadians = cameraYaw * 0.017453292f;
            float cameraSin = (float) Math.sin(cameraRadians);
            float cameraCos = (float) Math.cos(cameraRadians);
            float worldInputX = inputX * cameraCos + inputZ * cameraSin;
            float worldInputZ = -inputX * cameraSin + inputZ * cameraCos;

            // Face the actual travel direction: pressing 8 turns Claude around.
            float targetYaw = directionAngle(worldInputX, worldInputZ);
            yaw = turnTowards(yaw, targetYaw, TURN_RATE * elapsed);
        }

        boolean sprinting = sprint && hasInput && stamina > 0.0f;
        if (lastSprinting && !hasInput) {
            sprintStopPending = true;
        }
        if (hasInput) {
            sprintStopPending = false;
        }
        lastSprinting = sprinting;
        if (sprinting) {
            stamina -= STAMINA_DRAIN * elapsed;
            if (stamina < 0.0f) stamina = 0.0f;
        } else {
            stamina += STAMINA_RECOVERY * elapsed;
            if (stamina > MAX_STAMINA) stamina = MAX_STAMINA;
        }

        float targetSpeed = sprinting ? SPRINT_SPEED : RUN_SPEED;
        float targetVelocityX = 0.0f;
        float targetVelocityZ = 0.0f;
        if (hasInput) {
            float cameraRadians = cameraYaw * 0.017453292f;
            float cameraSin = (float) Math.sin(cameraRadians);
            float cameraCos = (float) Math.cos(cameraRadians);
            targetVelocityX = (inputX * cameraCos + inputZ * cameraSin) * targetSpeed;
            targetVelocityZ = (-inputX * cameraSin + inputZ * cameraCos) * targetSpeed;
        }

        // Approach the requested velocity instead of snapping to full speed.
        float deltaX = targetVelocityX - velocityX;
        float deltaZ = targetVelocityZ - velocityZ;
        float deltaLength = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float maxChange = (hasInput ? ACCELERATION : BRAKING) * elapsed;
        if (deltaLength <= maxChange || deltaLength == 0.0f) {
            velocityX = targetVelocityX;
            velocityZ = targetVelocityZ;
        } else {
            velocityX += deltaX / deltaLength * maxChange;
            velocityZ += deltaZ / deltaLength * maxChange;
        }

        float nextX = x + velocityX * elapsed;
        float nextZ = z + velocityZ * elapsed;
        if (nextX < -18.5f) {
            nextX = -18.5f;
            velocityX = 0.0f;
        } else if (nextX > 18.5f) {
            nextX = 18.5f;
            velocityX = 0.0f;
        }
        if (nextZ < -18.5f) {
            nextZ = -18.5f;
            velocityZ = 0.0f;
        } else if (nextZ > 18.5f) {
            nextZ = 18.5f;
            velocityZ = 0.0f;
        }
        if (!CityMap.blocks(nextX, z)) {
            x = nextX;
        } else {
            velocityX = 0.0f;
        }
        if (!CityMap.blocks(x, nextZ)) {
            z = nextZ;
        } else {
            velocityZ = 0.0f;
        }

        float horizontalSpeed = (float) Math.sqrt(velocityX * velocityX + velocityZ * velocityZ);
        if (!hasInput && horizontalSpeed > 0.15f) {
            float driftYaw = directionAngle(velocityX, velocityZ);
            yaw = turnTowards(yaw, driftYaw, TURN_RATE * elapsed);
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
            sprintStopPending = false;
            animationTransition = 0;
            transitionTime = 0.0f;
        } else if (landingTime > 0.0f) {
            state = LAND;
            sprintStopPending = false;
            if (animationTransition != ClaudeModel.LANDING) {
                animationTransition = ClaudeModel.LANDING;
                transitionTime = 0.0f;
            }
        } else if (horizontalSpeed > 2.45f) {
            state = SPRINT;
        } else if (horizontalSpeed > 0.85f) {
            state = RUN;
        } else if (horizontalSpeed > 0.12f) {
            state = WALK;
        } else {
            state = IDLE;
        }

        // Use a short one-shot start/stop clip, like GTA III's STARTWALK and RUNSTOP associations.
        if (jumpHeight <= 0.0f && landingTime <= 0.0f) {
            if (hasInput && lastHorizontalSpeed < 0.18f && horizontalSpeed > 0.12f) {
                animationTransition = ClaudeModel.START_WALK;
                transitionTime = 0.0f;
            } else if (!hasInput && sprintStopPending && horizontalSpeed < 0.4f) {
                int stopClip = (((int) (animationTime / 3.1415926f)) & 1) == 0
                        ? ClaudeModel.RUN_STOP_LEFT : ClaudeModel.RUN_STOP_RIGHT;
                animationTransition = stopClip;
                transitionTime = 0.0f;
                sprintStopPending = false;
            } else if (hasInput && (animationTransition == ClaudeModel.RUN_STOP_LEFT
                    || animationTransition == ClaudeModel.RUN_STOP_RIGHT)) {
                animationTransition = 0;
                transitionTime = 0.0f;
            }
        }

        if (animationTransition != 0) {
            float clipDuration = animationTransition == ClaudeModel.START_WALK ? 0.34f
                    : (animationTransition == ClaudeModel.LANDING ? 0.22f : 0.48f);
            transitionTime += elapsed / clipDuration;
            if (transitionTime >= 1.0f) {
                transitionTime = 1.0f;
                animationTransition = 0;
                transitionTime = 0.0f;
            }
        }
        lastHorizontalSpeed = horizontalSpeed;

        // Blend into and out of the gait instead of snapping from idle to a full stride.
        float targetGaitBlend = isMovingState() ? 1.0f : 0.0f;
        float blendRate = targetGaitBlend > gaitBlend ? 5.5f : 3.5f;
        float blendStep = blendRate * elapsed;
        if (targetGaitBlend > gaitBlend) {
            gaitBlend += blendStep;
            if (gaitBlend > targetGaitBlend) gaitBlend = targetGaitBlend;
        } else {
            gaitBlend -= blendStep;
            if (gaitBlend < targetGaitBlend) gaitBlend = targetGaitBlend;
        }

        float animationRate;
        if (isMovingState()) {
            // Keep cadence continuous as GTA-style walk/run/sprint blends change.
            animationRate = 6.4f + horizontalSpeed * 2.25f;
        } else if (state == IDLE) {
            animationRate = 1.8f;
        } else {
            animationRate = 5.0f;
        }
        animationTime += elapsed * animationRate;
        rig.animate(state, animationTime, gaitBlend, horizontalSpeed, animationTransition, transitionTime);
        updateTransforms();
    }

    // Java ME friendly direction angle approximation. No inverse-trigonometric APIs.
    // Returns the yaw of a vector measured from +Z, in degrees.
    private float directionAngle(float x, float z) {
        if (x == 0.0f && z == 0.0f) return yaw;

        float ax = x < 0.0f ? -x : x;
        float az = z < 0.0f ? -z : z;
        float ratio;
        float angle;

        if (ax <= az) {
            ratio = ax / az;
            angle = ratio * (45.0f - 14.0f * (1.0f - ratio));
        } else {
            ratio = az / ax;
            angle = 90.0f - ratio * (45.0f - 14.0f * (1.0f - ratio));
        }

        if (x >= 0.0f) {
            if (z < 0.0f) angle = 180.0f - angle;
        } else {
            if (z < 0.0f) angle = angle - 180.0f;
            else angle = -angle;
        }
        return angle;
    }

    private float turnTowards(float current, float target, float maxTurn) {
        float difference = target - current;
        while (difference > 180.0f) difference -= 360.0f;
        while (difference < -180.0f) difference += 360.0f;
        if (difference > maxTurn) difference = maxTurn;
        if (difference < -maxTurn) difference = -maxTurn;
        return current + difference;
    }

    private boolean isMovingState() {
        return state == WALK || state == RUN || state == SPRINT;
    }

    private void updateTransforms() {
        float scaleY;
        if (isMovingState()) {
            float step = (float) Math.sin(animationTime);
            float bounce = (float) Math.abs(step);
            scaleY = 1.0f + bounce * (state == SPRINT ? 0.045f : (state == RUN ? 0.035f : 0.018f));
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
        if (isMovingState()) {
            float step = (float) Math.sin(animationTime);
            float bounce = (float) Math.abs(step);
            bob = bounce * (state == SPRINT ? 0.060f : (state == RUN ? 0.048f : 0.025f));
            // Forward lean is now part of the torso pose, not a rigid whole-model tilt.
            lean = 0.0f;
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
        if (state == RUN) return "RUN";
        if (state == SPRINT) return "SPRINT";
        if (state == JUMP) return "JUMP";
        if (state == LAND) return "LAND";
        return "IDLE";
    }

    float getCameraYaw() {
        return cameraYaw;
    }

    void clearInput() {
        up = down = left = right = sprint = jumpRequested = false;
        lastSprinting = false;
        sprintStopPending = false;
        cameraUp = cameraDown = cameraLeft = cameraRight = false;
    }
}
