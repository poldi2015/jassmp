package com.jassmp.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.jassmp.JassMpDb.OrderDirection;
import com.jassmp.R;


public class OrderDialog extends DialogFragment {

    //
    // private members

    private final OrderDialogListener mListener;
    private int mSelectedItem = 0;

    public OrderDialog( final OrderDialogListener listener, final int selectedItem ) {
        // TODO: Add default selection
        mListener = listener;
        mSelectedItem = selectedItem;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog( Bundle savedInstanceState ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        builder.setTitle( R.string.dialog_menu_order );
        builder.setSingleChoiceItems( R.array.dialog_menu_order_items, mSelectedItem,
                                      new DialogInterface.OnClickListener() {
                                          public void onClick( DialogInterface dialog, int which ) {
                                              mSelectedItem = which;
                                          }
                                      } );
        builder.setPositiveButton( "Ascending", new DialogInterface.OnClickListener() {
            @Override
            public void onClick( final DialogInterface dialog, final int which ) {
                mListener.onOrderDialogClick( mSelectedItem, OrderDirection.ASC );
                dialog.dismiss();
            }
        } );
        builder.setNegativeButton( "Descending", new DialogInterface.OnClickListener() {
            @Override
            public void onClick( final DialogInterface dialog, final int which ) {
                mListener.onOrderDialogClick( mSelectedItem, OrderDirection.DESC );
                dialog.dismiss();
            }
        } );
        return builder.create();
    }

    public interface OrderDialogListener {
        public void onOrderDialogClick( int which, OrderDirection orderDirection );
    }

}
