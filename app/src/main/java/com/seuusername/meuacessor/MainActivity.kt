package com.seuusername.meuacessor

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.seuusername.meuacessor.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permissão concedida
        } else {
            // Permissão negada
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        enableEdgeToEdge()
        setContent {
            MeuAcessorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MeuAcessorApp(::requestNotificationPermission)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Canal Padrão"
            val descriptionText = "Canal para notificações gerais do app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("DEFAULT_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permissão já concedida
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explicar ao usuário por que a permissão é necessária (opcional)
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeuAcessorApp(requestNotificationPermission: () -> Unit) {
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
                NectarContentArea(navController, selectedRoute, requestNotificationPermission) { navController.popBackStack() }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = { ModalDrawerSheet { sidebar() } }
            ) {
                NectarContentArea(navController, selectedRoute, requestNotificationPermission) { 
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
    requestNotificationPermission: () -> Unit,
    onMenuClick: () -> Unit
) {
    NavHost(navController = navController, startDestination = AppDestination.Overview.route) {
        composable(AppDestination.Overview.route) {
            Scaffold(
                topBar = { CompactTopBar(onMenuClick, showMenuIcon = navController.previousBackStackEntry == null) }
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
        composable(AppDestination.Notification.route) { NotificationScreen(onMenuClick, requestNotificationPermission) }
    }
}

@Composable
private fun CompactTopBar(onMenuClick: () -> Unit, showMenuIcon: Boolean) {
    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = if (showMenuIcon) Icons.Rounded.Widgets else Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = if (showMenuIcon) "Abrir menu" else "Voltar"
                )
            }
            Text(
                text = "Meu Acessor",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
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

@Composable
private fun ChatScreen(onMenuClick: () -> Unit) {
    Scaffold(
        topBar = { CompactTopBar(onMenuClick, false) },
        bottomBar = { ChatInputBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.AutoMirrored.Rounded.Chat, null, modifier = Modifier.size(80.dp), tint = ChatAccent.copy(alpha = 0.7f))
            Spacer(Modifier.height(16.dp))
            Text("Converse com o Meu Acessor", style = MaterialTheme.typography.headlineSmall)
            Text("Faça perguntas, peça para criar tarefas e mais.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar() {
    var text by remember { mutableStateOf("") }
    Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) { Icon(Icons.Rounded.Mic, "Gravar áudio", tint = MaterialTheme.colorScheme.primary) }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Pergunte qualquer coisa...") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                maxLines = 5
            )
            IconButton(onClick = {}) { Icon(Icons.Rounded.AutoAwesome, "Enviar", tint = MaterialTheme.colorScheme.primary) }
        }
    }
}

data class TaskItemData(val id: Int, val text: String, var isCompleted: Boolean)

@Composable
private fun TasksScreen(onMenuClick: () -> Unit) {
    val tasks = remember { mutableStateListOf(
        TaskItemData(1, "Comprar leite e pão para o café da manhã", false),
        TaskItemData(2, "Estudar Jetpack Compose animations", false),
        TaskItemData(3, "Fazer caminhada de 30 minutos no parque", true),
        TaskItemData(4, "Responder e-mails importantes do trabalho", false),
        TaskItemData(5, "Agendar consulta no dentista", false)
    ) }

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
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick).padding(16.dp),
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
            HorizontalPager(state = pagerState) {
                page ->
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
    val entries = remember { listOf(
        DiaryEntry("14/11/2025", "Dia triste", "Hoje me senti triste pois acordei achando que era sexta-feira, quando na verdade era quinta-feira..."),
        DiaryEntry("13/11/2025", "Dia produtivo!", "Consegui finalizar a primeira versão da UI do app e estou muito feliz com o resultado. O design está limpo e moderno.")
    ) }

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

@Composable
private fun NotificationScreen(onMenuClick: () -> Unit, requestPermission: () -> Unit) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        requestPermission()
    }

    Scaffold(topBar = { CompactTopBar(onMenuClick, false) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Teste de Notificação", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text(
                "Clique no botão abaixo para enviar uma notificação de teste. Você pode precisar conceder a permissão primeiro.",
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = { sendTestNotification(context) }) {
                Text("Enviar Notificação de Teste")
            }
        }
    }
}

private fun sendTestNotification(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = NotificationCompat.Builder(context, "DEFAULT_CHANNEL")
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Use um ícone padrão
        .setContentTitle("Meu Acessor")
        .setContentText("Teste")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
    notificationManager.notify(1, notification)
}


private object NectarDestinations {
    val primary = listOf(AppDestination.Overview, AppDestination.Chat, AppDestination.Tasks, AppDestination.Finances, AppDestination.Diary, AppDestination.Notification)
    val modules = listOf(AppDestination.Tasks, AppDestination.Finances, AppDestination.Diary, AppDestination.Chat, AppDestination.Notification)
}

sealed class AppDestination(val route: String, val title: String, val subtitle: String, val icon: ImageVector, val accentColor: Color, val highlights: List<String>) {
    data object Overview : AppDestination("overview", "Visão Geral", "Panorama do seu assistente.", Icons.Rounded.Dashboard, NectarPrimary, listOf("Veja seus módulos", "Acesse atalhos rápidos", "Inicie conversas"))
    data object Chat : AppDestination("chat", "Chat IA", "Converse com seu assistente.", Icons.AutoMirrored.Rounded.Chat, ChatAccent, listOf("Tire dúvidas", "Crie planos", "Conecte seus dados"))
    data object Tasks : AppDestination("tasks", "Tarefas", "Organize seu dia a dia.", Icons.Rounded.CheckCircle, TasksAccent, listOf("Mostrar tarefas simples", "Criar tarefa: Comprar leite", "Marcar como concluída"))
    data object Finances : AppDestination("finances", "Finanças", "Controle seus gastos.", Icons.Rounded.Money, FinanceAccent, listOf("Ver gastos do mês", "Registrar gasto: R$ 25", "Ver relatório"))
    data object Diary : AppDestination("diary", "Diário", "Capture seus pensamentos.", Icons.Rounded.AutoStories, DiaryAccent, listOf("Ver entradas do diário", "Escrever sobre hoje", "Como me senti"))
    data object Notification : AppDestination("notification", "Notificação", "Teste o envio de notificações.", Icons.Rounded.Notifications, RemindersAccent, listOf("Testar permissão", "Enviar notificação", "Validar canal"))
}
