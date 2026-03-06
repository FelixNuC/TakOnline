package com.takonline.takonline.game.rules;

public class TakPieceRules {

    public static int flatsForBoard(int size) {
        return switch (size) {
            case 3 -> 10;
            case 4 -> 15;
            case 5 -> 21;
            case 6 -> 30;
            case 7 -> 40;
            case 8 -> 50;
            default -> throw new IllegalArgumentException("Invalid board size");
        };
    }

    public static int capstonesForBoard(int size) {
        return switch (size) {
            case 5,6 -> 1;
            case 7,8 -> 2;
            default -> 0;
        };
    }

}