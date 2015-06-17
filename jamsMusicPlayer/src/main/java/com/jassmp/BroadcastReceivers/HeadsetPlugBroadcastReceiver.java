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
package com.jassmp.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jassmp.Playback.Playback;
import com.jassmp.Utils.Common;

/**
 * BroadcastReceiver that handles and processes all headset
 * unplug/plug actions and events.
 *
 * @author Saravan Pantham
 */
public class HeadsetPlugBroadcastReceiver extends BroadcastReceiver {

    private Common mApp;

    @Override
    public void onReceive( Context context, Intent intent ) {
        assert intent.getAction().equals( Intent.ACTION_HEADSET_PLUG );

        final Playback playback = new Playback( context.getApplicationContext() );
        switch( intent.getIntExtra( "state", -1 ) ) {
            case 0:
                //Headset unplug event.
                playback.pause();
                break;
            case 1:
                //Headset plug-in event.
                playback.play();
                break;
        }
    }

}
