package moera.ermais.google.com.myplaces.fragment;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import moera.ermais.google.com.myplaces.PlacesCursorAdapter;
import moera.ermais.google.com.myplaces.R;
import moera.ermais.google.com.myplaces.service.GeoService;

import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.CONTENT_URI;

public class AllPlacesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = AllPlacesFragment.class.getSimpleName();
    private static final int TASK_LOADER_ID = 0;

    private PlacesCursorAdapter mAdapter;
    @BindView(R.id.list)
    RecyclerView mRecyclerView;


    // Define a new interface OnImageClickListener that triggers a callback in the host activity
    OnClickListener mCallback;

    public interface OnClickListener {
        void onClick(double lat, double lng);
    }

    //mandatory empty constructor
    public AllPlacesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_all_places_list, container, false);
        ButterKnife.bind(this, rootView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Uri uri = CONTENT_URI;
        ContentResolver p = getContext().getContentResolver();
        Cursor c = p.query(uri, null, null, null, null);


        mAdapter = new PlacesCursorAdapter(getContext(), c);
        mRecyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (viewHolder.itemView.getTag() != null) {

                    int id = (int) viewHolder.itemView.getTag();
                    Log.d(TAG, "Removed place: " + id);

                    String stringId = Integer.toString(id);
                    Uri uri = CONTENT_URI;
                    uri = uri.buildUpon().appendPath(stringId).build();

                    int res = getContext().getContentResolver().delete(uri, null, null);

                    if (res != 0) {
                        Log.d(TAG, "Removed place: " + stringId);
                    }

                    restartService();
                }


                getLoaderManager().restartLoader(TASK_LOADER_ID, null, AllPlacesFragment.this);

            }
        }).attachToRecyclerView(mRecyclerView);
        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);


        return rootView;

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(getContext()) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mTaskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mTaskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data

                // Query and load all task data in the background; sort by priority
                // [Hint] use a try/catch block to catch any errors in loading data
                Uri uri = CONTENT_URI;
                ContentResolver p = getContext().getContentResolver();
                try {
                    return p.query(uri, null, null, null, null);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };

    }


    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the data that the adapter uses to create ViewHolders
        mAdapter.swapCursor(data);
    }


    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.
     * onLoaderReset removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void restartService() {
        Intent service = new Intent(Objects.requireNonNull(getActivity()).getApplicationContext(), GeoService.class);
        getActivity().stopService(service);
        getActivity().startService(service);
    }
}
