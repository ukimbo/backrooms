package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;

public class Main {
    public static void main(String[] args) {
        int width = 80;
        int height = 45;
        TERenderer ter = new TERenderer();
        ter.initialize(width, height);

        World2 myWorld = new World2(width, height, "n1234567890123456789s");

        ter.renderFrame(myWorld.world);
    }
}
