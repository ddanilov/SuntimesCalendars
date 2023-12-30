/*
    Copyright (C) 2020-2023 Forrest Guice
    This file is part of SuntimesCalendars.

    SuntimesCalendars is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesCalendars is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesCalendars.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.forrestguice.suntimeswidget.calendar.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.forrestguice.suntimescalendars.R;

public class HelpDialog extends BottomSheetDialogFragment
{
    public static final String KEY_HELPTEXT = "helpText";
    public static final String KEY_DIALOGTHEME = "themeResID";

    private int themeResID = 0;
    public void setTheme(int themeResID) {
        this.themeResID = themeResID;
    }

    public HelpDialog() {
        super();
        setArguments(new Bundle());
    }

    private TextView txtView;
    private CharSequence rawContent = "";
    public CharSequence getContent() {
        return rawContent;
    }
    public void setContent( String content ) {
        setContent((CharSequence)Utils.fromHtml(content));
    }

    public void setContent( CharSequence content )
    {
        rawContent = content;
        if (txtView != null) {
            txtView.setText(content);
        }
    }

    public boolean showDefaultsButton() {
        return getArguments().getBoolean(KEY_SHOW_RESTORE_DEFAULTS, false);
    }
    public void setShowDefaultsButton(boolean value) {
        getArguments().putBoolean(KEY_SHOW_RESTORE_DEFAULTS, value);
    }
    public static final String KEY_SHOW_RESTORE_DEFAULTS = "showRestoreDefaults";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        View dialogContent;
        themeResID = ((savedState != null) ? savedState.getInt(KEY_DIALOGTHEME) : themeResID);
        if (themeResID != 0)
        {
            @SuppressLint("RestrictedApi") ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), themeResID);    // hack: contextWrapper required because base theme is not properly applied
            dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.dialog_help, parent, false);

        } else {
            dialogContent = inflater.inflate(R.layout.dialog_help, parent, false);
        }

        txtView = (TextView) dialogContent.findViewById(R.id.help_content);

        Button restoreDefaultsButton = (Button) dialogContent.findViewById(R.id.button_defaults);
        if (restoreDefaultsButton != null)
        {
            restoreDefaultsButton.setVisibility(showDefaultsButton() ? View.VISIBLE : View.GONE);
            restoreDefaultsButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if (dialogListener != null) {
                        dialogListener.onRestoreDefaultsClicked(HelpDialog.this);
                    }
                }
            });
        }

        if (savedState != null) {
            rawContent = savedState.getCharSequence(KEY_HELPTEXT);
        }
        return dialogContent;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        txtView.setText(getContent());
        expandSheet(getDialog());
    }

    private static void expandSheet(DialogInterface dialog)
    {
        if (dialog != null) {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
            if (layout != null) {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
                behavior.setHideable(false);
                behavior.setSkipCollapsed(false);
                behavior.setPeekHeight(200);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle out ) {
        out.putCharSequence(KEY_HELPTEXT, rawContent);
        out.putInt(KEY_DIALOGTHEME, themeResID);
        super.onSaveInstanceState(out);
    }

    /**
     * DialogListener
     */
    public static abstract class DialogListener
    {
        public void onRestoreDefaultsClicked(HelpDialog dialog) {}
    }
    public DialogListener dialogListener = null;
    public void setDialogListener( DialogListener listener )
    {
        this.dialogListener = listener;
    }

}
