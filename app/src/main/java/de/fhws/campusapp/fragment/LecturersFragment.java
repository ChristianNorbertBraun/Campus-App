package de.fhws.campusapp.fragment;


import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import de.fhws.campusapp.LecturerDetailActivity;
import de.fhws.campusapp.R;
import de.fhws.campusapp.adapter.LecturerListAdapter;

public class LecturersFragment extends Fragment implements
        LecturerListAdapter.OnLecturerClickListener,
        LecturerListAdapter.ActivateProgressBar,
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener {

    private ProgressBar progressBar;
    private LecturerListAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setHasOptionsMenu( true );
        setRetainInstance( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_lecturers, container, false );

        getActivity().setTitle( getString( R.string.lecturer ) );

        recyclerView = (RecyclerView) view.findViewById( R.id.list_rv );
        progressBar = (ProgressBar) view.findViewById( R.id.progressBar );
        progressBar.getIndeterminateDrawable().setColorFilter( ContextCompat.getColor( getActivity(), R.color.colorPrimary ), android.graphics.PorterDuff.Mode.MULTIPLY );

        int numberOfColumns = determineNumberOfColumns();

        final StaggeredGridLayoutManager llm = new StaggeredGridLayoutManager(
                numberOfColumns,
                StaggeredGridLayoutManager.VERTICAL );

        recyclerView.setLayoutManager( llm );
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new LecturerListAdapter(
                R.layout.card_lecturer,
                getActivity(),
                LecturersFragment.this,
                LecturersFragment.this );
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void showProgressBar( final boolean show ) {
        if( isAdded() ) {
            getActivity().runOnUiThread( new Runnable() {
                public void run() {
                    progressBar.setVisibility( show ? View.VISIBLE : View.INVISIBLE );
                }
            } );
        }
    }

    @Override
    public void onLecturerClick( String fullName, ImageView view ) {
        Intent intent = new Intent( getActivity(), LecturerDetailActivity.class );
        intent.putExtra("fullName", fullName);

        if( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(getActivity(), view, "pic");
            getActivity().startActivity(intent, options.toBundle());
        } else {
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
        }
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate( R.menu.search_menu, menu );

        MenuItem searchItem = menu.findItem( R.id.action_search );

        SearchView searchView;
        if( searchItem != null ) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(this);
            searchView.setOnCloseListener(this);
        }
    }

    @Override
    public boolean onQueryTextChange( String query ) {
        this.adapter.filter( query );
        return false;
    }

    @Override
    public boolean onQueryTextSubmit( String query ) {
        return false;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    private int determineNumberOfColumns()
    {
        int numberOfColumns = 2;

        if ( getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE )
        {
            numberOfColumns = 4;
        }
        return numberOfColumns;
    }

}