package com.example.android.bookstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.bookstore.data.BookContract.BookEntry;

import java.security.Provider;

public class BookProvider extends ContentProvider {

    private BookDbHelper mDbHelper;

    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    //TODO: adding a log tag in the future

    public static final String CONTENT_AUTHORITY="com.example.android.bookstore";

    private static final int BOOKS = 100;

    private static final int BOOK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    @Override
    public boolean onCreate() {

        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch(match) {
            case BOOKS:
                cursor = db.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for" + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {
        //BOOK_PRODUCT_NAME, BOOK_PRICE, BOOK_QUANTITY, BOOK_SUPPLIER_NAME, SUPPLIER_PHONE_NUMBER
        String name = values.getAsString(BookEntry.BOOK_PRODUCT_NAME);
        Integer price = values.getAsInteger(BookEntry.BOOK_PRICE);
        Integer quantity = values.getAsInteger(BookEntry.BOOK_QUANTITY);


        if(name == null) {
            throw new IllegalArgumentException("Book requires a name");
        }
        if(price != null && price < 0) {
            throw new IllegalArgumentException("Book requires a price");
        }
        if(quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Book requires a quantity");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(BookEntry.TABLE_NAME, null, values);
        if(id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for" + uri);
        }
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if(values.containsKey(BookEntry.BOOK_PRODUCT_NAME)) {
            String name = values.getAsString(BookEntry.BOOK_PRODUCT_NAME);
            if(name == null) {
                throw new IllegalArgumentException("Book requires a name");
            }
        }
        if(values.containsKey(BookEntry.BOOK_PRICE)) {
            Integer price = values.getAsInteger(BookEntry.BOOK_PRICE);
            if(price != null && price < 0) {
                throw new IllegalArgumentException("Book requires a price");
            }
        }
        if(values.containsKey(BookEntry.BOOK_QUANTITY)) {
            Integer quantity = values.getAsInteger(BookEntry.BOOK_QUANTITY);
            if(quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Book requires a quantity");
            }
        }

        if(values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        getContext().getContentResolver().notifyChange(uri, null);

        int rowsUpdated = db.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch(match) {
            case BOOKS:
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for" + uri);
        }

        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
