package core;

import edu.princeton.cs.algs4.WeightedQuickUnionUF;
import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {
    // build your own world!
    private TETile[][] worldArray;

    private int totalArea;
    private int usedArea;
    private List<Room> Rooms;
//    private WeightedQuickUnionUF roomConnections;
    public TETile[][] world;
    private String seed;
    private Random random;

    private int width;
    private int height;

    public World(int width, int height, String seed) {
        this.width = width;
        this.height = height;
        this.totalArea = this.width * this.height;
        this.usedArea = 0;
        this.Rooms = new ArrayList<>();
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
        boolean placed = false;
        int count = 0;
        int roomNumber = RandomUtils.uniform(random, 19, 25);
        while (!placed) {
            int floorWidth = RandomUtils.uniform(random, 2, 15);
            int floorHeight = RandomUtils.uniform(random, 2, 15);
            int xStart = RandomUtils.uniform(random, 1, width - floorWidth - 1);
            int yStart = RandomUtils.uniform(random, 1, height - floorHeight - 1);

            if (!overlaps(xStart, yStart, floorWidth, floorHeight)) {
                drawRoom(xStart, yStart, floorWidth, floorHeight);
                if (count == roomNumber) {
                    placed = true;
                }
                count += 1;
            }

        }
    }


    private boolean overlaps(int xStart, int yStart, int floorWidth, int floorHeight) {
        for (int x = xStart - 1; x <= xStart + floorWidth; x++) {
            for (int y = yStart - 1; y <= yStart + floorHeight; y++) {
                if (!world[x][y].equals(Tileset.NOTHING)) {
                    return true;
                }
            }
        }
        return false;
    }


    private void drawRoom(int xStart, int yStart, int floorWidth, int floorHeight) {
        //Keeping track of Rooms
        Room room = new Room(xStart, yStart, floorWidth, floorHeight);
        Rooms.add(room);
        //floors
        for (int x = xStart; x < xStart + floorWidth; x++) {
            for (int y = yStart; y < yStart + floorHeight; y++) {
                world[x][y] = Tileset.FLOOR;
            }
        }
        // walls
        for (int x = xStart - 1; x <= xStart + floorWidth; x++) {
            world[x][yStart - 1] = Tileset.WALL;
            world[x][yStart + floorHeight] = Tileset.WALL;
        }
        for (int y = yStart - 1; y <= yStart + floorHeight; y++) {
            world[xStart - 1][y] = Tileset.WALL;
            world[xStart + floorWidth][y] = Tileset.WALL;
        }
    }
    private class Room {
        private int[] bottomLeftCords; //bottom Left Coordinate of the Walls
        private int[] topLeftCords; //top Left Coordinate of the Walls
        private int length;
        private int height;
        private int area;
        private int id;
        public Room(int xStart, int yStart, int floorWidth, int floorHeight) {
            bottomLeftCords = new int[2];
            topLeftCords = new int[2];
            bottomLeftCords[0] = xStart - 1;
            bottomLeftCords[1] = yStart - 1;
            topLeftCords[0] = xStart + floorWidth;
            topLeftCords[1] = yStart + floorHeight;
            length = floorWidth + 1;
            height = floorHeight + 1;
            area = length * width;
            usedArea += area;
        }


    }

    private class Hallway extends Room {
        int[] startCords;
        int[] endCords;
        boolean isVertical;
        Room[] bridgeBetween;

        public Hallway(int xStart, int yStart, int floorWidth, int floorHeight) {
            super(xStart, yStart, floorWidth, floorHeight);
        }
    }

}


