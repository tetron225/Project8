package com.example.android.bookstore;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.bookstore.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    public void bindView(View view, final Context context, Cursor cursor) {
        TextView mName = (TextView) view.findViewById(R.id.name);
        TextView mPrice = (TextView) view.findViewById(R.id.price);
        TextView mQuantity = (TextView) view.findViewById(R.id.quantity);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.BOOK_PRODUCT_NAME));
        Integer price = cursor.getInt(cursor.getColumnIndexOrThrow(BookEntry.BOOK_PRICE));
        Integer quantity = cursor.getInt(cursor.getColumnIndexOrThrow(BookEntry.BOOK_QUANTITY));

        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(BookEntry._ID));
        final int q = quantity;

        mName.setText(name);
        //Need this checked to see if int can work or not
        mPrice.setText(String.valueOf(price));
        mQuantity.setText(String.valueOf(quantity));

        view.findViewById(R.id.sale).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               BookCatalogActivity bca = (BookCatalogActivity) context;
               bca.salePrice(id, q);
           }
        });

    }
}
