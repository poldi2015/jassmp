package com.jassmp.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.jassmp.R;


public class OrderDialog extends DialogFragment {

    private final OrderDialogListener mListener;

    public OrderDialog( final OrderDialogListener listener ) {
        mListener = listener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog( Bundle savedInstanceState ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        builder.setTitle( R.string.dialog_menu_order );
        builder.setItems( R.array.dialog_menu_order_items, new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
                mListener.onOrderDialogClick( which );
            }
        } );
        return builder.create();
    }

    public interface OrderDialogListener {
        public void onOrderDialogClick( int which );
    }

}
