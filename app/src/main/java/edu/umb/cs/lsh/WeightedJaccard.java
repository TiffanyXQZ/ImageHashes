package edu.umb.cs.lsh;

import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

public class WeightedJaccard {

    public static float similarity(Map<Integer,Integer> map1, Map<Integer,Integer> map2){
        Set<Integer> set1 = map1.keySet();
        Set<Integer> set2 = map2.keySet();
        Set<Integer> set = Sets.intersection(set1,set2);
        Long min_sum = 0l, max_sum= 0l;
        for (int i:set){
            Integer x = map1.get(i)==null ? 0: map1.get(i) ;
            Integer y = map2.get(i)==null ? 0: map2.get(i) ;

            min_sum +=  Math.min(x,y);
            max_sum +=  Math.max(x,y);
        }
        return ((float) min_sum)/max_sum;
    }
    public static float similarity(int[] vector1, int[] vector2){
        return similarity(HistoMap.vector2map(vector1), HistoMap.vector2map(vector2));
    }

    }
