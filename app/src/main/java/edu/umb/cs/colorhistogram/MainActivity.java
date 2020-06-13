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

public class MainActivity extends AppCompatActivity {

    private MyAdapter adapt;
    private List<ImageData_MinHash> imData_List = new ArrayList<>();
    private List<ImageBitmaps> imageBitmaps = new ArrayList<>();
    private List<Bitmap> bitmaps = new ArrayList<>();
    private List<Integer> imsID = new ArrayList<>();

    int index = 0;// image index to be compared
    int num = 5;//num: number of buckets for RGB to integers.
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

//                if (field.getName()!="a1") continue;
                if (field.getName() == "abc_ab_share_pack_mtrl_alpha") continue;

//                Drawable img = getResources().getDrawable(field.getInt(null));
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), field.getInt(null));
                if (bmp == null) continue;
                imsID.add( field.getInt(null));
//                System.out.println(field.getName());
                Bitmap copyBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
                /*crop white borders */
                CropMiddleFirstPixelTransformation ct = new CropMiddleFirstPixelTransformation();
                Bitmap bitmap = ct.transform(bmp);
                bitmaps.add(bmp);

                imageBitmaps.add(new ImageBitmaps(field.getName(), bitmap));
                ImageData_MinHash imdata = new ImageData_MinHash(field.getName(), bitmap, num, minhash);
                imData_List.add(imdata);


            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


        }


        for (int i = 0; i < imsID.size(); i++) {
            System.out.println(imsID.get(i));
        }


        String[] infos = imageInfo(imData_List);

        System.out.println(infos.length);
        System.out.println(imageBitmaps.size());
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

            ImageView img = (ImageView) row.findViewById(R.id.imageView);
            ImageView imgCrop =  (ImageView) row.findViewById(R.id.imageCropped);
//img.setImageResource(bitmaps.get(position));
//            img.setImageBitmap(bitmaps.get(position));
//            imgCrop.setImageBitmap(bitmaps.get(0));


//            img.setBackgroundColor(Color.parseColor("#000000"));

            TextView txv = row.findViewById(R.id.info);

            txv.setText(infos[position]);

            System.out.println(infos[position]);


//            txv.setText("Image " + Integer.toString(position)
//                    + "\nNumber of pixels: " + imgs.get(position).getNum_pixel()
//                    + "\nNumber of colors: " + imgs.get(position).getNum_color()
//                    + "\nNumber of buckets: " + imgs.get(position).getNum()
//
//                    + "\nTime of rhb_hashing: " + imgs.get(position).getTime_rgbhashing()
//                    + "\nTime of min_hashing: " + imgs.get(position).getTime_minhashing()
//                    + "\nTop " + ImageData_MinHash.getN() + " colors are : " +
//                    imgs.get(position).getTopNColor()
//                    + "\n Range from " + Arrays.toString(ImageData_MinHash.getRange()) + " colors are : " +
//                    imgs.get(position).getTopRangeColor()
//
//            );


            return row;
        }


    }


    public String[] imageInfo(List<ImageData_MinHash> imHashes){
        String[] infos = new String[imHashes.size()];
        for (int i = 0; i <imHashes.size() ; i++) {
            infos[i] = "Image " + Integer.toString(i)
                    + "\nNumber of pixels: " + imHashes.get(i).getNum_pixel()
                    + "\nNumber of colors: " + imHashes.get(i).getNum_color()
                    + "\nNumber of buckets: " + imHashes.get(i).getNum()

                    + "\nTime of rhb_hashing: " + imHashes.get(i).getTime_rgbhashing()
                    + "\nTime of min_hashing: " + imHashes.get(i).getTime_minhashing()
                    + "\nTop " + ImageData_MinHash.getN() + " colors are : " +
                    imHashes.get(i).getTopNColor()
                    + "\n Range from " + Arrays.toString(ImageData_MinHash.getRange()) + " colors are : " +
                    imHashes.get(i).getTopRangeColor();
        }
        return infos;
    }


}