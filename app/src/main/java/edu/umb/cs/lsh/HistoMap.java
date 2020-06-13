package edu.umb.cs.lsh;

import java.util.LinkedHashMap;

import lombok.Getter;

@Getter
public class HistoMap {
    private String name;
    private LinkedHashMap<Integer,Integer> map;

    public HistoMap(String name, int[] vector) {
        this.name = name;
        this.map = vector2map(vector);
    }

    public HistoMap(int[] vector) {
        this.map = vector2map(vector);
    }

    public static LinkedHashMap<Integer,Integer> vector2map(int[] vector) {
        LinkedHashMap<Integer,Integer> map = new LinkedHashMap<>();
        for (int num: vector){
            Integer j = map.get(num);
            map.put(num, j==null ? 1:j+1);
        }
        return map;
    }
}
