package com.interrupt.dungeoneer.profile;

public class ProfileStashEntry {

    public String itemName = "";
    public int amount = 1;

    public ProfileStashEntry() {
    }

    public ProfileStashEntry(String itemName, int amount) {
        this.itemName = itemName;
        this.amount = amount;
    }
}
