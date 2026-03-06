package com.takonline.takonline.game.dto;

import java.util.List;

public class MoveStackRequest {

    private String playerId;
    private int fromRow;
    private int fromCol;
    private String direction;
    private int pickupCount;
    private List<Integer> drops;

    public MoveStackRequest() {
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getFromRow() {
        return fromRow;
    }

    public void setFromRow(int fromRow) {
        this.fromRow = fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public void setFromCol(int fromCol) {
        this.fromCol = fromCol;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getPickupCount() {
        return pickupCount;
    }

    public void setPickupCount(int pickupCount) {
        this.pickupCount = pickupCount;
    }

    public List<Integer> getDrops() {
        return drops;
    }

    public void setDrops(List<Integer> drops) {
        this.drops = drops;
    }
}