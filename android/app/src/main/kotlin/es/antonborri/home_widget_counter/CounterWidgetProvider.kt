package es.antonborri.home_widget_counter

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetBackgroundIntent
import es.antonborri.home_widget.HomeWidgetProvider

class CounterWidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray,
            widgetData: SharedPreferences) {
        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.counter_widget).apply {
                val count = widgetData.getInt("counter", 0)
                setTextViewText(R.id.text_counter, count.toString())

                val incrementIntent = HomeWidgetBackgroundIntent.getBroadcast(
                        context,
                        Uri.parse("homeWidgetCounter://increment")
                )
                val clearIntent = HomeWidgetBackgroundIntent.getBroadcast(
                        context,
                        Uri.parse("homeWidgetCounter://clear")
                )

                setOnClickPendingIntent(R.id.button_increment, incrementIntent)
                setOnClickPendingIntent(R.id.button_clear, clearIntent)
            }
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
//
//
//class QuoteWidgetProvider : AppWidgetProvider() {
//
//    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
//        // Loop through each widget instance and update its configuration
//        for (appWidgetId in appWidgetIds) {
//            // Fetch the widget-specific font size from SharedPreferences
//            val prefs = context.getSharedPreferences("widget_prefs_$appWidgetId", Context.MODE_PRIVATE)
//            val fontSize = prefs.getInt("fontSize", 24)  // Default font size is 24 if not set
//
//            // Create RemoteViews to update the widget
//            val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
//
//            // Set the font size dynamically based on shared preferences
//            remoteViews.setTextViewTextSize(R.id.text_quotes, TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
//
//            // Update the widget with the new layout
//            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
//        }
//    }
//
//
//}
