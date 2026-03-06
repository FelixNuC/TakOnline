package com.takonline.takonline.game.model.board;

public class Board {

    private int size;
    private Stack[][] cells;

    public Board() {
    }

    public Board(int size) {
        this.size = size;
        this.cells = new Stack[size][size];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                cells[row][col] = new Stack();
            }
        }
    }

    public int getSize() {
        return size;
    }

    public Stack[][] getCells() {
        return cells;
    }

    public Stack getStack(int row, int col) {
        validatePosition(row, col);
        return cells[row][col];
    }

    public void placePiece(int row, int col, Piece piece) {
        validatePosition(row, col);

        Stack stack = cells[row][col];
        if (!stack.isEmpty()) {
            throw new IllegalStateException("Cell is not empty");
        }

        stack.addPiece(piece);
    }

    private void validatePosition(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IllegalArgumentException("Position out of bounds");
        }
    }
}