package cn.snowt.blog.view;

import android.widget.ImageView;

/**
 * cn.snowt.blog.view包下所有都是复制他人的代码
 * 来源：
 * https://github.com/sendtion/XRichText
 *
 *
 * 这个包下的代码负责富文本的编辑与展示
 */
public class XRichText {
    private static XRichText instance;
    private IImageLoader imageLoader;

    public static XRichText getInstance(){
        if (instance == null){
            synchronized (XRichText.class){
                if (instance == null){
                    instance = new XRichText();
                }
            }
        }
        return instance;
    }

    public void setImageLoader(IImageLoader imageLoader){
        this.imageLoader = imageLoader;
    }

    public void loadImage(String imagePath, ImageView imageView, int imageHeight){
        if (imageLoader != null){
            imageLoader.loadImage(imagePath, imageView, imageHeight);
        }
    }
}
