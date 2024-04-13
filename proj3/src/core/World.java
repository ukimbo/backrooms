package core;

import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class World {
    // build your own world!
    public TETile[][] world;
    private List<Room> Rooms;
    private Map<Integer, Room> roomGridPositionMap;
    private String seed;
    private Random random;
    private int screenWidth;
    private int screenHeight;
    public World(int screenWidth, int screenHeight, String seed) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.Rooms = new ArrayList<>();
        this.world = new TETile[screenWidth][screenHeight];
        System.out.println(seed);
        this.random = new Random(AutograderBuddy.parseSeed(seed));
        System.out.println(random);
        for (int x = 0; x < screenWidth; x++) {
            for (int y = 0; y < screenHeight; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        this.randomSquare();
        this.placeHalls();
    }


    public void randomSquare() {
        boolean placed = false;
        int count = 0;
        //int roomNumber = RandomUtils.uniform(random, 10, 25);
        int roomNumber = 24;
        int overflowCount = 0;
        while (!placed) {
            int roomWidth = RandomUtils.uniform(random, 4, 17);
            int roomHeight = RandomUtils.uniform(random, 4, 17);
            int xStart = RandomUtils.uniform(random, 1, screenWidth - roomWidth - 1);
            int yStart = RandomUtils.uniform(random, 1, screenHeight - roomHeight - 1);
            Room r = new Room(xStart, yStart, roomWidth, roomHeight);
            if (!r.overlaps()) {
                r.drawRoom();
                if (count == roomNumber) {
                    placed = true;
                }
                count += 1;
            }
            if (overflowCount == (roomNumber * roomNumber)) {
                break;
            }
            overflowCount += 1;
        }
    }

    public void placeHalls() {
        for (int i = 0; i < Rooms.size() - 1; i++) {
            Room sourceRoom = Rooms.get(i);
            Room destinationRoom = Rooms.get(i + 1);
            sourceRoom.connectRoomsRandom(destinationRoom);
        }
    }

    private class Room {
        private int leftX;
        private int rightX;
        private int topY;
        private int bottomY;
        private int centerX;
        private int centerY;
        private int randomX;
        private int randomY;
        private int length;
        private int height;
        public Room(int xStart, int yStart, int length, int height) {
            leftX = xStart;
            rightX = xStart + length - 1;
            bottomY = yStart;
            topY = yStart + height - 1;
            centerX = (rightX + leftX) / 2;
            centerY = (topY + bottomY) / 2;
            this.length = length;
            this.height = height;
            if (!this.overlaps()) {
                Rooms.add(this);
            }
        }

        private void drawRoom() {
            //Floors
            for (int x = leftX + 1; x < rightX; x++) {
                for (int y = bottomY + 1; y < topY; y++) {
                    world[x][y] = Tileset.FLOOR;
                }
            }
            //Walls
            for (int x = leftX; x <= rightX; x++) {
                world[x][bottomY] = Tileset.WALL;
                world[x][topY] = Tileset.WALL;
            }
            for (int y = bottomY; y <= topY; y++) {
                world[leftX][y] = Tileset.WALL;
                world[rightX][y] = Tileset.WALL;
            }
        }
        private boolean overlaps() {
            for (int x = leftX; x <= rightX; x++) {
                for (int y = bottomY; y <= topY; y++) {
                    if (!world[x][y].equals(Tileset.NOTHING)) {
                        return true;
                    }
                }
            }
            return false;
        }
        private void connectRoomsRandom(Room otherRoom) {
            this.randomX = RandomUtils.uniform(random, this.leftX + 1, this.rightX - 1);
            this.randomY = RandomUtils.uniform(random, this.bottomY + 1, this.topY - 1);
            otherRoom.randomX = RandomUtils.uniform(random, otherRoom.leftX + 1, otherRoom.rightX - 1);
            otherRoom.randomY = RandomUtils.uniform(random, otherRoom.bottomY + 1, otherRoom.topY - 1);
            if (this.randomY > otherRoom.bottomY && this.randomY < otherRoom.topY) {
                drawHorizontalHallway(this.randomX, otherRoom.randomX, this.randomY);
            } else if (this.randomX > otherRoom.leftX && this.randomX < otherRoom.rightX) {
                drawVerticalHallway(this.randomY, otherRoom.randomY, otherRoom.randomX);
            } else {
               drawLShapeHallway(this.randomX, otherRoom.randomX, this.randomY, otherRoom.centerY);
            }
        }
        private void drawLShapeHallway(int xStart, int xEnd, int yStart, int yEnd) {
            drawHorizontalHallway(xStart, xEnd, yStart);
            drawVerticalHallway(yStart, yEnd, xEnd);
        }
        private void drawHorizontalHallway(int xStart, int xEnd, int yStart) {
            int xOrigin;
            int xDestination;
            if (xStart > xEnd) {
                xOrigin = xEnd;
                xDestination = xStart;
            } else {
                xOrigin = xStart;
                xDestination = xEnd;
            }
            for (int x = xOrigin; x <= xDestination ; x++) {
                world[x][yStart] = Tileset.FLOOR;
                if (world[x][yStart - 1].equals(Tileset.NOTHING)) {
                    world[x][yStart - 1] = Tileset.WALL;
                }
                if (world[x][yStart + 1].equals(Tileset.NOTHING)) {
                    world[x][yStart + 1] = Tileset.WALL;
                }
            }
        }

        private void drawVerticalHallway(int yStart, int yEnd, int xStart) {
            int yOrigin;
            int yDestination;
            if (yStart > yEnd) {
                yOrigin = yEnd;
                yDestination = yStart;
            } else {
                yOrigin = yStart;
                yDestination = yEnd;
            }
            for (int y = yOrigin; y <= yDestination; y++) {
                world[xStart][y] = Tileset.FLOOR;
                if (world[xStart - 1][y].equals(Tileset.NOTHING)) {
                    world[xStart - 1][y] = Tileset.WALL;

                }
                if (world[xStart + 1][y].equals(Tileset.NOTHING)) {
                    world[xStart + 1][y] = Tileset.WALL;
                }

            }
        }
        private void connectRoomsCenter(Room otherRoom) {
            drawHorizontalHallway(this.centerX, otherRoom.centerX, centerY);
            drawVerticalHallway(this.centerY, otherRoom.centerY, otherRoom.centerX);
        }

    }
//    private int gridToInteger(int x, int y) {
//        return (screenWidth * y) + x;
//    }
//    private int[] integerToGrid(int value) {
//        int x = value % screenWidth;
//        int y = value / screenWidth;
//        return new int[]{x, y};
//    }
}


