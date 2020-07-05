package edu.umb.cs.colorhistogram;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.umb.cs.lsh.MyMinHash;


public class AverageDifferenceBySize {
    private int index;
    private int num_bucket;
    private int size;
    private int seed;
    private double diff_average;


    public double getDiff_average() {
        return diff_average;
    }

    public AverageDifferenceBySize(List<ImageBitmaps> imageBitmaps, int index, int size, int num,int seed) {

        this.index = index;
        this.num_bucket = num;
        this.size = size;
        this.seed = seed;
        this.diff_average = this.calcu_diff( imageBitmaps);
    }

    private double calcu_diff(List<ImageBitmaps> imageBitmaps) {

        MyMinHash minHash = new MyMinHash(size,num_bucket,seed);

        List<ImageData_MinHash> ims = new ArrayList<>();
        for (ImageBitmaps bitmap:imageBitmaps){
            ims.add(new ImageData_MinHash(bitmap.getName(),bitmap.getBitmap(),num_bucket,minHash,true));
        }
        double jac,minSim, diff=0;
        ImageData_MinHash imageData = ims.get(index);
        for (ImageData_MinHash im: ims){
            jac = minHash.jaccard(imageData.getPixel_hash(),im.getPixel_hash());
            minSim = minHash.similarity(imageData.getMin_hash(),im.getMin_hash());
            diff = diff + Math.abs(jac - minSim) ;
        }

        return diff/ims.size();
    }
}


