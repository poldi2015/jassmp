package com.jassmp.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.jassmp.Playback.Playback;

public class HeadsetButtonsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive( Context context, Intent intent ) {
        assert Intent.ACTION_MEDIA_BUTTON.equals( intent.getAction() );

        final KeyEvent event = intent.getParcelableExtra( Intent.EXTRA_KEY_EVENT );
        if( KeyEvent.ACTION_DOWN != event.getAction() ) {
            return;
        }

        final Playback playback = new Playback( context.getApplicationContext(), null );
        switch( event.getKeyCode() ) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                playback.pause();
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                playback.previous();
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                playback.next();
                break;

        }
    }

}
