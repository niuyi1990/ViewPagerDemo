package com.niuyi.viewpagerdemo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：${牛毅} on 2016/12/20 17:21
 * 邮箱：niuyi19900923@hotmail.com
 */
public class CustomViewPager extends RelativeLayout {

    private boolean isSwitchPage = false;//是否开启自动轮播

    private LinearLayout mLinearLayout;
    private ViewPager mViewPager;

    private List<ImageView> imageViewsList = new ArrayList<>();

    private int[] mDrawable = {R.mipmap.banner_four, R.mipmap.banner_four, R.mipmap.banner_four, R.mipmap.banner_four};

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    };

    public CustomViewPager(Context context) {
        super(context);
        init();
    }

    public CustomViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.custom_pager_view, this);

        mLinearLayout = (LinearLayout) view.findViewById(R.id.linear_layout);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);

        for (int i = 0; i < mDrawable.length; i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(mDrawable[i]);

            imageViewsList.add(imageView);
        }

        mViewPager.setAdapter(new MyAdapter());

        //当前要显示位置
        int item = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2 % imageViewsList.size());
        mViewPager.setCurrentItem(item);

//        mViewPager.setPageTransformer(true,new DepthPageTransformer());
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//实现画廊效果

        new Thread() {
            @Override
            public void run() {
                while (!isSwitchPage) {
                    SystemClock.sleep(3000);//每隔三秒切换一次
                    mHandler.sendEmptyMessage(0);
                }
            }
        }.start();

        mLinearLayout.setOnTouchListener(new View.OnTouchListener() {//边缘触摸也可以滑动
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mViewPager.dispatchTouchEvent(event);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {//Activity onDestroy后调用
        super.onDetachedFromWindow();
        isSwitchPage = true;
    }

    class MyAdapter extends PagerAdapter {

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public Object instantiateItem(View container, int position) {
            int newPosition = position % imageViewsList.size();
            ((ViewPager) container).addView(imageViewsList.get(newPosition));
            return imageViewsList.get(newPosition);
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

    /**
     * 官方提供的滑动效果
     */
    class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

}
