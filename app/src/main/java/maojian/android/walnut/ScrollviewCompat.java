package maojian.android.walnut;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by android on 29/10/16.
 */
public class ScrollviewCompat extends ScrollView {

    public ScrollviewCompat(Context context) {
        super(context);
    }

    public ScrollviewCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void requestChildFocus (View v, View focused){
        if(v.getClass().getName().equals("com.mapbox.mapboxsdk.maps.MapView")) {

            this.requestDisallowInterceptTouchEvent(true);

        }

//    }

    }
//    @Override
//    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {

}