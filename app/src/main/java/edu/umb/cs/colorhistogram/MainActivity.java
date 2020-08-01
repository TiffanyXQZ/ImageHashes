package edu.umb.cs.colorhistogram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.umb.cs.lsh.MyMinHash;
import edu.umb.cs.lsh.WeightedJaccard;

public class MainActivity extends AppCompatActivity {

    private MyAdapter adapt;
    private HashMap<String, Bitmap> images = new HashMap<>();
    private TreeMap<String, ArrayList<String>> imageGroup = new TreeMap<>();

    //imLibs are baseImage names for each folder
    private Map<String, String[]> imLibs;


    private HashMap<String, Integer> name2idx = new HashMap<>();
    private ArrayList<String> inames = new ArrayList<>(5);
    // index for each image in listview
    private TreeMap<String, ArrayList<Integer>> groupIndex = new TreeMap<>();




    private List<ImageData_MinHash> imData_List = new ArrayList<>();
    private List<Bitmap> bitmaps = new ArrayList<>();
    private List<Bitmap> crop_bitmaps = new ArrayList<>();

    private List<Integer> imsID = new ArrayList<>();
    private HashMap<String, Integer> origin_file = new HashMap<String, Integer>();
    int num = 10;//num: number of buckets for RGB to integers.
    int seed = 2020;
    long duration, startTime, endTime;
    MyMinHash minhash = new MyMinHash(0.2, num * num * num, seed);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        loadImageFromAssets();

/*

        String originfiles[] = {"ai10.jpg", "ai20.png", "ai30.jpg", "ai40.jpg", "ai50.jpg"};

        String originfile;// = originfiles[4];
        //origin_file.put(originfile, -1);
        int j = 0;


        for (int i = 0; i < originfiles.length; i++) {

            originfile = originfiles[i];
            origin_file.put(originfile, -1);

            for (String name : images.keySet()) {

                Bitmap bmp = images.get(name);
                if (!name.startsWith(originfile.substring(0, 3))) continue;
                boolean isOrigin = (origin_file.containsKey(name)); //check if it's the online (origin) image

                ImageData imageData = new ImageData(bmp);
                bitmaps.add(bmp);
                crop_bitmaps.add(imageData.getCroppedBitmap());

                //Bitmap img = isOrigin ? imageData.getCroppedBitmap() : bmp; //only crop online image

                Bitmap img = imageData.getCroppedBitmap(); //crop every picture for the library

                ImageData_MinHash imdata = new ImageData_MinHash(name, img, num, minhash, isOrigin);
                imData_List.add(imdata);
                if (isOrigin) origin_file.put(name, j);
                j++;
//            if (j >= 25) break;

            }
        }*/

        new Thread() {
            public void run() {
                print();
            }
        }.start();


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
        //System.out.println("hello from print");



        String[] baseImages = imLibs.get("isis");
        for (String baseImage : baseImages) {
            int baseIdx = name2idx.get(baseImage);
            for (String im  : imageGroup.get(baseImage)) {

                ImageData_MinHash imageData = imData_List.get(name2idx.get(im));


                startTime = System.nanoTime();
                double realJaccard = minhash.jaccard(imData_List.get(baseIdx).getPixel_hash(), imageData.getPixel_hash());
                long realJacTime = System.nanoTime() - startTime;

                startTime = System.nanoTime();
                double minHashSimi = minhash.similarity(imData_List.get(baseIdx).getMin_hash(), imageData.getMin_hash());
                long mHTime = System.nanoTime() - startTime;


                startTime = System.nanoTime();
                double weiSim = WeightedJaccard.similarity(imData_List.get(baseIdx).getColor_hist(), imageData.getColor_hist());
                long wHTime = System.nanoTime() - startTime;



                String str = String.format("\n\n%s and %s: " +
                                "\nsimilarity:\n" +
                                "\t%-20s\t%4.3f\n \t%-20s\t%4.3f\n \t%-20s\t%4.3f" +
                                "\ntime: \n" +
                                "\t%-40s\t%8d ms\n \t%-40s\t%8d ms\n" +
                                "\t%-40s\t%8d ms\n \t%-40s\t%8d ms\n \t%-40s\t%8d ms\n",
                        imageData.getName(), baseImage,
                        "minhash:",
                        minhash.similarity(imData_List.get(baseIdx).getMin_hash(), imageData.getMin_hash()),
                        "real Jaccad:",
                        minhash.jaccard(imData_List.get(baseIdx).getPixel_hash(), imageData.getPixel_hash()),
                        "weighted Jaccard:",
                        WeightedJaccard.similarity(imData_List.get(baseIdx).getColor_hist(), imageData.getColor_hist()),
                        "rgbHashing time:", imageData.getTime_rgbhashing() / 1000,
                        "minHashing time:", imageData.getTime_minhashing() / 1000,
                        "realJaccard time:", realJacTime / 1000,
                        "minHashing similarity time:", mHTime / 1000,
                        "weighted Jaccard similarity time:", wHTime / 1000);

                //System.out.println(str);
                Log.i("MyTag", str);

/*                try {

                    FileOutputStream fOut = openFileOutput(path, MODE_APPEND);
//                    System.out.println(fOut.getFD().toString());
                    OutputStreamWriter osw = new OutputStreamWriter(fOut);
                    osw.write(log);
                    osw.flush();
                    osw.close();

                } catch (IOException e) {

                }*/


            }
        }
    }


    private void loadImageFromAssets() {

//      Images Folders and their original files
        imLibs = new HashMap<String, String[]>() {
            {
                put("coil100", new String[]{"ac10.png", "ac20.png"});
                put("isis", new String[]{"ai10.jpg", "ai20.jpg", "ai30.jpg", "ai40.jpg", "ai50.jpg"});
                put("videoDataset", new String[]{"a1.bmp", "a2.bmp", "a3.bmp", "a4.bmp", "a5.bmp", "a6.bmp"
                        , "a7.bmp", "a8.bmp"});
            }
        };

        for (String path : imLibs.keySet()) {
            for (String origina : imLibs.get(path)) {
                imageGroup.put(origina, new ArrayList<>(5));
            }
        }

        int j=0;
        for (String path : imLibs.keySet()) {

            String[] list;
            try {
                list = getAssets().list(path);
                System.out.println(path + " " + list.length);
                if (list.length > 0) {
                    // This is a folder
                    for (String file : list) {

                        if (path.equals("videoDataset")) {
                            imageGroup.get(file.substring(0, 2) + ".bmp").add(file);

                        } else if (path.equals("isis")) {
                            imageGroup.get(file.substring(0, 3) + "0.jpg").add(file);
                        } else {
                            imageGroup.get(file.substring(0, 3) + "0.png").add(file);
                        }


                        InputStream inIm = getResources().getAssets().open(path + '/' + file);

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 3;
                        Bitmap bitmap = BitmapFactory.decodeStream(inIm, null, options);

                        images.put(file, bitmap);
                        bitmaps.add(bitmap);
                        ImageData imageData = new ImageData(bitmap);
                        Bitmap img = imageData.getCroppedBitmap(); //crop every picture for the library
                        crop_bitmaps.add(img);


                        boolean isOrigin = imageGroup.containsKey(file);
                        ImageData_MinHash imdata = new ImageData_MinHash(file, img, num, minhash, isOrigin);
                        imData_List.add(imdata);
                        name2idx.put(file, j);
                        j++;


                    }
                }
            } catch (IOException e) {

            }
        }

//        for (String or : imageGroup.keySet())
//        {
//            System.out.println(Arrays.toString(imageGroup.get(or).toArray()));
//        }
    }


    private void loadImageData() throws IllegalAccessException {
        Field[] drawablesFields = R.drawable.class.getFields();
        for (int i = 0; i < drawablesFields.length; i++) {
            String name = drawablesFields[i].getName();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), drawablesFields[i].getInt(null), options);
            if (!name.startsWith("a1") || name.startsWith("ab")) continue;
            images.put(name, bmp);
        }
    }


}