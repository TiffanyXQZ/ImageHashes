package edu.umb.cs.colorhistogram;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;


import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.umb.cs.lsh.MyMinHash;
import lombok.Getter;
import lombok.Setter;


@Getter
public class ImageData_MinHash {
    private String name;
    private long time_rgbhashing;
    private long time_minhashing;
    private int num;//num: number of buckets for RGB to integers.
    private int num_color;
    private int num_pixel;
    private Map<Integer,Integer> color_hist;
    private Map<Integer,Integer> sorted_color_hist;
    private int[] pixel_hash;
    private int[] min_hash;
    @Getter
    @Setter
    private static int n = 10; //Top n color
    public static int getN(){
        return n;
    }
    @Getter
    @Setter
    private static int[] range = {2,10};


    public ImageData_MinHash(String name,Bitmap bitmap, int num) {
        this.name = name;
        this.num = num;
        this.num_pixel = bitmap.getWidth() * bitmap.getHeight();
        this.calculate_rgbhash(bitmap);
    }

    public ImageData_MinHash(String name, Bitmap bitmap,int num, MyMinHash minHash) {
        this.name = name;
        this.num = num;
        this.num_pixel = bitmap.getWidth() * bitmap.getHeight();
        this.calculate_rgbhash(bitmap);
        this.calculate_minhash(bitmap,minHash);
    }

    private void calculate_rgbhash(Bitmap bitmap) {
        long duration, startTime,endTime;

        int[] pixels = new int[this.num_pixel];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());


        //RGB to int

        startTime = System.nanoTime();
        int[] pixels_Hash = new int[this.num_pixel];
        int w=(int)Math.ceil((float)256/this.num);
        for (int j=0;j<this.num_pixel;j++) {
            int red = Color.red(pixels[j]);
            int green = Color.green(pixels[j]);
            int blue = Color.blue(pixels[j]);

            pixels_Hash[j] = (int) Math.floor(red / w) * num * num
                    + (int) Math.floor(green / w) * num
                    + (int) Math.floor(blue / w);
        }
        this.pixel_hash = pixels_Hash;
        endTime = System.nanoTime();
        duration = endTime - startTime;
        this.time_rgbhashing = duration;


        //pixel_hash histogtram

        Map<Integer, Integer> color_hist = new HashMap<>();
        for (int i : pixels_Hash){
            Integer j = color_hist.get(i);
            color_hist.put(i,(j==null) ? 1 : j+1);
        }

        this.color_hist = color_hist;
        this.num_color = color_hist.size();

        LinkedHashMap<Integer, Integer> sortedMap = new LinkedHashMap<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            color_hist.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
        }
        this.sorted_color_hist = sortedMap;
    }

    private void calculate_minhash(Bitmap bitmap, MyMinHash minHash) {
        long duration, startTime,endTime;

        startTime = System.nanoTime();
        int[] min_hash = minHash.signature(this.color_hist.keySet());//this minHash is the minhash value of this image
        endTime = System.nanoTime();
        duration = endTime - startTime;
        this.time_minhashing = duration;
        this.min_hash = min_hash;
    }

    public LinkedHashMap getTopNColor(){
        LinkedHashMap<Integer,Integer> map = new LinkedHashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sorted_color_hist.entrySet().stream().limit(this.n).forEach(e->
                    map.put(e.getKey(),e.getValue()));
        }
        return map;
    }


    public LinkedHashMap getTopRangeColor(){
        LinkedHashMap<Integer,Integer> map = new LinkedHashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sorted_color_hist.entrySet().stream().limit(this.range[1]).forEach(e->
                    map.put(e.getKey(),e.getValue()));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sorted_color_hist.entrySet().stream().limit(this.range[0]).forEach(e->map.remove(e.getKey()));
        }

        return map;
    }
}
