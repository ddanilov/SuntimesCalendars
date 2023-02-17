/**
    Copyright (C) 2018-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.calendar.task.calendars;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTask;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;

import java.util.ArrayList;

@SuppressWarnings("Convert2Diamond")
public class TwilightCalendarCivil extends TwilightCalendarBase implements SuntimesCalendar
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapter.CALENDAR_TWILIGHT_CIVIL;
    private static final int resID_calendarTitle = R.string.calendar_civil_twilight_displayName;
    private static final int resID_calendarSummary = R.string.calendar_civil_twilight_summary;

    @Override
    public String calendarName() {
        return CALENDAR_NAME;
    }

    @Override
    public CalendarEventTemplate defaultTemplate() {
        return new CalendarEventTemplate("%cal", "%M @ %loc", "%loc");
    }

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettings settings)
    {
        super.init(context, settings);
        calendarTitle = context.getString(resID_calendarTitle);
        calendarSummary = context.getString(resID_calendarSummary);
        calendarDesc = null;
        calendarColor = settings.loadPrefCalendarColor(context, calendarName());
    }

    @Override
    public boolean initCalendar(@NonNull SuntimesCalendarSettings settings, @NonNull SuntimesCalendarAdapter adapter, @NonNull SuntimesCalendarTask task, @NonNull SuntimesCalendarTaskProgress progress0, @NonNull long[] window)
    {
        if (task.isCancelled()) {
            return false;
        }

        String calendarName = calendarName();
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColor);
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + window[0] + "-" + window[1]);
                String[] projection = new String[] { CalculatorProviderContract.COLUMN_SUN_CIVIL_RISE, CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE,
                        CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET, CalculatorProviderContract.COLUMN_SUN_CIVIL_SET };   // 0, 1, 2, 3 .. expected order: civil, sunrise, sunset, civil
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    String[] location = task.getLocation();
                    new SuntimesCalendarSettings().saveCalendarNote(context, calendarName, SuntimesCalendarSettings.NOTE_LOCATION_NAME, location[0]);

                    int c = 0;
                    String progressTitle = context.getString(R.string.summarylist_format, calendarTitle, location[0]);
                    int totalProgress = cursor.getCount();
                    SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(c, totalProgress, progressTitle);
                    task.publishProgress(progress0, progress);

                    String[] strings = SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendarName, defaultStrings()).getValues();
                    //0:s_SUNRISE, 1:s_SUNSET, 2:s_CIVIL_TWILIGHT, 3:s_NAUTICAL_TWILIGHT, 4:s_ASTRO_TWILIGHT, 5:s_POLAR_TWILIGHT, 6:s_CIVIL_NIGHT, 7:s_NAUTICAL_NIGHT, 8:s_DAWN_TWILIGHT, 9:s_DUSK_TWILIGHT, 10:s_WHITE_NIGHT

                    CalendarEventTemplate template = SuntimesCalendarSettings.loadPrefCalendarTemplate(context, calendarName, defaultTemplate());
                    ContentValues data = TemplatePatterns.createContentValues(null, this);
                    data = TemplatePatterns.createContentValues(data, task.getLocation());

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !task.isCancelled())
                    {
                        createSunCalendarEvent(context, adapter, task, eventValues, calendarID, cursor, 0, template, data, strings[0], strings[5], strings[2]);    // sunrise, polar twilight, civil twilight
                        createSunCalendarEvent(context, adapter, task, eventValues, calendarID, cursor, 2, template, data, strings[1], strings[10], strings[2]);   // sunset, white night, civil twilight
                        cursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || cursor.isLast()) {
                            adapter.createCalendarEvents( eventValues.toArray(new ContentValues[0]) );
                            eventValues.clear();
                        }
                        if (c % 8 == 0 || cursor.isLast()) {
                            progress.setProgress(c, totalProgress, progressTitle);
                            task.publishProgress(progress0, progress);
                        }
                    }
                    cursor.close();
                    createCalendarReminders(context, task, progress0);
                    return !task.isCancelled();

                } else {
                    lastError = "Failed to resolve URI! " + uri;
                    Log.e(getClass().getSimpleName(), lastError);
                    return false;
                }

            } else {
                lastError = "Unable to getContentResolver! ";
                Log.e(getClass().getSimpleName(), lastError);
                return false;
            }
        } else return false;
    }

}
