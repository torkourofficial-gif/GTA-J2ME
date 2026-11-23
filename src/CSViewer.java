package gta3me;

import java.util.Vector;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.m3g.Background;
import javax.microedition.m3g.Camera;
import javax.microedition.m3g.Graphics3D;
import javax.microedition.m3g.World;

/** Minimal J2ME/M3G viewer for the generated Claude OBJ mesh. */
public final class CSViewer extends MIDlet {
    private ViewerCanvas canvas;

    protected void startApp() throws MIDletStateChangeException {
        if (canvas == null) {
            canvas = new ViewerCanvas();
        }
        Display.getDisplay(this).setCurrent(canvas);
        canvas.start();
    }

    protected void pauseApp() {
        if (canvas != null) {
            canvas.stop();
        }
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        if (canvas != null) {
            canvas.stop();
        }
    }

    private static final class ViewerCanvas extends Canvas implements Runnable {
        private final Graphics3D graphics3D = Graphics3D.getInstance();
        private World world;
        private PlayerController controller;
        private Camera camera;
        private Background skyBackground;
        private Thread loop;
        private volatile boolean running;
        private String error;
        private int errorPage;
        private long previousFrameTime;

        ViewerCanvas() {
            setFullScreenMode(true);
            loadModel();
        }

        private void loadModel() {
            try {
                Image diffuseTexture = Image.createImage("/gta3me/playa5nu.png");
                world = new World();
                CityMap.addTo(world);

                skyBackground = new Background();
                skyBackground.setColor(0xff4f86bd);
                skyBackground.setImage(ProceduralTextures.createCloudSky());
                skyBackground.setImageMode(Background.REPEAT, Background.BORDER);
                skyBackground.setCrop(0, 0, ProceduralTextures.SKY_VIEW_WIDTH,
                        ProceduralTextures.SKY_VIEW_HEIGHT);
                world.setBackground(skyBackground);

                camera = new Camera();
                world.addChild(camera);
                world.setActiveCamera(camera);
                camera.setPerspective(45.0f, 1.0f, 0.1f, 1000.0f);
                ClaudeModel.Rig claudeRig = ClaudeModel.create(diffuseTexture);
                controller = new PlayerController(world, claudeRig, camera);

            } catch (Exception e) {
                error = "Could not load Claude model or gta3me/playa5nu.png: " + e.toString();
            }
        }

        synchronized void start() {
            if (running) {
                return;
            }
            running = true;
            loop = new Thread(this);
            loop.start();
        }

        synchronized void stop() {
            running = false;
            loop = null;
            previousFrameTime = 0L;
            if (controller != null) controller.clearInput();
        }

        public void run() {
            while (running) {
                repaint();
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    // Stop is checked at the top of the next frame.
                }
            }
        }

        protected void paint(Graphics g) {
            if (error != null) {
                paintError(g);
                return;
            }

            try {
                long now = System.currentTimeMillis();
                if (previousFrameTime == 0L) previousFrameTime = now;
                float elapsed = (now - previousFrameTime) / 1000.0f;
                previousFrameTime = now;
                if (controller != null) controller.update(elapsed);
                if (skyBackground != null && controller != null) {
                    float angle = controller.getCameraYaw() % 360.0f;
                    if (angle < 0.0f) angle += 360.0f;
                    int cropX = (int) (angle * (ProceduralTextures.SKY_WIDTH
                            - ProceduralTextures.SKY_VIEW_WIDTH) / 360.0f);
                    skyBackground.setCrop(cropX, 0, ProceduralTextures.SKY_VIEW_WIDTH,
                            ProceduralTextures.SKY_VIEW_HEIGHT);
                }
                camera.setPerspective(45.0f, (float) getWidth() / (float) getHeight(), 0.1f, 1000.0f);
                graphics3D.bindTarget(g);
                graphics3D.render(world);
            } catch (Exception e) {
                error = "Render error: " + e.toString();
            } finally {
                try {
                    graphics3D.releaseTarget();
                } catch (Exception ignored) {
                }
            }

            g.setColor(0xffffff);
            Font hudFont = g.getFont();
            g.drawString("DPAD CAM  2/4/6/8 MOVE",
                    3, 2, Graphics.TOP | Graphics.LEFT);
            g.drawString("5 JUMP  7 SPRINT  1/3 ZOOM  " + controller.getStateName(), 3,
                    hudFont.getHeight() + 3, Graphics.TOP | Graphics.LEFT);
        }

        private void paintError(Graphics g) {
            int width = getWidth();
            int height = getHeight();
            int margin = 6;
            Font previousFont = g.getFont();
            Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setColor(0x10151c);
            g.fillRect(0, 0, width, height);
            g.setFont(font);
            g.setColor(0xff7777);
            g.drawString("Claude model viewer error", margin, 4, Graphics.TOP | Graphics.LEFT);

            Vector lines = wrapError(error, font, width - margin * 2);
            int lineHeight = font.getHeight() + 1;
            int linesPerPage = (height - 38) / lineHeight;
            if (linesPerPage < 1) {
                linesPerPage = 1;
            }
            int pageCount = (lines.size() + linesPerPage - 1) / linesPerPage;
            if (errorPage < 0) {
                errorPage = 0;
            } else if (errorPage >= pageCount) {
                errorPage = pageCount - 1;
            }

            g.setColor(0xffffff);
            int firstLine = errorPage * linesPerPage;
            int lastLine = firstLine + linesPerPage;
            if (lastLine > lines.size()) {
                lastLine = lines.size();
            }
            int y = 22;
            for (int i = firstLine; i < lastLine; i++) {
                g.drawString((String) lines.elementAt(i), margin, y, Graphics.TOP | Graphics.LEFT);
                y += lineHeight;
            }

            g.setColor(0xaab4c0);
            if (pageCount > 1) {
                g.drawString("Up/Down: page " + (errorPage + 1) + "/" + pageCount,
                        margin, height - font.getHeight() - 2, Graphics.TOP | Graphics.LEFT);
            }
            g.setFont(previousFont);
        }

        private Vector wrapError(String message, Font font, int maxWidth) {
            Vector lines = new Vector();
            if (message == null) {
                message = "Unknown error";
            }
            message = message.replace('\r', ' ').replace('\n', ' ');
            int start = 0;
            while (start < message.length()) {
                while (start < message.length() && message.charAt(start) == ' ') {
                    start++;
                }
                if (start >= message.length()) {
                    break;
                }

                int end = start;
                int lastSpace = -1;
                while (end < message.length()) {
                    if (message.charAt(end) == ' ') {
                        lastSpace = end;
                    }
                    if (font.stringWidth(message.substring(start, end + 1)) > maxWidth) {
                        break;
                    }
                    end++;
                }

                if (end == message.length()) {
                    lines.addElement(message.substring(start, end));
                    break;
                } else if (end == start) {
                    lines.addElement(message.substring(start, start + 1));
                    start++;
                } else if (lastSpace >= start) {
                    lines.addElement(message.substring(start, lastSpace));
                    start = lastSpace + 1;
                } else {
                    lines.addElement(message.substring(start, end));
                    start = end;
                }
            }
            if (lines.size() == 0) {
                lines.addElement(" ");
            }
            return lines;
        }

        protected void keyPressed(int keyCode) {
            if (error != null) {
                int action = getGameActionSafe(keyCode);
                if (action == UP) {
                    errorPage--;
                } else if (action == DOWN) {
                    errorPage++;
                }
                repaint();
                return;
            }
            if (controller != null) {
                if (keyCode == KEY_NUM7) {
                    controller.setSprint(true);
                    repaint();
                    return;
                }
                int moveAction = getNumberPadMove(keyCode);
                if (moveAction != 0) {
                    controller.setDirection(moveAction, true);
                    repaint();
                    return;
                }
                if (controller.adjustCamera(keyCode)) {
                    repaint();
                    return;
                }
                int action = getGameActionSafe(keyCode);
                controller.setCameraDirection(action, true);
                if (action == FIRE || keyCode == KEY_NUM5) {
                    controller.requestJump();
                }
            }
            repaint();
        }

        protected void keyReleased(int keyCode) {
            if (error != null || controller == null) return;
            if (keyCode == KEY_NUM7) {
                controller.setSprint(false);
                return;
            }
            int moveAction = getNumberPadMove(keyCode);
            if (moveAction != 0) {
                controller.setDirection(moveAction, false);
                return;
            }
            controller.setCameraDirection(getGameActionSafe(keyCode), false);
        }

        private int getNumberPadMove(int keyCode) {
            if (keyCode == KEY_NUM8) return UP;
            if (keyCode == KEY_NUM2) return DOWN;
            if (keyCode == KEY_NUM4) return LEFT;
            if (keyCode == KEY_NUM6) return RIGHT;
            return 0;
        }

        private int getGameActionSafe(int keyCode) {
            try {
                return getGameAction(keyCode);
            } catch (IllegalArgumentException e) {
                return 0;
            }
        }
    }
}
