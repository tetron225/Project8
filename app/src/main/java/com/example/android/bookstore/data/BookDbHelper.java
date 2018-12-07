package com.example.android.bookstore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.bookstore.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = BookDbHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "books.db";

    public BookDbHelper (Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + "("
                + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookEntry.BOOK_PRODUCT_NAME + " TEXT NOT NULL, "
                + BookEntry.BOOK_PRICE + " INTEGER NOT NULL, "
                + BookEntry.BOOK_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + BookEntry.BOOK_SUPPLIER_NAME + " TEXT, "
                + BookEntry.SUPPLIER_PHONE_NUMBER + " TEXT);";

        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
