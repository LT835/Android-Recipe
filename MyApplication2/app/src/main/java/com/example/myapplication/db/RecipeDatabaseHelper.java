package com.example.myapplication.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RecipeDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "recipe.db";
    private static final int DATABASE_VERSION = 2;

    // 菜谱表
    public static final String TABLE_RECIPES = "recipes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_INGREDIENTS = "ingredients";
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_FAVORITE = "favorite";

    private static final String DATABASE_CREATE = "create table " + TABLE_RECIPES + "(" + COLUMN_ID
            + " integer primary key, " + COLUMN_NAME + " text not null, " + COLUMN_DESCRIPTION
            + " text, " + COLUMN_INGREDIENTS + " text, " + COLUMN_STEPS + " text, "
            + COLUMN_IMAGE_URL + " text, " + COLUMN_CATEGORY + " text, " + COLUMN_FAVORITE
            + " integer default 0);";

    public RecipeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(RecipeDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to " + newVersion);
        // 在升级时保留数据
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(RecipeDatabaseHelper.class.getName(),
                "Downgrading database from version " + oldVersion + " to " + newVersion);
        // 在降级时保留数据
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }
}