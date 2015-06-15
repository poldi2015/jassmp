package com.jassmp.Playback;

public interface PlaybackStateListener {

    void playStateChanged( PlayerState state );

    void queueChanged( QueueState state );

    void playPositionChanged( PlayPositionState state );

}
