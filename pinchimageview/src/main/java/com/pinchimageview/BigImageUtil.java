package com.pinchimageview;

import android.content.Intent;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

/**
 * Created by zhaoya on 2016/11/11.
 * 调用此方法为imageview设置点击事件跳转到PinViewActivity
 */
public class BigImageUtil {
    public static final void showBigImage(final ImageView imageView, final String imageUrl) {
        final ImageViewAware thumbAware = new ImageViewAware(imageView);
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(imageView.getContext()));
        }
        imageLoader.displayImage(imageUrl, thumbAware, new DisplayImageOptions.Builder().cacheInMemory(true).build());

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ImageView的宽和高
                // 屏幕密度
                float density = imageView.getContext().getResources().getDisplayMetrics().density;
                // 通过getBounds() 获得ImageView中Image的真实宽高，
                int dw = (int) (imageView.getDrawable().getBounds().width());
                int dh = (int) (imageView.getDrawable().getBounds().height());
                // 通过getIntrinsic获得ImageView中Image的真实宽高
                int inDw = (int) (imageView.getDrawable().getIntrinsicWidth());
                int inDh = (int) (imageView.getDrawable().getIntrinsicHeight());
                MyImageSource imageSource = new MyImageSource(imageUrl, dw, dh);
                Intent intent = new Intent(imageView.getContext(), PicViewActivity.class);
                intent.putExtra("image", imageSource);
                ImageSize targetSize = new ImageSize(thumbAware.getWidth(), thumbAware.getHeight());
                String memoryCacheKey = MemoryCacheUtils.generateKey(imageUrl, targetSize);
                intent.putExtra("cache_key", memoryCacheKey);
                Rect rect = new Rect();
                imageView.getGlobalVisibleRect(rect);
                intent.putExtra("rect", rect);
                intent.putExtra("scaleType", imageView.getScaleType());
                imageView.getContext().startActivity(intent);
            }
        });
    }

}
