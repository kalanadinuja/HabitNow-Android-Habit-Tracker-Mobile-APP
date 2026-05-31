package com.example.madlab3.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.madlab3.R
import com.example.madlab3.activities.HabitActivity
import com.example.madlab3.utils.HabitManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Widget provider for displaying habit completion percentage on the home screen
 */
class HabitProgressWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget instance is created
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget instance is deleted
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Handle custom actions if needed
        if (intent.action == ACTION_WIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(intent.component)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_WIDGET_UPDATE = "com.example.madlab3.ACTION_WIDGET_UPDATE"
        
        /**
         * Update all active widgets
         */
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, HabitProgressWidget::class.java).apply {
                action = ACTION_WIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
        
        /**
         * Updates a single app widget with current habit progress
         */
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val habitManager = HabitManager(context)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // Get habits and progress data
            val habits = habitManager.getHabits()
            val progressList = habitManager.getHabitProgressForDate(today)
            
            // Calculate completion percentage
            val totalHabits = habits.size
            val completedHabits = progressList.count { it.isCompleted }
            val percentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
            
            // Create the widget views
            val views = RemoteViews(context.packageName, R.layout.widget_habit_progress)
            
            // Set progress
            views.setProgressBar(R.id.widgetProgressBar, 100, percentage, false)
            views.setTextViewText(
                R.id.tvWidgetProgress,
                context.getString(R.string.habit_progress_text, completedHabits, totalHabits)
            )
            
            // Create a pendingIntent to open the HabitActivity when widget is clicked
            val intent = Intent(context, HabitActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tvWidgetTitle, pendingIntent)
            
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}