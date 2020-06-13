package edu.umb.cs.colorhistogram;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.umb.cs.lsh.WeightedJaccard;

public class WeightedJaccardTest {

    public static void main(String[] args) {
        Map<Integer,Integer> map1 = new LinkedHashMap<>();
        Map<Integer,Integer> map2 = new LinkedHashMap<>();
        int[] arr1 = {1,100};
        int[] arr2 = {100,1};

        for (int i=0;i<arr1.length;i++){
            map1.put(i,arr1[i]);
        }
        for (int i=0;i<arr2.length;i++){
            map2.put(i,arr2[i]);
        }
        System.out.println(map1);
        System.out.println(map2);
        System.out.printf("%f",WeightedJaccard.similarity(map1,map2));
    }

}
