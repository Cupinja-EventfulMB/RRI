package com.mygdx.game.utils;
import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    public List<BlockchainData> blockchainDataList;

    public Blockchain() {
        blockchainDataList = new ArrayList<>();
    }
    public void addData(BlockchainData blockchainData) {
        blockchainDataList.add(blockchainData);
    }

    public void displayData() {
        for (BlockchainData data : blockchainDataList) {
            System.out.println("People Count: " + data.peopleCount);
            System.out.println("Location: " + data.latitude + ", " + data.longitude);
        }
    }
}
