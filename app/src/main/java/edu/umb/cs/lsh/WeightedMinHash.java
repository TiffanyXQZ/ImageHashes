package edu.umb.cs.lsh;


import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.util.Arrays;

import lombok.Getter;

@Getter
public class WeightedMinHash {
    private int dim;
    private int sample_size;
    private int seed;
    private double[][] rs;
    private double[][] ln_cs;
    private double[][] betas;

    public WeightedMinHash(int dim, int sample_size, int seed) {
        this.dim = dim;
        this.sample_size = sample_size;
        this.seed = seed;

        this.rs = set_rs(sample_size,dim,seed);
        this.ln_cs = set_ln_cs(sample_size,dim,seed);
        this.betas = set_betas(sample_size,dim,seed);

    }


    public int[][] w_minhahes(int arr[]){
        int[][] hashes = new int[this.sample_size][2];
        int t[] = new int[this.dim], k=-1;
        double[] ln_y = new double[this.dim];
        double[] ln_a = new double[this.dim];
        double temp = Double.MAX_VALUE;
        for (int i = 0;i<this.sample_size;i++ ){
            for (int j=0;j<this.dim;j++) {

                if (arr[j] == 0) continue;
                t[j] = (int) Math.floor(Math.log(arr[j]) / this.rs[i][j] + this.betas[i][j]);
                ln_y[j] = (t[j] - this.betas[i][j]) * this.rs[i][j];
                ln_a[j] = this.ln_cs[i][j] - ln_y[j] - this.rs[i][j];

                if (ln_a[j] < temp) {
                    temp = ln_a[j];
                    k = j;
                }
            }

            hashes[i][0] = k;
            hashes[i][1] = t[k];
        }

        return hashes;
    }

    public double w_jaccard(int[][] hashes1,int[][] hashes2){
        if (hashes1.length != this.sample_size || hashes2.length != this.sample_size)
            System.err.println("Dimensions are not corret!");

        int comm=0;
        for (int i=0;i<this.sample_size;i++){
            if (Arrays.equals(hashes1[1],hashes2[i]))
                comm++;
        }

        return (double) comm/this.sample_size;
    }



    private double[][] set_betas(int sample_size, int dim, int seed) {
        double[][] betas = new double[sample_size][dim];

        UniformRealDistribution betas_gen = new UniformRealDistribution(0.0,1.0);
        for (int i=0;i<sample_size;i++)
            for (int j=0;j<dim;j++)
                betas[i][j] = betas_gen.sample();

        return betas;
    }

    private double[][] set_ln_cs(int sample_size, int dim, int seed) {
        double[][] ln_cs = new double[sample_size][dim];
        GammaDistribution  gama_gen = new GammaDistribution(2,1.0,1e-9);
        for (int i=0;i<sample_size;i++)
            for (int j=0;j<dim;j++)
                ln_cs[i][j] = Math.log(gama_gen.sample());
        return ln_cs;
    }

    private double[][] set_rs(int sample_size, int dim, int seed) {

        GammaDistribution  gama_gen = new GammaDistribution(2,1.0,1e-9);
        double[][] rs = new double[sample_size][dim];
        for (int i=0;i<sample_size;i++)
            for (int j=0;j<dim;j++)
                rs[i][j] = gama_gen.sample();
        return rs;
    }



}
