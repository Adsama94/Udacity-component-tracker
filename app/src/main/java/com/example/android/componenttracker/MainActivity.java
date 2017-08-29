package com.example.android.componenttracker;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.android.componenttracker.data.ComponentContract.ComponentEntry;

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final int COMPONENT_LOADER = 0;
    ComponentCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });
        ImageView emptyView = (ImageView) findViewById(R.id.empty_view_image);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        emptyView.startAnimation(animation);
        ListView compList = (ListView) findViewById(R.id.componentList);
        View emptyText = findViewById(R.id.empty_view);
        compList.setEmptyView(emptyText);
        mCursorAdapter = new ComponentCursorAdapter(this, null);
        compList.setAdapter(mCursorAdapter);
        compList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                Uri currentComponentUri = ContentUris.withAppendedId(ComponentEntry.CONTENT_URI, id);
                intent.setData(currentComponentUri);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(COMPONENT_LOADER, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_dummy_data:
                insertComponent();
                return true;
            case R.id.action_delete_all_entries:
                deleteComponents();
                return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void insertComponent() {
        ContentValues values = new ContentValues();
        values.put(ComponentEntry.COLUMN_NAME, "Seidon 120v Plus");
        values.put(ComponentEntry.COLUMN_MANUFACTURER, "COOLER MASTER");
        values.put(ComponentEntry.COLUMN_PRICE, 4500);
        values.put(ComponentEntry.COLUMN_QUANTITY, 7);
        values.put(ComponentEntry.COLUMN_PHOTO, "");
        getContentResolver().insert(ComponentEntry.CONTENT_URI, values);
    }

    private void deleteComponents() {
        getContentResolver().delete(ComponentEntry.CONTENT_URI, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ComponentEntry._ID,
                ComponentEntry.COLUMN_NAME,
                ComponentEntry.COLUMN_MANUFACTURER,
                ComponentEntry.COLUMN_PRICE,
                ComponentEntry.COLUMN_QUANTITY,
                ComponentEntry.COLUMN_PHOTO
        };

        return new CursorLoader(this, ComponentEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
