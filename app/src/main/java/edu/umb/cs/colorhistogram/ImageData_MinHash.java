package edu.umb.cs.colorhistogram;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private int num_pixel,width,height;
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
    private boolean isOrigin=false;
    @Getter
    @Setter
    private static int[] range = {2,10};
    private Pixel[][] pixel_objs;
    private List<PixelProperty> pList=new ArrayList<PixelProperty>();
    private Hashtable temp_ht=new Hashtable();

    private class Pixel{
        //List<PixelProperty> pList=new ArrayList<PixelProperty>();
        int x,y,col;
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
                int idx=0;
                if(p instanceof ColorProperty) idx=p.getIdx(col);
                else if(p instanceof NeighborProperty) idx=p.getIdx(this);
                ret+=idx*base;
                base*=p.getRange();
            }
            // if((x<10)&&(y<10)) System.out.printf("%d,%d: %d\t%d\n", x,y,ret, calHash1(num));

            return ret;
        }
    }

    interface PixelProperty{
        String name=null;
        int min=0, max=0;
        int getRange();
        int getIdx(Object... arg);
    }

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

    private abstract class NeighborProperty implements PixelProperty{
        String name;
        int min, max, num=-1;
        NeighborProperty(String name, int min, int max, int num){
            this.name=name;this.min=min;this.max=max; this.num=num;
        }
        public int getRange(){
            if(num>0) return num;
            else return max-min+1;
        }
        public int getIdx(Object... arg){ //int val, int num
            Pixel p=(Pixel)arg[0];
            int x=p.getX(), y=p.getY();
            return getVal(x,y);
        }
        abstract int getVal(int x, int y);
    }


    int getMostPopularColor(int x, int y){
        int ret=-1;
        temp_ht=new Hashtable();
        int gw=10;// check x-gw,x+gw,y-gw,y+gw
        for(int i=(int)Math.max(0,x-gw);i<=(int)Math.min(width-1,x+gw);i++){
            for(int j=(int)Math.max(0,y-gw);j<=(int)Math.min(height-1,y+gw);j++){
                int pcol=pixel_objs[i][j].getColor();
                Integer val=(Integer)temp_ht.get(new Integer(pcol));
                if(val==null) val=new Integer(1);
                else val++;
                temp_ht.put(new Integer(pcol),val);
            }
        }
        Set<Map.Entry<Integer, Integer>> entrySet = temp_ht.entrySet();

        int max_freq=0;
        for (Map.Entry<Integer, Integer> entry : entrySet)
        {
            if(entry.getValue() > max_freq)
            {
                ret = entry.getKey();
                max_freq = entry.getValue();
            }
        }
        return ret;
    }
    private class neighborRProperty extends NeighborProperty{
        neighborRProperty(String name, int min, int max, int num){
            super(name,min,max,num);
            //this.name=name;this.min=min;this.max=max; this.num=num;
        }

        //public int getIdx(Object... arg){ //int val, int num
        //    Pixel p=(Pixel)arg[0];
        //    int x=p.getX(), y=p.getY();
        int getVal(int x, int y){
            /*int sum=0, count=0;
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
             */
            int col=getMostPopularColor(x,y);
            int val=Color.red(col);
            int w=(int)Math.ceil((float)256/num);
            return (val-min)/w;
        }
    }


    private class neighborGProperty extends NeighborProperty{
        neighborGProperty(String name, int min, int max, int num){
            super(name,min,max,num);
            //this.name=name;this.min=min;this.max=max; this.num=num;
        }
        public int getVal(int x, int y){
            /*
            int sum=0, count=0;
            if(x>0){
                sum+=Color.green(pixel_objs[x-1][y].getColor());
                count++;
            }
            if(x<pixel_objs.length-1){
                sum+=Color.green(pixel_objs[x+1][y].getColor());
                count++;
            }
            if(y>0){
                sum+=Color.green(pixel_objs[x][y-1].getColor());
                count++;
            }
            if(y<pixel_objs[0].length-1){
                sum+=Color.green(pixel_objs[x][y+1].getColor());
                count++;
            }
            int val=(int)Math.round((float)sum/count);
            int w=(int)Math.ceil((float)256/num);
            return (val-min)/w;*/

            int col=getMostPopularColor(x,y);
            int val=Color.green(col);
            int w=(int)Math.ceil((float)256/num);
            return (val-min)/w;
        }
    }

    private class neighborBProperty extends NeighborProperty{
        neighborBProperty(String name, int min, int max, int num){
            super(name,min,max,num);
            //this.name=name;this.min=min;this.max=max; this.num=num;
        }
        public int getVal(int x, int y){
            /*
            int sum=0, count=0;
            if(x>0){
                sum+=Color.blue(pixel_objs[x-1][y].getColor());
                count++;
            }
            if(x<pixel_objs.length-1){
                sum+=Color.blue(pixel_objs[x+1][y].getColor());
                count++;
            }
            if(y>0){
                sum+=Color.blue(pixel_objs[x][y-1].getColor());
                count++;
            }
            if(y<pixel_objs[0].length-1){
                sum+=Color.blue(pixel_objs[x][y+1].getColor());
                count++;
            }
            int val=(int)Math.round((float)sum/count);
            int w=(int)Math.ceil((float)256/num);
            return (val-min)/w;*/

            int col=getMostPopularColor(x,y);
            int val=Color.blue(col);
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

    void init_pList(){
        pList.add(new gProperty("blue",0,255, num));
        pList.add(new bProperty("green",0,255, num));
        pList.add(new rProperty("red",0,255,num));
        pList.add(new neighborRProperty("neighbor_r",0,255,num));
        pList.add(new neighborGProperty("neighbor_g",0,255,num));
        pList.add(new neighborBProperty("neighbor_b",0,255,num));

        //pList.add(new rProperty("neight_red",0,255,num)); //test another property
    }

    public ImageData_MinHash(String name, Bitmap bitmap, int num, MyMinHash minHash, boolean isOrigin) {
        this.name = name;
        this.num = num;
        this.width=bitmap.getWidth();
        this.height=bitmap.getHeight();
        this.isOrigin=isOrigin;

        this.num_pixel = width*height;//bitmap.getWidth() * bitmap.getHeight();
        pixel_objs = new Pixel[width][height];//new Pixel[bitmap.getWidth()][bitmap.getHeight()];

        init_pList();

        this.calculate_rgbhash(bitmap);
        this.calculate_minhash(bitmap,minHash);
    }


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
        /*
        for (int j=0;j<this.num_pixel;j++) {

            int red = Color.red(pixels[j]);
            int green = Color.green(pixels[j]);
            int blue = Color.blue(pixels[j]);

            pixels_Hash[j] = (int) Math.floor(red / w) * num * num
                    + (int) Math.floor(green / w) * num
                    + (int) Math.floor(blue / w);
            }
            */
        for(int i=0;i<width;i++)
            for(int j=0;j<height;j++) {
                Pixel pobj = pixel_objs[i][j];

                if(!isOrigin&&inTLconer(pobj))//frame images filtering
                    pixels_Hash[i+j*width]=-1;
                else  pixels_Hash[i+j*width] = pobj.calHash();
            }

        //System.out.printf("%s: before %d\t",name, pixels_Hash.length);
        pixels_Hash = removeNegativeValue(pixels_Hash);
        //System.out.printf("after %d\n",pixels_Hash.length);

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
        }else{
            System.out.println("not supported");
        }
        this.sorted_color_hist = sortedMap;
    }

    /*
    in top-left corner
     */
    private boolean inTLconer(Pixel p){
        float threshold= (float) 0.1;
        int x=p.getX(), y=p.getY();
        if((x<threshold*width)&&(y<threshold*height))
        return true;
        else return false;
    }
    private int[] removeNegativeValue(int[] input){
        int[] ret=null;
        int count=0;
        for(int i: input){
            if(i>=0) count++;
        }
        ret=new int[count];
        int j=0;
        for(int i: input){
            if(i>=0) ret[j++]=i;
        }
        return ret;
    }

    private void calculate_minhash(Bitmap bitmap, MyMinHash minHash) {
        long duration, startTime,endTime;

        startTime = System.nanoTime();
        Log.i("MyTag",name);

        int[] min_hash = minHash.signature(this.color_hist.keySet());//this minHash is the minhash value of this image

        Log.i("MyTag",String.format("number of colors: %d\t signature size: %d",
                this.color_hist.keySet().size(),min_hash.length));

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
