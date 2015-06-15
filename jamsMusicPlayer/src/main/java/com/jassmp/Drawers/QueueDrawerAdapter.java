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
package com.jassmp.Drawers;

public class QueueDrawerAdapter /* extends ArrayAdapter<Integer> */ {

    //    private Context mContext;
    //    private Common  mApp;
    //    private int[]   mColors;
    //
    //    public QueueDrawerAdapter( Context context, ArrayList<Integer> playbackIndecesList ) {
    //        super( context, R.layout.queue_drawer_list_layout, playbackIndecesList );
    //
    //        mContext = context;
    //        mApp = (Common) mContext.getApplicationContext();
    //        mColors = UIElementsHelper.getQuickScrollColors( context );
    //    }
    //
    //    public View getView( final int position, View convertView, ViewGroup parent ) {
    //
    //        QueueDrawerHolder holder;
    //        if( convertView == null ) {
    //            convertView = LayoutInflater.from( mContext ).inflate( R.layout.queue_drawer_list_layout, parent,
    // false );
    //
    //            holder = new QueueDrawerHolder();
    //            holder.songTitleText = (TextView) convertView.findViewById( R.id.queue_song_title );
    //            holder.artistText = (TextView) convertView.findViewById( R.id.queue_song_artist );
    //            holder.removeSong = (ImageView) convertView.findViewById( R.id.queue_remove_song );
    //
    //            holder.songTitleText.setTextColor( UIElementsHelper.getRegularTextColor( mContext ) );
    //            holder.artistText.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
    //            holder.songTitleText.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
    //            holder.artistText.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
    //
    //            convertView.setTag( holder );
    //        } else {
    //            holder = (QueueDrawerHolder) convertView.getTag();
    //        }
    //
    //        //Get the song's basic info.
    //        SongHelper songHelper = new SongHelper();
    //        songHelper.populateBasicSongData( mContext, position );
    //
    //        holder.songTitleText.setText( songHelper.getTitle() );
    //        holder.artistText.setText( songHelper.getArtist() );
    //
    //        //Apply the item's colors.
    //        try {
    //            if( position == mApp.getAudioPlaybackService().getCurrentSongIndex() ) {
    //                holder.songTitleText.setTextColor( mColors[ 0 ] );
    //                holder.artistText.setTextColor( mColors[ 0 ] );
    //            } else if( mApp.getCurrentTheme() == Common.LIGHT_THEME ) {
    //                holder.songTitleText.setTextColor( UIElementsHelper.getRegularTextColor( mContext ) );
    //                holder.artistText.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
    //            } else if( mApp.getCurrentTheme() == Common.DARK_THEME ) {
    //                holder.songTitleText.setTextColor( UIElementsHelper.getRegularTextColor( mContext ) );
    //                holder.artistText.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
    //            }
    //
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //        }
    //
    //        return convertView;
    //    }
    //
    //    class QueueDrawerHolder {
    //        public TextView  songTitleText;
    //        public TextView  artistText;
    //        public ImageView removeSong;
    //    }

}
