package com.example.android.bookstore;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.example.android.bookstore.data.BookContract.BookEntry;
import com.example.android.bookstore.data.BookDbHelper;

public class BookCatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOK_LOADER = 0;

    BookCursorAdapter mCursorAdapter;

    private boolean isSaleClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Setting up FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookCatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView bookListView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);
        //displayDatabaseInfo();

        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);

        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
               Intent intent = new Intent(BookCatalogActivity.this, EditorActivity.class);
               Uri currentPetUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
               intent.setData(currentPetUri);
               startActivity(intent);
           }
        });


        getLoaderManager().initLoader(BOOK_LOADER, null, this);

    }

    private void insertBook() {

        //BookDbHelper mDbHelper = new BookDbHelper(this);
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BookEntry.BOOK_PRODUCT_NAME, "Romeo and Juliet");
        values.put(BookEntry.BOOK_PRICE, "9");
        values.put(BookEntry.BOOK_QUANTITY, "7");
        values.put(BookEntry.BOOK_SUPPLIER_NAME, "Barnes and Nobles");
        values.put(BookEntry.SUPPLIER_PHONE_NUMBER, "1235556789");

        Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_random_data:
                insertBook();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
                //does nothing
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {



        String[] projection = {
                BookEntry._ID,
                BookEntry.BOOK_PRODUCT_NAME,
                BookEntry.BOOK_PRICE,
                BookEntry.BOOK_QUANTITY
        };

        return new CursorLoader(this,
                BookEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mCursorAdapter.swapCursor(null);
    }

    public void salePrice(int id, int q) {

        if(q >= 1) {
            q--;
            Uri updateUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
            ContentValues values = new ContentValues();
            values.put(BookEntry.BOOK_QUANTITY, q);
            int rowsUpdated = getContentResolver().update(updateUri,
                    values,
                    null,
                    null
            );
                if(rowsUpdated == 1) {
                    Toast.makeText(this, R.string.sale_successful, Toast.LENGTH_SHORT).show();
                } else {
                Toast.makeText(this, R.string.sale_failure, Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, R.string.unavailable, Toast.LENGTH_LONG).show();
            }
        }
}

