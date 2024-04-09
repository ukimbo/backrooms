package core;
import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.util.List;
import java.util.Random;

public class World {
    // build your own world!
    public TETile[][] world;
    private String seed;
    private Random random;

    int width;
    int height;

    public World(int width, int height, String seed) {
        this.width = width;
        this.height = height;
        this.world = new TETile[width][height];
        System.out.println(seed);
        this.random = new Random(AutograderBuddy.parseSeed(seed));
        System.out.println(random);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        this.randomSquare();
    }

    public void randomSquare() {

        int roomWidth;
        int roomHeight;
        int count = 0;
        int roomNumber = 25;
        boolean placed = false;
        while (!placed) {
            roomWidth = RandomUtils.uniform(random, 3, 15);
            roomHeight = RandomUtils.uniform(random, 3, 15);
            int xStart = RandomUtils.uniform(random, 1, width - roomWidth - 1);
            int yStart = RandomUtils.uniform(random, 1, height - roomHeight - 1);

            if (!overlapsWithExistingStructure(xStart, yStart, roomWidth, roomHeight)) {
                placeRoom(xStart, yStart, roomWidth, roomHeight);
                if (count == roomNumber){
                    placed = true;
                }
                count += 1;

            }

        }
    }

    /**
     * Checks if the proposed room overlaps with existing structures.
     */
    private boolean overlapsWithExistingStructure(int xStart, int yStart, int roomWidth, int roomHeight) {
        for (int x = xStart - 1; x <= xStart + roomWidth; x++) {
            for (int y = yStart - 1; y <= yStart + roomHeight; y++) {
                if (!world[x][y].equals(Tileset.NOTHING)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Places a room in the world, with WALL surrounding a FLOOR interior.
     */
    private void placeRoom(int xStart, int yStart, int roomWidth, int roomHeight) {
        for (int x = xStart; x < xStart + roomWidth; x++) {
            for (int y = yStart; y < yStart + roomHeight; y++) {
                // Set the interior as FLOOR
                world[x][y] = Tileset.FLOOR;
            }
        }
        // Add WALL tiles around the perimeter of the room
        for (int x = xStart - 1; x <= xStart + roomWidth; x++) {
            world[x][yStart - 1] = Tileset.WALL;
            world[x][yStart + roomHeight] = Tileset.WALL;
        }
        for (int y = yStart - 1; y <= yStart + roomHeight; y++) {
            world[xStart - 1][y] = Tileset.WALL;
            world[xStart + roomWidth][y] = Tileset.WALL;
        }
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


