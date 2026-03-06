package com.takonline.takonline.game.model.board;

public record Position(int row, int col) {

    public Position {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("row and col must be >= 0");
        }
    }
}