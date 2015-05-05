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
package com.jams.music.player.Drawers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.R;

public class NavigationDrawerAdapter extends ArrayAdapter<String> {

    private Context                 mContext;
    private MainActivity.FragmentId mCurrentFragmentId;

    public NavigationDrawerAdapter( final Context context, final MainActivity.FragmentId currentFragmentId ) {
        super( context, R.layout.sliding_menu_browsers_layout, NavigationDrawerFragment.MenuItem.titles );
        mContext = context;
        mCurrentFragmentId = currentFragmentId;
    }

    @Override
    public View getView( final int position, View convertView, ViewGroup parent ) {
        final MenuItemHolder holder;
        if( convertView == null ) {
            convertView = LayoutInflater.from( mContext )
                                        .inflate( R.layout.sliding_menu_browsers_layout, parent, false );
            holder = new MenuItemHolder();
            holder.title = (TextView) convertView.findViewById( R.id.nav_drawer_item_title );
            convertView.setTag( holder );
        } else {
            holder = (MenuItemHolder) convertView.getTag();
        }

        holder.title.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
        holder.title.setText( NavigationDrawerFragment.MenuItem.titles[ position ] );
        holder.title.setTextColor( UIElementsHelper.getThemeBasedTextColor( mContext ) );

        //Highlight the current browser.
        int[] colors = UIElementsHelper.getQuickScrollColors( mContext );

        final NavigationDrawerFragment.MenuItem menuItem = NavigationDrawerFragment.MenuItem.getByPosition( position );
        if( menuItem.isFragment() && menuItem.fragmentId.equals( mCurrentFragmentId ) ) {
            holder.title.setTextColor( colors[ 0 ] );
        }

        return convertView;
    }

    static class MenuItemHolder {
        public TextView title;
    }

}
