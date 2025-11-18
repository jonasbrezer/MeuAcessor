package com.seuusername.meuacessor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.seuusername.meuacessor.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import java.text.NumberFormat
import java.util.Locale

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
    val summaryCards = remember {
        listOf(
            FinanceSummaryCardData("Saldo Atual", "R$ 19.482,91", "Disponível", AccentGreen),
            FinanceSummaryCardData("Guardado", "R$ 4.800,00", "Caixinhas", AccentBlue.copy(alpha = 0.8f)),
            FinanceSummaryCardData("Despesas (Mês)", "R$ 2.967,09", "Planejado", AccentOrange.copy(alpha = 0.9f)),
            FinanceSummaryCardData("Pendentes", "R$ 726,00", "Próximos 7 dias", AccentPurple)
        )
    }

    val categories = remember {
        listOf(
            FinanceCategory("Liquidar faturas", 1675.80, Color(0xFF3761F6)),
            FinanceCategory("Dízimo", 392.00, Color(0xFF7B4CF4)),
            FinanceCategory("Saúde", 298.90, Color(0xFF2CCEBB)),
            FinanceCategory("Água e Luz", 194.81, Color(0xFF00B4D8)),
            FinanceCategory("Vivo", 133.00, Color(0xFFFF8C42)),
            FinanceCategory("Internet", 113.00, Color(0xFFEF476F)),
            FinanceCategory("Roupas", 88.00, Color(0xFFFB8DA0)),
            FinanceCategory("Igreja", 55.00, Color(0xFF5D5FEF)),
            FinanceCategory("Escola", 20.00, Color(0xFF8E8D8A))
        )
    }

    val totalExpenses = remember(categories) { categories.sumOf { it.amount } }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedOperation by remember { mutableStateOf("Receitas") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    val highlightedCategory = categories.first()

    val quickStats = remember {
        listOf(
            FinanceQuickStat("Saldo", "R$ 19,48", AccentGreen.copy(alpha = 0.15f), AccentGreen),
            FinanceQuickStat("Pendentes", "R$ 0,00", AccentOrange.copy(alpha = 0.15f), AccentOrange),
            FinanceQuickStat("Caixinhas", "R$ 0,00", AccentBlue.copy(alpha = 0.15f), AccentBlue)
        )
    }

    val timeline = remember {
        listOf(
            FinanceTimelineDay(
                label = "08/11/2025",
                entries = listOf(
                    FinanceMovement("Roupas", "Conserto roupa", -20.0, FinanceMovementType.Expense, "Pago", Color(0xFFFB8DA0))
                )
            ),
            FinanceTimelineDay(
                label = "07/11/2025",
                entries = listOf(
                    FinanceMovement("Água e Luz", "Fatura novembro", -126.07, FinanceMovementType.Expense, "Pago", Color(0xFF00B4D8)),
                    FinanceMovement("JOBs", "Bico - Formatação", 45.0, FinanceMovementType.Income, "Recebido", Color(0xFF34C759)),
                    FinanceMovement("Igreja", "Dízimo semanal", -5.0, FinanceMovementType.Expense, "Pago", Color(0xFF5D5FEF)),
                    FinanceMovement("Escola", "Carnê Creche Helena", -50.0, FinanceMovementType.Expense, "Pago", Color(0xFF8E8D8A))
                )
            )
        )
    }

    val heroInfo = remember {
        FinanceHeroInfo(
            currentMonth = "Novembro 2025",
            userName = "Lucas",
            balance = "R$ 19.482,91",
            monthlyVariation = "+12% vs mês passado",
            lastSync = "Atualizado há 2 minutos"
        )
    }

    val actions = remember {
        listOf(
            FinanceQuickAction(Icons.Rounded.Insights, "Análise IA"),
            FinanceQuickAction(Icons.Rounded.Savings, "Caixinhas"),
            FinanceQuickAction(Icons.Rounded.Wallet, "Investir"),
            FinanceQuickAction(Icons.Rounded.AttachMoney, "Ganhos"),
        )
    }

    val financeGoals = remember {
        listOf(
            FinanceGoal("Reserva de Emergência", "R$ 20.000", 0.35f, "R$ 7.000 acumulados", AccentBlue.copy(alpha = 0.2f), AccentBlue),
            FinanceGoal("Viagem família 2026", "R$ 8.500", 0.58f, "R$ 4.930 acumulados", AccentPurple.copy(alpha = 0.2f), AccentPurple)
        )
    }

    val financeAlerts = remember {
        listOf(
            FinanceAlert("Energia acima da média", "Consumo 14% maior nos últimos 30 dias", AccentOrange, Icons.Rounded.EnergySavingsLeaf),
            FinanceAlert("Receita recorrente sem receber", "JOB fixo não identificado desde 15/10", AccentRed, Icons.Rounded.Warning)
        )
    }

    val upcomingBills = remember {
        listOf(
            FinanceUpcomingPayment("Cartão XP", "Vence em 3 dias", 725.90, AccentBlue),
            FinanceUpcomingPayment("Escola Helena", "12/11 - parcela 09", 410.00, AccentPurple),
            FinanceUpcomingPayment("Plano de saúde", "15/11", 298.90, AccentGreen)
        )
    }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finanças") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } },
                actions = { IconButton(onClick = { /* future action */ }) { Icon(Icons.Rounded.MoreVert, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* mock */ }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Rounded.Add, contentDescription = "Adicionar")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { FinanceHeroCard(heroInfo) }
            item { FinanceQuickActions(actions = actions) }
            item { FinanceSummaryRow(summaryCards) }
            item {
                FinanceAnalysisSection(
                    categories = categories,
                    total = totalExpenses,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    highlightedCategory = highlightedCategory,
                    currencyFormatter = currencyFormatter
                )
            }
            item { FinanceCategoryList(categories = categories, total = totalExpenses, currencyFormatter = currencyFormatter) }
            item { FinanceGoalsSection(goals = financeGoals) }
            item { FinanceAlertsSection(alerts = financeAlerts) }
            item { FinanceUpcomingPaymentsSection(upcomingBills = upcomingBills, currencyFormatter = currencyFormatter) }
            item { FinanceInsightsCard() }
            item {
                FinanceTimelineCard(
                    quickStats = quickStats,
                    timeline = timeline,
                    selectedOperation = selectedOperation,
                    onOperationSelected = { selectedOperation = it },
                    showCategoryMenu = showCategoryMenu,
                    onCategoryMenuToggle = { showCategoryMenu = !showCategoryMenu },
                    currencyFormatter = currencyFormatter
                )
            }
        }
    }
}

@Composable
private fun FinanceSummaryRow(cards: List<FinanceSummaryCardData>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cards.chunked(2).forEach { rowCards ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowCards.forEach { card ->
                    FinanceSummaryCard(data = card, modifier = Modifier.weight(1f))
                }
                if (rowCards.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FinanceHeroCard(info: FinanceHeroInfo) {
    val gradient = remember {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                MaterialTheme.colorScheme.secondary
            )
        )
    }

    Surface(shape = RoundedCornerShape(28.dp), color = Color.Transparent, tonalElevation = 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient, RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Olá, ${'$'}{info.userName}", color = Color.White.copy(alpha = 0.8f))
                        Text(info.currentMonth, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                        Text("Sincronizar", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Saldo consolidado", color = Color.White.copy(alpha = 0.8f))
                    Text(info.balance, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 34.sp)
                    Text(info.monthlyVariation, color = Color.White.copy(alpha = 0.9f))
                }
                Divider(color = Color.White.copy(alpha = 0.2f))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(info.lastSync, color = Color.White.copy(alpha = 0.8f))
                    Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White.copy(alpha = 0.9f))
                }
            }
        }
    }
}

@Composable
private fun FinanceQuickActions(actions: List<FinanceQuickAction>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        actions.forEach { action ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                        Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp))
                    }
                    Text(action.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun FinanceGoalsSection(goals: List<FinanceGoal>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Metas", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        goals.forEach { goal ->
            Surface(shape = RoundedCornerShape(20.dp), color = goal.backgroundColor) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(goal.title, fontWeight = FontWeight.Bold)
                            Text(goal.target, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(String.format("%d%%", (goal.progress * 100).toInt()), fontWeight = FontWeight.Bold, color = goal.accentColor)
                    }
                    LinearProgressIndicator(progress = goal.progress, color = goal.accentColor, trackColor = goal.accentColor.copy(alpha = 0.2f), modifier = Modifier.fillMaxWidth())
                    Text(goal.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun FinanceAlertsSection(alerts: List<FinanceAlert>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Alertas inteligentes", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        alerts.forEach { alert ->
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = alert.tint.copy(alpha = 0.15f)) {
                        Icon(alert.icon, contentDescription = null, tint = alert.tint, modifier = Modifier.padding(10.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(alert.title, fontWeight = FontWeight.SemiBold)
                        Text(alert.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun FinanceUpcomingPaymentsSection(upcomingBills: List<FinanceUpcomingPayment>, currencyFormatter: NumberFormat) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Próximos pagamentos", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        upcomingBills.forEach { bill ->
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(bill.tint))
                        Column {
                            Text(bill.title, fontWeight = FontWeight.SemiBold)
                            Text(bill.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                    Text(currencyFormatter.format(bill.amount), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FinanceSummaryCard(data: FinanceSummaryCardData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(data.title, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(data.value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = data.highlightColor)
            Text(data.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FinanceAnalysisSection(
    categories: List<FinanceCategory>,
    total: Double,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    highlightedCategory: FinanceCategory,
    currencyFormatter: NumberFormat
) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Análise Gráfica", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("novembro de 2025", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AssistChip(onClick = { /*mock*/ }, label = { Text("Exportar") }, leadingIcon = { Icon(Icons.Rounded.PieChart, null, modifier = Modifier.size(18.dp)) })
            }
            FinanceChartTabs(selectedTab = selectedTab, onTabSelected = onTabSelected)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FinanceDonutChart(categories = categories, modifier = Modifier.weight(1f))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = String.format(
                            Locale("pt", "BR"),
                            "%.2f%%\n%s",
                            highlightedCategory.amount / total * 100,
                            highlightedCategory.name
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Liquidar faturas consome mais da metade do seu orçamento mensal. Negocie os boletos para aliviar o fluxo de caixa deste mês.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Divider()
            FinanceCategorySummaryRow(total = total, currencyFormatter = currencyFormatter)
        }
    }
}

@Composable
private fun FinanceChartTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Categorias", "R vs D", "Evolução")
    ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp, containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary, indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = MaterialTheme.colorScheme.primary)
    }) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
}

@Composable
private fun FinanceDonutChart(categories: List<FinanceCategory>, modifier: Modifier = Modifier) {
    val total = categories.sumOf { it.amount }
    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            val strokeWidth = size.minDimension * 0.18f
            categories.forEach { category ->
                val sweep = (category.amount / total * 360f).toFloat()
                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val topCategory = categories.maxByOrNull { it.amount }
            if (topCategory != null) {
                val percentage = topCategory.amount / total * 100
                Text(String.format(Locale("pt", "BR"), "%.2f%%", percentage), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(topCategory.name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FinanceCategorySummaryRow(total: Double, currencyFormatter: NumberFormat) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("Total", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(currencyFormatter.format(total), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        }
        FilledTonalButton(onClick = { /*mock*/ }) { Text("Liquidar faturas") }
    }
}

@Composable
private fun FinanceCategoryList(categories: List<FinanceCategory>, total: Double, currencyFormatter: NumberFormat) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.forEach { category ->
                val percentage = category.amount / total * 100
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(category.color))
                        Column {
                            Text(category.name, fontWeight = FontWeight.Medium)
                            Text(currencyFormatter.format(category.amount), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                    Text(String.format(Locale("pt", "BR"), "%.2f%%", percentage), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Divider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Bold)
                Text(currencyFormatter.format(total), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FinanceInsightsCard() {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Insights da IA", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                TextButton(onClick = { /*mock*/ }) { Text("Gerar Novo") }
            }
            Text(
                "Alerta crítico de saldo",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                "Seu saldo disponível é de apenas R$ 19,48, o que é extremamente baixo. É crucial revisar seus gastos imediatamente para evitar dificuldades financeiras e buscar formas de aumentar sua receita.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinanceTimelineCard(
    quickStats: List<FinanceQuickStat>,
    timeline: List<FinanceTimelineDay>,
    selectedOperation: String,
    onOperationSelected: (String) -> Unit,
    showCategoryMenu: Boolean,
    onCategoryMenuToggle: () -> Unit,
    currencyFormatter: NumberFormat
) {
    val operations = listOf("Receitas", "Despesas", "Pagos", "Recebidos")
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Fluxo mensal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                quickStats.forEach { stat -> FinanceQuickStatChip(stat, modifier = Modifier.weight(1f)) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /*prev*/ }) { Icon(Icons.Rounded.ChevronLeft, contentDescription = "Mês anterior") }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("novembro de 2025", fontWeight = FontWeight.Bold)
                    Text("Consolidado", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                IconButton(onClick = { /*next*/ }) { Icon(Icons.Rounded.ChevronRight, contentDescription = "Próximo mês") }
            }
            FlowRowMainFilter(operations = operations, selected = selectedOperation, onOperationSelected = onOperationSelected)
            ExposedDropdownMenuBox(expanded = showCategoryMenu, onExpandedChange = { onCategoryMenuToggle() }) {
                OutlinedTextField(
                    value = "Todas as Categorias",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    )
                )
                DropdownMenu(expanded = showCategoryMenu, onDismissRequest = onCategoryMenuToggle) {
                    listOf("Todas", "Moradia", "Educação", "Lazer").forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = onCategoryMenuToggle)
                    }
                }
            }
            timeline.forEach { day ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(day.label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    day.entries.forEach { movement ->
                        FinanceMovementRow(movement = movement, currencyFormatter = currencyFormatter)
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceQuickStatChip(stat: FinanceQuickStat, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = stat.backgroundColor
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stat.title, color = stat.contentColor, fontSize = 12.sp)
            Text(stat.value, color = stat.contentColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FlowRowMainFilter(operations: List<String>, selected: String, onOperationSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        operations.forEach { operation ->
            val isSelected = selected == operation
            FilterChip(
                selected = isSelected,
                onClick = { onOperationSelected(operation) },
                label = { Text(operation) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
private fun FinanceMovementRow(movement: FinanceMovement, currencyFormatter: NumberFormat) {
    val amountColor = if (movement.type == FinanceMovementType.Income) AccentGreen else MaterialTheme.colorScheme.onSurface
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(movement.indicatorColor))
                Column {
                    Text(movement.title, fontWeight = FontWeight.SemiBold)
                    Text(movement.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (movement.type == FinanceMovementType.Income) "+${currencyFormatter.format(abs(movement.amount))}" else currencyFormatter.format(movement.amount),
                    color = amountColor,
                    fontWeight = FontWeight.Bold
                )
                Text(movement.status, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

data class FinanceSummaryCardData(val title: String, val value: String, val subtitle: String, val highlightColor: Color)

data class FinanceCategory(val name: String, val amount: Double, val color: Color)

data class FinanceQuickStat(val title: String, val value: String, val backgroundColor: Color, val contentColor: Color)

data class FinanceTimelineDay(val label: String, val entries: List<FinanceMovement>)

data class FinanceMovement(
    val title: String,
    val description: String,
    val amount: Double,
    val type: FinanceMovementType,
    val status: String,
    val indicatorColor: Color
)

data class FinanceHeroInfo(
    val currentMonth: String,
    val userName: String,
    val balance: String,
    val monthlyVariation: String,
    val lastSync: String
)

data class FinanceQuickAction(val icon: ImageVector, val label: String)

data class FinanceGoal(
    val title: String,
    val target: String,
    val progress: Float,
    val subtitle: String,
    val backgroundColor: Color,
    val accentColor: Color
)

data class FinanceAlert(
    val title: String,
    val description: String,
    val tint: Color,
    val icon: ImageVector
)

data class FinanceUpcomingPayment(
    val title: String,
    val subtitle: String,
    val amount: Double,
    val tint: Color
)

enum class FinanceMovementType { Income, Expense }

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
