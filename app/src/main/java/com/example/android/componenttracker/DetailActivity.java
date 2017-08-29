package com.example.android.componenttracker;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.componenttracker.data.ComponentContract;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_COMPONENT_LOADER = 0;
    private static int REQUEST_TAKE_PHOTO = 1;
    private Uri mCurrentItemUri;
    private EditText mNameEditText;
    private EditText mManufacEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private Button mSaleButton;
    private Button mShipmentButton;
    private Button mOrderButton;
    private Button mPhotoButton;
    private ImageView imageView;
    private int mCurrentQuantity;
    private String mCurrentPhotoPath;
    private boolean mComponentHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mComponentHasChanged = true;
            return false;
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mSaleButton = (Button) findViewById(R.id.sale_button);
        mShipmentButton = (Button) findViewById(R.id.add_shipment_button);
        mOrderButton = (Button) findViewById(R.id.order_button);
        mPhotoButton = (Button) findViewById(R.id.photo_button);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_component));
            mSaleButton.setVisibility(View.GONE);
            mOrderButton.setVisibility(View.GONE);
            mShipmentButton.setVisibility(View.GONE);

        } else {
            setTitle(getString(R.string.editor_activity_title_edit_component));
            getLoaderManager().initLoader(EXISTING_COMPONENT_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mManufacEditText = (EditText) findViewById(R.id.edit_manufacturer);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        imageView = (ImageView) findViewById(R.id.photo_image_view);

        mNameEditText.setOnTouchListener(mTouchListener);
        mManufacEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameString = mNameEditText.getText().toString();
                String manufactureString = mManufacEditText.getText().toString();
                String quantityString = mQuantityEditText.getText().toString();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Order Request");
                intent.putExtra(Intent.EXTRA_TEXT, "I need to order more of the following item(s): " + "\n" + nameString + "\n" + manufactureString + "\n" + "I currently have " + quantityString + " and need more.");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        mSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrement(v);
            }
        });

        mShipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment(v);
            }
        });

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    public void increment(View view) {
        if (mCurrentQuantity == 20) {
            Toast.makeText(getApplicationContext(), "Cannot add more than 20 packages", Toast.LENGTH_LONG).show();
            return;
        }
        mCurrentQuantity += 1;
        mQuantityEditText.setText(Integer.toString(mCurrentQuantity));
    }

    public void decrement(View view) {
        if (mCurrentQuantity > 0) {
            mCurrentQuantity -= 1;
            mQuantityEditText.setText(Integer.toString(mCurrentQuantity));
        } else {
            mCurrentQuantity = 0;
        }

    }

    private boolean isValidPositiveInteger(String input) {
        boolean isValid = true;
        try {
            int parsedInt = Integer.parseInt(input);

            if (parsedInt < 0) {
                isValid = false;
            }
        } catch (NumberFormatException e) {
            isValid = false;
        }

        return isValid;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveComponent();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mComponentHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mComponentHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
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

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteComponent();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveComponent() {

        String nameString = mNameEditText.getText().toString().trim();
        String manufacturerInsertString = mManufacEditText.getText().toString().trim();
        String quantityInsertString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();


        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.need_info), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPositiveInteger(quantityInsertString) || !isValidPositiveInteger(priceString)) {
            Toast.makeText(this, getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
            return;
        }


        ContentValues values = new ContentValues();
        values.put(ComponentContract.ComponentEntry.COLUMN_NAME, nameString);
        values.put(ComponentContract.ComponentEntry.COLUMN_MANUFACTURER, manufacturerInsertString);
        values.put(ComponentContract.ComponentEntry.COLUMN_QUANTITY, quantityInsertString);
        values.put(ComponentContract.ComponentEntry.COLUMN_PRICE, priceString);
        values.put(ComponentContract.ComponentEntry.COLUMN_PHOTO, mCurrentPhotoPath);

        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(ComponentContract.ComponentEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.insert_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.insert_item_success), Toast.LENGTH_SHORT).show();
            }
        } else {
            values.put(ComponentContract.ComponentEntry.COLUMN_QUANTITY, quantityInsertString);
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.update_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.update_item_success), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void deleteComponent() {
        if (mCurrentItemUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_item_success), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ComponentContract.ComponentEntry._ID,
                ComponentContract.ComponentEntry.COLUMN_NAME,
                ComponentContract.ComponentEntry.COLUMN_MANUFACTURER,
                ComponentContract.ComponentEntry.COLUMN_PRICE,
                ComponentContract.ComponentEntry.COLUMN_QUANTITY,
                ComponentContract.ComponentEntry.COLUMN_PHOTO
        };
        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ComponentContract.ComponentEntry.COLUMN_NAME);
            int manufacturerColumnIndex = cursor.getColumnIndex(ComponentContract.ComponentEntry.COLUMN_MANUFACTURER);
            int priceColumnIndex = cursor.getColumnIndex(ComponentContract.ComponentEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ComponentContract.ComponentEntry.COLUMN_QUANTITY);
            int photoColumnIndex = cursor.getColumnIndex(ComponentContract.ComponentEntry.COLUMN_PHOTO);

            String name = cursor.getString(nameColumnIndex);
            String manufacturer = cursor.getString(manufacturerColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String image = cursor.getString(photoColumnIndex);

            mNameEditText.setText(name);
            mManufacEditText.setText(manufacturer);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mCurrentQuantity = quantity;
            mCurrentPhotoPath = image;
            loadImage();
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mNameEditText.setText("");
        mManufacEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mCurrentQuantity = 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            loadImage();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("DetailActivity", "Error occurred while creating image file");
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }

        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName,  /* prefix */".jpg",         /* suffix */storageDir      /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void loadImage() {
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Log.e("targetW", Integer.toString(targetW));
        Log.e("targetH", Integer.toString(targetH));

        bmOptions.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }
}