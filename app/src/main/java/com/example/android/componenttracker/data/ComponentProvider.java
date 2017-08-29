package com.example.android.componenttracker.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


public class ComponentProvider extends ContentProvider {

    public static final String LOG_TAG = ContentProvider.class.getSimpleName();
    private static final int COMPONENTS = 100;
    private static final int COMPONENT_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ComponentContract.CONTENT_AUTHORITY, ComponentContract.PATH_COMPONENTS, COMPONENTS);
        sUriMatcher.addURI(ComponentContract.CONTENT_AUTHORITY, ComponentContract.PATH_COMPONENTS + "/#", COMPONENT_ID);
    }

    private ComponentDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ComponentDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case COMPONENTS:
                cursor = database.query(ComponentContract.ComponentEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case COMPONENT_ID:
                selection = ComponentContract.ComponentEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ComponentContract.ComponentEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMPONENTS:
                return ComponentContract.ComponentEntry.CONTENT_LIST_TYPE;
            case COMPONENT_ID:
                return ComponentContract.ComponentEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMPONENTS:
                return insertComponent(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMPONENTS:
                rowsDeleted = database.delete(ComponentContract.ComponentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case COMPONENT_ID:
                selection = ComponentContract.ComponentEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ComponentContract.ComponentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMPONENTS:
                return updateComponent(uri, values, selection, selectionArgs);
            case COMPONENT_ID:
                selection = ComponentContract.ComponentEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateComponent(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private Uri insertComponent(Uri uri, ContentValues values) {

        String name = values.getAsString(ComponentContract.ComponentEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Component requires a name");
        }

        String manufacturer = values.getAsString(ComponentContract.ComponentEntry.COLUMN_MANUFACTURER);
        if (manufacturer == null) {
            throw new IllegalArgumentException("Component requires a manufacturer");
        }

        Integer price = values.getAsInteger(ComponentContract.ComponentEntry.COLUMN_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Component requires a price");
        }

        Integer quantity = values.getAsInteger(ComponentContract.ComponentEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Default quantity set to 0");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ComponentContract.ComponentEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private int updateComponent(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ComponentContract.ComponentEntry.COLUMN_NAME)) {
            String name = values.getAsString(ComponentContract.ComponentEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Component requires a name");
            }
        }

        if (values.containsKey(ComponentContract.ComponentEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(ComponentContract.ComponentEntry.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Component requires valid price");
            }
        }

        if (values.containsKey(ComponentContract.ComponentEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(ComponentContract.ComponentEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Component requires valid quantity");
            }
        }

        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(ComponentContract.ComponentEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
