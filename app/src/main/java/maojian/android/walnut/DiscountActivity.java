package maojian.android.walnut;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by android on 25/10/16.
 */
public class DiscountActivity extends Activity {

    ImageView discount_card1;

    ImageView discount_card2;

    FrameLayout referral_code1;
    FrameLayout referral_code2;

    ImageButton discount_backbutton;

    TextView referral_number1;
    TextView referral_number2;
    TextView referral_times1;
    TextView referral_times2;
    TextView referral_date1;
    TextView referral_date2;

    FrameLayout coupon1;
    TextView coupon_date1;
    ImageView coupon_image1;

    ImageView coupon_label;

    SimpleDateFormat formatter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_discount);

        discount_card1 = (ImageView) findViewById(R.id.discount_card1);

        discount_card2 = (ImageView) findViewById(R.id.discount_card2);

        referral_code1 = (FrameLayout) findViewById(R.id.referral_code1);

        referral_code2 = (FrameLayout) findViewById(R.id.referral_code2);

        referral_number1 = (TextView) findViewById(R.id.referral_number1);

        referral_number2 = (TextView) findViewById(R.id.referral_number2);

        referral_times1 = (TextView) findViewById(R.id.referral_times1);

        referral_times2 = (TextView) findViewById(R.id.referral_times2);

        referral_date1 = (TextView) findViewById(R.id.referral_date1);

        referral_date2 = (TextView) findViewById(R.id.referral_date2);

        coupon1 = (FrameLayout) findViewById(R.id.coupon1);
        coupon_date1 = (TextView) findViewById(R.id.coupon_date1);
        coupon_image1 = (ImageView) findViewById(R.id.coupon_card1);

        coupon_label = (ImageView) findViewById(R.id.coupon_label);

        //coupon1.setVisibility(View.GONE);

        discount_card1.setVisibility(View.GONE);
        discount_card2.setVisibility(View.GONE);

        referral_code1.setVisibility(View.GONE);
        referral_code2.setVisibility(View.GONE);

        formatter = new SimpleDateFormat("MM-dd-yyyy");

        discount_backbutton = (ImageButton) findViewById(R.id.discount_backbutton);
        discount_backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiscountActivity.this.finish();
            }
        });

        getWallImages();

        discount_card1.setOnClickListener(goToStore);
        discount_card2.setOnClickListener(goToStore);
        referral_code1.setOnClickListener(goToStore);
        referral_code2.setOnClickListener(goToStore);
    }

    private View.OnClickListener goToStore =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(DiscountActivity.this, StoreWebActivity.class);
            startActivity(intent);
        }
    };

    public void getWallImages() {

        AVQuery<AVObject> discoverpostquery = new AVQuery<>("Discounts_Rewards");
        discoverpostquery.whereEqualTo("User", AVUser.getCurrentUser());
        discoverpostquery.orderByDescending("createdAt");



        discoverpostquery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                int coupon_count=0;
                if (list != null) {



                    for (int i = 0; i < list.size(); i++) {
                        final AVObject post = (AVObject) list.get(i);

                        if(post.get("type").equals("discount1")){

                            discount_card1.setVisibility(View.VISIBLE);

                        }
                        if(post.get("type").equals("discount2")){

                            discount_card2.setVisibility(View.VISIBLE);

                        }
                        if(post.get("type").equals("refer1")){

                            referral_code1.setVisibility(View.VISIBLE);

                            referral_number1.setText(post.get("Code").toString());

                            referral_date1.setText(formatter.format(post.get("ExpirationDate")));
                            referral_times1.setText("×" + post.get("Times").toString());


                        }
                        if(post.get("type").equals("refer2")){

                            referral_code2.setVisibility(View.VISIBLE);

                            referral_number2.setText(post.get("Code").toString());
                            referral_date2.setText(formatter.format(post.get("ExpirationDate")));
                            referral_times2.setText("×"+post.get("Times").toString());

                        }

                        if(post.get("type").equals("coupons")){

                            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                            View v = vi.inflate(R.layout.coupon_layout, null);

                            v.setOnClickListener(goToStore);

                            ViewGroup insertPoint = (ViewGroup) findViewById(R.id.coupon1);

                            insertPoint.addView(v, coupon_count, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

                            coupon_count++;
                        }

                    }
                }


            }
        });


    }

}
