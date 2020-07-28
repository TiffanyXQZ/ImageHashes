package edu.umb.cs.colorhistogram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

//import org.opencv.android.OpenCVLoader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.umb.cs.lsh.MyMinHash;
import edu.umb.cs.lsh.WeightedJaccard;

public class MainActivity extends AppCompatActivity {

    private MyAdapter adapt;
    private HashMap<String, Bitmap> images = new HashMap<>();


    private List<ImageData_MinHash> imData_List = new ArrayList<>();
    private List<Bitmap> bitmaps = new ArrayList<>();
    private List<Bitmap> crop_bitmaps = new ArrayList<>();

    private List<Integer> imsID = new ArrayList<>();
    private HashMap<String, Integer> origin_file = new HashMap<String, Integer>();
    int index = 0;// image index to be compared
    int num = 10;//num: number of buckets for RGB to integers.
    int seed = 2020;
    long duration, startTime, endTime;
    MyMinHash minhash = new MyMinHash(0.2, num * num * num, seed);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        loadImageFromAssets();


//        try {
//            loadImageData();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }


//<<<<<<< HEAD
//        if (!OpenCVLoader.initDebug()) {
//            OpenCVLoader.initDebug();
//        }
//=======
//        System.out.println(drawablesFields.length);
////       if (!OpenCVLoader.initDebug()) {
////            OpenCVLoader.initDebug();
////        }
//>>>>>>> fe2e785adb4bc38e391c1e2ca8bea08ae943921f


        String originfiles[] = {"ai10.jpg","ai20.png","ai30.jpg","ai40.jpg","ai50.jpg"};

        String originfile = originfiles[4];
        origin_file.put(originfile, -1);
        int j = 0;

        for (String name : images.keySet()) {

            Bitmap bmp = images.get(name);



            System.out.println();
            if (!name.startsWith(originfile.substring(0,3))) continue;
            boolean isOrigin = (origin_file.containsKey(name)); //check if it's the online (origin) image

            System.out.println(name);

            ImageData imageData = new ImageData(bmp);
            bitmaps.add(bmp);
            crop_bitmaps.add(imageData.getCroppedBitmap());

            Bitmap img = isOrigin ? imageData.getCroppedBitmap() : bmp; //only crop online image


            ImageData_MinHash imdata = new ImageData_MinHash(name, img, num, minhash, isOrigin);
            imData_List.add(imdata);
            if (isOrigin) origin_file.put(name, j);
            j++;
//            if (j >= 25) break;

        }


        print();


        String[] infos = imageInfo(imData_List);
        adapt = new MyAdapter(this, infos);
        ListView listTask = (ListView) findViewById(R.id.listview);
        listTask.setAdapter(adapt);

    }


    private class MyAdapter extends ArrayAdapter<String> {
        Context context;
        //        private List<Integer> bitmaps;
        private String[] infos;

        public MyAdapter(Context c, String[] objects) {
            super(c, R.layout.row, objects);
            infos = objects;
            context = c;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if (row == null) {

                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.row, parent, false);
            }

            ImageView img = (ImageView) row.findViewById(R.id.image);
            ImageView imgCrop = (ImageView) row.findViewById(R.id.imageCropped);

            img.setImageBitmap(bitmaps.get(position));
            imgCrop.setImageBitmap(crop_bitmaps.get(position));
//            img.setImageBitmap(bitmaps.get(position));
//            imgCrop.setImageBitmap(bitmaps.get(0));


            TextView txv = row.findViewById(R.id.info);

            txv.setText(infos[position]);


            return row;
        }


    }


    public String[] imageInfo(List<ImageData_MinHash> imHashes) {
        String[] infos = new String[imHashes.size()];
        for (int i = 0; i < imHashes.size(); i++) {
            infos[i] = "Image " + Integer.toString(i)
                    + "\nName: " + imHashes.get(i).getName()
                    + "\nNumber of pixels: " + imHashes.get(i).getNum_pixel()
                    + "\nNumber of colors: " + imHashes.get(i).getNum_color()
                    + "\nNumber of buckets: " + imHashes.get(i).getNum()

                    + "\nTime of rhb_hashing: " + imHashes.get(i).getTime_rgbhashing()
                    + "\nTime of min_hashing: " + imHashes.get(i).getTime_minhashing()

   /*                 + "\nTop " + ImageData_MinHash.getN() + " colors are : " +
                    imHashes.get(i).getTopNColor()
                    + "\n Range from " + Arrays.toString(ImageData_MinHash.getRange()) + " colors are : " +
                    imHashes.get(i).getTopRangeColor()
            */
            ;
        }
        return infos;
    }

    private void print() {
        //     Consider all colors
//     Print minhash similarity between each image to a00000.jpg
//     Print Real Jaccard similarity between each image to a00000.jpg
//     Print Weighted Jaccard similarity between each image to a00000.jpg
        System.out.println("hello from print");

        for (String name : origin_file.keySet()) {
            int index = origin_file.get(name);
            System.out.println(name);
            System.out.println(index );
            if (index == -1) continue;
            System.out.println(imData_List.size());


//            System.out.println(Arrays.toString(name.split("\\.")));
//            String path = "/home/haoyu/projects/ImageHashes/Data/data/" + name.split("\\.")[0] + ".txt";

            String path = name.split("\\.")[0] + ".txt";
            for (ImageData_MinHash imageData : imData_List) {

                startTime = System.nanoTime();
                double realJaccard =  minhash.jaccard(imData_List.get(index).getPixel_hash(), imageData.getPixel_hash());
                long realJacTime = System.nanoTime() - startTime;

                startTime = System.nanoTime();
                double minHashSimi =   minhash.similarity(imData_List.get(index).getMin_hash(), imageData.getMin_hash()) ;
                long mHTime = System.nanoTime() - startTime;


                startTime = System.nanoTime();
                double weiSim =   WeightedJaccard.similarity(imData_List.get(index).getColor_hist(), imageData.getColor_hist()) ;
                long wHTime = System.nanoTime() - startTime;



                String log = "-----------------\n" +
                        "Minhash similarity, real Jaccad similarity and weighted Jaccard similarity of " + imageData.getName()
                        + " to " + name + " are: " + minHashSimi + " and " +  realJaccard + " and " +weiSim  + "\n"+
                        "RGBHashing time" + imageData.getTime_rgbhashing() +"\n"+
                                "minHashing time" + imageData.getTime_minhashing() +

                                "realJacard time" + realJacTime +"\n"+
                "MinHashing Similarity time" + mHTime +"\n"+
                        "weightedJaccard Similarity time" + wHTime

                        ;
//                System.out.println(log);


                System.out.printf("%s and %s: \tminhash:%04f, \treal Jaccad:%04f,  \tweighted Jaccard:%04f\n",
                        imageData.getName(),name,
                        minhash.similarity(imData_List.get(index).getMin_hash(), imageData.getMin_hash()),
                        minhash.jaccard(imData_List.get(index).getPixel_hash(), imageData.getPixel_hash()),
                        WeightedJaccard.similarity(imData_List.get(index).getColor_hist(), imageData.getColor_hist()) );

                try {

                    FileOutputStream fOut = openFileOutput(path, MODE_APPEND);
//                    System.out.println(fOut.getFD().toString());
                    OutputStreamWriter osw = new OutputStreamWriter(fOut);
                    osw.write(log);
                    osw.flush();
                    osw.close();

                } catch (IOException e) {

                }


            }
        }
    }


    private void loadImageFromAssets() {
        System.out.println("hell");
        String[] list;
        String path = "isis";
        try {
            list = getAssets().list(path);
//            System.out.println(list.length);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
//                    System.out.println(file);

                    InputStream inIm = getResources().getAssets().open(path + '/' + file);

                    Bitmap bitmap = BitmapFactory.decodeStream(inIm);
                    System.out.println(bitmap.getHeight());
                    images.put(file, bitmap);
//                    inIm.close();

                }
            }
        } catch (IOException e) {

        }
    }


    private void loadImageData() throws IllegalAccessException {
        Field[] drawablesFields = R.drawable.class.getFields();
        for (int i = 0; i < drawablesFields.length; i++) {
            String name = drawablesFields[i].getName();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize=4;




            Bitmap bmp = BitmapFactory.decodeResource(getResources(), drawablesFields[i].getInt(null),options);
            if (!name.startsWith("a1") || name.startsWith("ab")) continue;
            images.put(name, bmp);
        }
    }


}