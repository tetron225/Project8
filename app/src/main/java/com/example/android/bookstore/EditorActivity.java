package com.example.android.bookstore;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.app.LoaderManager;
import android.content.Loader;
import android.content.CursorLoader;

import com.example.android.bookstore.data.BookDbHelper;
import com.example.android.bookstore.data.BookContract.BookEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri mCurrentBookUri;

    private EditText mNameEditText;

    private EditText mPriceText;

    //Changed Quantity from EditText to TextView since it will be reading
    //the numbers only
    private TextView mQuantityText;

    private EditText mSupplierNameText;

    private EditText mSupplierPhoneText;

    private int mCurrentQuantity;

    //TODO: may need this?
    private boolean mBookHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        if(mCurrentBookUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_book));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_book));
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_book_name);
        mPriceText = (EditText) findViewById(R.id.edit_price);
        mQuantityText = (TextView) findViewById(R.id.edit_quantity);
        mSupplierNameText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhoneText = (EditText) findViewById(R.id.edit_phone);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceText.setOnTouchListener(mTouchListener);
        mQuantityText.setOnTouchListener(mTouchListener);
        mSupplierNameText.setOnTouchListener(mTouchListener);
        mSupplierPhoneText.setOnTouchListener(mTouchListener);


        final Button button = findViewById(R.id.contact);
        button.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mSupplierPhoneText.getText().toString()));
               startActivity(intent);
           }

        });

        final Button increaseButton = findViewById(R.id.plus);
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
            }
        });

        final Button decreaseButton = findViewById(R.id.minus);
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrement();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_save:
                saveBook();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if(!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }



    private void saveBook() {

        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceText.getText().toString().trim();
        String quantityString = mQuantityText.getText().toString().trim();
        String supplierString = mSupplierNameText.getText().toString().trim();
        String phoneString = mSupplierPhoneText.getText().toString().trim();

        Log.v("Testing", quantityString);
        Log.v("Testing 2", supplierString);
        Log.v("Testing 3", phoneString);
        Log.v("Testing 4", priceString);

        if(mCurrentBookUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString) && TextUtils.isEmpty(phoneString)) {
            return;
        }

       if(TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString)
                || TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplierString) || TextUtils.isEmpty(phoneString)) {
            Toast.makeText(this, getString(R.string.required_field), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(BookEntry.BOOK_PRODUCT_NAME, nameString);

        int priceInt = 0;
        if(!TextUtils.isEmpty(priceString)) {
            priceInt = Integer.parseInt(mPriceText.getText().toString().trim());
        }
        values.put(BookEntry.BOOK_PRICE, priceInt);

        /*int quantityInt = mCurrentQuantity;
        if(!TextUtils.isEmpty(quantityString)) {
            quantityInt = Integer.parseInt(mQuantityText.getText().toString().trim());
        }
        values.put(BookEntry.BOOK_QUANTITY, quantityInt);
        */
        values.put(BookEntry.BOOK_QUANTITY, mCurrentQuantity);
        values.put(BookEntry.BOOK_SUPPLIER_NAME, supplierString);
        values.put(BookEntry.SUPPLIER_PHONE_NUMBER, phoneString);

        if(mCurrentBookUri == null) {
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            if(newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_book_successful), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            if(rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        if(mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            if(rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
                BookEntry._ID,
                BookEntry.BOOK_PRODUCT_NAME,
                BookEntry.BOOK_PRICE,
                BookEntry.BOOK_QUANTITY,
                BookEntry.BOOK_SUPPLIER_NAME,
                BookEntry.SUPPLIER_PHONE_NUMBER
        };

        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if(cursor == null || cursor.getCount() < 1) {
            return;
        }

        if(cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.BOOK_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.BOOK_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.BOOK_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(BookEntry.SUPPLIER_PHONE_NUMBER);

            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);

            mNameEditText.setText(name);
            mPriceText.setText(Integer.toString(price));
            mQuantityText.setText(Integer.toString(quantity));
            mSupplierNameText.setText(supplier);
            mSupplierPhoneText.setText(phone);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceText.setText("");
        mQuantityText.setText("");
        mSupplierNameText.setText("");
        mSupplierPhoneText.setText("");
    }

    private void increment() {

        ContentValues values = new ContentValues();
        //Uri updateUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, this);
        mCurrentQuantity = Integer.parseInt(mQuantityText.getText().toString().trim());

        mCurrentQuantity++;

        /*int rowsUpdated = getContentResolver().update(updateUri,
                values,
                null,
                null
        );
        if(rowsUpdated == 1) {
            Toast.makeText(this, "Successful Update", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
        }*/

        mQuantityText.setText(Integer.toString(mCurrentQuantity));

    }

    private void decrement() {

        ContentValues values = new ContentValues();
        //Uri updateUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, );
        mCurrentQuantity = Integer.parseInt(mQuantityText.getText().toString().trim());
        mCurrentQuantity--;
        if(mCurrentQuantity < 0) {
            Toast.makeText(this, "Invalid input, try again", Toast.LENGTH_SHORT).show();
            mCurrentQuantity = 0;
            //values.put(BookEntry.BOOK_QUANTITY, mCurrentQuantity);
            mQuantityText.setText(Integer.toString(mCurrentQuantity));
        } else {
            //values.put(BookEntry.BOOK_QUANTITY, mCurrentQuantity);
            mQuantityText.setText(Integer.toString(mCurrentQuantity));
        }
    }
}
