package com.wreckballs.implementation;

import com.wreckballs.framework.GraphicsInterface.ImageFormat;
import com.wreckballs.framework.ImageInterface;

import android.graphics.Bitmap;


public class Image implements ImageInterface {
    Bitmap bitmap;
    ImageFormat format;
   
    public Image(Bitmap bitmap, ImageFormat format) {
        this.bitmap = bitmap;
        this.format = format;
    }

    @Override
    public int getWidth() {
        return bitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return bitmap.getHeight();
    }

    @Override
    public ImageFormat getFormat() {
        return format;
    }

    @Override
    public void dispose() {
        bitmap.recycle();
    }      
}
