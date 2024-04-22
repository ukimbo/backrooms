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
    StringBuilder saveCheck = new StringBuilder();
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
    private int doorPosX;
    private int doorPosY;
    private boolean controlVisuals = false;
    private boolean placeChar;
    private boolean gameStatus;
    private static TETile wall = Tileset.BACKROOMS;
    private static TETile floor = Tileset.BACKROOMSFLOOR;
    private TETile avatar = Tileset.AVATAR;
    private List<Key> keyList = new ArrayList<>();

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
        if (placeChar) {
            this.placeAvatarRandom();
        }
        this.placeKeys();
        this.gameStatus = true;

    }

    public TETile[][] getWorld() {
        return world;
    }

    public void placeAvatarRandom() {
        int roomNumber = RandomUtils.uniform(random, 0, rooms.size());
        Room spawnRoom = rooms.get(roomNumber);
        rooms.remove(roomNumber);
        world[spawnRoom.centerX][spawnRoom.centerY] = this.avatar;
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

    public void placeKeys() {
        List<TETile> keyTypes = new ArrayList<>();
        keyTypes.add(Tileset.BRONZEKEY);
        keyTypes.add(Tileset.SILVERKEY);
        keyTypes.add(Tileset.GOLDKEY);
        for (int i = 0; i < 3; i++) {
            Room spawnRoom = rooms.get(i);
            Key currKey = new Key(spawnRoom.centerX, spawnRoom.centerY, keyTypes.get(i));
            keyList.add(currKey);
            world[spawnRoom.centerX][spawnRoom.centerY] = currKey.key;
        }
        int realKey = RandomUtils.uniform(random, 0, 3);
        keyList.get(realKey).realKey = true;
        int roomNumber = RandomUtils.uniform(random, 3, rooms.size() - 1);
        Room doorRoom = rooms.get(roomNumber);
        doorRoom.randomX = RandomUtils.uniform(random, doorRoom.leftX + 1, doorRoom.rightX - 1);
        doorRoom.randomY = RandomUtils.uniform(random, doorRoom.bottomY + 1, doorRoom.topY - 1);
        world[doorRoom.randomX][doorRoom.randomY] = Tileset.LOCKED_DOOR;
        doorPosX = doorRoom.randomX;
        doorPosY = doorRoom.randomY;
    }

    public void runGame() {
        TERenderer ter = new TERenderer();
        while (gameStatus) {
            ter.renderFrame(getWorld());
            updateBoard();
            hud();
            StdDraw.show();
            StdDraw.pause(20);  // Adjust based on desired frame rate
        }
        System.out.println("Game Over");
    }

    private void updateBoard() {
        StringBuilder colonQ = new StringBuilder(":Q");
        while (StdDraw.hasNextKeyTyped()) {
            char key = StdDraw.nextKeyTyped();
            if (key == 'a') {
                tryMove(-1, 0);
            } else if (key == 'd') {
                tryMove(1, 0);
            } else if (key == 's') {
                tryMove(0, -1);
            } else if (key == 'w') {
                tryMove(0, 1);
            } else if (key == 'l') {
                loadGame("save.txt");
            } else if (key == 'e') {
                interact();
            } else if (key == 'c') {
                controlVisuals = !controlVisuals;
            } else if (key == 'm') {
                this.gameStatus = false;
                mainMenu();
            }else if (key == ':') {
                saveCheck.append(":");
                System.out.println(saveCheck);
            } else if (key == 'Q') {
                saveCheck.append("Q");
                System.out.println(saveCheck);

                if (saveCheck.compareTo(colonQ) == 0) {
                    System.out.println(saveCheck);
                    saveGame("save.txt");
                    gameStatus = false;
                }
                saveCheck = new StringBuilder();
            }
        }
    }

    void tryMove(int X, int Y) {
        int newX = charPosX + X;
        int newY = charPosY + Y;

        if (canMove(newX, newY)) {
            if (!(charPosX == doorPosX && charPosY == doorPosY)) {
                world[charPosX][charPosY] = floor; // Set the old position back to floor
            } else {
                world[charPosX][charPosY] = Tileset.UNLOCKED_DOOR;
            }
            charPosX = newX;
            charPosY = newY;
            world[charPosX][charPosY] = avatar; // Move avatar to new position

        }
    }

    private boolean canMove(int x, int y) {
        return (x >= 0 && x < screenWidth && y >= 0 && y < screenHeight) &&
                (world[x][y] == floor || world[x][y].equals(Tileset.UNLOCKED_DOOR));
    }

    private void interact() {
        for (Key key : keyList) {
            if (interactHelper(key.x, key.y)) {
                key.interact();
            }
        }
        if (interactHelper(doorPosX, doorPosY) && Key.realKeyCollected) {
            world[doorPosX][doorPosY] = Tileset.UNLOCKED_DOOR;
            Key.realKeyCollected = false;
        }
        interactDoor();
    }
    private boolean interactHelper(int x, int y) {
        if (charPosX == x && charPosY == y) {
            return true;
        } else if (charPosX - 1 == x && charPosY == y) {
            return true;
        } else if (charPosX + 1 == x && charPosY == y) {
            return true;
        } else if (charPosX == x && charPosY + 1 == y) {
            return true;
        } else if (charPosX == x && charPosY - 1 == y) {
            return true;
        } else {
            return false;
        }
    }

    private void interactDoor() {
        if (charPosX == doorPosX && charPosY == doorPosY) {
            this.gameStatus = false;
            World newGame = new World(80, 45, "ns", true);
            newGame.runGame();
        }
    }

    public void saveGame(String filename) {
        String context = seed + "\n" + charPosX + "\n" + charPosY + "\n";
        FileUtils.writeFile(filename, context);
    }

    public void loadGame(String filename) {

        In in = new In(filename);
        if (!in.exists()) {
            System.out.println("Save file does not exist");
            return;
        }
        if (in.hasNextChar()) {
            seed = in.readLong();
            charPosX = in.readInt();
            charPosY = in.readInt();
            world = new World(screenWidth, screenHeight, "n" + seed + "s", false).world;
            world[charPosX][charPosY] = avatar;
            in.close();
        }
    }

    public Long loadSeed() {
        In in = new In("save.txt");

        if (!in.exists()) {
            System.out.println("Save file does not exist");
            return null;
        }
        if (in.hasNextChar()) {
            seed = in.readLong();
            return seed;
        }
        return null;
    }

    public void mainMenu() {
        boolean loopMenu = true;

        while (loopMenu) {
            StdDraw.clear(StdDraw.BLACK);
            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.picture(40, 37, "aesthetic/title.jpg");
            StdDraw.picture(40, 10, "aesthetic/q.png", 40, 3);
            StdDraw.picture(40, 15, "aesthetic/theme select.png", 40, 3);
            StdDraw.picture(40, 20, "aesthetic/load.png", 40, 3);
            StdDraw.picture(40, 25, "aesthetic/new.png", 40, 3);
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
                } else if (choice == 'l') {
                    World newGame;
                    Long getSeed = loadSeed();
                    if (getSeed == null) {
                        System.out.println("Can't Load because there is nothing saved!");
                    } else {
                        loopMenu = false;
                        newGame = new World(80, 45, "n" + getSeed + "s", false);
                        newGame.loadGame("save.txt");
                        newGame.runGame();
                        break;
                    }
                } else if (choice == 't') {
                    themeMenu();
                } else if (choice == 'q') {
                    loopMenu = false;
                    break;
                }
            }
            StdDraw.pause(100);
        }
    }

    public String getInputSeed() {
        StringBuilder seedBuilder = new StringBuilder();
        seedBuilder.append("n");
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(40, 20, "Enter Seed: (Press 'S' to Start)");
        StdDraw.text(40, 5, "Press 'B' to go back");
        StdDraw.show();
        boolean enterSeed = true;
        int count = 3;
        while (enterSeed) {
            if (StdDraw.hasNextKeyTyped()) {
                char ch = StdDraw.nextKeyTyped();
                if (Character.isDigit(ch)) {
                    count += 1;
                    seedBuilder.append(ch);
                    StdDraw.text(33 + count, 18, String.valueOf(ch));
                    StdDraw.show();
                } else if (ch == 's' || ch == 'S') {
                    if (seedBuilder.length() > 0) {
                        enterSeed = false;
                    }
                } else if (ch == 'b' || ch == 'B') {
                    enterSeed = false;
                    this.mainMenu();
                    break;
                }
            }

            StdDraw.pause(50);
        }
        seedBuilder.append("s");
        return seedBuilder.toString();
    }

    public void themeMenu() {
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(40, 40, "Choose Theme:");
        StdDraw.text(20, 30, "Backrooms: Press '1'");
        StdDraw.text(40, 30, "Default: Press '2'");
        StdDraw.text(60, 30, "Castle: Press '3'");
        StdDraw.text(40, 5, "Press 'B' to go back");
        StdDraw.picture(20, 35, "aesthetic/backrooms preview.png");
        StdDraw.picture(40, 35, "aesthetic/default preview.png");
        StdDraw.picture(60, 35, "aesthetic/castle preview.png");
        StdDraw.text(40, 25, "Choose Avatar:");
        StdDraw.picture(40, 20, "aesthetic/green.jpg");
        StdDraw.show();
        boolean chooseTheme = true;
        int count = 3;
        while (chooseTheme) {
            if (StdDraw.hasNextKeyTyped()) {
                char ch = StdDraw.nextKeyTyped();
                if (ch == '1') {
                    wall = Tileset.BACKROOMS;
                    floor = Tileset.BACKROOMSFLOOR;
                } else if (ch == '2') {
                    wall = Tileset.WALL;
                    floor = Tileset.FLOOR;
                } else if (ch == '3') {
                    wall = Tileset.CASTLE;
                    floor = Tileset.CASTLEFLOOR;
                } else if (ch == 'b' || ch == 'B') {
                    chooseTheme = false;
                    this.mainMenu();
                }
            }
            StdDraw.pause(50);


        }
    }

    private void hud() {
        StdDraw.setPenColor(StdDraw.WHITE);
        StringBuilder currPos = new StringBuilder("Current Position: (");
        currPos.append(charPosX).append(", ").append(charPosY).append(")");
        StdDraw.textLeft(1, 47, currPos.toString());
        StdDraw.textLeft(1, 46, "Press 'C' for Controls");
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
        String formattedDateTime = currentDateTime.format(formatter);
        StdDraw.textRight(79, 47, formattedDateTime);
        StdDraw.textRight(79, 46, "Press 'M' for Main Menu");
        controlHUD();
        keyHUD();
        mouseHover();
    }

    private void controlHUD() {
        if (controlVisuals) {
            StdDraw.text(40, 47, "'W' -> Up, 'S' -> Down, 'E' -> Interact,");
            StdDraw.text(40, 46, "'A' -> Left, 'D' -> Right, ':' + 'Q' -> Save");
        }
    }
    private void keyHUD() {
        StdDraw.textRight(74, 45, "Keys Collected: ");
        if (keyList.get(0).collected) {
            StdDraw.picture(75, 45, "aesthetic/bronze key.jpg", 1, 1);
        }
        if (keyList.get(1).collected) {
            StdDraw.picture(77, 45, "aesthetic/silver key.jpg", 1, 1);
        }
        if (keyList.get(2).collected) {
            StdDraw.picture(79, 45, "aesthetic/gold key.jpg", 1, 1);
        }
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

    private int gridToInteger(int x, int y) {
        return x + (y * screenWidth);
    }
    private class Key {
        private int x;
        private int y;
        private TETile key;
        private boolean collected;
        private boolean realKey;
        private static boolean realKeyCollected;

        public Key(int x, int y, TETile typeKey) {
            this.x = x;
            this.y = y;
            this.key = typeKey;
            this.collected = false;
            this.realKey = false;
        }

        private void interact() {
            this.collected = true;
            world[x][y] = floor;
            if (this.realKey) {
                realKeyCollected = true;
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
                    world[x][y] = floor;
                    roomGridPositionMap.put(gridToInteger(x, y), this);
                }
            }
            //Walls
            for (int x = leftX; x <= rightX; x++) {
                world[x][bottomY] = wall;
                world[x][topY] = wall;
            }
            for (int y = bottomY; y <= topY; y++) {
                world[leftX][y] = wall;
                world[rightX][y] = wall;
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
                world[x][yStart] = floor;
                checkConnectionsDuringHallwayGeneration(x, yStart);

                if (world[x][yStart - 1].equals(Tileset.NOTHING)) {
                    world[x][yStart - 1] = wall;
                }
                if (world[x][yStart + 1].equals(Tileset.NOTHING)) {
                    world[x][yStart + 1] = wall;
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
                world[xStart][y] = floor;
                checkConnectionsDuringHallwayGeneration(xStart, y);
                if (world[xStart - 1][y].equals(Tileset.NOTHING)) {
                    world[xStart - 1][y] = wall;

                }
                if (world[xStart + 1][y].equals(Tileset.NOTHING)) {
                    world[xStart + 1][y] = wall;
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
}
