package maojian.android.walnut;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by android on 29/10/16.
 */


public class ViewPagerCompat extends ViewPager {

    public ViewPagerCompat(Context context) {
        super(context);
    }

    public ViewPagerCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {

        Log.e("map debug!!", "ViewPagerCompat canScroll  " + v.getClass().getName());

        //this.requestDisallowInterceptTouchEvent(true);

        if(v.getClass().getName().equals("maojian.android.walnut.ScrollviewCompat")) {

            Log.e("map debug@@@","ViewPagerCompat canScroll");

            ScrollviewCompat a = (ScrollviewCompat)v;

            //a.requestDisallowInterceptTouchEvent(true);

        }

        if(v.getClass().getName().equals("com.mapbox.mapboxsdk.maps.MapView")) {

            Log.e("map debug","ViewPagerCompat canScroll");

            this.requestDisallowInterceptTouchEvent(true);

            return true;
        }
        //if(v instanceof MapView){
        //    return true;
        //}
        return super.canScroll(v, checkV, dx, x, y);
    }
}