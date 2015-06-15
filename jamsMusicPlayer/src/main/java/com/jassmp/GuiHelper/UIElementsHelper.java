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
package com.jassmp.GuiHelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.jassmp.Preferences.Preferences;
import com.jassmp.Preferences.Theme;

/**
 * This class contains the static methods that return the appropriate UI
 * elements/colors based on the selected theme (light or dark).
 *
 * @author Saravan Pantham
 */
public class UIElementsHelper {

    private static Theme getCurrentTheme( final Context context ) {
        final Preferences preferences = new Preferences( context );
        return preferences.getCurrentTheme();
    }

    private static int getThemeBasedColor( final Context context, final String darkColor, final String lightColor ) {
        return getCurrentTheme( context ) == Theme.DARK ? Color.parseColor( darkColor )
                                                        : Color.parseColor( lightColor );
    }

    public static Typeface getRegularTypeface( Context context ) {
        return TypefaceHelper.getTypeface( context, "Roboto-Regular" );
    }

    public static Typeface getBoldTypeface( Context context ) {
        return TypefaceHelper.getTypeface( context, "Roboto-Medium" );
    }

    public static int getRegularTextColor( Context context ) {
        return getThemeBasedColor( context, "#DEDEDE", "#404040" );
    }

    public static int getHighLightTextColor( Context context ) {
        return getThemeBasedColor( context, "#FFFFFF", "#000000" );
    }

    public static int getSmallTextColor( Context context ) {
        return getThemeBasedColor( context, "#999999", "#7F7F7F" );
    }

    public static int getHighLightSmallTextColor( Context context ) {
        return getThemeBasedColor( context, "#E0E0E0", "#202020" );
    }

    /**
     * Returns a solid background color based on the selected theme.
     */
    public static int getBackgroundColor( Context context ) {
        return getThemeBasedColor( context, "#FF111111", "#FFDDDDD" );
    }

    /**
     * Do not use this method if the GridView needs cards as its background.
     */
    public static int getGridViewBackground( Context context ) {
        return getThemeBasedColor( context, "#FF131313", "#FFFFFFFF" );
    }

    public static int getIcon( Context context, String iconName ) {

        int resourceID = 0;
        if( !"".equals( iconName ) ) {

            //We're using "cloud" and "pin" in the settings page so we don't want them to be affected by the player
            // color.
            if( iconName.equals( "cloud_settings" ) || iconName.equals( "pin_settings" ) || iconName.equals(
                    "equalizer_settings" ) ) {
                if( iconName.equals( "cloud_settings" ) ) {
                    iconName = "cloud";
                } else if( iconName.equals( "pin_settings" ) ) {
                    iconName = "pin";
                } else if( iconName.equals( "equalizer_settings" ) ) {
                    iconName = "equalizer";
                }
            }

            // Note that the actual theme that is applied and the suffix of the file name are flipped: DARK_THEME
            // uses "xxx_light.png" while LIGHT_THEME
            //                  * uses "xxx.png".
            final String iconIdentifier = getCurrentTheme( context ) == Theme.DARK ? iconName + "_light" : iconName;
            resourceID = context.getResources().getIdentifier( iconIdentifier, "drawable", context.getPackageName() );
        }

        return resourceID;

    }

    /**
     * Returns the ActionBar color based on the selected color theme (not used for the player).
     */
    public static Drawable getGeneralActionBarBackground( Context context ) {
        final Preferences preferences = new Preferences( context );
        switch( preferences.getNowPlayingColorTheme() ) {
            default:
            case BLUE:
                return new ColorDrawable( 0xFF0099CC );
            case RED:
                return new ColorDrawable( 0xFFB0120A );
            case GREEN:
                return new ColorDrawable( 0xFF0A7E07 );
            case ORANGE:
                return new ColorDrawable( 0xFFEF6C00 );
            case PURPLE:
                return new ColorDrawable( 0xFF6A1B9A );
            case MAGENTA:
                return new ColorDrawable( 0xFFC2185B );
        }
    }

    /**
     * Returns an array of color values for the QuickScroll view.
     */
    public static int[] getQuickScrollColors( Context context ) {
        final Preferences preferences = new Preferences( context );
        switch( preferences.getNowPlayingColorTheme() ) {
            default:
            case BLUE:
                return new int[]{ 0xFF0099CC, 0x990099CC, Color.WHITE };
            case RED:
                return new int[]{ 0xFFB0120A, 0x99B0120A, Color.WHITE };
            case GREEN:
                return new int[]{ 0xFF0A7E07, 0x990A7E07, Color.WHITE };
            case ORANGE:
                return new int[]{ 0xFFEF6C00, 0x99EF6C00, Color.WHITE };
            case PURPLE:
                return new int[]{ 0xFF6A1B9A, 0x996A1B9A, Color.WHITE };
            case MAGENTA:
                return new int[]{ 0xFFC2185B, 0x99C2185B, Color.WHITE };
        }
    }

    public static int getShadowedCircle( Context context ) {
        final Preferences preferences = new Preferences( context );
        String resourceName;
        switch( preferences.getNowPlayingColorTheme() ) {
            default:
            case BLUE:
                resourceName = "shadowed_circle_blue";
                break;
            case RED:
                resourceName = "shadowed_circle_red";
                break;
            case GREEN:
                resourceName = "shadowed_circle_green";
                break;
            case ORANGE:
                resourceName = "shadowed_circle_orange";
                break;
            case PURPLE:
                resourceName = "shadowed_circle_purple";
                break;
            case MAGENTA:
                resourceName = "shadowed_circle_magenta";
                break;
        }
        return context.getResources().getIdentifier( resourceName, "drawable", context.getPackageName() );
    }

    public static int getEmptyColorPatch( Context context ) {
        switch( getCurrentTheme( context ) ) {
            case DARK:
                return context.getResources()
                              .getIdentifier( "empty_color_patch", "drawable", context.getPackageName() );
            case LIGHT:
                return context.getResources()
                              .getIdentifier( "empty_color_patch_light", "drawable", context.getPackageName() );
            default:
                return 0;
        }
    }

    public static int getEmptyCircularColorPatch( Context context ) {
        switch( getCurrentTheme( context ) ) {
            case DARK:
                return context.getResources()
                              .getIdentifier( "empty_color_patch_circular", "drawable", context.getPackageName() );
            case LIGHT:
                return context.getResources()
                              .getIdentifier( "empty_color_patch_circular_light", "drawable",
                                              context.getPackageName() );
            default:
                return 0;
        }
    }

}
