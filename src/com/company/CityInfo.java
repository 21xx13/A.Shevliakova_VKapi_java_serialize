package com.company;

import java.math.BigDecimal;
import java.math.RoundingMode;

//информация о городе
public class CityInfo {
    private final int globalCountFriends;
    private final String name; //название
    private int count; //количество друзей, живущих в данном городе
    private double percent; //процентное соотношение

    public CityInfo(String name, int count, int globalCount) {
        this.globalCountFriends = globalCount;
        this.count = count;
        this.name = name;
        this.percent = new BigDecimal((double) count / globalCount * 100)
                .setScale(3, RoundingMode.HALF_EVEN).doubleValue();
    }

    public String name() {
        return this.name;
    }

    public int count() {
        return this.count;
    }

    public double percent() {
        return this.percent;
    }

    public void incCount(){
        this.count++;
        this.percent = new BigDecimal((double) count / this.globalCountFriends * 100)
                .setScale(3, RoundingMode.HALF_EVEN).doubleValue();
    }

    public String toString() {
        return String.format("%s: %d, " + this.percent, this.name, this.count);
    }
}
