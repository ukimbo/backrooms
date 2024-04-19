package core;

import edu.princeton.cs.algs4.WeightedQuickUnionUF;
import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.util.*;

public class World {
    // build your own world!
    private String seed;
    private Random random;
    private int screenWidth;
    private int screenHeight;
    private List<Room> rooms;
    private Map<Integer, Room> roomGridPositionMap;
    private WeightedQuickUnionUF roomConnections;
    private TETile[][] world;
    public World(int screenWidth, int screenHeight, String seed) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.random = new Random(AutograderBuddy.parseSeed(seed));
        this.rooms = new ArrayList<>();
        this.roomGridPositionMap = new HashMap<>();
        this.world = new TETile[screenWidth][screenHeight];
        for (int x = 0; x < screenWidth; x++) {
            for (int y = 0; y < screenHeight; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        this.randomSquare();
        this.placeHalls();
        this.placeAvatar();
    }

    public TETile[][] getWorld() {
        return world;
    }

    public void placeAvatar() {
        int roomNumber = RandomUtils.uniform(random, 0, rooms.size() - 1);
        Room spawnRoom = rooms.get(roomNumber);
        world[spawnRoom.centerX][spawnRoom.centerY] = Tileset.AVATAR;
    }


    public void randomSquare() {
        boolean placed = false;
        int count = 0;
        int roomNumber = RandomUtils.uniform(random, 10, 24);
        int overflowCount = 0;
        roomConnections = new WeightedQuickUnionUF(roomNumber + 1);
        while (!placed) {
            int roomWidth = RandomUtils.uniform(random, 4, 17);
            int roomHeight = RandomUtils.uniform(random, 4, 17);
            int xStart = RandomUtils.uniform(random, 1, screenWidth - roomWidth - 1);
            int yStart = RandomUtils.uniform(random, 1, screenHeight - roomHeight - 1);
            Room r = new Room(xStart, yStart, roomWidth, roomHeight);
            r.id = count;
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
        int lastConnected = 0;
        for (int i = 0; i <= rooms.size() - 1; i++) {
            if (!roomConnections.connected(0, rooms.get(i).id)) {
                Room sourceRoom = rooms.get(lastConnected);
                Room destinationRoom = rooms.get(i);
                sourceRoom.connectRoomsRandom(destinationRoom);
                lastConnected = i;
            }
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
        private int id;
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
                rooms.add(this);
            }
        }

        private void drawRoom() {
            //Floors
            for (int x = leftX + 1; x < rightX; x++) {
                for (int y = bottomY + 1; y < topY; y++) {
                    world[x][y] = Tileset.FLOOR;
                    roomGridPositionMap.put(gridToInteger(x, y), this);
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
            roomConnections.union(this.id, otherRoom.id);
            if (this.randomY > otherRoom.bottomY && this.randomY < otherRoom.topY) {
                drawHorizontalHallway(this.randomX, otherRoom.randomX, this.randomY);
            } else if (this.randomX < otherRoom.leftX && this.randomX > otherRoom.rightX) {
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
            for (int x = xOrigin; x <= xDestination; x++) {
                world[x][yStart] = Tileset.FLOOR;
                checkConnectionsDuringHallwayGeneration(x, yStart);

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
                checkConnectionsDuringHallwayGeneration(xStart, y);
                if (world[xStart - 1][y].equals(Tileset.NOTHING)) {
                    world[xStart - 1][y] = Tileset.WALL;

                }
                if (world[xStart + 1][y].equals(Tileset.NOTHING)) {
                    world[xStart + 1][y] = Tileset.WALL;
                }

            }
        }
        private void checkConnectionsDuringHallwayGeneration(int x, int y) {
            int gridToInt = gridToInteger(x, y);
            Room otherRoom = roomGridPositionMap.get(gridToInt);
            if (otherRoom != null && !otherRoom.equals(this)) {
                roomConnections.union(this.id, otherRoom.id);
            }
        }
        private void connectRoomsCenter(Room otherRoom) {
            drawHorizontalHallway(this.centerX, otherRoom.centerX, centerY);
            drawVerticalHallway(this.centerY, otherRoom.centerY, otherRoom.centerX);
        }

    }
    private int gridToInteger(int x, int y) {
        return x + (y * screenWidth);
    }
}

