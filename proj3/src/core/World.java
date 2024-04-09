package core;

import edu.princeton.cs.algs4.WeightedQuickUnionUF;
import tileengine.TETile;
import tileengine.Tileset;

import java.util.List;

public class World {
    // build your own world!
    private TETile[][] worldArray;
    private final String SEED;
    private static final int screenWidth = 80;
    private static final int screenHeight = 45;

    private int totalArea;
    private int usedArea;
    private List<Room> Rooms;
    private WeightedQuickUnionUF roomConnections;

    public World(String seed) {
        SEED = seed;
        worldArray = new TETile[screenWidth][screenHeight];
        for (int x = 0; x < screenWidth; x++) {
            for (int y = 0; y < screenHeight; y++) {
                worldArray[x][y] = Tileset.NOTHING;
            }
        }
    }

    public void drawRoom(Room room) {

    }

    private class Room {
        private int[] bottomLeftCords;
        private int[] topLeftCords;
        private int length;
        private int height;
        private int area;
        private int id;
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
