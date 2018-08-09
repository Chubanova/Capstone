package moera.ermais.google.com.myplaces.activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import moera.ermais.google.com.myplaces.R;
import moera.ermais.google.com.myplaces.utils.Utils;

public class AddPlaceActivity extends AppCompatActivity {
    @BindView(R.id.nav_view_add)
    NavigationView mNavigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        ButterKnife.bind(this);

        mNavigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    menuItem.setChecked(true);
                    Utils.goMenu(this, menuItem.getItemId());
                    mDrawerLayout.closeDrawers();

                    return true;
                });
    }
}
