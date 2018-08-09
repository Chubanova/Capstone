package moera.ermais.google.com.myplaces;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import moera.ermais.google.com.myplaces.database.PlaceContract;
import moera.ermais.google.com.myplaces.fragment.AllPlacesFragment;

import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_NOTIFICATION;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_DESCR;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_NAME;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_XY_LAT;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_XY_LNG;

public class PlacesCursorAdapter extends RecyclerView.Adapter<PlacesCursorAdapter.PlaceViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private static OnClickListener listener;


    /**
     * Constructor using the context and the db cursor
     *
     * @param context the calling context/activity
     */
    public PlacesCursorAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
        this.listener = (OnClickListener) context;

    }

    public PlacesCursorAdapter(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item
     *
     * @param parent   The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new PlaceViewHolder that holds a View with the item_place_card layout
     */
    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.place_card, parent, false);
        return new PlaceViewHolder(view);
    }

    /**
     * Binds the data from a particular position in the cursor to the corresponding view holder
     *
     * @param holder   The PlaceViewHolder instance corresponding to the required position
     * @param position The current position that needs to be loaded with data
     */
    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {

        int idIndex = mCursor.getColumnIndex(PlaceContract.PlaceEntry._ID);
        int placeNameIndex = mCursor.getColumnIndex(COLUMN_PLACE_NAME);
        int placeDescrIndex = mCursor.getColumnIndex(COLUMN_PLACE_DESCR);
        int notificationIndex = mCursor.getColumnIndex(COLUMN_NOTIFICATION);
        int lat = mCursor.getColumnIndex(COLUMN_PLACE_XY_LAT);
        int lng = mCursor.getColumnIndex(COLUMN_PLACE_XY_LNG);

        mCursor.moveToPosition(position); // get to the right location in the cursor

        // Determine the values of the wanted data
        final int id = mCursor.getInt(idIndex);
        holder.itemView.setTag(id);
        String description = mCursor.getString(placeNameIndex) + " / " + mCursor.getString(placeDescrIndex);
        String notification = mCursor.getString(notificationIndex);
        holder.notifyTextView.setText(notification);
        holder.addressTextView.setText(description);
        holder.bind(mCursor.getDouble(lat), mCursor.getDouble(lng), listener);

    }


    /**
     * Returns the number of items in the cursor
     *
     * @return Number of items in the cursor, or 0 if null
     */
    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    /**
     * PlaceViewHolder class for the recycler view item
     */
    class PlaceViewHolder extends RecyclerView.ViewHolder {

        TextView notifyTextView;
        TextView addressTextView;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            notifyTextView = (TextView) itemView.findViewById(R.id.notification_text_view);
            addressTextView = (TextView) itemView.findViewById(R.id.address_text_view);
        }


        public void bind(double lat, double lng, final OnClickListener listener) {
            addressTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(lat, lng);
                }
            });
        }
    }
}
