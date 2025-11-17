package com.seuusername.meuacessor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.seuusername.meuacessor.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeuAcessorApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeuAcessorApp() {
    var useDarkTheme by remember { mutableStateOf(true) }

    MeuAcessorTheme(darkTheme = useDarkTheme) {
        val navController = rememberNavController()
        val destinations = remember { AppDestination.entries.filter { it.route != AppDestination.Overview.route } }
        val currentDestination by navController.currentBackStackEntryAsState()
        val selectedRoute = currentDestination?.destination?.route ?: AppDestination.Overview.route
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        val blurAmount by animateDpAsState(targetValue = if (drawerState.isOpen) 16.dp else 0.dp, label = "blurAnimation")

        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(drawerContainerColor = Color.Transparent) {
                        AppSidebar(
                            destinations = destinations,
                            selectedRoute = selectedRoute,
                            onDestinationSelected = { destination ->
                                scope.launch { drawerState.close() }
                                if (selectedRoute != destination.route) {
                                    navController.navigate(destination.route) { popUpTo(AppDestination.Overview.route) }
                                }
                            },
                            isDarkTheme = useDarkTheme,
                            onThemeToggle = { useDarkTheme = !useDarkTheme }
                        )
                    }
                },
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
            ) {
                AppContentArea(
                    navController = navController,
                    modifier = Modifier.blur(radius = blurAmount)
                ) {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        scope.launch { drawerState.open() }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppContentArea(navController: NavHostController, modifier: Modifier = Modifier, onMenuClick: () -> Unit) {
    NavHost(navController = navController, startDestination = AppDestination.Overview.route, modifier = modifier) {
        composable(AppDestination.Overview.route) { OverviewScreen(onMenuClick = onMenuClick, onModuleSelected = { navController.navigate(it.route) }) }
        composable(AppDestination.Chat.route) { ChatScreen(onBackClick = { navController.popBackStack() }) }
        composable(AppDestination.Tasks.route) { TasksScreen(onBackClick = { navController.popBackStack() }) }
        composable(AppDestination.Finances.route) { FinancesScreen(onBackClick = { navController.popBackStack() }) }
        composable(AppDestination.Diary.route) { DiaryScreen(onBackClick = { navController.popBackStack() }) }
    }
}

@Composable
private fun AppSidebar(
    destinations: List<AppDestination>,
    selectedRoute: String,
    onDestinationSelected: (AppDestination) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)).safeDrawingPadding().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("Meu Acessor", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                destinations.forEach { destination ->
                    val isSelected = selectedRoute == destination.route
                    NavigationDrawerItem(
                        label = { Text(destination.title) },
                        selected = isSelected,
                        onClick = { onDestinationSelected(destination) },
                        icon = { Icon(destination.icon, destination.title) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tema Escuro", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isDarkTheme, onCheckedChange = { onThemeToggle() })
            }
            NavigationDrawerItem(label = { Text("Sair") }, selected = false, onClick = { /* TODO */ }, icon = { Icon(Icons.AutoMirrored.Rounded.Logout, null) }, modifier = Modifier.clip(RoundedCornerShape(16.dp)))
        }
    }
}

// --- Screens (UI Focus) ---

@Composable
fun OverviewScreen(onMenuClick: () -> Unit, onModuleSelected: (AppDestination) -> Unit) {
    val modules = remember { AppDestination.entries.filter { it.isModule } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HomeHeader(onMenuClick) }
        item { DailyScoreCard() }
        item { Text("Módulos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
        items(modules.size / 2 + modules.size % 2) { rowIndex ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val firstIndex = rowIndex * 2
                Box(modifier = Modifier.weight(1f)) {
                    ModuleCard(destination = modules[firstIndex], onClick = { onModuleSelected(modules[firstIndex]) })
                }
                val secondIndex = firstIndex + 1
                if (secondIndex < modules.size) {
                    Box(modifier = Modifier.weight(1f)) {
                        ModuleCard(destination = modules[secondIndex], onClick = { onModuleSelected(modules[secondIndex]) })
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun HomeHeader(onMenuClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("Bem-vindo de volta,", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text("Jonas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = onMenuClick) { Icon(Icons.Rounded.Menu, contentDescription = "Abrir Menu", tint = MaterialTheme.colorScheme.onSurface) }
    }
}

@Composable
fun DailyScoreCard() {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Pontuação Diária", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimary)
                Text("4/7 tarefas completas", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { 0.9f }, modifier = Modifier.size(50.dp), color = MaterialTheme.colorScheme.onPrimary, trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f), strokeWidth = 5.dp)
                Text("90%", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ModuleCard(destination: AppDestination, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(destination.icon, contentDescription = destination.title, tint = destination.accentColor, modifier = Modifier.size(32.dp))
            Text(destination.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

// --- Chat Screen (Mocked) ---

data class ChatMessage(val content: String, val isFromAssistant: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onBackClick: () -> Unit) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf(ChatMessage("Olá! Como posso te ajudar?", true)) }
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    if (showSettingsDialog) { SettingsDialog(onDismiss = { showSettingsDialog = false }) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chat IA") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }, actions = { IconButton(onClick = { showSettingsDialog = true }) { Icon(Icons.Rounded.Settings, null) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) },
        bottomBar = {
            ChatInputBar(
                text = inputText, onTextChange = { inputText = it },
                onSendClick = {
                    if (inputText.isNotBlank()) {
                        val userMessage = inputText
                        messages.add(0, ChatMessage(userMessage, isFromAssistant = false))
                        inputText = ""
                        isLoading = true
                        scope.launch { delay(1500); messages.add(0, ChatMessage("Esta é uma resposta de exemplo para: '$userMessage'", true)); isLoading = false }
                    }
                },
                isLoading = isLoading
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), reverseLayout = true, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(messages) { message -> ChatMessageItem(message = message) }
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val backgroundColor = if (message.isFromAssistant) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (message.isFromAssistant) Arrangement.Start else Arrangement.End) {
        Surface(shape = RoundedCornerShape(20.dp), color = backgroundColor, modifier = Modifier.widthIn(max = 300.dp)) {
            Text(text = message.content, modifier = Modifier.padding(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar(text: String, onTextChange: (String) -> Unit, onSendClick: () -> Unit, isLoading: Boolean) {
    Surface(modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding(), shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = text, onValueChange = onTextChange, placeholder = { Text("Pergunte qualquer coisa...") }, modifier = Modifier.weight(1f), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), enabled = !isLoading)
            if (isLoading) { CircularProgressIndicator(modifier = Modifier.padding(horizontal = 12.dp).size(24.dp)) } else { IconButton(onClick = onSendClick, enabled = text.isNotBlank()) { Icon(Icons.Rounded.Send, "Enviar", tint = MaterialTheme.colorScheme.primary) } }
        }
    }
}

@Composable
private fun SettingsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Configurações", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Gemini API Key (Mock)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "gemini-1.5-flash-latest", onValueChange = {}, label = { Text("Modelo (Mock)") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onDismiss) { Text("Fechar") } }
            }
        }
    }
}

// --- Other Placeholder Screens ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(onBackClick: () -> Unit) {
    val tasks = remember { mutableStateListOf(TaskItemData(1, "Comprar leite e pão", true), TaskItemData(2, "Estudar Jetpack Compose", false)) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Minhas Tarefas") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(tasks) { task -> TaskItem(task = task, onClick = { task.isCompleted = !task.isCompleted }) }
        }
    }
}

data class TaskItemData(val id: Int, val text: String, var isCompleted: Boolean)

@Composable
fun TaskItem(task: TaskItemData, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = task.isCompleted, onCheckedChange = { onClick() }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary, checkmarkColor = MaterialTheme.colorScheme.surface))
        Spacer(Modifier.width(16.dp))
        Text(task.text, textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null, color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Finanças") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) },
        containerColor = MaterialTheme.colorScheme.background
    ) { Box(modifier = Modifier.fillMaxSize().padding(it), contentAlignment = Alignment.Center) { Text("Finances Screen") } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Diário") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) },
        containerColor = MaterialTheme.colorScheme.background
    ) { Box(modifier = Modifier.fillMaxSize().padding(it), contentAlignment = Alignment.Center) { Text("Diary Screen") } }
}

// --- App Navigation Structure ---

enum class AppDestination(val route: String, val title: String, val icon: ImageVector, val accentColor: Color, val isModule: Boolean = false) {
    Overview("overview", "Visão Geral", Icons.Rounded.Dashboard, Color.Transparent),
    Chat("chat", "Chat IA", Icons.AutoMirrored.Rounded.Chat, AccentGreen, isModule = true),
    Tasks("tasks", "Tarefas", Icons.Rounded.CheckCircle, AccentPurple, isModule = true),
    Finances("finances", "Finanças", Icons.Rounded.Money, AccentBlue, isModule = true),
    Diary("diary", "Diário", Icons.Rounded.AutoStories, AccentOrange, isModule = true)
}
