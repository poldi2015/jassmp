package com.jams.music.player.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.R;

public class FilterDialog extends DialogFragment {

    public static final MainActivity.FragmentId[] FRAGMENT_IDS = { MainActivity.FragmentId.GENRES,
                                                                   MainActivity.FragmentId.ARTISTS,
                                                                   MainActivity.FragmentId.ALBUMS };

    private final FilterDialogListener mListener;

    public FilterDialog( final FilterDialogListener listener ) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        builder.setTitle( R.string.dialog_menu_filter )
               .setItems( R.array.dialog_menu_filter_items, new DialogInterface.OnClickListener() {
                   public void onClick( DialogInterface dialog, int which ) {
                       mListener.onFilterDialogClick( which );
                   }
               } );

        return builder.create();
    }

    public interface FilterDialogListener {
        public void onFilterDialogClick( int which );
    }

}
