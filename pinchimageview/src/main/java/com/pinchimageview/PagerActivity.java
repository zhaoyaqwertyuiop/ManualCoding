package com.pinchimageview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class PagerActivity extends Activity {

    private TextView countTV;
    private List<String> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinchimageview_pager);

        countTV = (TextView) super.findViewById(R.id.countTV);

        imageList = getIntent().getStringArrayListExtra("images");
        if (imageList == null) {
            return;
        }

        final LinkedList<PinchImageView> viewCache = new LinkedList<PinchImageView>();
        final DisplayImageOptions thumbOptions = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).cacheInMemory(true).build();
        final DisplayImageOptions originOptions = new DisplayImageOptions.Builder().build();

        final PinchImageViewPager pager = (PinchImageViewPager) findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return imageList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object o) {
                return view == o;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                PinchImageView piv;
                if (viewCache.size() > 0) {
                    piv = viewCache.remove();
                    piv.reset();
                } else {
                    piv = new PinchImageView(PagerActivity.this);
                }

                // ImageView的宽和高
                getImageLoader(getApplicationContext()).displayImage(imageList.get(position), piv, thumbOptions);
                container.addView(piv);
                return piv;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                PinchImageView piv = (PinchImageView) object;
                container.removeView(piv);
                viewCache.add(piv);
            }

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                PinchImageView piv = (PinchImageView) object;
//                getImageLoader(getApplicationContext()).displayImage(image.getUrl(image.getOriginWidth(), image.getOriginHeight()), piv, originOptions);
                getImageLoader(getApplicationContext()).displayImage(imageList.get(position), piv, originOptions);
                pager.setMainPinchImageView(piv);
            }
        });

        pager.setOnPageChangeListener(new PinchImageViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                countTV.setText(position + 1 + "/" + imageList.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        int currentPosition = getIntent().getIntExtra("position", 0);
        pager.setCurrentItem(currentPosition);
        countTV.setText(currentPosition + 1 + "/" + imageList.size());
    }

    public static ImageLoader getImageLoader(Context context) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        }
        return imageLoader;
    }

    /** 打开PagerActivity */
    public static final void startActivity(Activity activity, ArrayList<String> imageList, int position) {
        Intent intent = new Intent(activity, PagerActivity.class);
        intent.putStringArrayListExtra("images", imageList);
        intent.putExtra("position", position);
        activity.startActivity(intent);
    }
}