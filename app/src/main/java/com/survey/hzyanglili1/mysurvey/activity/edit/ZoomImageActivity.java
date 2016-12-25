package com.survey.hzyanglili1.mysurvey.activity.edit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.survey.hzyanglili1.mysurvey.R;

/**
 * Created by Administrator on 2016/12/24.
 */

public class ZoomImageActivity extends Activity {

    private ImageView imageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置全屏显示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_zoomimage);

        String imagePath = getIntent().getExtras().getString("imagePath");
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        imageView = (ImageView) findViewById(R.id.activity_zoomimage);
        imageView.setImageBitmap(bitmap);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }
}
