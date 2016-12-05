package com.ganwal.locationTodo.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.ganwal.locationTodo.R;

public class ListActivity extends AppCompatActivity {

    private static final String TAG = ListActivity.class.getSimpleName();

    private boolean mTwoPane;
    public static final String MAIN_FRAGMENT_TAG = "MFTAG";
    public static final String DETAIL_FRAGMENT_TAG = "DFTAG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_todo);
        FragmentManager fm = getSupportFragmentManager();
        if (findViewById(R.id.todo_detail_container) != null) {
            mTwoPane = true;
            DetailFragment df = (DetailFragment)fm.findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if(df == null) {
                df = new DetailFragment();
                fm.beginTransaction().replace(R.id.todo_detail_container, df, DETAIL_FRAGMENT_TAG)
                        .commit();
            }

        } else {
            mTwoPane = false;
        }

        ListFragment mainFragment = (ListFragment)fm.findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (mainFragment == null) {
            mainFragment = new ListFragment();
            fm.beginTransaction().replace(R.id.fragment_container, mainFragment, MAIN_FRAGMENT_TAG).commit();
        }

    }


}
