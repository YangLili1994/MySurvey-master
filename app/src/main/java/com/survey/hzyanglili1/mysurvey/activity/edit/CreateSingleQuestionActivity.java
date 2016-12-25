package com.survey.hzyanglili1.mysurvey.activity.edit;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.survey.hzyanglili1.mysurvey.Application.Constants;
import com.survey.hzyanglili1.mysurvey.CustomView.MyGridView;
import com.survey.hzyanglili1.mysurvey.R;
import com.survey.hzyanglili1.mysurvey.activity.BaseActivity;
import com.survey.hzyanglili1.mysurvey.adapter.MyQuesOptionAdapter;
import com.survey.hzyanglili1.mysurvey.adapter.MyTitleGridViewAdapter;
import com.survey.hzyanglili1.mysurvey.db.DBHelper;
import com.survey.hzyanglili1.mysurvey.db.QuestionTableDao;
import com.survey.hzyanglili1.mysurvey.db.SurveyTableDao;
import com.survey.hzyanglili1.mysurvey.entity.XuanZeQuestion;
import com.survey.hzyanglili1.mysurvey.utils.CountHelper;
import com.survey.hzyanglili1.mysurvey.utils.DensityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by hzyanglili1 on 2016/10/31.
 */

public class CreateSingleQuestionActivity extends BaseActivity {

    private Boolean isTitleImageAdded = false;
    private View curImageView = null;

    /**-------------------View布局 -------------------**/
    private EditText titleEt = null;
    private LinearLayout optionLayout = null;
    private TextView customTitle = null;
    private MyGridView titlePicGridView = null; //题目标题图片gridview
    private LinearLayout addTitleImageBt = null; //添加标题图片按钮
    private LinearLayout addoptionBt = null;    //添加选项按钮
    private ImageView mustOptionToggleButton;
    private Button finishBt = null;

    /**-------------------题目信息 -------------------**/
    private String titleString = null;
    private List<String> titleImages = null; //存储title Bmp图像
    private Boolean isMustOption = false;
    private Boolean isMultiOption = false;
    private int surveyId = -1;
    private int quesId = -1;
    private int totalQuesCount = 0;
    private String surveyName = null;
    private List<String>  optionTexts = null;
    private List<String> optionImages = null;

    /**-------------------标志位 -------------------**/
    private MyQuesOptionAdapter myAdapter = null;
    private Boolean mustOptionToggleFlag = false;
    private int clickCount = 0;
    private Boolean isNew = false;

    /**-------------------适配器 -------------------**/
    private MyTitleGridViewAdapter titleGridViewAdapter = null;  //gridview适配器

    //其他
    private Bitmap bmp;//导入临时图片
    private SurveyTableDao surveyTableDao = null;
    private QuestionTableDao questionTableDao = null;


    private Handler clickEventHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1://单击事件，选择图片
                    clickCount = 0;
                    choosePic();
                    break;
                case 2:  //双击事件,放大
                    clickCount = 0;

                    if ((((View)curImageView.getParent()).getTag()) == null){//原图为空时 和单击事件同
                        clickCount = 0;
                        choosePic();
                    }else {
                        Intent intent1 = new Intent(CreateSingleQuestionActivity.this,ZoomImageActivity.class);
                        intent1.putExtra("imagePath",(String) ((View)curImageView.getParent()).getTag());
                        startActivity(intent1);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createsinglequestion);

        surveyTableDao = new SurveyTableDao(new DBHelper(this,1));
        questionTableDao = new QuestionTableDao(new DBHelper(this,1));

        getQuesInfo();
        initViewAndEvent();
    }

    //获取题目信息
    private void getQuesInfo(){

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        surveyId = bundle.getInt("survey_id");

        Cursor surveyCursor = surveyTableDao.selectSurveyById(surveyId);
        surveyCursor.moveToNext();
        surveyName = surveyCursor.getString(surveyCursor.getColumnIndex("survey_name"));

        Cursor quesCursor = questionTableDao.selectQuestionBySurveyId(surveyId);
        quesCursor.moveToNext();
        totalQuesCount = quesCursor.getCount();

        isNew = bundle.getBoolean("isNew");


        if (isNew){//新建问题
            titleImages = new ArrayList<>();
            optionTexts = new ArrayList<>();
            optionImages = new ArrayList<>();


            quesId = questionTableDao.getAllCount();

        }else {//修改问题
            quesId = bundle.getInt("ques_id");
            //查询数据库获得问题
            Cursor cursor = questionTableDao.selectQuestionByQuestionId(quesId);
            cursor.moveToNext();
            XuanZeQuestion question = (XuanZeQuestion)  questionTableDao.cursor2Ques(cursor);
            //title
            titleString = question.getTitle();

            //title image
            String titleImageStrings = question.getImagePath();
            //注意Arrays.asList生成的list是固定长度！！！!
            titleImages = new ArrayList<>(Arrays.asList(titleImageStrings.split("\\$")));

            Log.d("haha","get info -- titleimage = "+titleImages.get(0));

            //option text
            String optionTextStrings = question.getTextOption();
            optionTexts = new ArrayList<>(Arrays.asList(optionTextStrings.split("\\$")));

            //option image
            String optionImageStrings = question.getImageOption();
            optionImages = new ArrayList<>(Arrays.asList(optionImageStrings.split("\\$")));

            isMultiOption = question.getIsMulti();
            isMustOption = question.getIsMust();
        }

        isMultiOption = intent.getExtras().getBoolean("isMultiOption");
    }

    private void initViewAndEvent(){
        //标题栏
        customTitle = (TextView)findViewById(R.id.custom_title_text);

        //ques title
        titleEt = (EditText)findViewById(R.id.activity_createsinglequestion_title);

        optionLayout = (LinearLayout) findViewById(R.id.activity_createsinglequestion_option_layout);
        addoptionBt = (LinearLayout) findViewById(R.id.activity_createsinglequestion_addoption);
        addTitleImageBt = (LinearLayout) findViewById(R.id.activity_createsinglequestion_addtitleimage);

        titlePicGridView = (MyGridView)findViewById(R.id.activity_createsinglequestion_titlepic_gridview) ;

        mustOptionToggleButton = (ImageView)findViewById(R.id.activity_createsinglequestion_mustoptiontogglebt) ;

        if (isNew){
            //初始化选项布局   默认显示两项
            addNewOptionItem(Constants.KONG,Constants.KONG);
            addNewOptionItem(Constants.KONG,Constants.KONG);

            //默认为必选项
            mustOptionToggleButton.setSelected(true);
            mustOptionToggleFlag = true;

            //init customtitle
            customTitle.setText(surveyName +"   "+"Q."+(totalQuesCount+1));
        }else {
            //init customtitle
            customTitle.setText(surveyName +"   "+"Q."+(quesId+1));
            //init ques title
            titleEt.setText(titleString.trim());

            //init option
            for (int i = 0;i<optionTexts.size();i++){
                if (optionImages.get(i).equals(Constants.KONG) && optionTexts.get(i).equals(Constants.KONG)) continue;
                addNewOptionItem(optionImages.get(i),optionTexts.get(i));
            }

            mustOptionToggleButton.setSelected(isMustOption);
            mustOptionToggleFlag = isMustOption;

        }

        mustOptionToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mustOptionToggleFlag){
                    mustOptionToggleButton.setSelected(false);
                    mustOptionToggleFlag = false;
                }else {
                    mustOptionToggleButton.setSelected(true);
                    mustOptionToggleFlag = true;
                }
            }
        });

        finishBt = (Button)findViewById(R.id.activity_createsinglequestion_finish) ;


        addoptionBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("lala","add option is clicked.");
                addNewOptionItem(Constants.KONG,Constants.KONG);
            }
        });

        addTitleImageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isTitleImageAdded = true;
                choosePic();
            }
        });


        //完成题目
        finishBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addQuestion()){
                    Intent intent = new Intent(CreateSingleQuestionActivity.this,StartSurveyActivity.class);
                    intent.putExtra("survey_id",surveyId);
                    startActivity(intent);
                    finish();
                };

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

                    isTitleImageAdded = true;
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

                    isTitleImageAdded = true;
                    choosePic();

                }else {//点击放大图片

                    Log.d("haha","点击放大图片");

                    Intent intent1 = new Intent(CreateSingleQuestionActivity.this,ZoomImageActivity.class);
                    intent1.putExtra("imagePath",titleImages.get(i));
                    startActivity(intent1);

                }
            }
        });

    }


    /**
     * 添加新选项
     * @param imagePath  为空时传入null
     * @param optionText 为空时传入null
     */
    void addNewOptionItem(String imagePath,String optionText){
        View view = LayoutInflater.from(CreateSingleQuestionActivity.this).inflate(R.layout.item_questionoption,null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(20,20,20,20);
        view.setLayoutParams(layoutParams);


        view.setTag(imagePath);

        //delete view
        ImageView delView = (ImageView) view.findViewById(R.id.item_questionoption_delete);
        delView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("lala","delete option view clicked.");
                curImageView = view;
                showDelOptionDialog();

            }
        });

        if (optionText !=null) {
            if (!optionText.equals(Constants.KONG)) {
                EditText editText = (EditText) view.findViewById(R.id.item_questionoption_title);
                editText.setText(optionText);
            }
        }

        CircleImageView addImage = (CircleImageView) view.findViewById(R.id.item_questionoption_image);

        if (imagePath != null) {
            if (!imagePath.equals(Constants.KONG)) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                addImage.setImageBitmap(bitmap);
            }
        }

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickCount++;

                curImageView = view;

                if (clickCount<2){//一次单击事件,但也有可能是双击事件的一部分  所以delay执行
                    clickEventHandler.sendEmptyMessageDelayed(1,200);
                }else {//双击事件，确定是双击事件
                    clickEventHandler.removeMessages(1);
                    clickEventHandler.sendEmptyMessage(2);
                }

            }
        });

        addImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {//长按删除
                curImageView = view;

                if (((View)view.getParent()).getTag() == null){//原图片为空  则为选择图片
                    choosePic();
                }else {
                    showDelImageDialog(-1);
                }
                return true;
            }
        });

        optionLayout.addView(view);
    }

    //选择相册
    private void choosePic(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, Constants.CHOOSE_PHOTO);
    }

    //删除图片
    private void showDelImageDialog(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("确认删除此图吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (position == -1){
                    //删除选项图片
                    CircleImageView imageView = (CircleImageView)curImageView;
                    imageView.setImageDrawable(ContextCompat.getDrawable(CreateSingleQuestionActivity.this,R.drawable.tupian));
                    imageView.setBorderWidth(0);
                    ((View)imageView.getParent()).setTag(null);
                }else{
                    //删除标题图片
                    titleImages.remove(position);
                    titleGridViewAdapter.notifyDataSetChanged();
                }

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    //删除选项
    private void showDelOptionDialog() {

        final Boolean flag = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("确认删除此选项吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //删除选项
                optionLayout.removeView((View) curImageView.getParent());
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    //添加（修改）问题
    Boolean addQuestion(){
        //题目标题
        titleString = titleEt.getText().toString().trim();
        //记录选项的个数，判断输入有效性（>=2）
        int optionCount = 0;

        StringBuilder optionText = new StringBuilder();
        StringBuilder optionImagePath = new StringBuilder();
        StringBuilder titleImagePath = new StringBuilder();

        //记录选项列表
        for (int i = 0;i<optionLayout.getChildCount();i++){
            String title = ((EditText)optionLayout.getChildAt(i).findViewById(R.id.item_questionoption_title)).getText().toString();
            String imagePath = (String) optionLayout.getChildAt(i).getTag().toString();

            if (title.trim().isEmpty())  title = Constants.KONG;

            if (title.equals(Constants.KONG) && imagePath.equals(Constants.KONG)){//选项图片和文字都为空
                Log.d("haha","option title and image both are null");
                continue;
            }

            Log.d("haha","option"+i+"--    text = "+title.toString()+"--   optionImage = "+imagePath);
            //记录选项文字和图片
            if(i == optionLayout.getChildCount()-1) {
                optionText.append(title);
                optionImagePath.append(imagePath);
            }else {
                optionText.append(title).append('$');
                optionImagePath.append(imagePath).append('$');
            }
            optionCount++;
        }

        if (titleString.isEmpty()){
            //题目标题为空
            Toast.makeText(CreateSingleQuestionActivity.this,"请输入题目标题！",Toast.LENGTH_LONG).show();
            return false;
        }else if (optionCount<2){
            //题目选项少于两项
            Toast.makeText(CreateSingleQuestionActivity.this,"题目选项不能少于两项！",Toast.LENGTH_LONG).show();
            return false;
        }else {
            //title image路径
            for (int i = 0;i<titleImages.size();i++){
                if (i == titleImages.size() -1){
                    titleImagePath.append(titleImages.get(i));
                }else {
                    titleImagePath.append(titleImages.get(i)).append("$");
                }
            }

            Log.d("haha","title image "+titleImagePath.toString());
            Log.d("haha","option text "+optionText.toString());
            Log.d("haha","option image "+optionImagePath.toString());

//            int endIndex = optionImagePath.length()-1;
//            if (optionImagePath.charAt(endIndex) == '$' && optionText.charAt(endIndex) == '$'){
//                optionImagePath.deleteCharAt(endIndex);
//                optionText.deleteCharAt(endIndex);
//            }

            XuanZeQuestion question = new XuanZeQuestion(surveyId, quesId, titleString, titleImagePath.toString(),
                    mustOptionToggleFlag, isMultiOption, optionText.toString(), optionImagePath.toString());

            if (isNew) {
                //创建题目，把题目添加到db
                questionTableDao.addQuestion(question);
                Log.d(TAG, "已添加题目");
            }else {//修改题目
                questionTableDao.updateQuestion(question,quesId);

            }
        }

        return true;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.CHOOSE_PHOTO://选择图片
                if (resultCode == RESULT_OK){
                    ChoosePicHelper choosePicHelper = new ChoosePicHelper(this);
                    String imagePath = choosePicHelper.getPic(data);

                    if (isTitleImageAdded){//此图片为题目图片
                        isTitleImageAdded = false;

                        if (imagePath == null) return;

                        titleImages.set(titleImages.size()-1,imagePath);
                        titleImages.add(Constants.KONG);
                        titleGridViewAdapter.notifyDataSetChanged();

                    }else {

                        if (curImageView == null) return;

                        if (imagePath != null) {//已经获得图片
                            bmp = BitmapFactory.decodeFile(imagePath);
                            CircleImageView imageView = (CircleImageView) curImageView;

                            Log.d("lala", "获得的图片path = " + imagePath);
                            //把imagepathsetTag  便于完成题目时获得imagepath
                            ((View) curImageView.getParent()).setTag(imagePath);
                            imageView.setImageBitmap(bmp);
                            imageView.setBorderWidth(DensityUtil.dip2px(this,1));
                            imageView.setBorderColor(ContextCompat.getColor(this,R.color.lightskyblue));
                            //刷新后释放防止手机休眠后自动添加
                            bmp = null;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK){//监听返回键

            Log.d("haha",TAG+"---"+"back key");

            Intent intent = new Intent(CreateSingleQuestionActivity.this,StartSurveyActivity.class);
            intent.putExtra("survey_id",surveyId);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
