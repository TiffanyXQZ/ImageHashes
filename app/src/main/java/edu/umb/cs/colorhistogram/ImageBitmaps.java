package edu.umb.cs.colorhistogram;

import android.graphics.Bitmap;

import lombok.Getter;


@Getter
public class ImageBitmaps{
    private String name;
    private Bitmap bitmap;

    public ImageBitmaps(String name, Bitmap bitmap) {
        this.name = name;
        this.bitmap = bitmap;
    }
}
