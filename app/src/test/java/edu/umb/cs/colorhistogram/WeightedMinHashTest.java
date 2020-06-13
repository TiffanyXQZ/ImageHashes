package edu.umb.cs.colorhistogram;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.umb.cs.lsh.WeightedJaccard;
import edu.umb.cs.lsh.WeightedMinHash;

public class WeightedMinHashTest {

    public static void main(String[] args) {

//        WeightedMinHash w_minhash = new WeightedMinHash(10,1,1);
//
//        for (double[] a : w_minhash.getRs())
//        System.out.println(Arrays.toString(a));



        int[] arr1 = {1, 3, 4, 5, 6, 7, 8, 9, 10, 4};
        int[] arr2 = {2, 4, 3, 8, 4, 7, 10, 9, 0, 0};
        WeightedMinHash w_MinHash = new WeightedMinHash(arr1.length,128,1);

        int[][] wh1 = w_MinHash.w_minhahes(arr1);
        int[][] wh2 = w_MinHash.w_minhahes(arr2);

        System.out.println("Estimated Jaccard is: " + w_MinHash.w_jaccard(wh1,wh2));



        Map<Integer,Integer> map1 = new LinkedHashMap<>();
        Map<Integer,Integer> map2 = new LinkedHashMap<>();


        for (int i=0;i<arr1.length;i++){
            map1.put(i,arr1[i]);
        }
        for (int i=0;i<arr2.length;i++){
            map2.put(i,arr2[i]);
        }
        System.out.println(map1);
        System.out.println(map2);
        System.out.printf("%f", WeightedJaccard.similarity(map1,map2));





    }
}
