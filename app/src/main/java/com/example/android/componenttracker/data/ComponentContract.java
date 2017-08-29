package com.example.android.componenttracker.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ComponentContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.componenttracker";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_COMPONENTS = "components";

    private ComponentContract() {
    }

    public static abstract class ComponentEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_COMPONENTS);
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMPONENTS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMPONENTS;

        public static final String TABLE_NAME = "Components";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_MANUFACTURER = "manufacturer";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_PHOTO = "image";
    }
}
