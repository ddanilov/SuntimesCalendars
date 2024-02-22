/**
    Copyright (C) 2018-2020 Forrest Guice
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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.forrestguice.suntimescalendars.BuildConfig;
import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class AboutDialog extends BottomSheetDialogFragment
{
    public static final String WEBSITE_URL = "https://forrestguice.github.io/SuntimesWidget/";
    public static final String PRIVACY_URL = "https://github.com/forrestguice/SuntimesCalendars/wiki/Privacy";
    public static final String CHANGELOG_URL = "https://github.com/forrestguice/SuntimesCalendars/blob/master/CHANGELOG.md";
    public static final String COMMIT_URL = "https://github.com/forrestguice/SuntimesCalendars/commit/";

    public static final String KEY_ICONID = "paramIconID";
    public static final String KEY_APPVERSION = "paramAppVersion";
    public static final String KEY_PROVIDERVERSION = "paramProviderVersion";
    public static final String KEY_PROVIDER_PERMISSIONDENIED = "paramProviderDenied";

    private int param_iconID = R.drawable.ic_suntimes_calendar;
    public void setIconID( int resID )
    {
        param_iconID = resID;
    }

    /**
     * onCreateDialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        BottomSheetDialog dialog = (BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
                View layout = bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);
                if (layout != null)
                {
                    BottomSheetBehavior.from(layout).setHideable(true);
                    BottomSheetBehavior.from(layout).setPeekHeight((int)getResources().getDimension(R.dimen.about_peekheight));
                    //BottomSheetBehavior.from(layout).setSkipCollapsed(true);
                    BottomSheetBehavior.from(layout).setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        if (savedInstanceState != null)
        {
            providerPermissionsDenied = savedInstanceState.getBoolean(KEY_PROVIDER_PERMISSIONDENIED);

            if (savedInstanceState.containsKey(KEY_APPVERSION)) {
                appVersion = savedInstanceState.getString(KEY_APPVERSION);
            }
            if (savedInstanceState.containsKey(KEY_PROVIDERVERSION)) {
                providerVersion = savedInstanceState.getInt(KEY_PROVIDERVERSION);
            }
        }

        return dialog;
    }

    /**
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View dialogContent = inflater.inflate(R.layout.layout_dialog_about, container, false);
        initViews(getActivity(), dialogContent);
        return dialogContent;
    }

    /**
     * initViews
     */
    public void initViews(Context context, View dialogContent)
    {
        TextView nameView = (TextView) dialogContent.findViewById(R.id.txt_about_name);
        nameView.setOnClickListener(new ViewUtils.ThrottledClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openLink(WEBSITE_URL);
            }
        }));
        if (Build.VERSION.SDK_INT >= 17)
            nameView.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(context, param_iconID), null, null, null);
        else nameView.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, param_iconID), null, null, null);

        TextView versionView = (TextView) dialogContent.findViewById(R.id.txt_about_version);
        versionView.setMovementMethod(LinkMovementMethod.getInstance());
        versionView.setText(Utils.fromHtml(htmlVersionString()));

        TextView providerView = (TextView) dialogContent.findViewById(R.id.txt_about_provider);
        providerView.setText(Utils.fromHtml(providerVersionString(context)));

        TextView urlView = (TextView) dialogContent.findViewById(R.id.txt_about_url);
        urlView.setMovementMethod(LinkMovementMethod.getInstance());
        urlView.setText(Utils.fromHtml(context.getString(R.string.app_url)));

        TextView supportView = (TextView) dialogContent.findViewById(R.id.txt_about_support);
        supportView.setMovementMethod(LinkMovementMethod.getInstance());
        supportView.setText(Utils.fromHtml(context.getString(R.string.app_support_url)));

        final TextView donateView = (TextView) dialogContent.findViewById(R.id.text_about_donate);
        donateView.setMovementMethod(LinkMovementMethod.getInstance());
        donateView.setText(Utils.fromHtml(context.getString(R.string.app_donate_url, context.getString(R.string.app_name))));
        donateView.setVisibility(View.GONE);

        CheckBox checkDonate = (CheckBox) dialogContent.findViewById(R.id.check_donate);
        checkDonate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                donateView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        TextView legalView1 = (TextView) dialogContent.findViewById(R.id.txt_about_legal1);
        legalView1.setMovementMethod(LinkMovementMethod.getInstance());
        legalView1.setText(Utils.fromHtml(context.getString(R.string.app_legal1)));

        TextView legalView2 = (TextView) dialogContent.findViewById(R.id.txt_about_legal2);
        legalView2.setMovementMethod(LinkMovementMethod.getInstance());
        legalView2.setText(Utils.fromHtml(context.getString(R.string.app_legal2, initTranslationCredits(context))));

        TextView legalView4 = (TextView) dialogContent.findViewById(R.id.txt_about_legal4);
        String permissionsExplained = context.getString(R.string.privacy_permission_calendar);

        String privacy = context.getString(R.string.privacy_policy, permissionsExplained);
        legalView4.setText(Utils.fromHtml(privacy));

        TextView legalView5 = (TextView) dialogContent.findViewById(R.id.txt_about_legal5);
        legalView5.setMovementMethod(LinkMovementMethod.getInstance());
        legalView5.setText(Utils.fromHtml(context.getString(R.string.privacy_url)));
    }

    /**
     * onSaveInstanceState
     */
    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        outState.putInt(KEY_ICONID, param_iconID);
        outState.putBoolean(KEY_PROVIDER_PERMISSIONDENIED, providerPermissionsDenied);
        if (appVersion != null) {
            outState.putString(KEY_APPVERSION, appVersion);
        }
        if (providerVersion != null) {
            outState.putInt(KEY_PROVIDERVERSION, providerVersion);
        }

        super.onSaveInstanceState(outState);
    }

    private String appVersion = null;
    private Integer providerVersion = null;
    public void setVersion(String appVersion, Integer providerVersion)
    {
        this.appVersion = appVersion;
        this.providerVersion = providerVersion;
    }

    private boolean providerPermissionsDenied = false;
    public void setPermissionStatus( boolean status ) {
        providerPermissionsDenied = status;
    }

    protected String providerVersionString(@NonNull Context context)
    {
        String denied = context.getString(R.string.app_provider_version_denied);
        String missingVersion = context.getString(R.string.app_provider_version_missing);
        String versionString = (appVersion == null) ? missingVersion
                                                    : (appVersion + " (" + ((providerVersion != null) ? providerVersion
                                                                                                      : (providerPermissionsDenied ? denied : missingVersion)) + ")");
        return context.getString(R.string.app_provider_version, versionString);
    }

    public static String anchor(String url, String text)
    {
        return "<a href=\"" + url + "\">" + text + "</a>";
    }

    protected static String smallText(String text)
    {
        return "<small>" + text + "</small>";
    }

    public String htmlVersionString()
    {
        String buildString = anchor(COMMIT_URL + BuildConfig.GIT_HASH, BuildConfig.GIT_HASH);
        String versionString = anchor(CHANGELOG_URL, BuildConfig.VERSION_NAME) + " " + smallText("(" + buildString + ")");
        if (BuildConfig.DEBUG)
        {
            versionString += " " + smallText("[" + BuildConfig.BUILD_TYPE + "]");
        }
        return getString(R.string.app_version, versionString);
    }

    protected void openLink(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        Activity activity = getActivity();
        if (activity != null && intent.resolveActivity(activity.getPackageManager()) != null)
        {
            startActivity(intent);
        }
    }

    private static String initTranslationCredits(Context context)
    {
        final String[] localeValues = context.getResources().getStringArray(R.array.locale_values);
        final String[] localeCredits = context.getResources().getStringArray(R.array.locale_credits);
        final String[] localeDisplay = context.getResources().getStringArray(R.array.locale_display);

        final String currentLanguage = Locale.getDefault().getLanguage();
        Integer[] index = new Integer[localeDisplay.length];    // sort alphabetical (localized)
        for (int i=0; i < index.length; i++) {
            index[i] = i;
        }
        Arrays.sort(index, new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                if (localeValues[i1].startsWith(currentLanguage)) {
                    return -1;
                } else if (localeValues[i2].startsWith(currentLanguage)) {
                    return 1;
                } else return localeDisplay[i1].compareTo(localeDisplay[i2]);
            }
        });

        StringBuilder credits = new StringBuilder();
        for (int i=0; i<index.length; i++)
        {
            int j = index[i];

            String localeCredits_j = (localeCredits.length > j ? localeCredits[j] : "");
            if (!localeCredits[j].isEmpty())
            {
                String localeDisplay_j = (localeDisplay.length > j ? localeDisplay[j] : localeValues[j]);
                String[] authorList = localeCredits_j.split("\\|");

                String authors = "";
                if (authorList.length < 2) {
                    authors = authorList[0];

                } else if (authorList.length == 2) {
                    authors = context.getString(R.string.authorListFormat_n, authorList[0], authorList[1]);

                } else {
                    for (int k=0; k<authorList.length-1; k++)
                    {
                        if (authors.isEmpty())
                            authors = authorList[k];
                        else authors = context.getString(R.string.authorListFormat_i, authors, authorList[k]);
                    }
                    authors = context.getString(R.string.authorListFormat_n, authors, authorList[authorList.length-1]);
                }

                String line = context.getString(R.string.translationCreditsFormat, localeDisplay_j, authors);
                if (i != index.length-1) {
                    if (!line.endsWith("<br/>") && !line.endsWith("<br />"))
                        line = line + "<br/>";
                }
                credits.append(line);
            }
        }
        return credits.toString();
    }

}
