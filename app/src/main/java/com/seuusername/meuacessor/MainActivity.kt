package com.seuusername.meuacessor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.seuusername.meuacessor.data.DEFAULT_GEMINI_MODEL
import com.seuusername.meuacessor.data.GeminiModelService
import com.seuusername.meuacessor.data.GeminiPreferences
import com.seuusername.meuacessor.data.GeminiSettings
import com.seuusername.meuacessor.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeuAcessorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MeuAcessorApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeuAcessorApp() {
    val navController = rememberNavController()
    val destinations = remember { NectarDestinations.primary }
    val currentDestination by navController.currentBackStackEntryAsState()
    val selectedRoute = currentDestination?.destination?.route ?: AppDestination.Overview.route

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedLayout = maxWidth > 960.dp
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        val sidebar: @Composable () -> Unit = {
            NectarSidebar(
                destinations = destinations,
                selectedRoute = selectedRoute,
                onDestinationSelected = { destination ->
                    scope.launch { drawerState.close() }
                    if (selectedRoute != destination.route) {
                        navController.navigate(destination.route) {
                            launchSingleTop = true
                            popUpTo(AppDestination.Overview.route)
                        }
                    }
                },
                modifier = if (isExpandedLayout) Modifier else Modifier.width(280.dp)
            )
        }

        if (isExpandedLayout) {
            PermanentNavigationDrawer(drawerContent = sidebar) {
                NectarContentArea(navController, selectedRoute) { navController.popBackStack() }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = { ModalDrawerSheet { sidebar() } }
            ) {
                NectarContentArea(navController, selectedRoute) {
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
private fun NectarContentArea(
    navController: NavHostController,
    selectedRoute: String,
    onMenuClick: () -> Unit
) {
    NavHost(navController = navController, startDestination = AppDestination.Overview.route) {
        composable(AppDestination.Overview.route) {
            Scaffold(
                topBar = { CompactTopBar(onMenuClick = onMenuClick, showMenuIcon = navController.previousBackStackEntry == null) }
            ) { padding ->
                OverviewScreen(
                    modifier = Modifier.padding(padding),
                    modules = NectarDestinations.modules,
                    onModuleSelected = { navController.navigate(it.route) },
                    highlightRoute = selectedRoute
                )
            }
        }
        composable(AppDestination.Chat.route) { ChatScreen(onMenuClick) }
        composable(AppDestination.Tasks.route) { TasksScreen(onMenuClick) }
        composable(AppDestination.Finances.route) { FinancesPagerScreen(onMenuClick) }
        composable(AppDestination.Diary.route) { DiaryScreen(onMenuClick) }
    }
}

@Composable
private fun CompactTopBar(
    onMenuClick: () -> Unit,
    showMenuIcon: Boolean,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = if (showMenuIcon) Icons.Rounded.Widgets else Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = if (showMenuIcon) "Abrir menu" else "Voltar"
                )
            }
            Text(
                text = "Meu Acessor",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                actions()
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp).padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun NectarSidebar(
    destinations: List<AppDestination>,
    selectedRoute: String,
    onDestinationSelected: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = NectarSurface,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Meu Acessor",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "Seu assistente pessoal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    destinations.forEach { destination ->
                        val isSelected = selectedRoute == destination.route
                        NavigationDrawerItem(
                            label = { Text(destination.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            selected = isSelected,
                            onClick = { onDestinationSelected(destination) },
                            icon = { Icon(destination.icon, destination.title) },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = destination.accentColor.copy(alpha = 0.16f),
                                selectedIconColor = destination.accentColor,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.clip(RoundedCornerShape(24.dp))
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DrawerQuickAction(Icons.Rounded.Refresh, "Limpar Chat")
                DrawerQuickAction(Icons.AutoMirrored.Rounded.Logout, "Sair")
            }
        }
    }
}

@Composable
private fun DrawerQuickAction(icon: ImageVector, text: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val background by animateColorAsState(targetValue = if (isPressed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else Color.Transparent, label = "quick-action-bg")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(interactionSource, null) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, text, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp).size(22.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
}

@Composable
private fun OverviewScreen(
    modifier: Modifier = Modifier,
    modules: List<AppDestination>,
    onModuleSelected: (AppDestination) -> Unit,
    highlightRoute: String
) {
    Column(
        modifier = modifier.fillMaxSize().background(NectarBackground).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Olá! Eu sou o Meu Acessor ✨", style = MaterialTheme.typography.displayMedium)
                Text("Seu assistente inteligente para organizar a vida!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f))
            }
            Box(
                modifier = Modifier.size(68.dp).clip(CircleShape).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AccountCircle, null, tint = Color.White, modifier = Modifier.size(42.dp))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items(modules) { module ->
                ModuleCard(destination = module, isHighlighted = module.route == highlightRoute, onClick = { onModuleSelected(module) })
            }
        }
    }
}

@Composable
private fun ModuleCard(destination: AppDestination, isHighlighted: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "module-scale")
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 10.dp else 6.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier.background(Brush.verticalGradient(listOf(destination.accentColor.copy(alpha = 0.25f), Color.Transparent))).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(destination.accentColor.copy(alpha = 0.18f)), Alignment.Center) {
                Icon(destination.icon, destination.title, tint = destination.accentColor, modifier = Modifier.size(26.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(destination.title, style = MaterialTheme.typography.titleMedium)
                destination.highlights.take(3).forEach {
                    Text("• $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}

// --- Telas dos Módulos ---

data class ChatMessage(val content: String, val isFromAssistant: Boolean)

@Composable
private fun ChatScreen(onMenuClick: () -> Unit) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val settings by GeminiPreferences.observeSettings(context).collectAsState(initial = GeminiSettings())
    val resolvedModelName = settings.modelName.ifBlank { DEFAULT_GEMINI_MODEL }

    val messages = remember {
        mutableStateListOf(
            ChatMessage("Olá! Como posso ajudar você hoje? Para começar, insira sua chave de API nas configurações.", isFromAssistant = true)
        )
    }
    var inputText by remember { mutableStateOf("") }

    if (showSettingsDialog) {
        SettingsDialog(
            currentApiKey = settings.apiKey,
            currentModelName = resolvedModelName,
            onDismiss = { showSettingsDialog = false },
            onSave = { newApiKey, newModel ->
                scope.launch {
                    GeminiPreferences.saveSettings(
                        context = context,
                        apiKey = newApiKey.trim(),
                        modelName = newModel.ifBlank { DEFAULT_GEMINI_MODEL }
                    )
                    showSettingsDialog = false
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CompactTopBar(
                onMenuClick = onMenuClick, 
                showMenuIcon = false,
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Configurações")
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSendClick = {
                    if (inputText.isNotBlank() && !isLoading) {
                        val userMessage = inputText
                        messages.add(ChatMessage(userMessage, isFromAssistant = false))
                        inputText = ""
                        isLoading = true

                        scope.launch {
                            try {
                                val trimmedApiKey = settings.apiKey.trim()
                                if (trimmedApiKey.isBlank()) {
                                    messages.add(
                                        ChatMessage(
                                            "Por favor, insira sua chave de API do Gemini nas configurações.",
                                            isFromAssistant = true
                                        )
                                    )
                                    isLoading = false
                                    return@launch
                                }

                                val modelName = resolvedModelName
                                val assistantReply = GeminiModelService.generateContent(
                                    apiKey = trimmedApiKey,
                                    modelName = modelName,
                                    userMessage = userMessage
                                )

                                messages.add(ChatMessage(assistantReply, isFromAssistant = true))
                            } catch (e: Exception) {
                                val errorMessage = e.message ?: "Erro desconhecido"
                                val suggestion = if (errorMessage.contains("not found", ignoreCase = true)) {
                                    " Verifique o nome do modelo nas configurações ou liste os modelos liberados para sua chave."
                                } else {
                                    ""
                                }
                                messages.add(
                                    ChatMessage(
                                        "Ocorreu um erro ao chamar a API: $errorMessage$suggestion",
                                        isFromAssistant = true
                                    )
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                isLoading = isLoading
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message = message)
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val clipboardManager = LocalClipboardManager.current
    val backgroundColor = if (message.isFromAssistant) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromAssistant) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = backgroundColor,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(16.dp).weight(1f)
                )
                if (message.isFromAssistant) {
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(message.content)) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copiar")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean
) {
    Surface(modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding(), shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}, enabled = !isLoading) { Icon(Icons.Rounded.Mic, "Gravar áudio", tint = MaterialTheme.colorScheme.primary) }
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Pergunte qualquer coisa...") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                maxLines = 5,
                enabled = !isLoading
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(horizontal = 12.dp).size(24.dp))
            } else {
                IconButton(onClick = onSendClick) { Icon(Icons.Rounded.AutoAwesome, "Enviar", tint = MaterialTheme.colorScheme.primary) }
            }
        }
    }
}

@Composable
private fun SettingsDialog(
    currentApiKey: String,
    currentModelName: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var tempApiKey by remember { mutableStateOf(currentApiKey) }
    var tempModel by remember { mutableStateOf(currentModelName) }
    var availableModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var listErrorMessage by remember { mutableStateOf<String?>(null) }
    var isListingModels by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Configurar acesso ao Gemini", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = tempApiKey,
                    onValueChange = { tempApiKey = it },
                    label = { Text("Gemini API Key") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = tempModel,
                    onValueChange = { tempModel = it },
                    label = { Text("Nome do modelo") },
                    supportingText = { Text("Ex.: gemini-1.5-flash-latest") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (tempApiKey.isBlank()) {
                            listErrorMessage = "Informe a API Key antes de listar os modelos."
                            return@Button
                        }
                        isListingModels = true
                        listErrorMessage = null
                        scope.launch {
                            runCatching { GeminiModelService.fetchAvailableModels(tempApiKey.trim()) }
                                .onSuccess { models ->
                                    availableModels = models
                                    if (models.isEmpty()) {
                                        listErrorMessage = "Nenhum modelo retornado. Verifique no Google AI Studio se sua chave tem acesso liberado."
                                    }
                                }
                                .onFailure { throwable ->
                                    listErrorMessage = throwable.message ?: "Falha ao listar modelos para esta chave."
                                }
                            isListingModels = false
                        }
                    },
                    enabled = !isListingModels
                ) {
                    if (isListingModels) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text("Listar modelos disponíveis")
                }
                listErrorMessage?.let { message ->
                    Spacer(Modifier.height(8.dp))
                    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                if (availableModels.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Modelos liberados para esta chave:", style = MaterialTheme.typography.labelLarge)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableModels.forEach { model ->
                            FilterChip(
                                selected = tempModel == model,
                                onClick = { tempModel = model },
                                label = { Text(model) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "A lista acima reflete o resultado do endpoint ListModels para a sua chave. Escolha um dos nomes retornados ou informe o modelo manualmente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onSave(tempApiKey.trim(), tempModel.trim().ifBlank { DEFAULT_GEMINI_MODEL }) }) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}

data class TaskItemData(val id: Int, val text: String, var isCompleted: Boolean)

@Composable
private fun TasksScreen(onMenuClick: () -> Unit) {
    val tasks = remember {
        mutableStateListOf(
            TaskItemData(1, "Comprar leite e pão para o café da manhã", false),
            TaskItemData(2, "Estudar Jetpack Compose animations", false),
            TaskItemData(3, "Fazer caminhada de 30 minutos no parque", true),
            TaskItemData(4, "Responder e-mails importantes do trabalho", false),
            TaskItemData(5, "Agendar consulta no dentista", false)
        )
    }

    Scaffold(topBar = { CompactTopBar(onMenuClick, false) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Minhas Tarefas", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 8.dp)) }
            items(tasks) { task -> TaskItem(task) { task.isCompleted = !task.isCompleted } }
        }
    }
}

@Composable
fun TaskItem(task: TaskItemData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = task.isCompleted, onCheckedChange = { onClick() })
        Spacer(Modifier.width(16.dp))
        Text(task.text, textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null, color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface)
    }
}

data class Transaction(val description: String, val amount: Double, val date: String, val category: String)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FinancesPagerScreen(onMenuClick: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val tabTitles = listOf("Análise", "Transações", "Mais Opções")

    Scaffold(
        topBar = { CompactTopBar(onMenuClick, false) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> FinanceAnalysisScreen()
                    1 -> FinanceTransactionsScreen()
                    2 -> FinanceMoreOptionsScreen()
                }
            }
        }
    }
}

@Composable
fun FinanceAnalysisScreen() {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryCard("Saldo Atual", "R$ 19,48", Modifier.weight(1f))
                SummaryCard("Guardado", "R$ 0,00", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryCard("Despesas (Mês)", "R$ 2.967,09", Modifier.weight(1f))
                SummaryCard("Pendentes", "R$ 0,00", Modifier.weight(1f))
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Análise Gráfica", style = MaterialTheme.typography.titleLarge)
                    // Mock chart and category list
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.LightGray.copy(alpha=0.3f)), contentAlignment = Alignment.Center) {
                        Text("[Mock Gráfico Pizza]")
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceTransactionsScreen() {
    var showDialog by remember { mutableStateOf(false) }
    val transactions = remember {
        listOf(
            Transaction("Salário", 3500.0, "01/11/2025", "Receitas"),
            Transaction("Almoço (iFood)", -35.50, "14/11/2025", "Alimentação"),
            Transaction("Roupas", -20.00, "08/11/2025", "Compras"),
            Transaction("Água e Luz", -126.07, "07/11/2025", "Casa")
        )
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Adicionar transação")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(transactions.groupBy { it.date }.entries.toList()) { (date, transactionsOnDate) ->
                Text(date, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                transactionsOnDate.forEach { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
    if (showDialog) {
        NewTransactionDialog(onDismiss = { showDialog = false })
    }
}

@Composable
fun FinanceMoreOptionsScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        SettingsItem(icon = Icons.Rounded.Category, title = "Categorias e Caixinhas", subtitle = "Edite suas fontes de receita e despesa")
        SettingsItem(icon = Icons.Rounded.RequestQuote, title = "Orçamentos", subtitle = "Defina limites de gastos mensais")
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null)
    }
}


@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun NewTransactionDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nova Transação", style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, "Fechar") }
                }
                Text("O que você gostaria de registrar?", modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
                
                LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { TransactionTypeButton(icon = Icons.Rounded.ArrowUpward, text = "Despesa", color = Color(0xFFF44336)) }
                    item { TransactionTypeButton(icon = Icons.Rounded.ArrowDownward, text = "Receita", color = Color(0xFF4CAF50)) }
                    item { TransactionTypeButton(icon = Icons.Rounded.Savings, text = "Guardar", color = Color(0xFF2196F3)) }
                    item { TransactionTypeButton(icon = Icons.Rounded.Redeem, text = "Resgatar", color = Color(0xFFFF9800)) }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Calculate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ajustar Saldo")
                }
            }
        }
    }
}

@Composable
fun TransactionTypeButton(icon: ImageVector, text: String, color: Color, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = text, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(text, fontWeight = FontWeight.Medium)
        }
    }
}


@Composable
fun BalanceCard(balance: Double) {
    Card(elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("Balanço do Mês", style = MaterialTheme.typography.titleMedium)
            Text(String.format("R$ %.2f", balance), style = MaterialTheme.typography.displaySmall, color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336))
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        val icon = if (transaction.amount >= 0) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward
        Icon(icon, null, tint = if (transaction.amount >= 0) Color(0xFF4CAF50) else Color(0xFFF44336))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(transaction.description)
            Text(transaction.date, fontSize = 12.sp, color = Color.Gray)
        }
        Text(String.format("R$ %.2f", transaction.amount), color = if (transaction.amount >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider()
}

data class DiaryEntry(val date: String, val title: String, val content: String)

@Composable
private fun DiaryScreen(onMenuClick: () -> Unit) {
    val entries = remember {
        listOf(
            DiaryEntry("14/11/2025", "Dia triste", "Hoje me senti triste pois acordei achando que era sexta-feira, quando na verdade era quinta-feira..."),
            DiaryEntry("13/11/2025", "Dia produtivo!", "Consegui finalizar a primeira versão da UI do app e estou muito feliz com o resultado. O design está limpo e moderno.")
        )
    }

    Scaffold(topBar = { CompactTopBar(onMenuClick, false) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Meu Diário", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 8.dp)) }
            items(entries) { entry -> DiaryEntryItem(entry) }
        }
    }
}

@Composable
fun DiaryEntryItem(entry: DiaryEntry) {
    var expanded by remember { mutableStateOf(false) }
    Card(onClick = { expanded = !expanded }, elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(entry.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(entry.title, style = MaterialTheme.typography.titleLarge)
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Text(entry.content, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private object NectarDestinations {
    val primary = listOf(AppDestination.Overview, AppDestination.Chat, AppDestination.Tasks, AppDestination.Finances, AppDestination.Diary)
    val modules = listOf(AppDestination.Tasks, AppDestination.Finances, AppDestination.Diary, AppDestination.Chat)
}

sealed class AppDestination(val route: String, val title: String, val subtitle: String, val icon: ImageVector, val accentColor: Color, val highlights: List<String>) {
    data object Overview : AppDestination("overview", "Visão Geral", "Panorama do seu assistente.", Icons.Rounded.Dashboard, NectarPrimary, listOf("Veja seus módulos", "Acesse atalhos rápidos", "Inicie conversas"))
    data object Chat : AppDestination("chat", "Chat IA", "Converse com seu assistente.", Icons.AutoMirrored.Rounded.Chat, ChatAccent, listOf("Tire dúvidas", "Crie planos", "Conecte seus dados"))
    data object Tasks : AppDestination("tasks", "Tarefas", "Organize seu dia a dia.", Icons.Rounded.CheckCircle, TasksAccent, listOf("Mostrar tarefas simples", "Criar tarefa: Comprar leite", "Marcar como concluída"))
    data object Finances : AppDestination("finances", "Finanças", "Controle seus gastos.", Icons.Rounded.Money, FinanceAccent, listOf("Ver gastos do mês", "Registrar gasto: R$ 25", "Ver relatório"))
    data object Diary : AppDestination("diary", "Diário", "Capture seus pensamentos.", Icons.Rounded.AutoStories, DiaryAccent, listOf("Ver entradas do diário", "Escrever sobre hoje", "Como me senti"))
}
