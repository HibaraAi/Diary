package cn.snowt.blog.view;

import android.widget.ImageView;

public interface IImageLoader {
    void loadImage(String imagePath, ImageView imageView, int imageHeight);
}
