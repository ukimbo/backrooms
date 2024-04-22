package core;

import tileengine.TETile;
import tileengine.Tileset;

public class AutograderBuddy {

    /**
     * Simulates a game, but doesn't render anything or call any StdDraw
     * methods. Instead, returns the world that would result if the input string
     * had been typed on the keyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quit and
     * save. To "quit" in this method, save the game to a file, then just return
     * the TETile[][]. Do not call System.exit(0) in this method.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public static TETile[][] getWorldFromInput(String input) {
        World myWorld = new World(80, 45, "ns", true);

        int i = 0;
        if (input.charAt(i) == 'n') {
            i++;
            StringBuilder seedSb = new StringBuilder();
            while (Character.isDigit(input.charAt(i))) {
                seedSb.append(input.charAt(i++));
            }
            if (input.charAt(i) == 's') {
                i++;
                myWorld = new World(80, 45, seedSb.toString(), true);
            }
        } else if (input.charAt(i) == 'l') {
            i++;
            myWorld.loadGame("save.txt");
        }

        for (; i < input.length(); i++) {
            char command = input.charAt(i);
            if (command == ':') {
                if (i + 1 < input.length() && input.charAt(i + 1) == 'q') {
                    myWorld.saveGame("save.txt");  // Ensure correct handling of save filename/path
                    break;
                }
            } else {
                if (command == 'a') {
                    myWorld.tryMove(-1, 0);
                } else if (command == 'b') {
                    myWorld.tryMove(1, 0);
                } else if (command == 's') {
                    myWorld.tryMove(0, -1);
                } else if (command == 'w') {
                    myWorld.tryMove(0, 1);
                } else if (command == 'l') {
                    myWorld.loadGame("save.txt");
                }
            }


        }
        return myWorld.getWorld();

    }

    public static long parseSeed(String input) {
        if (input.equals("") || input.equals("ns")) {
            return System.currentTimeMillis();
        } else if (input.length() < 2 || input.charAt(0) != 'n' || !input.endsWith("s")) {
            throw new IllegalArgumentException("Input must start with 'N' and end with 'S'.");
        }
        String seedStr = input.substring(1, input.length() - 1);
        System.out.println(seedStr);


        return Long.parseLong(seedStr);

    }


    /**
     * Used to tell the autograder which tiles are the floor/ground (including
     * any lights/items resting on the ground). Change this
     * method if you add additional tiles.
     */
    public static boolean isGroundTile(TETile t) {
        return t.character() == Tileset.FLOOR.character()
                || t.character() == Tileset.AVATAR.character()
                || t.character() == Tileset.FLOWER.character();
    }

    /**
     * Used to tell the autograder while tiles are the walls/boundaries. Change
     * this method if you add additional tiles.
     */
    public static boolean isBoundaryTile(TETile t) {
        return t.character() == Tileset.WALL.character()
                || t.character() == Tileset.LOCKED_DOOR.character()
                || t.character() == Tileset.UNLOCKED_DOOR.character();
    }
}
