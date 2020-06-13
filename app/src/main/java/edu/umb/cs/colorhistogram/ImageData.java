package edu.umb.cs.colorhistogram;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.ArrayMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import edu.umb.cs.lsh.HistoMap;
import lombok.Data;
import lombok.Getter;

@Data
public class ImageData {
    private String image_name; // image name
    private Bitmap bitmap = null; // image bitmap
    private int[] orig_pixels;  //  all pixels in one dimensional
    private int[][] imagePixels; // all pixel in 2D array

    private int[] pixels_Hash; // RGB array to hashed int array
    private int[][] imageRGBHash;
    private int[][] croppedImageRGBHash;

    private Bitmap croppedBitmap;


    private int[] minhash;    // mimhash array


    private int num_color;
    private Set<Integer> colorSet;

    private long time_rgbhash;
    private long time_minhash;


    //    private ArrayList<ImageDist> distList = new ArrayList<ImageDist>();
    private int num_default = 5;
    private int id = -1; //index number as the ID
    private int[] bin_idx;
    private int[] bin_hist;

    ImageData(Bitmap bmp, String name) {
        bitmap = bmp;
        image_name = name;
        orig_pixels = readPixels(bitmap);
        bin_idx = new int[orig_pixels.length];
        croppedPixelHash(0.5);
//        calHist(num_default);
    }

    // Flatten bitmap to one dimensional
    private int[] readPixels(Bitmap bitmap) {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());

        int[][] imagePixels = new int[bitmap.getHeight()][bitmap.getWidth()];
        for (int i = 0; i < imagePixels.length; i++) {
            for (int j = 0; j < imagePixels[0].length; j++) {
                imagePixels[i][j] = bitmap.getPixel(i, j);
            }
        }
        this.imagePixels = imagePixels;
        return pixels;
    }


    //Xiaoqian added
    // RGB three dimensions to int one dimension
    //num: 0-255 to num buckets
    public void setPixelsHash(int num) {
        int[] pixels_Hash = new int[orig_pixels.length];
        int[][] imageRGBHash = new int[imagePixels.length][imagePixels[0].length];
        int w = (int) Math.ceil((float) 256 / num);
        for (int j = 0; j < this.orig_pixels.length; j++) {
            int red = Color.red(orig_pixels[j]);
            int green = Color.green(orig_pixels[j]);
            int blue = Color.blue(orig_pixels[j]);

            pixels_Hash[j] = (int) Math.floor(red / w) * num * num
                    + (int) Math.floor(green / w) * num
                    + (int) Math.floor(blue / w);

            imageRGBHash[j / imagePixels.length][j % imagePixels[0].length] = pixels_Hash[j];

        }

        this.imageRGBHash = imageRGBHash;
        this.pixels_Hash = pixels_Hash;
    }


    public void croppedPixelHash(double thresh) {
        if (imageRGBHash == null) setPixelsHash(num_color);
        int col_low = 0, col_high = imageRGBHash.length - 1, row_low = 0, row_high = imageRGBHash.length - 1;
        for (int i = 0; i < imageRGBHash.length; i++) {
            LinkedHashMap<Integer, Integer> map =
                    HistoMap.vector2map(imageRGBHash[i]);
            int max = -1, sum = 0;
            for (Integer j : map.values()) {
                sum += j;
                if (j > max) max = j;
            }
            double freq = (double) max / sum;
            if (freq < thresh) {
                row_low = i;
                break;
            }
        }

        for (int i = imageRGBHash.length - 1; i >= 0; i--) {
            LinkedHashMap<Integer, Integer> map =
                    HistoMap.vector2map(imageRGBHash[i]);
            int max = -1, sum = 0;
            for (Integer j : map.values()) {
                sum += j;
                if (j > max) max = j;
            }
            double freq = (double) max / sum;
            if (freq < thresh) {
                row_high = i;
                break;
            }
        }


        for (int i = 0; i < imageRGBHash[0].length; i++) {
            LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
            for (int j = 0; j < imageRGBHash.length; j++) {
                Integer k = map.get(imageRGBHash[j][i]);
                map.put(imageRGBHash[j][i], k == null ? 1 : k + 1);
            }

            int max = -1, sum = 0;
            for (Integer j : map.values()) {
                sum += j;
                if (j > max) max = j;
            }
            double freq = (double) max / sum;
            if (freq < thresh) {
                col_low = i;
                break;
            }
        }


        for (int i = imageRGBHash[0].length - 1; i >= 0; i--) {
            LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
            for (int j = 0; j < imageRGBHash.length; j++) {
                Integer k = map.get(imageRGBHash[j][i]);
                map.put(imageRGBHash[j][i], k == null ? 1 : k + 1);
            }

            int max = -1, sum = 0;
            for (Integer j : map.values()) {
                sum += j;
                if (j > max) max = j;
            }
            double freq = (double) max / sum;
            if (freq < thresh) {
                col_high = i;
                break;
            }
        }
        System.out.printf("rowlo%rowlow\trowlo%rowhigh\trowlo%collow\trowlo%colhigh\t",
                row_low,row_high,col_low,col_high);
        croppedImageRGBHash = new int[row_high-row_low+1][col_high-col_low+1];
        for (int i = row_low; i <= row_high; i++) {
            for (int j = col_low; j <= col_high; j++) {
                croppedImageRGBHash[i][j] = imageRGBHash[i][j];
            }
        }

        croppedBitmap =  Bitmap.createBitmap(bitmap,
                row_low,col_low,row_high-row_low,col_high-col_low);
        try (FileOutputStream out = new FileOutputStream("cropped" + image_name)) {
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//class ImageDist {
//    private ImageData imgData;
//    private double dist;
//
//    ImageDist(ImageData imgData, double dist) {
//        this.imgData = imgData;
//        this.dist = dist;
//    }
//
//    protected ImageData getImageData() {
//        return imgData;
//    }
//
//    protected double getDist() {
//        return dist;
//    }
//
//}


//    void calHist(int num) {
//        int w = (int) Math.ceil((float) 256 / num);
//        bin_hist = new int[num * num * num];
//        for (int j = 0; j < orig_pixels.length; j++) {
//            int red = Color.red(orig_pixels[j]);
//            int green = Color.green(orig_pixels[j]);
//            int blue = Color.blue(orig_pixels[j]);
//
//            bin_idx[j] = (int) Math.floor(red / w) * num * num
//                    + (int) Math.floor(green / w) * num
//                    + (int) Math.floor(blue / w);
//
//            if (bin_idx[j] > bin_hist.length) {
//                System.out.printf("num:%d, w:%d,r:%d,g:%d,b:%d\n", num, w, red, green, blue);
//            }
//            bin_hist[bin_idx[j]]++;
//
//            //int alpha=Color.alpha(pixels[j]);
//            //int luma = (int)(0.299f * red + 0.587f * green + 0.114f * blue);
//        }
//    }
//
//    protected int[] getBinHist() {
//        return bin_hist;
//    }

//    protected void addDistList(ImageData id, double dist) {
//        if (distList.size() == 0) {
//            distList.add(new ImageDist(id, dist));
//            return;
//        }
//        for (int i = 0; i < distList.size(); i++) {
//            double curDist = distList.get(i).getDist();
//            if (dist < curDist) {
//                distList.add(i, new ImageDist(id, dist));
//                break;
//            }
//        }
//    }

//    protected List<ImageDist> getTopN(int n) {
//        if (n > distList.size()) {
//            System.out.println("out of distList's index range");
//            return distList;
//        }
//        return distList.subList(0, n);
//    }

//    protected String getTopNString(int n) {
//        List alist = getTopN(n);
//        String ret = new String();
//        for (int i = 0; i < alist.size(); i++) {
//            ImageDist imd = (ImageDist) alist.get(i);
//            ret += "ID:" + Integer.toString(imd.getImageData().getId()) + ",dist:"
//                    + String.format("%.03f", imd.getDist()) + "\n";
//
//        }
//        return ret;
//    }

//    public String getMinHashString() {
//        return Arrays.toString(minhash);
//    }

    public static void main(String[] args) {
        Field[] drawablesFields = R.drawable.class.getFields();
        Field field = drawablesFields[1];
        System.out.printf("images number," + field.getName());


        Bitmap bmp = BitmapFactory.decodeFile("res/drawable/a01.jpg");


        System.out.printf("\n," + bmp.getHeight());

//        try {
//            System.out.printf("images number,"+field.getInt(null));
////            Bitmap bmp= BitmapFactory.decodeResource(getResources(), field.getInt(null));
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
    }

}
