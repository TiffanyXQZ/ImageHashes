package edu.umb.cs.colorhistogram;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.umb.cs.lsh.MyMinHash;
import lombok.Getter;
import lombok.Setter;
 /*
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * */

@Getter
public class ImageData_MinHash {

    /*
    * @param pList R G B 's property and their bucket num
    * @param pixel_objs Pixel[][]  all pixel converted to Pixel(row,col, color)
    *
    * @param name  the original image name
    * @param num   the number of buckets for RGB to integers
    *
    *
    *
    * */


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
    private Pixel[][] pixel_objs;
    private List<PixelProperty> pList=new ArrayList<PixelProperty>();

    private class Pixel{
        //List<PixelProperty> pList=new ArrayList<PixelProperty>();
        int x,y,col;
        /*
        * @param x  the row index of a pixel
        * @param y  the col index of a pixel
        * @param col  the value of a color
        *
        * */


        Pixel(int x, int y, int col){
            this.x=x;this.y=y;this.col=col;
        }
        int getX(){return x;}
        int getY(){return y;}
        int getColor(){return col;}
        /*
                int calHash1(int num){
                    int w=(int)Math.ceil((float)256/num);
                    int red = Color.red(col);
                    int green = Color.green(col);
                    int blue = Color.blue(col);

                    int ret = (int) Math.floor(red / w) * num * num
                            + (int) Math.floor(green / w) * num
                            + (int) Math.floor(blue / w);
                    return ret;
                }
        */
        int calHash(){
            int ret=0, base=1;
            for(PixelProperty p : pList){
                ret+=p.getIdx(col)*base;
                base*=p.getRange();
            }
            // if((x<10)&&(y<10)) System.out.printf("%d,%d: %d\t%d\n", x,y,ret, calHash1(num));
            return ret;
        }
    }

    /*
    *
    *
    *
    * getIdx() return the RGB hashvalues
    *
    * */


    interface PixelProperty{
        String name=null;
        int min=0, max=0;
        int getRange();
        int getIdx(Object... arg);
    }

    /*
    *
    *
    *
    *
    * */


    private abstract class ColorProperty implements PixelProperty{
        String name;
        int min, max, num=-1;
        ColorProperty(String name, int min, int max, int num){
            this.name=name;this.min=min;this.max=max; this.num=num;
        }
        public int getRange(){
            if(num>0) return num;
            else return max-min+1;
        }


        public int getIdx(Object... arg){ //int val, int num
            int val=getVal((Integer)arg[0]);
            int w=(int)Math.ceil((float)256/num);
            return (val-min)/w;
        }
        abstract int getVal(int val);
    }

    private class rProperty extends ColorProperty{
        rProperty(String name, int min, int max,int num){
            super(name,min,max,num);
        }
        int getVal(int val) {
            return Color.red(val);
        }
    }
    private class bProperty extends ColorProperty{
        bProperty(String name, int min, int max,int num){
            super(name,min,max,num);
        }
        int getVal(int val) {
            return Color.blue(val);
        }
    }
    private class gProperty extends ColorProperty{
        gProperty(String name, int min, int max,int num){
            super(name,min,max,num);
        }
        int getVal(int val) {
            return Color.green(val);
        }
    }


/*
*
*
*
*
*
*
* */



    private class neighborRProperty implements PixelProperty{
        String name;
        int min, max, num=-1;
        neighborRProperty(String name, int min, int max, int num){
            this.name=name;this.min=min;this.max=max; this.num=num;
        }
        public int getRange(){
            if(num>0) return num;
            else return max-min+1;
        }
        public int getIdx(Object... arg){ //int val, int num
            Pixel p=(Pixel)arg[0];
            int x=p.getX(), y=p.getY();
            int sum=0, count=0;
            if(x>0){
                sum+=Color.red(pixel_objs[x-1][y].getColor());
                count++;
            }
            if(x<pixel_objs.length-1){
                sum+=Color.red(pixel_objs[x+1][y].getColor());
                count++;
            }
            if(y>0){
                sum+=Color.red(pixel_objs[x][y-1].getColor());
                count++;
            }
            if(y<pixel_objs[0].length-1){
                sum+=Color.red(pixel_objs[x][y+1].getColor());
                count++;
            }
            int val=(int)Math.round((float)sum/count);
            int w=(int)Math.ceil((float)256/num);
            return (val-min)/w;
        }
    }

    /*
    public ImageData_MinHash(String name,Bitmap bitmap, int num) {
        this.name = name;
        this.num = num;
        this.num_pixel = bitmap.getWidth() * bitmap.getHeight();
        //pixel_objs = new Pixel[bitmap.getWidth()][bitmap.getHeight()];
        //System.out.println(pixel_objs.length);

        this.calculate_rgbhash(bitmap);
    }*/

    /*
    * @param
    *
    *
    *
    * */



    void init_pList(){
        pList.add(new gProperty("blue",0,255, num));
        pList.add(new bProperty("green",0,255, num));
        pList.add(new rProperty("red",0,255,num));
        //pList.add(new rProperty("neight_red",0,255,num)); //test another property
    }

    /*
    *
    *  Creating pixel_object and initilizing pList for this image
    *
    *
    * @param name image name
    * @param bitmap  image bitmap
    * @param num bucket number for RGB hashing
    * @param minhash  minhash
    *
    * */




    public ImageData_MinHash(String name, Bitmap bitmap, int num, MyMinHash minHash) {
        this.name = name;
        this.num = num;
        this.num_pixel = bitmap.getWidth() * bitmap.getHeight();
        pixel_objs = new Pixel[bitmap.getWidth()][bitmap.getHeight()];

        init_pList();

        this.calculate_rgbhash(bitmap);
        this.calculate_minhash(bitmap,minHash);
    }

/*
*   Initializing pixel_objects
*   Initializing pixels_Hash: Calculating the RGB hash for each pixel object into pixels_Hash by pixel.calHash
*
*
*   More
*
*
* @param bitmap
* */
    private void calculate_rgbhash(Bitmap bitmap) {
        long duration, startTime,endTime;

        int[] pixels = new int[this.num_pixel];
        for(int i=0;i<bitmap.getWidth();i++)
            for(int j=0;j<bitmap.getHeight();j++){
                int col=bitmap.getPixel(i,j);
                pixel_objs[i][j]= new Pixel(i,j,col);
//                this.pixel_objs[i][j].setRGBProperties(this.num);
                //pixels[i+j*bitmap.getWidth()]=col;
            }

        //bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0,
          //      bitmap.getWidth(), bitmap.getHeight());


        //RGB to int

        startTime = System.nanoTime();
        int[] pixels_Hash = new int[this.num_pixel];
        int w=(int)Math.ceil((float)256/this.num);
        for (int j=0;j<this.num_pixel;j++) {
            /*int red = Color.red(pixels[j]);
            int green = Color.green(pixels[j]);
            int blue = Color.blue(pixels[j]);

            pixels_Hash[j] = (int) Math.floor(red / w) * num * num
                    + (int) Math.floor(green / w) * num
                    + (int) Math.floor(blue / w);
            */
            Pixel pobj=pixel_objs[j%bitmap.getWidth()][j/bitmap.getWidth()];
            pixels_Hash[j] = pobj.calHash();
        }
        this.pixel_hash = pixels_Hash;

        endTime = System.nanoTime();
        duration = endTime - startTime;
        this.time_rgbhashing = duration;


        filterPixelHash();// filter -1 RGB hash value


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
        }else{
            System.out.println("not supported");
        }
        this.sorted_color_hist = sortedMap;
    }

    private void shrink(int[] arr, int j){

        int[] a = new int[j];
        for (int i = 0; i < j; i++) {
            a[i] = arr[i];
        }
        arr = a;
    }


    private void filterPixelHash(){
        int j=0;
        for (int i = 0; i < pixel_hash.length; i++) {
            if(pixel_hash[i]==-1)
                pixel_hash[j++] = pixel_hash[i];
        }
        shrink(pixel_hash, j);

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
