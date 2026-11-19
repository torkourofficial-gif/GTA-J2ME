package gta;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;

public final class MainMIDlet extends MIDlet {
    private GameCanvas game;

    public void startApp() {
        if (game == null) {
            game = new GameCanvas();
        }
        Display.getDisplay(this).setCurrent(game);
        game.start();
    }

    public void pauseApp() {
        if (game != null) game.pause();
    }

    public void destroyApp(boolean unconditional) {
        if (game != null) game.stop();
        game = null;
    }
}
