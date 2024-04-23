package core;

import tileengine.TERenderer;

public class Main {
    public static void main(String[] args) {
        int width = 80;
        int height = 45;
        World myWorld = new World(width, height, "ns", false);
        myWorld.mainMenu();
    }
}
