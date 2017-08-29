package com.example.android.componenttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.componenttracker.data.ComponentContract;


public class ComponentCursorAdapter extends CursorAdapter {


    public ComponentCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView componentName = (TextView) view.findViewById(R.id.productName);
        TextView componentQuantity = (TextView) view.findViewById(R.id.quantity);
        TextView componentPrice = (TextView) view.findViewById(R.id.price);

        int idColumnIndex = cursor.getColumnIndex((ComponentContract.ComponentEntry._ID));
        int actualName = cursor.getColumnIndexOrThrow(ComponentContract.ComponentEntry.COLUMN_NAME);
        int actualPrice = cursor.getColumnIndexOrThrow(ComponentContract.ComponentEntry.COLUMN_PRICE);
        int actualQuantity = cursor.getColumnIndexOrThrow(ComponentContract.ComponentEntry.COLUMN_QUANTITY);

        String productName = cursor.getString(actualName);
        String productPrice = cursor.getString(actualPrice);
        String productQuantity = cursor.getString(actualQuantity);

        final long currentItemId = cursor.getLong(idColumnIndex);
        final Context currentContext = context;
        final int currentQuantity = cursor.getInt(actualQuantity);

        componentName.setText(productName);
        componentPrice.setText(productPrice);
        componentQuantity.setText(productQuantity);


        Button saleButton = (Button) view.findViewById(R.id.saleButton);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateQuantity(currentItemId, currentContext, currentQuantity);
            }
        });
    }

    public void updateQuantity(long id, Context context, int quantity) {
        if (quantity > 0) {
            quantity -= 1;
            ContentValues values = new ContentValues();
            values.put(ComponentContract.ComponentEntry.COLUMN_QUANTITY, quantity);
            Uri uri = Uri.withAppendedPath(ComponentContract.ComponentEntry.CONTENT_URI, Long.toString(id));

            context.getContentResolver().update(uri, values, null, null);
        }
    }
}
