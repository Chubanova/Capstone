package moera.ermais.google.com.myplaces.entity;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import moera.ermais.google.com.myplaces.R;

public class MyPlace implements Parcelable {

    private String id;
    private String title;
    private double lat;
    private double lng;

    public MyPlace(String id, String title, double lat, double lng) {
        this.id = id;
        this.title = title;
        this.lat = lat;
        this.lng = lng;
    }

    protected MyPlace(Parcel in) {
        id = in.readString();
        title = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    public static final Creator<MyPlace> CREATOR = new Creator<MyPlace>() {
        @Override
        public MyPlace createFromParcel(Parcel in) {
            return new MyPlace(in);
        }

        @Override
        public MyPlace[] newArray(int size) {
            return new MyPlace[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getPlaceText(Context context) {
        StringBuilder result = new StringBuilder();

        result.append(context.getString(R.string.place_header)).append("\n");
        result.append(title);

        return result.toString();
    }
}
