package com.takonline.takonline.game.model.board;

public class Piece {

    private PieceColor color;
    private PieceType type;

    public Piece() {
    }

    public Piece(PieceColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public PieceColor getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }
}