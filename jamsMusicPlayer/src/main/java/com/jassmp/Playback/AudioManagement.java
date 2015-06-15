package com.jassmp.Playback;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;

public class AudioManagement {

    //
    // defines

    public static final int DUCK_VOLUME = 5;
    public static final int DUCK_STEP   = 1;

    //
    // private members

    private final Context            mContext;
    private final AudioManager       mAudioManager;
    private final AudioFocusListener mAudioFocusListener;
    private final Handler mHandler        = new Handler();
    private       boolean mAudioFocus     = false;
    private       int     mOriginalVolume = 0;

    public AudioManagement( final Context context, final AudioFocusListener audioFocusListener ) {
        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
        mAudioFocusListener = audioFocusListener;
    }

    public AudioManager getAudioManager() {
        return mAudioManager;
    }

    public boolean hasAudioFocus() {
        return mAudioFocus;
    }

    public boolean requestAudioFocus() {
        if( hasAudioFocus() ) {
            return true;
        }

        int result = mAudioManager.requestAudioFocus( mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                                                      AudioManager.AUDIOFOCUS_GAIN );

        if( result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED ) {
            mAudioFocusListener.audioFocusFailed();
            return mAudioFocus = false;
        } else {
            return mAudioFocus = true;
        }
    }

    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener
            = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange( final int focusChange ) {
            switch( focusChange ) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS:
                    mAudioFocus = false;
                    if( mAudioFocusListener != null ) {
                        mAudioFocusListener.audioFocusLost();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mOriginalVolume = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
                    new DuckVolume( DUCK_VOLUME, -DUCK_STEP );
                    if( mAudioFocusListener != null ) {
                        mAudioFocusListener.audioFocusDucked();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    new DuckVolume( mOriginalVolume, DUCK_STEP );
                    mAudioFocus = true;
                    if( mAudioFocusListener != null ) {
                        mAudioFocusListener.audioFocusGained();
                    }
                    break;
            }
        }
    };

    private class DuckVolume implements Runnable {

        private       int mCurrentVolume;
        private final int mTargetVolume;
        private final int mStep;

        public DuckVolume( final int targetVolume, final int step ) {
            mCurrentVolume = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
            mTargetVolume = targetVolume;
            mStep = step;
            mHandler.postDelayed( this, 50 );
        }

        @Override
        public void run() {
            if( mStep < 0 ) {
                if( mCurrentVolume <= mTargetVolume ) {
                    return;
                }
            } else {
                if( mCurrentVolume >= mTargetVolume ) {
                    return;
                }
            }
            mCurrentVolume += mStep;
            mAudioManager.setStreamVolume( AudioManager.STREAM_MUSIC, ( mCurrentVolume ), 0 );
            mHandler.postDelayed( this, 50 );
        }
    }


    public interface AudioFocusListener {

        public void audioFocusGained();

        public void audioFocusDucked();

        public void audioFocusLost();

        public void audioFocusFailed();

    }

}
