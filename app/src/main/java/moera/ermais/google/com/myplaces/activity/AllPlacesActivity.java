package moera.ermais.google.com.myplaces.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import moera.ermais.google.com.myplaces.R;
import moera.ermais.google.com.myplaces.fragment.AllPlacesFragment;
import moera.ermais.google.com.myplaces.utils.Utils;

public class AllPlacesActivity extends AppCompatActivity implements AllPlacesFragment.OnClickListener {
    public static final String TAG = AllPlacesActivity.class.getSimpleName();
    @BindView(R.id.nav_view_all)
    NavigationView mNavigationView;
    @BindView(R.id.drawer_layout)
     DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_places);
        ButterKnife.bind(this);


        mNavigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    menuItem.setChecked(true);
                    Utils.goMenu(this, menuItem.getItemId());
                    mDrawerLayout.closeDrawers();

                    return true;
                });
    }

    public void openMap(View view) {
        Utils.goMenu(this, R.id.map);
    }


    @Override
    public void onClick(double lat, double lng) {
        Log.d(TAG, "Pressed place");
        // Edit or remove marker
        Intent intent = new Intent();
        intent.setClass(this, AddPlaceActivity.class);
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", lat);
        bundle.putDouble("lng", lng);
        intent.putExtras(bundle);
        getApplicationContext().startActivity(intent);
    }
}
