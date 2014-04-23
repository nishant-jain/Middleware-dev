package com.middleware.pubsubclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class UploadPrefs extends DialogFragment
	{
		Context mContext;

		public UploadPrefs()
			{
				mContext = getActivity();
			}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
			{
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
				alertDialogBuilder.setTitle("Really?");
				alertDialogBuilder.setMessage("Are you sure?");
				// null should be your on click listener
				alertDialogBuilder.setPositiveButton("OK", null);
				alertDialogBuilder.setNegativeButton(0, new OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
							}
					});

				return alertDialogBuilder.create();
			}
	}
