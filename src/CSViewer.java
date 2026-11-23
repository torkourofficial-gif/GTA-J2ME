package gta3me;

import java.util.Vector;
import java.io.InputStream;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.m3g.Background;
import javax.microedition.m3g.Camera;
import javax.microedition.m3g.Graphics3D;
import javax.microedition.m3g.Light;
import javax.microedition.m3g.Loader;
import javax.microedition.m3g.Node;
import javax.microedition.m3g.Object3D;
import javax.microedition.m3g.Transform;
import javax.microedition.m3g.World;

/** Minimal J2ME/M3G viewer for the CS.m3g asset in this repository. */
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
        private Node model;
        private Camera camera;
        private Thread loop;
        private volatile boolean running;
        private String error;
        private int errorPage;
        private float angle;
        private float cameraDistance = 8.0f;

        ViewerCanvas() {
            setFullScreenMode(true);
            loadModel();
        }

        private void loadModel() {
            try {
                InputStream resource = getClass().getResourceAsStream("/CS.m3g");
                if (resource == null) {
                    error = "Missing /CS.m3g inside the JAR. Make sure res/CS.m3g is included, then rebuild in NetBeans.";
                    return;
                }
                resource.close();

                Object3D[] roots = Loader.load("/CS.m3g");
                if (roots == null || roots.length == 0) {
                    error = "CS.m3g has no scene objects";
                    return;
                }

                for (int i = 0; i < roots.length; i++) {
                    if (roots[i] instanceof World) {
                        world = (World) roots[i];
                        break;
                    }
                }

                if (world == null) {
                    world = new World();
                }

                // Prefer a top-level node from the file. If it only contains a World,
                // use its first child as the rotatable model root.
                for (int i = 0; i < roots.length; i++) {
                    if (roots[i] instanceof Node && !(roots[i] instanceof World)
                            && !(roots[i] instanceof Camera) && !(roots[i] instanceof Light)) {
                        model = (Node) roots[i];
                        if (model.getParent() == null) {
                            world.addChild(model);
                        }
                        break;
                    }
                }
                if (model == null) {
                    for (int i = 0; i < world.getChildCount(); i++) {
                        Node child = world.getChild(i);
                        if (!(child instanceof Camera) && !(child instanceof Light)) {
                            model = child;
                            break;
                        }
                    }
                }
                if (model == null) {
                    error = "No renderable model node found in CS.m3g";
                    return;
                }

                camera = world.getActiveCamera();
                if (camera == null) {
                    camera = new Camera();
                    world.addChild(camera);
                    world.setActiveCamera(camera);
                }
                setCameraTransform();
                camera.setPerspective(45.0f, 1.0f, 0.1f, 1000.0f);

                if (world.getBackground() == null) {
                    Background background = new Background();
                    background.setColor(0x18202b);
                    world.setBackground(background);
                }
                if (world.getChildCount() < 2) {
                    Light light = new Light();
                    light.setMode(Light.AMBIENT);
                    light.setIntensity(1.0f);
                    world.addChild(light);
                }
            } catch (Exception e) {
                error = "Could not load CS.m3g: " + e.toString();
            }
        }

        private void setCameraTransform() {
            Transform transform = new Transform();
            transform.setIdentity();
            transform.postTranslate(0.0f, 0.0f, cameraDistance);
            camera.setTransform(transform);
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
            g.drawString("CS.m3g  |  Left/Right rotate  Up/Down zoom", 3, 2, Graphics.TOP | Graphics.LEFT);
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
            g.drawString("CS.m3g viewer error", margin, 4, Graphics.TOP | Graphics.LEFT);

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
            int action = getGameAction(keyCode);
            if (error != null) {
                if (action == UP) {
                    errorPage--;
                } else if (action == DOWN) {
                    errorPage++;
                }
                repaint();
                return;
            }
            if (model == null || camera == null) {
                return;
            }
            if (action == LEFT) {
                angle -= 10.0f;
                model.setOrientation(angle, 0.0f, 1.0f, 0.0f);
            } else if (action == RIGHT) {
                angle += 10.0f;
                model.setOrientation(angle, 0.0f, 1.0f, 0.0f);
            } else if (action == UP) {
                cameraDistance -= 0.5f;
                if (cameraDistance < 1.0f) cameraDistance = 1.0f;
                setCameraTransform();
            } else if (action == DOWN) {
                cameraDistance += 0.5f;
                if (cameraDistance > 100.0f) cameraDistance = 100.0f;
                setCameraTransform();
            }
            repaint();
        }
    }
}





