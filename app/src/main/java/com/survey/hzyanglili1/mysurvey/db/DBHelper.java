package com.survey.hzyanglili1.mysurvey.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.survey.hzyanglili1.mysurvey.Application.Constants;


/**
 * Created by Administrator on 2016/7/6.
 */
public class DBHelper extends SQLiteOpenHelper {

    String TAG = getClass().getSimpleName();

    final String CREATE_SURVEIES_TABLE_SQL = "create table "+ Constants.SURVEIES_TABLENAME
            +"(_id integer primary key," +
            "survey_id String," +
            "survey_name String," +
            "survey_desc String)";

    final String CREATE_QUESTIONS_TABLE_SQL = "create table "+ Constants.QUESTIONS_TABLENAME
            +"(_id integer primary key," +
            "survey_id String," +
            "question_id interger," +
            "question_type integer," +
            "question_title String," +
            "question_image String," +
            "option_text String," +
            "option_image String," +
            "qustion_ismust integer default 1," +
            "question_ismulti integer default 1)";

    final String CREATE_RESULTS_TABLE_SQL = "create table "+ Constants.RESULTS_TABLENAME
            +"(_id integer primary key," +
            "result_id id," +
            "survey_id interger," +
            "result_time INT4," +
            "result_content text)";

    final String CREATE_OPTIONS_TABLE_SQL = "create table "+ Constants.OPTIONS_TABLENAME
            +"(_id integer primary key," +
            "question_id String," +
            "option_id interger," +
            "option_type integer," +
            "option_content String)";



    public DBHelper(Context context,int version) {
        super(context,Constants.DB_NAME,null,version);
    }

    //为创建数据库的时候执行（数据库已存在则不执行）
    @Override
    public void onCreate(SQLiteDatabase db) {


        //第一次使用数据库时自动建表
        db.execSQL(CREATE_SURVEIES_TABLE_SQL);
        db.execSQL(CREATE_QUESTIONS_TABLE_SQL);
        //db.execSQL(CREATE_OPTIONS_TABLE_SQL);
        db.execSQL(CREATE_RESULTS_TABLE_SQL);

        Log.d(TAG, "数据库创建成功");
    }

    //版本更新
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG,"-----database onupgrade-----");
        db.execSQL("drop table if exists "+Constants.SURVEIES_TABLENAME);
        db.execSQL("drop table if exists "+Constants.QUESTIONS_TABLENAME);
        //db.execSQL("drop table if exists "+Constants.OPTIONS_TABLENAME);
        db.execSQL("drop table if exists "+Constants.RESULTS_TABLENAME);

        onCreate(db);
    }
}
