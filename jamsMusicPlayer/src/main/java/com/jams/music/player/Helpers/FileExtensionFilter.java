package com.jams.music.player.Helpers;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by poldi on 03.05.15.
 */
public class FileExtensionFilter implements FilenameFilter {

    private final String[] mExtensions;

    public FileExtensionFilter( final String[] extensions ) {
        mExtensions = extensions;
    }

    @Override
    public boolean accept( final File dir, final String filename ) {
        for( final String extension : mExtensions ) {
            if( filename.endsWith( extension ) ) {
                return true;
            }
        }
        return false;
    }
}
