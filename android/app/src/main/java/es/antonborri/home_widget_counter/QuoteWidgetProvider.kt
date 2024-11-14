package es.antonborri.home_widget_counter

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetBackgroundIntent
import es.antonborri.home_widget.HomeWidgetProvider

class QuoteWidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences
    ) {
        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.quote_widget).apply {
                // Retrieve the saved quote from SharedPreferences
                val quote = widgetData.getString("quote", "Fetching quote...") ?: "Fetching quote..."
                setTextViewText(R.id.text_quotes, quote)

                // Create an intent for fetching a new quote
                val fetchQuoteIntent = HomeWidgetBackgroundIntent.getBroadcast(
                    context,
                    Uri.parse("homeWidgetQuote://fetchQuote")
                )

                // Set onClick to fetch a new quote
                setOnClickPendingIntent(R.id.button_next, fetchQuoteIntent)
            }
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}

