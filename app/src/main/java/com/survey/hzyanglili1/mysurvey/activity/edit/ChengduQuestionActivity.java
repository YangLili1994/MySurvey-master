package com.survey.hzyanglili1.mysurvey.activity.edit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.survey.hzyanglili1.mysurvey.Application.Constants;
import com.survey.hzyanglili1.mysurvey.Application.MySurveyApplication;
import com.survey.hzyanglili1.mysurvey.CustomView.MyDialog;
import com.survey.hzyanglili1.mysurvey.CustomView.MyGridView;
import com.survey.hzyanglili1.mysurvey.CustomView.PickerView;
import com.survey.hzyanglili1.mysurvey.R;
import com.survey.hzyanglili1.mysurvey.activity.BaseActivity;
import com.survey.hzyanglili1.mysurvey.adapter.MyTitleGridViewAdapter;
import com.survey.hzyanglili1.mysurvey.db.DBHelper;
import com.survey.hzyanglili1.mysurvey.db.QuestionTableDao;
import com.survey.hzyanglili1.mysurvey.db.SurveyTableDao;
import com.survey.hzyanglili1.mysurvey.entity.ChengduQuestion;
import com.survey.hzyanglili1.mysurvey.entity.Survey;
import com.survey.hzyanglili1.mysurvey.utils.CountHelper;
import com.survey.hzyanglili1.mysurvey.utils.DensityUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.id.toggle;

/**
 * Created by hzyanglili1 on 2016/11/7.
 */

public class ChengduQuestionActivity extends BaseActivity {

    /**-------------------View布局 -------------------**/
    private TextView customTitle = null;
    private EditText titleEt = null;
    private ImageView toggleButton;
    private TextView chengduminTV = null;
    private LinearLayout chengduminLayout = null;
    private TextView chengdumaxTV = null;
    private LinearLayout chengdumaxLayout = null;
    private EditText leftTextView = null;
    private EditText rightTextView = null;
    private Button finishBt = null;
    private MyGridView titlePicGridView = null; //题目标题图片gridview
    private LinearLayout addTitleImageBt = null; //添加标题图片按钮

    /**-------------------题目信息 -------------------**/
    private List<String> titleImages = null; //存储title Bmp图像
    private String surveyName;
    private  int surveyId = 0;
    private int quesId = 0;
    private int quesNum = 0;
    int ismust = 1; //默认必选
    int min = 1;
    int max = 5;
    String quesTitle = Constants.KONG;
    String quesTitleImage;
    String leftText = Constants.KONG;
    String rightText = Constants.KONG;

    /**-------------------适配器 -------------------**/
    private MyTitleGridViewAdapter titleGridViewAdapter = null;  //gridview适配器

    /**-------------------标志位 -------------------**/
    private Boolean isNew = false;
    private Boolean toggleFlag = false;

    //其他
    private int totalQuesCount = 0;
    private SurveyTableDao surveyTableDao = null;
    private QuestionTableDao questionTableDao = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chengduques);

        surveyTableDao = new SurveyTableDao(new DBHelper(this,1));
        questionTableDao = new QuestionTableDao(new DBHelper(this,1));

        Intent intent = getIntent();

        surveyId = intent.getExtras().getInt("survey_id");
        quesNum = intent.getExtras().getInt("ques_num")+1;
        isNew = intent.getExtras().getBoolean("isNew");

        Cursor surveyCursor = surveyTableDao.selectSurveyById(surveyId);
        surveyCursor.moveToNext();
        surveyName = surveyCursor.getString(surveyCursor.getColumnIndex("survey_name"));

        Cursor quesCursor = questionTableDao.selectQuestionBySurveyId(surveyId);
        quesCursor.moveToNext();
        totalQuesCount = quesCursor.getCount();


        if (isNew){//创建题目
            titleImages = new ArrayList<>();
            quesNum = totalQuesCount+1;

        }else {//修改题目

            quesId = intent.getExtras().getInt("ques_id");
            Cursor questionCursor = questionTableDao.selectQuestionByQuestionId(quesId);
            getQuestionInfo(questionCursor);

        }
        initViewAndEvent();
    }

    void getQuestionInfo(Cursor cursor){

        cursor.moveToNext();
        ismust = cursor.getInt(cursor.getColumnIndex("qustion_ismust"));
        quesTitle = cursor.getString(cursor.getColumnIndex("question_title"));
        quesTitleImage = cursor.getString(cursor.getColumnIndex("question_image"));

        String optionString = cursor.getString(cursor.getColumnIndex("option_text"));
        String[] options = optionString.split("\\$");


        titleImages = new ArrayList<>(Arrays.asList(quesTitleImage.split("\\$")));

        leftText = options[0];
        rightText = options[1];
        min = Integer.parseInt(options[2]);
        max = Integer.parseInt(options[3]);
    }

    private void initViewAndEvent(){
        customTitle = (TextView)findViewById(R.id.custom_title_text);

        titleEt = (EditText)findViewById(R.id.activity_chengduques_title);
        toggleButton = (ImageView) findViewById(R.id.activity_chengduques_togglebt);

        chengduminTV = (TextView)findViewById(R.id.activity_chengduques_min_text);
        chengduminLayout = (LinearLayout)findViewById(R.id.activity_chengduques_min);

        chengdumaxTV = (TextView)findViewById(R.id.activity_chengduques_max_text);
        chengdumaxLayout = (LinearLayout)findViewById(R.id.activity_chengduques_max);

        rightTextView = (EditText)findViewById(R.id.activity_chengduques_righttext);
        leftTextView = (EditText)findViewById(R.id.activity_chengduques_lefttext);

        titlePicGridView = (MyGridView) findViewById(R.id.activity_chengduques_titlepic_gridview);
        addTitleImageBt = (LinearLayout) findViewById(R.id.activity_chengduques_addtitleimage);

        customTitle.setText(surveyName+"   "+"Q."+quesNum);

        addTitleImageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePic();
            }
        });

        if (!quesTitle.equals(Constants.KONG)) {
            titleEt.setText(quesTitle);
        }

        if (!leftText.equals(Constants.KONG)) {
            leftTextView.setText(leftText);
        }

        if (!rightText.equals(Constants.KONG)) {
            rightTextView.setText(rightText);
        }

        chengduminTV.setText(min+" ");
        chengdumaxTV.setText(max+" ");

        if (ismust == 1){//必选
            toggleButton.setSelected(true);
            toggleFlag = true;
        }else {
            toggleButton.setSelected(false);
            toggleFlag = false;
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!toggleFlag) {//必选题
                    toggleButton.setSelected(true);
                    toggleFlag = true;
                }else { //非必选题
                    toggleButton.setSelected(false);
                    toggleFlag = false;
                }

            }
        });

        chengduminLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(ChengduQuestionActivity.this).setTitle("请选择程度等级").setIcon(
                        android.R.drawable.ic_dialog_info).setSingleChoiceItems(
                        new String[] { " -5", " -4"," -3"," -2"," -1"," 0"," 1"}, (min+5),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                chengduminTV.setText((which-5)+" ");
                                dialog.dismiss();
                            }
                        }).setNegativeButton("取消", null).show();

            }
        });

        chengdumaxLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                new AlertDialog.Builder(ChengduQuestionActivity.this).setTitle("请选择程度等级").setIcon(
                        android.R.drawable.ic_dialog_info).setSingleChoiceItems(
                        new String[] { " 3", " 4"," 5"," 6"," 7"," 8"," 9"," 10" }, (max-3),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                chengdumaxTV.setText((which+3)+" ");
                                dialog.dismiss();
                            }
                        }).setNegativeButton("取消", null).show();

            }
        });

        finishBt = (Button)findViewById(R.id.activity_chengduques_finish);

        finishBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addQuestion()){
                    Intent intent = new Intent(ChengduQuestionActivity.this,StartSurveyActivity.class);
                    intent.putExtra("survey_id",surveyId);
                    startActivity(intent);
                    finish();
                }
            }
        });

        initGridView();
    }

    private void initGridView(){

        if (titleImages.size() == 0) titleImages.add(Constants.KONG);

        Log.d("haha","init gridview --  titleimage = "+titleImages.get(0));

        titleGridViewAdapter = new MyTitleGridViewAdapter(this, titleImages, new MyTitleGridViewAdapter.OnVisibleChangedListenner() {
            @Override
            public void onVisibleChanged(Boolean isVisible) {
                if (isVisible){
                    titlePicGridView.setVisibility(View.VISIBLE);
                    addTitleImageBt.setVisibility(View.GONE);
                }else {
                    titlePicGridView.setVisibility(View.GONE);
                    addTitleImageBt.setVisibility(View.VISIBLE);
                }
            }
        });
        titlePicGridView.setAdapter(titleGridViewAdapter);
        //设置图片行间距
        titlePicGridView.setVerticalSpacing(DensityUtil.dip2px(this,5));
        //长按删除
        titlePicGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == titleGridViewAdapter.getCount()-1){//最后一张，添加
                    choosePic();

                }else {//删除
                    showDelImageDialog(i);
                }

                return true;
            }
        });
        //单击放大和添加图片
        titlePicGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (i == titleGridViewAdapter.getCount()-1){//最后一张
                    choosePic();

                }else {//点击放大图片

                    Log.d("haha","点击放大图片");
                    Intent intent1 = new Intent(ChengduQuestionActivity.this,ZoomImageActivity.class);
                    intent1.putExtra("imagePath",titleImages.get(i));
                    startActivity(intent1);

                }
            }
        });

    }

    //选择相册
    private void choosePic(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, Constants.CHOOSE_PHOTO);
    }

    //删除图片
    private void showDelImageDialog(final int position) {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);

        builder.setMessage("确认删除此图吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
        //删除标题图片
        titleImages.remove(position);
        titleGridViewAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private Boolean addQuestion(){

        String titleString = titleEt.getText().toString().trim();

        leftText = leftTextView.getText().toString().trim();
        rightText = rightTextView.getText().toString().trim();
        min = Integer.parseInt(chengduminTV.getText().toString().trim());
        max = Integer.parseInt(chengdumaxTV.getText().toString().trim());

        StringBuilder options = new StringBuilder();
        options.append(leftText).append("$").append(rightText).append("$").append(min).append("$").append(max);

        Log.d("haha",TAG+"添加程度题  options --- "+options.toString());



        if (titleString == null || titleString.isEmpty() || titleString.equals("")){
            Toast.makeText(this,"题目标题不能为空！",Toast.LENGTH_LONG).show();
            return false;
        }else {

            StringBuilder titleImagesSB = new StringBuilder();
            for (String s : titleImages){
                titleImagesSB.append(s).append('$');
            }
            titleImagesSB.deleteCharAt(titleImagesSB.length()-1);

            if (isNew) {//新加题目
                int count = questionTableDao.getAllCount();
                ChengduQuestion question = new ChengduQuestion(surveyId,count,titleString,titleImagesSB.toString(),options.toString(),toggleFlag);
                questionTableDao.addQuestion(question);
            }else {//修改题目
                ChengduQuestion question = new ChengduQuestion(surveyId,quesId,titleString,titleImagesSB.toString(),options.toString(),toggleFlag);
                questionTableDao.updateQuestion(question,question.getQuestionId());
            }

            return true;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CHOOSE_PHOTO){
            if (resultCode == RESULT_OK){
                //添加图片
                ChoosePicHelper choosePicHelper = new ChoosePicHelper(this);
                String imagePath = choosePicHelper.getPic(data);

                if (imagePath == null) return;

                titleImages.set(titleImages.size()-1,imagePath);
                titleImages.add(Constants.KONG);
                titleGridViewAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK){//监听返回键

            Log.d("haha",TAG+"---"+"back key");

            Intent intent = new Intent(ChengduQuestionActivity.this,StartSurveyActivity.class);
            intent.putExtra("survey_id",surveyId);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
