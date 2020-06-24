package edu.umb.cs.colorhistogram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.umb.cs.lsh.MyMinHash;
import edu.umb.cs.lsh.WeightedJaccard;

public class MainActivity extends AppCompatActivity {

    private MyAdapter adapt;
    private List<ImageData_MinHash> imData_List = new ArrayList<>();
    private List<ImageBitmaps> imageBitmaps = new ArrayList<>();
    private List<Bitmap> bitmaps = new ArrayList<>();
    private List<Bitmap> crop_bitmaps = new ArrayList<>();

    private List<Integer> imsID = new ArrayList<>();

    int index = 0;// image index to be compared
    int num = 10;//num: number of buckets for RGB to integers.
    int seed = 2020;
    long duration, startTime, endTime;
    MyMinHash minhash = new MyMinHash(0.2, num * num * num, seed);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Field[] drawablesFields = R.drawable.class.getFields();

        System.out.println(drawablesFields.length);


        for (int i = 0; i < drawablesFields.length; i++) {
            Field field = drawablesFields[i];

            try {

                if(!field.getName().startsWith("a00") &&
                        !field.getName().startsWith("b")) continue;

//                if (field.getName()!="a1") continue;
                if (field.getName() == "abc_ab_share_pack_mtrl_alpha") continue;

//                Drawable img = getResources().getDrawable(field.getInt(null));
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), field.getInt(null));
                if (bmp == null) continue;
                imsID.add( field.getInt(null));
                ImageData imageData = new ImageData(bmp);
                bitmaps.add(bmp);
                crop_bitmaps.add(imageData.getCroppedBitmap());
                imageBitmaps.add(new ImageBitmaps(field.getName(), bmp));
                ImageData_MinHash imdata = new ImageData_MinHash(field.getName(), imageData.getCroppedBitmap(), num, minhash);
                imData_List.add(imdata);


            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


        }

        print();


        String[] infos = imageInfo(imData_List);
        adapt = new MyAdapter(this, imsID, infos);
        ListView listTask = (ListView) findViewById(R.id.listview);
        listTask.setAdapter(adapt);

    }


    private class MyAdapter extends ArrayAdapter<String> {
        Context context;
        private List<Integer> bitmaps;
        private String[] infos;

        public MyAdapter(Context c,List<Integer> bitmaps, String[] objects) {
            super(c, R.layout.row, objects);
            this.bitmaps = bitmaps;
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
            ImageView imgCrop =  (ImageView) row.findViewById(R.id.imageCropped);

            img.setImageResource(bitmaps.get(position));
            imgCrop.setImageBitmap(crop_bitmaps.get(position));
//            img.setImageBitmap(bitmaps.get(position));
//            imgCrop.setImageBitmap(bitmaps.get(0));



            TextView txv = row.findViewById(R.id.info);

            txv.setText(infos[position]);



            return row;
        }


    }


    public String[] imageInfo(List<ImageData_MinHash> imHashes){
        String[] infos = new String[imHashes.size()];
        for (int i = 0; i <imHashes.size() ; i++) {
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

        for (ImageData_MinHash imageData : imData_List) {
            System.out.println("-----------------\n");
            System.out.printf("Minhash similarity, real Jaccad similarity and weighted Jaccard similarity of " + imageData.getName()
                    + " to a00000 are: " + minhash.similarity(imData_List.get(index).getMin_hash(),
                    imageData.getMin_hash()) + " and " +
                    minhash.jaccard(imData_List.get(index).getPixel_hash(), imageData.getPixel_hash()) + " and " +
                    WeightedJaccard.similarity(imData_List.get(index).getColor_hist(), imageData.getColor_hist()) + "\n");
        }
    }










}