package com.survey.hzyanglili1.mysurvey.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.survey.hzyanglili1.mysurvey.Application.Constants;
import com.survey.hzyanglili1.mysurvey.R;
import com.survey.hzyanglili1.mysurvey.utils.DensityUtil;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2016/12/24.
 */

public class MyTitleGridViewAdapter extends BaseAdapter {

    public interface OnVisibleChangedListenner{
        abstract void onVisibleChanged(Boolean isVisible);
    }

    private Context context;
    private List<String> imagePaths;
    private OnVisibleChangedListenner listenner;

    private int preCount = 0;

    public MyTitleGridViewAdapter(Context context, List<String> imagePaths, OnVisibleChangedListenner listenner) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.listenner = listenner;
    }

    @Override
    public int getCount() {

        int nowCount = imagePaths.size();

        if (nowCount >=2 && preCount < 2){
            listenner.onVisibleChanged(true);
        }

        if (nowCount<2 && preCount >=2){
            listenner.onVisibleChanged(false);
        }

        preCount = nowCount;
        return imagePaths.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        CircleImageView circleImageView;
        if (view == null){
            //circleImageView = (CircleImageView) LayoutInflater.from(context).inflate(R.layout.picgridview_item,null);
            circleImageView = new CircleImageView(context);
        }else {
            circleImageView = (CircleImageView) view;
        }

        if (i != getCount()-1){//最后一个添加图片   不设置外边框
            circleImageView.setBorderColor(ContextCompat.getColor(context,R.color.lightskyblue));
           circleImageView.setBorderWidth(2);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DensityUtil.dip2px(context,80),DensityUtil.dip2px(context,80)) ;
        circleImageView.setLayoutParams(params);


        if (imagePaths.get(i).equals(Constants.KONG)) {
            circleImageView.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.gridview_addpic));
            circleImageView.setBorderWidth(0);
        }else {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePaths.get(i));
            circleImageView.setImageBitmap(bitmap);
        }

        return circleImageView;
    }

}
