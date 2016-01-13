package de.fhws.campusapp.network;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.fhws.campusapp.entity.Module;

public class ModuleNetwork extends BaseNetwork {

    public interface FetchAllModulesListener {
        void fetchAllModules( List<Module> allModules );
    }

    public interface FetchFilteredModules {
        void fetchFilteredModules( List<Module> modules );
    }

    public ModuleNetwork() {
        super( "http://193.175.31.146:8080/fiwincoming/api" );
    }

    public void fetchAllModules( int size, int offset, final FetchAllModulesListener listener ) {
        String url = host + "/modules?size=" + size + "&offset=" + offset;
        requestAsync(
                new Request(
                        url,
                        METHOD_GET,
                        new String[]{"Accept:application/json"},
                        null
                ),
                new OnResultListener() {
                    @Override
                    public void onResultListener( Response response ) {

                        listener.fetchAllModules( extractModulesFromResponse( response ) );
                    }
                } );
    }

    public void fetchFilteredModules(
            Module.Program program,
            Module.Language language,
            Module.Level level,
            Module.Semester semester,
            int size,
            int offset,
            final FetchFilteredModules listener ) {

        String url = createFilterUrl( program, language, level, semester, size, offset );
        requestAsync(
                new Request(
                        url,
                        METHOD_GET,
                        new String[]{"Accept:application/json"},
                        null
                ),
                new OnResultListener() {
                    @Override
                    public void onResultListener( Response response ) {

                        listener.fetchFilteredModules( extractModulesFromResponse( response ) );
                    }
                } );

    }

    private List<Module> extractModulesFromResponse( Response response ) {
        List<Module> modules = new ArrayList<>();
        if( successfulRequest( response.getCode() ) ) {
            Type listType = new TypeToken<List<Module>>() {
            }.getType();
            modules = gson.fromJson( response.getString(), listType );
        }
        return modules;
    }

    private String createFilterUrl( Module.Program program,
                                    Module.Language language,
                                    Module.Level level,
                                    Module.Semester semester,
                                    int size,
                                    int offset ) {

        String url = host + "/modules?size=" + size + "&offset=" + offset;

        if( program != null )
            url += "&program=" + program.name();
        if( language != null )
            url += "&lang=" + language.name();
        if( semester != null )
            url += "&semester=" + semester.name();
        if( level != null ) {
            String currentLvl;
            if( level == Module.Level.ONE )
                currentLvl = "1";
            else if( level == Module.Level.TWO )
                currentLvl = "2";
            else if( level == Module.Level.THREE )
                currentLvl = "3";
            else
                currentLvl = "4";
            url += "&level=" + currentLvl;
        }
        return url;
    }
}