/**
    Copyright (C) 2020-2022 Forrest Guice
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

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.CalendarEventFlags;
import com.forrestguice.suntimeswidget.calendar.CalendarEventStrings;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;
import com.forrestguice.suntimeswidget.calendar.ui.Utils;

import java.lang.ref.WeakReference;

@SuppressWarnings("Convert2Diamond")
public abstract class SuntimesCalendarBase implements SuntimesCalendar
{
    protected WeakReference<Context> contextRef = null;
    protected String calendarTitle, calendarSummary, calendarDesc;
    protected String defaultCalendarTitle;
    protected int calendarColor;
    protected String lastError;

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettings settings) {
        contextRef = new WeakReference<>(context);
        Utils.initDisplayStrings(context);
    }

    @Override
    public String lastError() {
        return lastError;
    }

    @Override
    public abstract String calendarName();

    @Override
    public String defaultCalendarTitle() {
        return defaultCalendarTitle;
    }

    @Override
    public abstract CalendarEventTemplate defaultTemplate();

    @Override
    public TemplatePatterns[] supportedPatterns()
    {
        return new TemplatePatterns[] {
                TemplatePatterns.pattern_event, TemplatePatterns.pattern_eZ, TemplatePatterns.pattern_eA, TemplatePatterns.pattern_eD, TemplatePatterns.pattern_eR, null,
                TemplatePatterns.pattern_illum, TemplatePatterns.pattern_dist, null,
                TemplatePatterns.pattern_loc, TemplatePatterns.pattern_lat, TemplatePatterns.pattern_lon, TemplatePatterns.pattern_lel, null,
                TemplatePatterns.pattern_cal, TemplatePatterns.pattern_summary, TemplatePatterns.pattern_color, TemplatePatterns.pattern_percent
        };
    }

    @Override
    public abstract CalendarEventStrings defaultStrings();

    @Override
    public abstract CalendarEventFlags defaultFlags();

    @Override
    public String flagLabel(int i) {
        return "";
    }

    @Override
    public String calendarTitle() {
        return calendarTitle;
    }

    @Override
    public String calendarSummary() {
        return calendarSummary;
    }

    @Override
    public int calendarColor() {
        return calendarColor;
    }

    public void createCalendarReminders(Context context, @NonNull SuntimesCalendarTaskInterface task, @NonNull SuntimesCalendarTaskProgress progress0) {
        progress0.setProgress(progress0.itemNum(), progress0.getCount(), progress0.getMessage() + "\n" + context.getString(R.string.reminder_dialog_msg));
        task.createCalendarReminders(context, calendarName(), progress0);
    }

    @Override
    public boolean initCalendar(@NonNull final SuntimesCalendarSettings settings, @NonNull final SuntimesCalendarAdapter adapter, @NonNull final SuntimesCalendarTaskInterface task, @NonNull final SuntimesCalendarTaskProgress progress0, @NonNull long[] window)
    {
        return initCalendar(settings, adapter, task, progress0, window, new CalendarInitializer()
        {
            @Override
            public long calendarID() {
                return adapter.queryCalendarID(calendarName());
            }

            @Override
            public boolean onStarted()
            {
                if (!adapter.hasCalendar(calendarName())) {
                    adapter.createCalendar(calendarName(), calendarTitle, calendarColor);
                    return (calendarID() != -1);
                }
                return false;
            }
            @Override
            public void processEventValues(ContentValues... values) {
                adapter.createCalendarEvents(values);
            }

            @Override
            public void onFinished() {
                createCalendarReminders(contextRef.get(), task, progress0);
            }
        });
    }

    @Override
    public long[] defaultWindow() {
        return new long[] {0, 0};
    }

}
