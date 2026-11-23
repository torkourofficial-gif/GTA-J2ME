package gta3me;

import javax.microedition.lcdui.Image;
import javax.microedition.m3g.Image2D;
import javax.microedition.m3g.Texture2D;

/** Small deterministic textures generated at startup for the city scene. */
final class ProceduralTextures {
    static final int ASPHALT = 0;
    static final int CONCRETE = 1;
    static final int GRASS = 2;
    static final int BRICK = 3;
    static final int GLASS = 4;
    static final int BARK = 5;
    static final int LEAVES = 6;
    static final int METAL = 7;

    private static final int TILE_SIZE = 32;
    static final int SKY_WIDTH = 256;
    static final int SKY_HEIGHT = 128;
    static final int SKY_VIEW_WIDTH = 112;
    static final int SKY_VIEW_HEIGHT = 128;

    private ProceduralTextures() {
    }

    static Texture2D createTexture(int style, int baseColor, int seed) {
        int[] pixels = new int[TILE_SIZE * TILE_SIZE];
        int baseR = (baseColor >> 16) & 255;
        int baseG = (baseColor >> 8) & 255;
        int baseB = baseColor & 255;
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int noise = randomBits(x, y, seed) & 31;
                int delta = noise - 15;
                int r = baseR + delta;
                int g = baseG + delta;
                int b = baseB + delta;
                if (style == BRICK) {
                    int row = y / 8;
                    int offset = (row & 1) * 8;
                    boolean mortar = (y % 8 == 0) || ((x + offset) % 16 == 0);
                    if (mortar) {
                        r -= 34; g -= 32; b -= 28;
                    } else {
                        int variation = (randomBits(x / 4, y / 4, seed + 91) & 15) - 7;
                        r += variation; g += variation; b += variation;
                    }
                } else if (style == ASPHALT) {
                    int grain = randomBits(x, y, seed + 17) & 15;
                    r += grain - 7; g += grain - 7; b += grain - 6;
                    if ((x * 13 + y * 7 + seed) % 83 == 0) { r -= 28; g -= 27; b -= 25; }
                } else if (style == CONCRETE) {
                    if ((randomBits(x, y, seed + 23) & 31) == 0) { r -= 30; g -= 29; b -= 27; }
                } else if (style == GRASS) {
                    int green = (randomBits(x, y, seed + 31) & 31) - 15;
                    r += green / 3; g += green; b += green / 4;
                } else if (style == GLASS) {
                    int shine = ((x + seed) % 13 < 2) ? 52 : 0;
                    r += shine / 3; g += shine; b += shine;
                    if ((y + seed) % 16 == 0) { r -= 20; g -= 20; b -= 15; }
                } else if (style == BARK) {
                    int grain = (randomBits(x / 2, y, seed + 41) & 31) - 16;
                    if ((x + seed) % 7 == 0) grain -= 24;
                    r += grain; g += grain / 2; b += grain / 3;
                } else if (style == LEAVES) {
                    int leaf = (randomBits(x / 2, y / 2, seed + 53) & 63) - 31;
                    r += leaf / 2; g += leaf; b += leaf / 3;
                } else if (style == METAL) {
                    int stripe = (y % 8 == 0) ? 22 : 0;
                    r += stripe + delta / 2; g += stripe + delta / 2; b += stripe + delta / 2;
                }
                pixels[y * TILE_SIZE + x] = rgb(clamp(r), clamp(g), clamp(b));
            }
        }
        Image image = Image.createRGBImage(pixels, TILE_SIZE, TILE_SIZE, false);
        return makeTexture(image);
    }

    static Image2D createCloudSky() {
        int[] pixels = new int[SKY_WIDTH * SKY_HEIGHT];
        for (int y = 0; y < SKY_HEIGHT; y++) {
            float horizon = (float) y / (float) (SKY_HEIGHT - 1);
            int skyR = (int) (20 + horizon * 105);
            int skyG = (int) (58 + horizon * 125);
            int skyB = (int) (112 + horizon * 112);
            for (int x = 0; x < SKY_WIDTH; x++) {
                float broad = valueNoise(x, y, 8, 4, 11);
                float middle = valueNoise(x, y, 19, 8, 29);
                float detail = valueNoise(x, y, 43, 17, 47);
                float cloud = broad * 0.54f + middle * 0.31f + detail * 0.15f;
                float horizonFade = 1.0f - smoothstep(0.78f, 1.0f, horizon);
                float density = smoothstep(0.54f, 0.72f, cloud) * horizonFade * 0.88f;
                int cloudR = 234;
                int cloudG = 239;
                int cloudB = 244;
                int r = (int) (skyR + (cloudR - skyR) * density);
                int g = (int) (skyG + (cloudG - skyG) * density);
                int b = (int) (skyB + (cloudB - skyB) * density);
                pixels[y * SKY_WIDTH + x] = rgb(r, g, b);
            }
        }
        Image image = Image.createRGBImage(pixels, SKY_WIDTH, SKY_HEIGHT, false);
        return new Image2D(Image2D.RGB, image);
    }

    private static Texture2D makeTexture(Image image) {
        Image2D image2D = new Image2D(Image2D.RGB, image);
        Texture2D texture = new Texture2D(image2D);
        texture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
        texture.setWrapping(Texture2D.WRAP_REPEAT, Texture2D.WRAP_REPEAT);
        return texture;
    }

    private static float valueNoise(int x, int y, int cellsX, int cellsY, int seed) {
        float px = ((float) x / (float) SKY_WIDTH) * (float) cellsX;
        float py = ((float) y / (float) SKY_HEIGHT) * (float) cellsY;
        int ix = (int) px;
        int iy = (int) py;
        float fx = smooth(px - (float) ix);
        float fy = smooth(py - (float) iy);
        int x0 = ix % cellsX;
        int x1 = (ix + 1) % cellsX;
        int y0 = iy;
        int y1 = iy + 1;
        float a = randomValue(x0, y0, seed);
        float b = randomValue(x1, y0, seed);
        float c = randomValue(x0, y1, seed);
        float d = randomValue(x1, y1, seed);
        float top = a + (b - a) * fx;
        float bottom = c + (d - c) * fx;
        return top + (bottom - top) * fy;
    }

    private static float randomValue(int x, int y, int seed) {
        return (float) (randomBits(x, y, seed) & 65535) / 65535.0f;
    }

    private static int randomBits(int x, int y, int seed) {
        int value = x * 374761393 + y * 668265263 + seed * 69069;
        value = (value ^ (value >>> 13)) * 1274126177;
        value ^= value >>> 16;
        return value;
    }

    private static float smooth(float value) {
        return value * value * (3.0f - 2.0f * value);
    }

    private static float smoothstep(float low, float high, float value) {
        if (value <= low) return 0.0f;
        if (value >= high) return 1.0f;
        return smooth((value - low) / (high - low));
    }

    private static int rgb(int r, int g, int b) {
        return 0xff000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int clamp(int value) {
        if (value < 0) return 0;
        if (value > 255) return 255;
        return value;
    }
}
