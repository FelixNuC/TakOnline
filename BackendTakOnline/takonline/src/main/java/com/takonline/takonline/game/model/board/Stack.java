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

    public void addPieces(List<Piece> newPieces) {
        pieces.addAll(newPieces);
    }

    public Piece getTopPiece() {
        if (pieces.isEmpty()) {
            return null;
        }
        return pieces.get(pieces.size() - 1);
    }

    public List<Piece> removeTopPieces(int count) {
        if (count <= 0 || count > pieces.size()) {
            throw new IllegalArgumentException("Invalid number of pieces to remove");
        }

        int fromIndex = pieces.size() - count;
        List<Piece> removed = new ArrayList<>(pieces.subList(fromIndex, pieces.size()));
        pieces.subList(fromIndex, pieces.size()).clear();
        return removed;
    }

    public List<Piece> getPieces() {
        return pieces;
    }
}