package com.github.hteph.tables;

import java.util.TreeMap;

/**

 */
public class StarClassificationTable {

    private final static TreeMap<Integer, String> map = new TreeMap<>();

    static {
        map.put(60000,"X");
        map.put(30000, "O");
        map.put(10000, "B");
        map.put(7500, "A");
        map.put(6000, "F");
        map.put(5200, "G");
        map.put(3700, "K");
        map.put(2400, "M");
        map.put(1300, "L");
        map.put(250, "T");
        map.put(0, "Y");
    }

    public static String findStarClass(int temperature){

        Integer baseTemp = map.floorKey(temperature);
        Integer topTemp = map.ceilingKey(temperature);
        System.out.println("method:findStarClass ;"+baseTemp +" to "+ topTemp);
        int deciNumber = 10 -(10*(temperature-baseTemp)/(topTemp-baseTemp));
        return map.get(baseTemp)+ deciNumber;
    }
}
