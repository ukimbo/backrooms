package core;

import tileengine.TERenderer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int width = 80;
        int height = 45;
        TERenderer ter = new TERenderer();
        ter.initialize(width, height);
//        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
//        System.out.println("Enter a starting String with the convetion of nSEEDHEREs, ex:n18913088s :");
//
//        String userString = myObj.nextLine();  // Read user input
        World myWorld = new World(width, height, "ns", false);
        myWorld.mainMenu();
    }
}
