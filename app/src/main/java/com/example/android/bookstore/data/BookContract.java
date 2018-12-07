package com.example.android.bookstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class BookContract {

    //content authority added
    public static final String CONTENT_AUTHORITY = "com.example.android.bookstore";

    //base content uri added
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //path added
    public static final String PATH_BOOKS = "bookstore";


    private BookContract() {}

    public static final class BookEntry implements BaseColumns {

        public final static String TABLE_NAME = "bookstore";

        //content uri added
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        //content list type added
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        //content item added
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public final static String _ID = BaseColumns._ID;
        public final static String BOOK_PRODUCT_NAME = "name";
        public final static String BOOK_PRICE = "price";
        public final static String BOOK_QUANTITY = "quantity";
        public final static String BOOK_SUPPLIER_NAME = "supplier";
        public final static String SUPPLIER_PHONE_NUMBER = "phone";

    }
}
