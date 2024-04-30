package core;

import tileengine.TERenderer;

public class Main {
    public static void main(String[] args) {
        int width = 80;
        int height = 45;
        TERenderer ter = new TERenderer();
        ter.initialize(width, height + 3);
        World myWorld = new World(width, height, "ns", false);
        myWorld.mainMenu();
    }
}