package es.antonborri.home_widget_counter

import HomeWidgetGlanceState
import HomeWidgetGlanceStateDefinition
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class QuoteGlanceWidget : GlanceAppWidget() {
    override val stateDefinition = HomeWidgetGlanceStateDefinition()
    private var glanceId: GlanceId? = null
    private var isPeriodicUpdateStarted = false  // Flag to track if handler has started

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        glanceId = id
        if (!isPeriodicUpdateStarted) {
            // Introduce a slight delay to ensure widget initialization
            Handler(Looper.getMainLooper()).postDelayed({
                startPeriodicUpdate(context)
                isPeriodicUpdateStarted = true
            }, 5000)  // 5 second delay
        }
        provideContent {
            GlanceContent(context, currentState())
        }
    }

    private fun startPeriodicUpdate(context: Context) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                Log.d("Trig", "Triggered Fetch Quote")

                CoroutineScope(Dispatchers.IO).launch {
                    val newQuote = fetchAutoQuote()
                    Log.d("Fe", "Quotes Fetched")

                    val prefs = context.getSharedPreferences("home_widget_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("quote", newQuote).apply()

                    // Update widget only if glanceId is still valid
                    glanceId?.let { validId ->
                        QuoteGlanceWidget().update(context, validId)
                    }
                }

                handler.postDelayed(this, 1*60 * 1000)  // Update every 1 minute
            }
        }

        handler.post(runnable)
    }

    private fun fetchAutoQuote(): String {
        return try {
            val url = URL("https://staticapis.pragament.com/daily/quotes-en-gratitude.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val quotesArray = JSONObject(response).getJSONArray("quotes")
                val randomIndex = (0 until quotesArray.length()).random()
                val quoteObject = quotesArray.getJSONObject(randomIndex)
                quoteObject.getString("quote")
            } else {
                "Error fetching quote"
            }
        } catch (e: Exception) {
            "Error fetching quote"
        }
    }

    @Composable
    private fun GlanceContent(context: Context, currentState: HomeWidgetGlanceState) {
        val prefs = context.getSharedPreferences("home_widget_prefs", Context.MODE_PRIVATE)
        val quote = prefs.getString("quote", "Welcome to Gratitude Quotes")

        Box(
            modifier = GlanceModifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = "Daily Quotes: ",
                    style = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                )
                Text(
                    text = quote ?: "Loading...",
                    style = TextStyle(fontSize = 18.sp, textAlign = TextAlign.Center, fontStyle = FontStyle.Italic),
                )
                Spacer(GlanceModifier.defaultWeight())
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = GlanceModifier.run { clickable(onClick = actionRunCallback<FetchQuoteAction>()) }
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.baseline_add_24),
                            contentDescription = "Refresh",
                            colorFilter = ColorFilter.tint(ColorProvider(Color.Black)),
                            modifier = GlanceModifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}


class FetchQuoteAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val newQuote = fetchQuoteFromAPI()
        Log.d("Fe", "Quotes Fetched")
        // Save the new quote in SharedPreferences
        val prefs = context.getSharedPreferences("home_widget_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("quote", newQuote).apply()

        // Trigger the widget update
        QuoteGlanceWidget().update(context, glanceId)
    }

    private suspend fun fetchQuoteFromAPI(): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://staticapis.pragament.com/daily/quotes-en-gratitude.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val quotesArray = JSONObject(response).getJSONArray("quotes")
                val randomIndex = (0 until quotesArray.length()).random()
                val quoteObject = quotesArray.getJSONObject(randomIndex)
                return@withContext quoteObject.getString("quote")
            } else {
                return@withContext "Error fetching quote"
            }
        } catch (e: Exception) {
            return@withContext "Error fetching quote"
        }
    }
}



