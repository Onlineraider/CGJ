package com.cgj.app

import android.os.Build
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.CookieManager
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.cgj.app.ui.theme.CGJTheme
import com.github.barteksc.pdfviewer.PDFView
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

val android.content.Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val USE_GREEN_THEME = booleanPreferencesKey("use_green_theme")
private const val SUBSTITUTION_PDF_URL = "https://www.c-g-j.de/asset/bKc51TObRB6ulM2yoIax1g/vertretungsplan.pdf"
private const val GRADES_PDF_URL = "https://www.c-g-j.de/asset/fP9-RyRHQ1qCnG-3opj34A/"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Enable Cookies and WebView Storage
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(WebView(this@MainActivity), true)
        }
        
        setContent {
            val useDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val useGreenTheme by dataStore.data
                .map { preferences ->
                    preferences[USE_GREEN_THEME] ?: !useDynamicColor
                }
                .collectAsState(initial = !useDynamicColor)
            
            CGJTheme(
                dynamicColor = !useGreenTheme
            ) {
                MainScreen(
                    useGreenTheme = useGreenTheme,
                    onThemeChange = { newValue ->
                        CoroutineScope(Dispatchers.IO).launch {
                            dataStore.edit { preferences ->
                                preferences[USE_GREEN_THEME] = newValue
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    useGreenTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var selectedGradesTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    
    val tabs = listOf(
        TabItem("Vertretung", R.drawable.ic_substitution),
        TabItem("Essen", R.drawable.ic_food),
        TabItem("Moodle", R.drawable.ic_moodle),
        TabItem("Leistungen", R.drawable.ic_grades)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "CGJ",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Reload Button für alle Screens
                    IconButton(onClick = {
                        when (selectedTab) {
                            0 -> substitutionReloadTrigger.value = !substitutionReloadTrigger.value
                            1 -> foodReloadTrigger.value = !foodReloadTrigger.value
                            2 -> moodleReloadTrigger.value = !moodleReloadTrigger.value
                            3 -> when (selectedGradesTab) {
                                0 -> homeInfoPointReloadTrigger.value = !homeInfoPointReloadTrigger.value
                                1 -> gradesPdfReloadTrigger.value = !gradesPdfReloadTrigger.value
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_rotate),
                            contentDescription = "Neu laden"
                        )
                    }

                    // Download Buttons wie bisher
                    if (selectedTab == 0) {
                        IconButton(onClick = {
                            val request = DownloadManager.Request(Uri.parse(SUBSTITUTION_PDF_URL))
                                .setTitle("Vertretungsplan.pdf")
                                .setDescription("CGJ Vertretungsplan wird heruntergeladen")
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Vertretungsplan.pdf")
                                .setAllowedOverMetered(true)
                                .setAllowedOverRoaming(true)

                            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            downloadManager.enqueue(request)
                        }) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_menu_save),
                                contentDescription = "PDF herunterladen"
                            )
                        }
                    } else if (selectedTab == 3 && selectedGradesTab == 1) {
                        IconButton(onClick = {
                            val request = DownloadManager.Request(Uri.parse(GRADES_PDF_URL))
                                .setTitle("Leistungsnachweise.pdf")
                                .setDescription("CGJ Leistungsnachweise wird heruntergeladen")
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Leistungsnachweise.pdf")
                                .setAllowedOverMetered(true)
                                .setAllowedOverRoaming(true)

                            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            downloadManager.enqueue(request)
                        }) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_menu_save),
                                contentDescription = "PDF herunterladen"
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more),
                                contentDescription = "Mehr"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (useGreenTheme) "System Farben" else "Grünes Theme") },
                                onClick = {
                                    onThemeChange(!useGreenTheme)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { 
                            Icon(
                                painter = painterResource(tab.iconRes), 
                                contentDescription = tab.title,
                                tint = if (selectedTab == index) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            ) 
                        },
                        label = { 
                            Text(
                                tab.title,
                                color = if (selectedTab == index) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> SubstitutionScreen(Modifier.padding(innerPadding))
            1 -> FoodScreen(Modifier.padding(innerPadding))
            2 -> MoodleScreen(Modifier.padding(innerPadding))
            3 -> GradesScreen(
                modifier = Modifier.padding(innerPadding),
                onTabSelected = { selectedGradesTab = it }
            )
        }
    }
}

data class TabItem(val title: String, val iconRes: Int)

// Reload Trigger für jeden Screen
private val substitutionReloadTrigger = mutableStateOf(false)
private val foodReloadTrigger = mutableStateOf(false)
private val moodleReloadTrigger = mutableStateOf(false)
private val homeInfoPointReloadTrigger = mutableStateOf(false)
private val gradesPdfReloadTrigger = mutableStateOf(false)

@Composable
fun SubstitutionScreen(modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }
    val reloadTrigger by substitutionReloadTrigger
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PDFView(ctx, null).apply {
                    loadPdf(SUBSTITUTION_PDF_URL) { isLoading = false }
                }
            },
            update = { view ->
                isLoading = true
                view.loadPdf(SUBSTITUTION_PDF_URL) { isLoading = false }
            }
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FoodScreen(modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }
    val reloadTrigger by foodReloadTrigger
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }
                    }
                    loadUrl("https://bestellung-ac.mpibs.de")
                }
            },
            update = { webView ->
                if (reloadTrigger) {
                    isLoading = true
                    webView.reload()
                }
            }
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MoodleScreen(modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }
    val reloadTrigger by moodleReloadTrigger
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }
                    }
                    loadUrl("https://moodle.jsp.jena.de")
                }
            },
            update = { webView ->
                if (reloadTrigger) {
                    isLoading = true
                    webView.reload()
                }
            }
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GradesScreen(
    modifier: Modifier = Modifier,
    onTabSelected: (Int) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { 
                    selectedTab = 0
                    onTabSelected(0)
                },
                text = { Text("Home.InfoPoint") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { 
                    selectedTab = 1
                    onTabSelected(1)
                },
                text = { Text("Leistungsnachweise") }
            )
        }
        
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> HomeInfoPointScreen()
                1 -> GradesPdfScreen()
            }
        }
    }
}

@Composable
fun HomeInfoPointScreen(modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }
    val reloadTrigger by homeInfoPointReloadTrigger
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }
                    }
                    loadUrl("https://internes.c-g-j.de/default.php")
                }
            },
            update = { webView ->
                if (reloadTrigger) {
                    isLoading = true
                    webView.reload()
                }
            }
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GradesPdfScreen(modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }
    val reloadTrigger by gradesPdfReloadTrigger
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PDFView(ctx, null).apply {
                    loadPdf(GRADES_PDF_URL) { isLoading = false }
                }
            },
            update = { view ->
                if (reloadTrigger) {
                    isLoading = true
                    view.loadPdf(GRADES_PDF_URL) { isLoading = false }
                }
            }
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Hilfsfunktion für PDFView
private fun PDFView.loadPdf(url: String, onLoadComplete: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            val inputStream = BufferedInputStream(connection.inputStream)
            launch(Dispatchers.Main) {
                fromStream(inputStream)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .onLoad {
                        onLoadComplete()
                    }
                    .load()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}