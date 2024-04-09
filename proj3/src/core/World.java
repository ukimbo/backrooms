package core;

import tileengine.TETile;

import java.util.List;

public class World {
    // build your own world!
    private TETile[][] worldArray;
    private final String SEED;
    private int height;
    private int width;
    private int totalArea;
    private int usedArea;
    private List<Room> Rooms;

    public World(String seed) {
        SEED = seed;
    }

    private class Room {
        private int[] bottomLeftCords;
        private int[] topLeftCords;
        private int length;
        private int height;
        private int area;
        int numberOfSockets;
        int[] socketCords; //We can adjust this to apply for when numberOfSockets > 1


    }

    private class Hallway extends Room {
        int[] startCords;
        int[] endCords;
        boolean isVertical;
        Room[] bridgeBetween;
    }

}
