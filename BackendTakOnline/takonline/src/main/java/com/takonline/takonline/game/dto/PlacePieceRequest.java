package com.takonline.takonline.game.dto;

public class PlacePieceRequest {

    private String playerId;
    private int row;
    private int col;

    public PlacePieceRequest() {
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}