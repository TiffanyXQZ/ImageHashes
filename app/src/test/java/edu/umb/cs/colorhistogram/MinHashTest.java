package edu.umb.cs.colorhistogram;

import java.util.Arrays;
import java.util.TreeSet;

import edu.umb.cs.lsh.MyMinHash;
import info.debatty.java.lsh.MinHash;

public class MinHashTest {
    public static void main(String[] args) {
        // Initialize the hash function for an similarity error of 0.1
        // For sets built from a dictionary of 5 items
        MyMinHash myMinHash = new MyMinHash(0.1, 5, 2019);


        // Or as a set of integers:
        // set2 = [1 0 1 1 0]
        TreeSet<Integer> set2 = new TreeSet<Integer>();
        set2.add(0);
        set2.add(2);
        set2.add(3);
        int[] sig2 = myMinHash.signature(set2);
        System.out.println("Signature : " + Arrays.toString(sig2));


        MinHash minHash = new MinHash(0.1,5,2019);
        System.out.println("Signature : " + Arrays.toString(minHash.signature(set2)));















        System.out.println(Arrays.toString(myMinHash.getPerms()[0]));
        System.out.println(Arrays.toString(myMinHash.getPerms()[1]));
        System.out.println(myMinHash.getSize_signature());

        long[][] coes = minHash.getCoefficients();
        System.out.println(Integer.MAX_VALUE);
        System.out.println(coes.length);

        for (int i=0;i<coes.length;i++){
            System.out.printf("%d, ",coes[i][0]);
        }
        System.out.println("");
        for (int i=0;i<coes.length;i++){
            System.out.printf("%d, ",coes[i][1]);
        }

//        System.out.println("Signature similarity: " + minhash.similarity(sig1, sig2));
//        System.out.println("Real similarity (Jaccard index)" +
//                MyMinHash.jaccardIndex(MyMinHash.convert2Set(vector1), set2));
    }
}
