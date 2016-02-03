package de.fhws.campusapp;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import de.fhws.campusapp.fragment.LecturersFragment;
import de.fhws.campusapp.fragment.MapFragment;
import de.fhws.campusapp.fragment.ModuleViewPagerFragment;
import de.fhws.campusapp.fragment.WebViewFragment;
import de.fhws.campusapp.receiver.NetworkChangeReceiver;

public class MainActivity extends AppCompatActivity {

    public static Location location;
    public  ActionBarDrawerToggle drawerToggle;
    private FragmentManager fm;
    private DrawerLayout drawerLayout;
    private GoogleApiClient googleApiClient;

    public static void replaceFragment( FragmentManager fm, Fragment fragment ) {
      replaceFragment(fm, fragment, true);
    }

    public static void replaceFragment( FragmentManager fm, Fragment fragment, Boolean standardAnimation ) {
        FragmentTransaction transaction = fm.beginTransaction();

        if(standardAnimation){
            transaction.setCustomAnimations(R.anim.slide_in_left,
                    R.anim.slide_out_right,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
        }
        transaction.replace( R.id.content_container,
                fragment, fragment.getClass().getName())
                .addToBackStack(null)
                .commit();
    }

    public static void replaceFragmentPopBackStack( FragmentManager fm, Fragment fragment ) {
        fm.popBackStack();
        replaceFragment( fm, fragment );
    }

    public static void startDialogFragment(FragmentManager fragmentManager, DialogFragment fragment) {
        fragment.show( fragmentManager, "" );
    }

    @Override
    public boolean onSupportNavigateUp() {
        fm.popBackStack();

        return false;
    }

    @Override
    public void onBackPressed() {
        if( drawerLayout.isDrawerOpen( GravityCompat.START ) ) {
            drawerLayout.closeDrawer( GravityCompat.START );
        } else {
            if( fm.getBackStackEntryCount() > 1 )
                super.onBackPressed();
            else
                finish();
        }
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerNetworkChangeReceiver();
        fm = getSupportFragmentManager();
        buildGoogleApiClient(this);
        checkPermissions();

        setUpActionBar();
        if( savedInstanceState == null ) {
            replaceFragment( fm, new LecturersFragment() );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int grantResults[] ) {
        switch ( requestCode ) {
            case 1:
                if( grantResults.length > 0 && grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED ) {
                    Toast.makeText(
                            this,
                            R.string.noGPS,
                            Toast.LENGTH_SHORT ).show();

                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if( googleApiClient.isConnected() )
            googleApiClient.disconnect();

        try {
            getBaseContext().unregisterReceiver(NetworkChangeReceiver.getInstance());
        } catch (IllegalArgumentException e){
            //die silently
        }
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        drawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout );
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close );
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById( R.id.navigation_view );
        navigationView.setNavigationItemSelectedListener( new MyDrawerClickListener() );
    }

    private synchronized void buildGoogleApiClient( Context context )
    {
        googleApiClient = new GoogleApiClient.Builder( context )
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks()
                        {
                            @Override
                            public void onConnected( Bundle connectionHint )
                            {
                                try {
                                    location = LocationServices
                                            .FusedLocationApi
                                            .getLastLocation( googleApiClient );
                                } catch ( SecurityException ex ) {
                                }
                            }

                            @Override
                            public void onConnectionSuspended( int cause )
                            {
                                googleApiClient.connect();
                            }
                        } )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener()
                        {
                            @Override
                            public void onConnectionFailed(
                                    ConnectionResult result )
                            {
                            }
                        } )
                .addApi( LocationServices.API )
                .build();
    }

    private void registerNetworkChangeReceiver(){
        IntentFilter networkFilter = new IntentFilter();
        networkFilter.addAction( ConnectivityManager.CONNECTIVITY_ACTION);
        getBaseContext().registerReceiver( NetworkChangeReceiver.getInstance(), networkFilter);
    }

    private void checkPermissions() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;

        if( ContextCompat.checkSelfPermission( this, permission ) ==
                PackageManager.PERMISSION_GRANTED )
            return;

        ActivityCompat.requestPermissions( this, new String[]{permission}, 1 );
    }


    private class MyDrawerClickListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected( MenuItem item ) {
            switch ( item.getItemId() ) {
                case R.id.navigation_lecturers:
                    replaceFragmentPopBackStack( fm, new LecturersFragment() );
                    break;
                case R.id.navigation_modules:
                    replaceFragmentPopBackStack( fm, new ModuleViewPagerFragment() );
                    break;
                case R.id.navigation_map:
                    replaceFragmentPopBackStack( fm, new MapFragment() );
                    break;
                case R.id.elearning:
                    replaceFragmentPopBackStack( fm, WebViewFragment.newInstance( getString( R.string.elearning ), "https://elearning.fhws.de/" ) );
                    break;
                case R.id.student_portal:
                    replaceFragmentPopBackStack( fm, WebViewFragment.newInstance( getString( R.string.student_portal ), "https://studentenportal.fhws.de" ) );
                    break;
            }
            drawerLayout.closeDrawer( GravityCompat.START );
            return true;
        }
    }

}