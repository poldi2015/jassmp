package com.jassmp.Playback;

public interface PlaybackStateListener {

    void onPlayStateChanged( PlayerState state );

    void onQueueChanged( QueueState state );

    void onPlayPositionChanged( PlayPositionState state );

}
