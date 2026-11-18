package com.example.shreegurudevclinic

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

class MainActivity : ComponentActivity() {

    // This variable is needed for uploading files (like X-rays)
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    // Register Activity Result API to handle file chooser
    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        val resultCode = result.resultCode
        val results = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
        filePathCallback?.onReceiveValue(results)
        filePathCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable Chrome DevTools inspection (chrome://inspect)
        WebView.setWebContentsDebuggingEnabled(true)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WebViewScreen()
                }
            }
        }
    }

    // This allows the web page to open the Android file picker for X-rays
    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun WebViewScreen() {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true

                    // Handle File Uploads (X-rays) and Log console messages
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(
                            webView: WebView?,
                            newFilePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            filePathCallback?.onReceiveValue(null)
                            filePathCallback = newFilePathCallback
                            val intent = fileChooserParams?.createIntent()
                            if (intent != null) {
                                try {
                                    fileChooserLauncher.launch(intent)
                                    return true
                                } catch (_: Exception) {
                                    filePathCallback = null
                                    return false
                                }
                            }
                            filePathCallback = null
                            return false
                        }

                        // Log JavaScript console messages to Logcat for easier debugging
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            Log.d("WebViewConsole", "${consoleMessage?.message()} -- From line " +
                                    "${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                            return true
                        }
                    }

                    // Map the root of the domain to the assets folder
                    // This is required because index.html uses root-relative paths like "/app.js"
                    val assetLoader = WebViewAssetLoader.Builder()
                        .setDomain("appassets.androidplatform.net")
                        .addPathHandler("/", WebViewAssetLoader.AssetsPathHandler(context))
                        .build()

                    webViewClient = object : WebViewClientCompat() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest
                        ): WebResourceResponse? {
                            return assetLoader.shouldInterceptRequest(request.url)
                        }
                    }

                    // Load index.html from the root domain
                    loadUrl("https://appassets.androidplatform.net/index.html")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
