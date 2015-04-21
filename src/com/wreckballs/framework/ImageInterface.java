package com.wreckballs.framework;

import com.wreckballs.framework.GraphicsInterface.ImageFormat;


public interface ImageInterface {
    public int getWidth();
    public int getHeight();
    public ImageFormat getFormat();
    public void dispose();
}
