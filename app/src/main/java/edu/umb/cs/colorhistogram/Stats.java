package edu.umb.cs.colorhistogram;

import lombok.Data;

@Data
public class Stats {
    private int[][] mat;
    private double[] rowVariance ;
    private double[] colVariance ;
    private double[] colMean;
    private double[] rowMean;

    public Stats(int[][] mat) {
        this.mat = mat;
        rowMean = new double[mat.length];
        rowVariance = new double[mat.length];
        colMean = new double[mat[0].length];
        colVariance = new double[mat[0].length];
        calMean();
        calVar();
    }

    public void calMean(){
        int i = 0, j = 0;
        for (int k = 0; k < mat[0].length; k++) {
            colMean[k] = 0.0;
        }

        for (i = 0; i < mat.length; i++) {
            rowMean[i] = 0.0;
            for (j = 0; j < mat[0].length; j++) {
                rowMean[i] += mat[i][j];
                colMean[j] += mat[i][j];
            }
            rowMean[i] /= mat[0].length;
        }

        for (int k = 0; k < mat[0].length; k++) {
            colMean[k] /= mat.length;
        }
    }
    public void calVar(){
        int i = 0, j = 0;
        for (int k = 0; k < mat[0].length; k++) {
            colVariance[k] = 0.0;
        }

        for ( i = 0; i < mat.length; i++) {
            rowVariance[i] = 0.0;
            for (j = 0; j < mat[0].length; j++) {
                rowVariance[i] += Math.pow(mat[i][j]-rowMean[i],2);
                colVariance[j] += Math.pow(mat[i][j]-colMean[j],2);
            }
            rowVariance[i] /= mat[0].length;
        }
        for (int k = 0; k < mat[0].length; k++) {
            colVariance[k] /= mat.length;
        }
    }

}
