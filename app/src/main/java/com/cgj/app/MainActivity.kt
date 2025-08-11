package com.cgj.app

import android.os.Build
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.CookieManager
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import kotlinx.coroutines.withContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.fillMaxWidth

val android.content.Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val USE_GREEN_THEME = booleanPreferencesKey("use_green_theme")
private const val GRADES_BASE_URL = "https://www.c-g-j.de"
private const val GRADES_LIST_URL = "$GRADES_BASE_URL/aktuelles-und-termine/leistungsnachweise/"
private const val SUBSTITUTION_BASE_URL = "https://www.c-g-j.de"
private const val SUBSTITUTION_PAGE_URL = "$SUBSTITUTION_BASE_URL/aktuelles-und-termine/vertretungsplan/"
private const val SUBSTITUTION_IMAGE_URL = "$SUBSTITUTION_PAGE_URL/vplan.png"
private var currentGradesPdfUrl: String? = null
private var currentSubstitutionPdfUrl: String? = null

// Funktion zum Extrahieren der PDF-URL für Leistungsnachweise
private suspend fun fetchGradesPdfUrl(): String? {
    return withContext(Dispatchers.IO) {
        try {
            val connection = URL(GRADES_LIST_URL).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val content = connection.inputStream.bufferedReader().use { it.readText() }
            
            // Regex zum Finden des PDF-Links
            val regex = """<a href="(/asset/[^"]+/leistungsnachweise-stand[^"]+\.pdf)">""".toRegex()
            val match = regex.find(content)
            
            match?.groupValues?.get(1)?.let { pdfPath ->
                "$GRADES_BASE_URL$pdfPath"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// Funktion zum Extrahieren der Vertretungsplan-PDF-URL
private suspend fun fetchSubstitutionPdfUrl(): String? {
    return withContext(Dispatchers.IO) {
        try {
            val connection = URL(SUBSTITUTION_PAGE_URL).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val content = connection.inputStream.bufferedReader().use { it.readText() }
            
            // Regex zum Finden des PDF-Links für Vertretungsplan
            val regex = """<a href="(/asset/[^"]+/vertretungsplan[^"]*\.pdf)">""".toRegex()
            val match = regex.find(content)
            
            match?.groupValues?.get(1)?.let { pdfPath ->
                "$SUBSTITUTION_BASE_URL$pdfPath"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// Funktion zum Öffnen der Moodle-App oder Weiterleitung zum Store
private fun openMoodleApp(context: Context) {
    val packageName = "com.moodle.moodlemobile"
    
    try {
        // Prüfe ob die Moodle-App installiert ist
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        
        if (intent != null) {
            // App ist installiert - öffne sie
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            // App ist nicht installiert - öffne Store
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            try {
                context.startActivity(storeIntent)
            } catch (e: Exception) {
                // Falls Google Play Store nicht verfügbar ist, öffne Browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(browserIntent)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback: Öffne Moodle im Browser
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://moodle.jsp.jena.de"))
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(browserIntent)
    }
}

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

private typealias TabBackHandler = () -> Boolean

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
    val activity = LocalContext.current as? android.app.Activity

    // Track previous tab to animate direction
    var lastSelectedTab by remember { mutableStateOf(0) }

    // Child-provided back handler (e.g., WebView back)
    val registeredTabBackHandler = remember { mutableStateOf<TabBackHandler?>(null) }

    fun handleBack(): Boolean {
        registeredTabBackHandler.value?.let { if (it()) return true }
        return if (selectedTab != 0) {
            lastSelectedTab = selectedTab
            selectedTab = 0
            true
        } else {
            activity?.finish()
            true
        }
    }

    BackHandler(enabled = true) {
        handleBack()
    }

    val tabs = listOf(
        TabItem("Vertretung", R.drawable.ic_substitution),
        TabItem("Essen", R.drawable.ic_food),
        TabItem("Moodle", R.drawable.ic_moodle),
        TabItem("Leistungen", R.drawable.ic_grades)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
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
                        // Reload Button für alle Screens (außer Moodle)
                        if (selectedTab != 2) { // Moodle-Tab überspringen
                            IconButton(onClick = {
                                when (selectedTab) {
                                    0 -> substitutionReloadTrigger.value = !substitutionReloadTrigger.value
                                    1 -> foodReloadTrigger.value = !foodReloadTrigger.value
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
                        }

                        // Download Buttons
                        if (selectedTab == 0) {
                            // Download für Vertretungsplan (PDF oder Bild)
                            IconButton(onClick = {
                                val downloadUrl = currentSubstitutionPdfUrl ?: SUBSTITUTION_IMAGE_URL
                                val fileName = if (currentSubstitutionPdfUrl != null) "Vertretungsplan.pdf" else "Vertretungsplan.png"
                                val title = if (currentSubstitutionPdfUrl != null) "Vertretungsplan.pdf" else "Vertretungsplan.png"
                                
                                val request = DownloadManager.Request(Uri.parse(downloadUrl))
                                    .setTitle(title)
                                    .setDescription("CGJ Vertretungsplan wird heruntergeladen")
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                                    .setAllowedOverMetered(true)
                                    .setAllowedOverRoaming(true)

                                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                downloadManager.enqueue(request)
                            }) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_menu_save),
                                    contentDescription = if (currentSubstitutionPdfUrl != null) "PDF herunterladen" else "Bild herunterladen"
                                )
                            }
                        } else if (selectedTab == 3 && selectedGradesTab == 1) {
                            // Download für Leistungsnachweise (immer verfügbar)
                            IconButton(onClick = {
                                currentGradesPdfUrl?.let { pdfUrl ->
                                    val request = DownloadManager.Request(Uri.parse(pdfUrl))
                                        .setTitle("Leistungsnachweise.pdf")
                                        .setDescription("CGJ Leistungsnachweise wird heruntergeladen")
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Leistungsnachweise.pdf")
                                        .setAllowedOverMetered(true)
                                        .setAllowedOverRoaming(true)

                                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                    downloadManager.enqueue(request)
                                } ?: run {
                                    // Fallback: Versuche die PDF-URL neu zu laden
                                    gradesPdfReloadTrigger.value = !gradesPdfReloadTrigger.value
                                }
                            }) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_menu_save),
                                    contentDescription = if (currentGradesPdfUrl != null) "PDF herunterladen" else "PDF neu laden"
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
                
                // Grades Tab Row (animated visibility)
                AnimatedVisibility(
                    visible = selectedTab == 3,
                    enter = slideInVertically(initialOffsetY = { -it / 2 }, animationSpec = tween(350)) + fadeIn(tween(350)),
                    exit = slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = tween(250)) + fadeOut(tween(250))
                ) {
                    TabRow(
                        selectedTabIndex = selectedGradesTab,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedGradesTab == 0,
                            onClick = { selectedGradesTab = 0 },
                            text = { Text("Home.InfoPoint") },
                            modifier = Modifier.weight(1f)
                        )
                        Tab(
                            selected = selectedGradesTab == 1,
                            onClick = { selectedGradesTab = 1 },
                            text = { Text("Leistungsnachweise") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected by remember(selectedTab) { derivedStateOf { selectedTab == index } }
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1.0f,
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                        label = "iconScale"
                    )
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            lastSelectedTab = selectedTab
                            selectedTab = index
                        },
                        icon = { 
                            Icon(
                                painter = painterResource(tab.iconRes), 
                                contentDescription = tab.title,
                                tint = if (isSelected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.scale(iconScale)
                            ) 
                        },
                        label = { 
                            Text(
                                tab.title,
                                color = if (isSelected) 
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
        val targetTab = selectedTab
        AnimatedContent(
            targetState = targetTab,
            transitionSpec = {
                val toLeft = (targetState > initialState)
                val slideIn = slideInHorizontally(
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    initialOffsetX = { if (toLeft) it else -it }
                ) + fadeIn(animationSpec = tween(200))
                val slideOut = slideOutHorizontally(
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    targetOffsetX = { if (toLeft) -it / 2 else it / 2 }
                ) + fadeOut(animationSpec = tween(200))
                slideIn togetherWith slideOut
            },
            label = "TabAnimatedContent"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> SubstitutionScreen(Modifier.padding(innerPadding))
                1 -> FoodScreen(
                    modifier = Modifier.padding(innerPadding),
                    onRegisterBackHandler = { handler -> registeredTabBackHandler.value = handler }
                )
                2 -> MoodleScreen(Modifier.padding(innerPadding))
                3 -> GradesScreen(
                    modifier = Modifier.padding(innerPadding),
                    selectedGradesTab = selectedGradesTab,
                    onRegisterBackHandler = { handler -> registeredTabBackHandler.value = handler }
                )
            }
        }
    }
}

data class TabItem(val title: String, val iconRes: Int)

// Reload Trigger für jeden Screen
private val substitutionReloadTrigger = mutableStateOf(false)
private val foodReloadTrigger = mutableStateOf(false)
private val homeInfoPointReloadTrigger = mutableStateOf(false)
private val gradesPdfReloadTrigger = mutableStateOf(false)

@Composable
fun SubstitutionScreen(modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }
    var pdfUrl by remember { mutableStateOf<String?>(currentSubstitutionPdfUrl) }
    var showImage by remember { mutableStateOf(false) }
    val reloadTrigger by substitutionReloadTrigger
    val coroutineScope = rememberCoroutineScope()
    
    // Effekt zum Laden der PDF-URL beim ersten Start und bei Reload
    LaunchedEffect(Unit) {
        isLoading = true
        showImage = false
        coroutineScope.launch {
            pdfUrl = fetchSubstitutionPdfUrl()
            currentSubstitutionPdfUrl = pdfUrl
            if (pdfUrl == null) {
                showImage = true
            }
            isLoading = false
        }
    }
    
    LaunchedEffect(reloadTrigger) {
        isLoading = true
        showImage = false
        coroutineScope.launch {
            pdfUrl = fetchSubstitutionPdfUrl()
            currentSubstitutionPdfUrl = pdfUrl
            if (pdfUrl == null) {
                showImage = true
            }
            isLoading = false
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        if (pdfUrl != null && !showImage) {
            // PDF anzeigen
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PDFView(ctx, null).apply {
                        loadPdf(pdfUrl!!) { isLoading = false }
                    }
                },
                update = { view ->
                    if (reloadTrigger) {
                        isLoading = true
                        view.loadPdf(pdfUrl!!) { isLoading = false }
                    }
                }
            )
        } else if (showImage) {
            // Bild anzeigen
            AsyncImage(
                model = SUBSTITUTION_IMAGE_URL,
                contentDescription = "Vertretungsplan",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                onSuccess = { isLoading = false },
                onError = { isLoading = false }
            )
        }
        
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
fun FoodScreen(modifier: Modifier = Modifier, onRegisterBackHandler: (TabBackHandler?) -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    val reloadTrigger by foodReloadTrigger
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onRegisterBackHandler {
            val view = webViewRef.value
            if (view != null && view.canGoBack()) {
                view.goBack()
                true
            } else {
                false
            }
        }
        onDispose { onRegisterBackHandler(null) }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewRef.value = this
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
    val context = LocalContext.current
    
    // Automatische Weiterleitung zur Moodle-App beim ersten Laden
    LaunchedEffect(Unit) {
        openMoodleApp(context)
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Weiterleitung zur Moodle-App...",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GradesScreen(
    modifier: Modifier = Modifier,
    selectedGradesTab: Int,
    onRegisterBackHandler: (TabBackHandler?) -> Unit
) {
    when (selectedGradesTab) {
        0 -> HomeInfoPointScreen(modifier, onRegisterBackHandler)
        1 -> GradesPdfScreen(modifier)
    }
}

@Composable
fun HomeInfoPointScreen(modifier: Modifier = Modifier, onRegisterBackHandler: (TabBackHandler?) -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    val reloadTrigger by homeInfoPointReloadTrigger
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onRegisterBackHandler {
            val view = webViewRef.value
            if (view != null && view.canGoBack()) {
                view.goBack()
                true
            } else {
                false
            }
        }
        onDispose { onRegisterBackHandler(null) }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewRef.value = this
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
    var pdfUrl by remember { mutableStateOf<String?>(currentGradesPdfUrl) }
    val coroutineScope = rememberCoroutineScope()
    
    // Effekt zum Laden der PDF-URL beim ersten Start und bei Reload
    LaunchedEffect(Unit) {
        isLoading = true
        coroutineScope.launch {
            pdfUrl = fetchGradesPdfUrl()
            currentGradesPdfUrl = pdfUrl
            isLoading = false
        }
    }
    
    LaunchedEffect(reloadTrigger) {
        isLoading = true
        coroutineScope.launch {
            pdfUrl = fetchGradesPdfUrl()
            currentGradesPdfUrl = pdfUrl
            isLoading = false
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        pdfUrl?.let { url ->
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PDFView(ctx, null).apply {
                        loadPdf(url) { isLoading = false }
                    }
                },
                update = { view ->
                    if (reloadTrigger) {
                        isLoading = true
                        view.loadPdf(url) { isLoading = false }
                    }
                }
            )
        } ?: run {
            // Zeige Fehlermeldung wenn PDF-URL nicht gefunden wurde
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "PDF konnte nicht geladen werden",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Versuchen Sie es erneut mit dem Reload-Button",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
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