package com.csian.travella

import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.webkit.WebView
import android.webkit.WebViewClient


class TotalBudgetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_total_budget)

        // Handle window insets to avoid overlapping with system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the WebView
        val webView: WebView = findViewById(R.id.chartWebView)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d("WebViewConsole", consoleMessage?.message() ?: "No message")
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Log.e("WebViewError", "Error: $description")
            }
        }


        // Define the chart data and options in HTML
        val htmlContent = """
    <html>
    <head>
        <script src="https://cdn.anychart.com/releases/v8/js/anychart-bundle.min.js"></script>
        <link rel="stylesheet" href="https://cdn.anychart.com/releases/v8/css/anychart-ui.min.css">
        <link rel="stylesheet" href="https://cdn.anychart.com/releases/v8/fonts/css/anychart-font.css">
        <style>
            html, body, #container {
                width: 100%;
                height: 100%;
                margin: 0;
                padding: 0;
            }
        </style>
    </head>
    <body>
        <div id="container" style="width: 100%; height: 100%;"></div>
        <script>
            anychart.onDocumentReady(function() {
                // create pie chart
                var chart = anychart.pie([
                // set the category and value here
                    {x: 'Transport', value: 200},
                    {x: 'Food', value: 500},
                    {x: 'Entertainment', value: 380},
                    {x: 'Accommodation', value: 700},
                    {x: 'Others', value: 250}
                ]);
                
                // Customize chart to have a ring style
                chart.innerRadius("60%");

                // set chart title
                chart.title('Top 10 Cosmetic Products by Revenue');

                // set container id for the chart
                chart.container('container');

                // initiate chart drawing
                chart.draw();
                
                console.log('Pie chart drawn successfully');
            });
        </script>
    </body>
    </html>
"""

// Load the HTML content into the WebView
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)

    }

    private fun enableEdgeToEdge() {
        // Implementation for enabling edge-to-edge mode, if needed
    }
}