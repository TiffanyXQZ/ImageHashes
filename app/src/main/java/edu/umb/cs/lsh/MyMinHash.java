package edu.umb.cs.lsh;


import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import lombok.Getter;

@Getter
public class MyMinHash {

    private static final int LARGE_PRIME = 2147483647; // = 2^31 - 1 !
    private int size_signature;
    private int size_vector;//the total length of categorical vector length
    private int[][] perms;
    private int seed;



    public int[] signature(Set<Integer> set){
        int[] sig = new int[size_signature];
        for (int i = 0; i < size_signature; i++) {
            sig[i] = Integer.MAX_VALUE;
        }
        for (int num : set){
            for (int i=0;i<size_signature;i++){
                sig[i] = Math.min(sig[i], this.permu(i,num));
            }
        }

        return sig;
    }

    public int[] signature(int[] arr){
        Set<Integer> set = new HashSet<>();
        for (int i : arr){ set.add(i);}
        return signature(set);
    }


    private int permu(int i,int num) {
        return (int) (( perms[0][i] * (long) num + perms[1][i]) % LARGE_PRIME);
    }


    public MyMinHash(double error, int size_vector, int seed) {
        this.setSize_vector(size_vector);
        this.setPerms(this.calculateSize(error), seed);
    }

    public MyMinHash(int size_signature, int size_vector, int seed) {
        this.setSize_vector(size_vector);
        this.setPerms(size_signature, seed);
    }

    public MyMinHash(int size_signature, int seed) {
        this.setPerms(size_signature, seed);
    }

    private int calculateSize(double error) {
        if (error < 0 && error > 1) {
            throw new IllegalArgumentException("error should be in [0 .. 1]");
        }
        return (int) (1 / (error * error));
    }

    private void setPerms(int size_sigature, final int seed) {
        if (size_sigature <= 0) {
            throw new InvalidParameterException(
                    "Signature size should be positive");
        }
        if (seed <= 0) {
            throw new InvalidParameterException(
                    "Random seed should be positive");
        }
        this.seed = seed;
        this.size_signature = size_sigature;
        Random random = new Random(seed);

        this.perms = new int[2][size_sigature];
        for (int i=0;i<size_sigature;i++){
            this.perms[0][i] = random.nextInt(LARGE_PRIME - 1) + 1;
            this.perms[1][i] = random.nextInt(LARGE_PRIME - 1) + 1;
        }

    }

    private void setSize_vector(int size_vector) {
        if (size_vector <= 0) {
            throw new InvalidParameterException(
                    "Dictionary size (or vector size) should be positive");
        }

        if (size_vector > (Integer.MAX_VALUE - size_vector) / size_vector) {
            throw new InvalidParameterException(
                    "Dictionary size (or vector size) is too big and will "
                            + "cause a multiplication overflow");
        }
        this.size_vector = size_vector;
    }



    public static double jaccard(Set<Integer> s1, Set<Integer> s2){
        Set<Integer> intersection = new HashSet<Integer>(s1);
        intersection.retainAll(s2);
        Set<Integer> union = new HashSet<Integer>(s1);
        union.addAll(s2);
        if (union.isEmpty()) {
            return 0;
        }

        return (double) intersection.size() / union.size();
    }


    public double similarity(int[] sig1, int[] sig2) {
        if (sig1.length != sig2.length) {
            throw new IllegalArgumentException(
                    "Size of signatures should be the same");
        }

        double sim = 0;
        for (int i = 0; i < sig1.length; i++) {
            if (sig1[i] == sig2[i]) {
                sim += 1;
            }
        }

        return sim / sig1.length;
    }

    public static double jaccard(int[] min_hash, int[] min_hash1) {

        Set<Integer> set = new HashSet<>();
        Set<Integer> set1 = new HashSet<>();


        for (int i : min_hash ){
            set.add(i);
        }
        for (int i : min_hash1 ){
            set1.add(i);
        }
        return jaccard(set,set1);
    }
}
