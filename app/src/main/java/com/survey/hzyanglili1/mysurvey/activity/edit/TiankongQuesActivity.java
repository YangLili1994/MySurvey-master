package com.survey.hzyanglili1.mysurvey.activity.edit;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.survey.hzyanglili1.mysurvey.CustomView.MyGridView;
import com.survey.hzyanglili1.mysurvey.R;
import com.survey.hzyanglili1.mysurvey.activity.BaseActivity;
import com.survey.hzyanglili1.mysurvey.adapter.MyTitleGridViewAdapter;
import com.survey.hzyanglili1.mysurvey.db.DBHelper;
import com.survey.hzyanglili1.mysurvey.db.QuestionTableDao;
import com.survey.hzyanglili1.mysurvey.db.SurveyTableDao;
import com.survey.hzyanglili1.mysurvey.entity.ChengduQuestion;
import com.survey.hzyanglili1.mysurvey.entity.Survey;
import com.survey.hzyanglili1.mysurvey.entity.TiankongQuestion;
import com.survey.hzyanglili1.mysurvey.utils.CountHelper;
import com.survey.hzyanglili1.mysurvey.utils.DensityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by hzyanglili1 on 2016/11/7.
 */

public class TiankongQuesActivity extends BaseActivity {

    /**-------------------View布局 -------------------**/
    private TextView customTitle = null;
    private EditText titleEt = null;
    private ImageView toggleButton;
    private Button finishBt = null;
    private MyGridView titlePicGridView = null; //题目标题图片gridview
    private LinearLayout addTitleImageBt = null; //添加标题图片按钮

    /**-------------------题目信息 -------------------**/
    private String surveyName = null;
    String titleString = null;
    private  int surveyId = 0;
    private int quesId = 0;    //题目id
    private int quesNum = 0;   //题目编号
    int ismust = 0;
    String quesTitle = "";
    private List<String> titleImages = null; //存储title Bmp图像

    /**-------------------标志位 -------------------**/
    private Boolean isNew = false;
    private Boolean toggleFlag = false;


    /**-------------------适配器 -------------------**/
    private MyTitleGridViewAdapter titleGridViewAdapter = null;  //gridview适配器


    //其他
    private int totalQuesCount = 0;
    private SurveyTableDao surveyTableDao = null;
    private QuestionTableDao questionTableDao = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiankongques);

        surveyTableDao = new SurveyTableDao(new DBHelper(this,1));
        questionTableDao = new QuestionTableDao(new DBHelper(this,1));

        Intent intent = getIntent();

        surveyId = intent.getExtras().getInt("survey_id");
        Cursor surveyCursor = surveyTableDao.selectSurveyById(surveyId);
        surveyCursor.moveToNext();
        surveyName = surveyCursor.getString(surveyCursor.getColumnIndex("survey_name"));

        isNew = intent.getExtras().getBoolean("isNew");
        if (isNew){
            Cursor quesCursor = questionTableDao.selectQuestionBySurveyId(surveyId);
            quesCursor.moveToNext();
            totalQuesCount = quesCursor.getCount();

            quesId = totalQuesCount + 1;

            titleImages = new ArrayList<>();

        }else {
            quesNum = intent.getExtras().getInt("ques_num")+1;
            quesId = intent.getExtras().getInt("ques_id");
            Cursor questionCursor = questionTableDao.selectQuestionByQuestionId(quesId);
            getQuestionInfo(questionCursor);
        }

        initViewAndEvent();
    }

    void getQuestionInfo(Cursor cursor){

        cursor.moveToNext();

        String quesTitleImage;

        ismust = cursor.getInt(cursor.getColumnIndex("qustion_ismust"));
        quesTitle = cursor.getString(cursor.getColumnIndex("question_title"));
        quesTitleImage = cursor.getString(cursor.getColumnIndex("question_image"));
        titleImages = new ArrayList<>(Arrays.asList(quesTitleImage.split("\\$")));
    }

    private void initViewAndEvent(){
        customTitle = (TextView)findViewById(R.id.custom_title_text);

        titlePicGridView = (MyGridView) findViewById(R.id.activity_tiankongques_titlepic_gridview);
        addTitleImageBt = (LinearLayout) findViewById(R.id.activity_tiankongques_addtitleimage);

        addTitleImageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //选择相册
                choosePic();
            }
        });

        titleEt = (EditText)findViewById(R.id.activity_tiankongques_title);
        toggleButton = (ImageView) findViewById(R.id.activity_tiankongques_togglebt);

        if (!isNew){
            customTitle.setText(surveyName+"   "+"Q."+quesNum);
            titleEt.setText(quesTitle);
            if (ismust == 1){//必选
                toggleButton.setSelected(true);
                toggleFlag = true;
            }
        }else {
            customTitle.setText(surveyName+"   "+"Q."+(questionTableDao.getQuesCountBySurveyId(surveyId)+1));
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

        finishBt = (Button)findViewById(R.id.activity_tiankongques_finish);

        finishBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addQuestion()){
                    Intent intent = new Intent(TiankongQuesActivity.this,StartSurveyActivity.class);
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

                    Intent intent1 = new Intent(TiankongQuesActivity.this,ZoomImageActivity.class);
                    intent1.putExtra("imagePath",titleImages.get(i));
                    startActivity(intent1);
                }
            }
        });

    }

    //删除图片
    private void showDelImageDialog(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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


    //选择相册
    private void choosePic(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, Constants.CHOOSE_PHOTO);
    }


    private Boolean addQuestion(){

        titleString = titleEt.getText().toString().trim();
        if (titleString == null || titleString.isEmpty() || titleString.equals("")){
            Toast.makeText(this,"题目标题不能为空！",Toast.LENGTH_LONG).show();
            return false;
        }else {

            StringBuilder titleImageSB = new StringBuilder();

            for (String s : titleImages){
                titleImageSB.append(s).append('$');
            }
            //多添加了一个$
            titleImageSB.deleteCharAt(titleImageSB.length()-1);

            if (isNew) {//新加题目

                int count = questionTableDao.getAllCount();

                TiankongQuestion question = new TiankongQuestion(surveyId,count,titleString,titleImageSB.toString(),toggleFlag);
                questionTableDao.addQuestion(question);
            }else {//修改题目
                TiankongQuestion question = new TiankongQuestion(surveyId,quesId,titleString,titleImageSB.toString(),toggleFlag);
                questionTableDao.updateQuestion(question,question.getQuestionId());
            }

            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Constants.CHOOSE_PHOTO:
                if (resultCode == RESULT_OK){
                    ChoosePicHelper choosePicHelper = new ChoosePicHelper(this);
                    String imagePath = choosePicHelper.getPic(data);

                    if (imagePath == null) return;
                    titleImages.set(titleImages.size()-1,imagePath);
                    titleImages.add(Constants.KONG);
                    titleGridViewAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK){//监听返回键

            Log.d("haha",TAG+"---"+"back key");

            Intent intent = new Intent(TiankongQuesActivity.this,StartSurveyActivity.class);
            intent.putExtra("survey_id",surveyId);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
