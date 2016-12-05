package com.ganwal.locationTodo.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ganwal.locationTodo.R;
import com.ganwal.locationTodo.db.ContentProviderContract;
import com.ganwal.locationTodo.db.LocationTodo;
import com.ganwal.locationTodo.service.HelperUtility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        RecyclerView.OnItemTouchListener,
        View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = ListFragment.class.getSimpleName();

    private Cursor mCursor = null;
    private TodoListAdapter mAdapter = null;
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private TextView mMsgView;
    private long mUserID;
    GestureDetectorCompat mGestureDetector;
    ActionMode mActionMode;
    FloatingActionButton mNewFab;

    public ListFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        getActivity().setTitle(R.string.title_activity_list);
        mUserID = getActivity().getIntent().getLongExtra(LoginActivity.EXTRA_USER_ID, 0);
        if (mUserID == 0) {
            Log.e(TAG, "onCreate: Can't find the logged in user ID:"+mUserID );
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, parent, false);
        // Get a reference to the RecyclerView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TodoListAdapter(mCursor);
        mRecyclerView.setAdapter(mAdapter);

        mEmptyView = (TextView) rootView.findViewById(R.id.empty_list);
        mMsgView = (TextView) rootView.findViewById(R.id.error_banner);
        if(HelperUtility.isConnectedToNetwork(this.getActivity())) {
            mMsgView.setVisibility(View.GONE);
        } else {
            mMsgView.setVisibility(View.VISIBLE);
        }

        mNewFab = (FloatingActionButton) rootView.findViewById(R.id.newFAB);
        mNewFab.setOnClickListener(this);
        if (savedInstanceState != null) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
        }
        mRecyclerView.addOnItemTouchListener(this);
        mGestureDetector = new GestureDetectorCompat(this.getActivity(), new TodoGestureListener());
        return rootView;
    }


    @Override
    public void onClick(View view) {
        if(view == null) {
            return;
        }
        if (view.getId() == R.id.newFAB) {
            // Create new
            Intent i = new Intent(getActivity(), DetailActivity.class);
            i.putExtra(HelperUtility.EXTRA_USER_ID, mUserID);
            startActivity(i);
        } else if (view.getId() == R.id.location_todo_list_item) {
            // item click
            int idx = mRecyclerView.getChildAdapterPosition(view);
            if (mActionMode != null) {
                myToggleSelection(idx);
                return;
            }
            Long itemID = mAdapter.getItemId(idx);
            // Show LocationTodos details
            Intent i = new Intent(getActivity(), DetailActivity.class);
            i.putExtra(HelperUtility.EXTRA_USER_ID, mUserID);
            i.putExtra(HelperUtility.EXTRA_TODO_ID, itemID);
            startActivity(i);
        }
    }

    private void myToggleSelection(int idx) {
        mAdapter.toggleSelection(idx);
        String title = getString(R.string.selected_count, mAdapter.getSelectedItemCount());
        mActionMode.setTitle(title);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {
    }



    private class TodoGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            Log.d(TAG, "onSingleTapConfirmed: view:"+view);
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (mActionMode != null) {
                return;
            }
            // Start the CAB using the ActionMode.Callback defined above
            mActionMode = ListFragment.
                    this.getActivity().startActionMode( new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    MenuInflater inflater = actionMode.getMenuInflater();
                    inflater.inflate(R.menu.menu_cab_delete, menu);
                    mNewFab.setVisibility(View.GONE);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    int menuItemid = menuItem.getItemId();
                    if(menuItemid == R.id.menu_delete
                            || menuItemid == R.id.menu_done) {
                            List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
                            int currPos;
                            //mark all the rows deleted
                            for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                                currPos = selectedItemPositions.get(i);
                                if(mCursor.moveToPosition(currPos)) {
                                    LocationTodo todo = ContentProviderContract.LocationTodoEntry.cursorToLocationTodo(mCursor);
                                    Log.d(TAG, "Updating/Deleting Location TODO:" + todo);
                                    long todaysDate = new Date().getTime();
                                    if(menuItemid == R.id.menu_delete) {
                                        todo.setDeleted(true);
                                        todo.setLastUpdateDate(todaysDate);
                                    } else if(menuItemid == R.id.menu_done){
                                        todo.setCompleted(!todo.getCompleted());
                                        todo.setLastUpdateDate(todaysDate);
                                    }
                                    int noOfRowsUpdated =
                                            getActivity().getContentResolver().update(
                                                    ContentProviderContract.LocationTodoEntry.getLocationTodoWithUserUri(
                                                            todo.getUserId(),todo.getId()),
                                                    ContentProviderContract.LocationTodoEntry.loadContentValues(todo),
                                                    ContentProviderContract.LocationTodoEntry._ID+ " = ?",
                                                    new String[]{todo.getId()+""});
                                    getLoaderManager().restartLoader(0, null, ListFragment.this);
                                    //notifying widgets about data update
                                    Log.d(TAG, "Refresh widgets..sending the broadcast");
                                    Intent dataRefreshIntent = new Intent(
                                            ContentProviderContract.ACTION_WIDGET_DATA_UPDATED).setPackage(getActivity().getPackageName());
                                    getActivity().sendBroadcast(dataRefreshIntent);
                                }
                            }
                            actionMode.finish();
                            return true;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    mActionMode = null;
                    mAdapter.clearSelections();
                    mNewFab.setVisibility(View.VISIBLE);
                }
            });
            int idx = mRecyclerView.getChildAdapterPosition(view);
            myToggleSelection(idx);
            super.onLongPress(e);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        mAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_todo_list_menu, menu);
    }


    /* ****************** Handle Menu Item Selection ********************** */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(getActivity(), AppSettingsActivity.class);
                startActivityForResult(i, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /* ****************** Handle Preference Changes ********************** */

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int sortOrderValue = Integer.parseInt(prefs.getString("pref_sort_order", HelperUtility.DUE_DATE_SORT_ORDER_VALUE+""));
        getLoaderManager().restartLoader(0, null, ListFragment.this);
    }

    /********************* Cursor Loader Callback methods ********************** */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int sortOrderValue = Integer.parseInt(prefs.getString("pref_sort_order", HelperUtility.DUE_DATE_SORT_ORDER_VALUE+""));
        String sortOrderQueryParam;
        switch (sortOrderValue) {
            case(HelperUtility.PRIORITY_SORT_ORDER_VALUE):
                sortOrderQueryParam = " priority asc";
                break;
            case(HelperUtility.NAME_SORT_ORDER_VALUE):
                sortOrderQueryParam = " name asc";
                break;
            default:
                sortOrderQueryParam = " due_date asc";
        }

        boolean showCompleted = prefs.getBoolean("pref_show_completed",false);
        //if show completed task is selected, then disregard completed flag and show all tasks
        // otherwise only show user non unfinished tasks
        StringBuffer selection = new StringBuffer().
            append(ContentProviderContract.LocationTodoEntry.COL_USER_ID + " = ? and ").
            append(ContentProviderContract.LocationTodoEntry.COL_DELETED + " = ? ");
        if(!showCompleted) {
            selection.append(" and "+ContentProviderContract.LocationTodoEntry.COL_COMPLETED + " = ?");
        }

        String[] selectionArgs = (showCompleted) ? new String[]{mUserID + "", "0"} :
                    new String[]{mUserID + "", "0", "0"};

        CursorLoader cursorLoader = new CursorLoader(this.getActivity(),
                ContentProviderContract.LocationTodoEntry.getLocationTodoUserUri(mUserID),
                ContentProviderContract.LocationTodoEntry.projections,
                 selection.toString(),
                selectionArgs,
                sortOrderQueryParam);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        mCursor = data;
        Log.d(TAG, "onLoadFinished: data:" + data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    /* ****************** RecyclerView Adapter ********************** */

    public class TodoListAdapter
            extends RecyclerView.Adapter<TodoListAdapter.TodoListViewHolder> {
        private Cursor mCursor;
        private SparseBooleanArray mSelectedItems;

        public TodoListAdapter(Cursor cursor) {
            Log.d(TAG, "TodoListAdapter: constructor");
            mCursor = cursor;
            mSelectedItems = new SparseBooleanArray();
        }

        @Override
        public long getItemId(int position) {
            Log.d(TAG, "TodoListAdapter.getItemId: ");
            mCursor.moveToPosition(position);
            return mCursor.getLong(0);
        }

        @Override
        public TodoListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.list_item, parent, false);
            //view.setFocusable(true);
            final TodoListViewHolder vh = new TodoListViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(TodoListViewHolder viewHolder, int position) {
            mCursor.moveToPosition(position);
            LocationTodo locationTodo = ContentProviderContract.LocationTodoEntry.
                    cursorToLocationTodo(mCursor);
            viewHolder.titleTextView.setText(locationTodo.getName());
            viewHolder.summaryTextView.setText(HelperUtility.getShortenedSummary(locationTodo.getSummary()));
            viewHolder.dateTextView.setText(HelperUtility.convertLongToString(
                    locationTodo.getDueDate()));
            viewHolder.doneCheckBox.setChecked(locationTodo.getCompleted());

            viewHolder.alarmImageView.setVisibility(
                    locationTodo.getLocationAlert() == true? View.VISIBLE: View.GONE);

            int bkColor = ContextCompat.getColor(getContext(), R.color.priority_3_background);
            switch (locationTodo.getPriority()) {
                case 1:
                    bkColor = ContextCompat.getColor(getContext(), R.color.priority_1_background);
                    break;
                case 2:
                    bkColor = ContextCompat.getColor(getContext(), R.color.priority_2_background);
                    break;
                case 4:
                    bkColor = ContextCompat.getColor(getContext(), R.color.priority_4_background);
                    break;
            }

            viewHolder.itemView.setBackgroundColor(bkColor);

            viewHolder.itemView.setActivated(mSelectedItems.get(position, false));
            if(viewHolder.itemView.isActivated()) {
                viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary_dark));
            }

        }

        @Override
        public int getItemCount() {
            return (mCursor != null && mCursor.getCount() > 0) ? mCursor.getCount() : 0;
        }

        public void onSaveInstanceState(Bundle outState) {
        }

        public void onRestoreInstanceState(Bundle savedInstanceState) {
        }

        public void swapCursor(Cursor newCursor) {
            mCursor = newCursor;
            notifyDataSetChanged();
            mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            mRecyclerView.setVisibility(getItemCount() == 0 ? View.GONE : View.VISIBLE);
        }

        /************
         * For Long Press Multi Select
         ******************/
        public void toggleSelection(int pos) {
            if (mSelectedItems.get(pos, false)) {
                mSelectedItems.delete(pos);
            } else {
                mSelectedItems.put(pos, true);
            }
            notifyItemChanged(pos);
        }

        public void clearSelections() {
            mSelectedItems.clear();
            notifyDataSetChanged();
        }

        public int getSelectedItemCount() {
            return mSelectedItems.size();
        }

        public List<Integer> getSelectedItems() {
            List<Integer> items = new ArrayList<Integer>(mSelectedItems.size());
            for (int i = 0; i < mSelectedItems.size(); i++) {
                items.add(mSelectedItems.keyAt(i));
            }
            return items;
        }

        /********************* RecyclerView ViewHolder ********************** */
        public class TodoListViewHolder extends RecyclerView.ViewHolder {

            TableLayout tableLayout;
            TextView titleTextView;
            TextView summaryTextView;
            TextView dateTextView;
            CheckBox doneCheckBox;
            ImageView alarmImageView;

            public TodoListViewHolder(View view) {
                super(view);
                tableLayout = (TableLayout) view.findViewById(R.id.location_todo_list_item);
                titleTextView = (TextView) view.findViewById(R.id.list_item_nameTextView);
                summaryTextView = (TextView) view.findViewById(R.id.list_item_summaryTextView);
                dateTextView = (TextView) view.findViewById(R.id.list_item_dateTextView);
                doneCheckBox = (CheckBox) view.findViewById(R.id.list_item_doneCheckBox);
                alarmImageView = (ImageView) view.findViewById(R.id.list_item_alarmImage);
            }

        }

    }
}

