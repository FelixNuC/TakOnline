package com.takonline.takonline.game.model.board;

import java.util.ArrayList;
import java.util.List;

public class Stack {

    private List<Piece> pieces = new ArrayList<>();

    public Stack() {
    }

    public boolean isEmpty() {
        return pieces.isEmpty();
    }

    public int size() {
        return pieces.size();
    }

    public void addPiece(Piece piece) {
        pieces.add(piece);
    }

    public Piece getTopPiece() {
        if (pieces.isEmpty()) {
            return null;
        }
        return pieces.get(pieces.size() - 1);
    }

    public List<Piece> getPieces() {
        return pieces;
    }
}