
package core;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;
import tileengine.TETile;
import tileengine.Tileset;
import utils.FileUtils;
import utils.RandomUtils;
import tileengine.TERenderer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class World {
    // build your own world!
    private long seed;
    private Random random;
    private int screenWidth;
    private int screenHeight;
    private List<Room> rooms;
    private Map<Integer, Room> roomGridPositionMap;
    private WeightedQuickUnionUF roomConnections;
    private TETile[][] world;
    private int charPosX;
    private int charPosY;

    private boolean placeChar;

    private boolean gameStatus;
    public World(int screenWidth, int screenHeight, String seed, boolean placeChar) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.seed = AutograderBuddy.parseSeed(seed);
        this.random = new Random(this.seed);
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
        if (placeChar){
            this.placeAvatarRandom();
        }
        this.gameStatus = true;

    }

    public TETile[][] getWorld() {
        return world;
    }

    public void placeAvatarRandom() {
        int roomNumber = RandomUtils.uniform(random, 0, rooms.size() - 1);
        Room spawnRoom = rooms.get(roomNumber);
        world[spawnRoom.centerX][spawnRoom.centerY] = Tileset.AVATAR;
        charPosX = spawnRoom.centerX;
        charPosY = spawnRoom.centerY;
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

    public void runGame() {
        TERenderer ter = new TERenderer();
        while (gameStatus) {
            ter.renderFrame(getWorld());
            updateBoard();
            HUD();
            StdDraw.show();
            StdDraw.pause(20);  // Adjust based on desired frame rate
        }
        System.out.println("Game Over");
    }
    private void updateBoard() {
        while (StdDraw.hasNextKeyTyped()) {
            char key = StdDraw.nextKeyTyped();

            if (key == 'a') {
                tryMove( - 1, 0);
            } else if (key == 'd') {
                tryMove( 1, 0);
            } else if (key == 's') {
                tryMove(0, - 1);
            } else if (key == 'w') {
                tryMove(0,  1);
            } else if (key == 'l') {
                loadGame("save.txt");
            } else if (key == ':') {
//                 char key2 = StdDraw.nextKeyTyped();
//                 if (key2 == 'Q') {
                    saveGame("save.txt");
//                }
            }else if (key == 'z') {
                gameStatus = false;
            }
        }
    }

    private void tryMove(int X, int Y) {
        int newX = charPosX + X;
        int newY = charPosY + Y;

        if (canMove(newX, newY)) {
            world[charPosX][charPosY] = Tileset.FLOOR; // Set the old position back to floor
            charPosX = newX;
            charPosY = newY;
            world[charPosX][charPosY] = Tileset.AVATAR; // Move avatar to new position
            //System.out.println("Went from " + charPosX + " " + charPosY + " to " + newX + " " + newY);

        } else {
            System.out.println("Cant move");
            System.out.println("Tried to go from " + charPosX + " " + charPosY + " to " + newX + " " + newY);

            world[charPosX][charPosY] = Tileset.WRONG;
        }
    }

    private boolean canMove(int x, int y) {
        return (x >= 0 && x < screenWidth && y >= 0 && y < screenHeight) && (world[x][y] == Tileset.FLOOR);
    }

    public void saveGame(String filename) {
        System.out.println("Should save");
        String context = seed + "\n" + charPosX + "\n" + charPosY + "\n";
        FileUtils.writeFile(filename, context);
    }

    public void loadGame(String filename) {

        In in = new In(filename);
        if (!in.exists()) {
            System.out.println("Save file does not exist");
            return;
        }
        if (in.hasNextChar()){
            seed = in.readLong();
            charPosX = in.readInt();
            charPosY = in.readInt();
            world = new World(screenWidth, screenHeight, "n" + seed + "s", false).world;
            world[charPosX][charPosY] = Tileset.AVATAR;
            in.close();
        }
    }

    public void mainMenu() {
        boolean loopMenu = true;

        while (loopMenu) {
            StdDraw.clear(StdDraw.BLACK);
            StdDraw.setPenColor(StdDraw.WHITE);

            StdDraw.text(40, 30, "New Game (Press 'N')");
            StdDraw.text(40, 20, "Load Game (Press 'L')");
            StdDraw.text(40, 10, "Quit (Press 'Q')");
            StdDraw.show();

            // Check for user input
            if (StdDraw.hasNextKeyTyped()) {
                char choice = StdDraw.nextKeyTyped();
                if (choice == 'n') {
                    loopMenu = false;
                    String getSeed = getInputSeed();
                    World newGame = new World(80, 45, getSeed, true);
                    newGame.runGame();
                    break;
                }
                else if (choice == 'l') {
                    World newGame;
                    Long getSeed = loadSeed();
                    if (getSeed == null){
                        System.out.println("Can't Load because there is nothing saved!");
                    } else {
                        loopMenu = false;
                         newGame = new World(80, 45, "n" + getSeed + "s", false);
                         newGame.loadGame("save.txt");
                         newGame.runGame();
                        break;
                    }
                }
            }
            StdDraw.pause(100);
        }
    }
    public Long loadSeed() {
        In in = new In("save.txt");

        if (!in.exists()) {
            System.out.println("Save file does not exist");
            return null;
        }
        if (in.hasNextChar()){
            seed = in.readLong();
            return seed;
        }
       return null;
    }
    public static String getInputSeed() {
        StringBuilder seedBuilder = new StringBuilder();
        seedBuilder.append("n");
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(40, 20, "Enter Seed: (Press 'S' to Start)");

        StdDraw.show();
        boolean enterSeed = true;
        int count = 3;
        while (enterSeed) {
            if (StdDraw.hasNextKeyTyped()) {
                char ch = StdDraw.nextKeyTyped();
                if (Character.isDigit(ch)) {
                    count += 1;
                    seedBuilder.append(ch);
                    StdDraw.text(33 + count,18 , String.valueOf(ch));
                    StdDraw.show();
                } else if (ch == 's' || ch == 'S') {
                    if (seedBuilder.length() > 0) {
                        enterSeed = false;
                    }
                }
            }

            StdDraw.pause(50);
        }
        seedBuilder.append("s");
        return seedBuilder.toString();
    }
    private void HUD() {
        StdDraw.setPenColor(StdDraw.WHITE);
        StringBuilder currPos = new StringBuilder("Current Position: (");
        currPos.append(charPosX).append(", ").append(charPosY).append(")");
        StdDraw.textLeft(1, 46, currPos.toString());
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
        String formattedDateTime = currentDateTime.format(formatter);
        StdDraw.textRight(79, 46, formattedDateTime);
        mouseHover();
    }
    private void mouseHover() {
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();
        if ((mouseX < screenWidth && mouseX >= 0) && (mouseY < screenHeight && mouseY >= 0)) {
            if (world[mouseX][mouseY] != Tileset.NOTHING) {
                StdDraw.textLeft(1, 45, "Tile: " + world[mouseX][mouseY].description());
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

