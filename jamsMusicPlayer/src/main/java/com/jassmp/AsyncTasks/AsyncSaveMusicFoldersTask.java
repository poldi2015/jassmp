/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jassmp.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.jassmp.DBHelpers.FolderTableAccessor;
import com.jassmp.Utils.Common;

import java.util.HashMap;

public class AsyncSaveMusicFoldersTask extends AsyncTask<String, Void, Boolean> {

    private Common                   mApp;
    private HashMap<String, Boolean> mMusicFolders;

    public AsyncSaveMusicFoldersTask( Context context, HashMap<String, Boolean> musicFolders ) {
        mApp = (Common) context;
        mMusicFolders = musicFolders;
    }

    @Override
    protected Boolean doInBackground( String... params ) {
        FolderTableAccessor.getInstance( mApp ).replaceMusicFolders( mMusicFolders );
        return true;
    }

    @Override
    protected void onPostExecute( Boolean result ) {
        super.onPostExecute( result );

    }

}
