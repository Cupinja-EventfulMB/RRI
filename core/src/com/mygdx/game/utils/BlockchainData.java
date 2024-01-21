package com.mygdx.game.utils;

public class BlockchainData {
    public double latitude;
    public double longitude;
    public int peopleCount;

    public BlockchainData(double latitude, double longitude, int peopleCount) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.peopleCount = peopleCount;
    }

    @Override
    public String toString() {
        return "BlockchainData{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", peopleCount=" + peopleCount +
                '}';
    }
}