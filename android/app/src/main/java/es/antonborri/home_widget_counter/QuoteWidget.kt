package es.antonborri.home_widget_counter

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import android.util.TypedValue
import es.antonborri.home_widget.HomeWidgetProvider
import android.content.SharedPreferences
import android.net.Uri
import es.antonborri.home_widget.HomeWidgetBackgroundIntent

/**
 * Implementation of App Widget functionality.
 */
class QuoteWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Retrieve SharedPreferences to manage other updates, excluding font size
        val prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)

        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.quote_widget).apply {
                // Retrieve the saved quote from SharedPreferences
                val quote = prefs.getString("quote", "Fetching quote...") ?: "Fetching quote..."
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

    override fun onEnabled(context: Context) {
        // This is called when the first instance of your widget is created
        val prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        val widgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, QuoteWidget::class.java)
        val widgetIds = widgetManager.getAppWidgetIds(componentName)

        if (widgetIds.isNotEmpty()) {
            widgetIds.forEach { widgetId ->
                // Generate a unique key using the widget ID
                val fontSizeKey = "flutter.fontSize"

                // Retrieve and set the font size
                val fontSize = prefs.getString(fontSizeKey, "2").toString()

                val views = RemoteViews(context.packageName, R.layout.quote_widget).apply {
                    setTextViewTextSize(R.id.text_quotes, TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
                }
                widgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    override fun onDisabled(context: Context) {
        // This is called when the last instance of your widget is removed
        super.onDisabled(context)
    }
}
