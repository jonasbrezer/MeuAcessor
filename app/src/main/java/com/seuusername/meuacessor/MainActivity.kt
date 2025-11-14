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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.seuusername.meuacessor.ui.theme.ChatAccent
import com.seuusername.meuacessor.ui.theme.DiaryAccent
import com.seuusername.meuacessor.ui.theme.FinanceAccent
import com.seuusername.meuacessor.ui.theme.HabitsAccent
import com.seuusername.meuacessor.ui.theme.MeuAcessorTheme
import com.seuusername.meuacessor.ui.theme.NectarBackground
import com.seuusername.meuacessor.ui.theme.NectarPrimary
import com.seuusername.meuacessor.ui.theme.NectarSurface
import com.seuusername.meuacessor.ui.theme.ProjectsAccent
import com.seuusername.meuacessor.ui.theme.TasksAccent
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
                    MeuAcessorApp()
                }
            }
        }
    }
}

@Composable
private fun MeuAcessorApp() {
    val navController = rememberNavController()
    val destinations = remember { NectarDestinations.primary }
    val currentDestination by navController.currentBackStackEntryAsState()
    val selectedRoute = currentDestination?.destination?.route ?: AppDestination.Overview.route

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedLayout = maxWidth > 960.dp

        if (isExpandedLayout) {
            PermanentNavigationDrawer(
                drawerContent = {
                    NectarSidebar(
                        destinations = destinations,
                        selectedRoute = selectedRoute,
                        onDestinationSelected = { destination ->
                            if (selectedRoute != destination.route) {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    popUpTo(AppDestination.Overview.route)
                                }
                            }
                        }
                    )
                }
            ) {
                NectarContentArea(
                    navController = navController,
                    selectedRoute = selectedRoute
                )
            }
        } else {
            val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
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
                        modifier = Modifier.width(280.dp)
                    )
                }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    CompactTopBar(onMenuClick = { scope.launch { drawerState.open() } })
                    NectarContentArea(
                        navController = navController,
                        selectedRoute = selectedRoute,
                        modifier = Modifier.weight(1f)
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
private fun NectarContentArea(
    navController: NavHostController,
    selectedRoute: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = AppDestination.Overview.route
        ) {
            composable(AppDestination.Overview.route) {
                OverviewScreen(
                    modules = NectarDestinations.modules,
                    onModuleSelected = { navController.navigate(it.route) },
                    highlightRoute = selectedRoute
                )
            }
            composable(AppDestination.Chat.route) {
                ModulePlaceholderScreen(destination = AppDestination.Chat)
            }
            composable(AppDestination.Tasks.route) {
                ModulePlaceholderScreen(destination = AppDestination.Tasks)
            }
            composable(AppDestination.Habits.route) {
                ModulePlaceholderScreen(destination = AppDestination.Habits)
            }
            composable(AppDestination.Projects.route) {
                ModulePlaceholderScreen(destination = AppDestination.Projects)
            }
            composable(AppDestination.Finances.route) {
                ModulePlaceholderScreen(destination = AppDestination.Finances)
            }
            composable(AppDestination.Diary.route) {
                ModulePlaceholderScreen(destination = AppDestination.Diary)
            }
        }
    }
}

@Composable
private fun CompactTopBar(onMenuClick: () -> Unit) {
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
                Icon(imageVector = Icons.Rounded.Widgets, contentDescription = "Abrir menu")
            }
            Text(
                text = "Néctar",
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
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp),
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
                        text = "Néctar",
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
                            label = {
                                Text(
                                    text = destination.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            selected = isSelected,
                            onClick = { onDestinationSelected(destination) },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title
                                )
                            },
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
                DrawerQuickAction(
                    icon = Icons.Rounded.Refresh,
                    text = "Limpar Chat"
                )
                DrawerQuickAction(
                    icon = Icons.Rounded.Logout,
                    text = "Sair"
                )
            }
        }
    }
}

@Composable
private fun DrawerQuickAction(icon: ImageVector, text: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val background by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else Color.Transparent,
        label = "quick-action-bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(interactionSource = interactionSource, indication = null) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(12.dp)
                .size(22.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun OverviewScreen(
    modules: List<AppDestination>,
    onModuleSelected: (AppDestination) -> Unit,
    highlightRoute: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NectarBackground)
            .padding(horizontal = 32.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Olá! Eu sou o Néctar ✨",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Seu assistente pessoal inteligente para organizar sua vida!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth(),
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(modules) { module ->
                    ModuleCard(
                        destination = module,
                        isHighlighted = module.route == highlightRoute,
                        onClick = { onModuleSelected(module) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ChatInputBar()
    }
}

@Composable
private fun ModuleCard(
    destination: AppDestination,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "module-scale")

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 10.dp else 6.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 0.dp, shape = RoundedCornerShape(20.dp))
            .background(Color.Transparent)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(destination.accentColor.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(destination.accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.title,
                    tint = destination.accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = destination.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                destination.highlights.take(3).forEach { highlight ->
                    Text(
                        text = "• $highlight",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar() {
    var text by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = "Gravar áudio",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Pergunte qualquer coisa...") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 2
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "Enviar mensagem",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ModulePlaceholderScreen(destination: AppDestination) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NectarBackground)
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = destination.title,
            style = MaterialTheme.typography.displayMedium,
            color = destination.accentColor
        )
        Text(
            text = destination.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(destination.accentColor.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                destination.highlights.forEach { highlight ->
                    Text(
                        text = "• $highlight",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

private object NectarDestinations {
    val primary = listOf(
        AppDestination.Chat,
        AppDestination.Overview,
        AppDestination.Tasks,
        AppDestination.Habits,
        AppDestination.Projects,
        AppDestination.Finances,
        AppDestination.Diary
    )

    val modules = listOf(
        AppDestination.Tasks,
        AppDestination.Finances,
        AppDestination.Diary,
        AppDestination.Chat
    )
}

sealed class AppDestination(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val highlights: List<String>
) {
    data object Overview : AppDestination(
        route = "overview",
        title = "Visão Néctar",
        subtitle = "Um panorama de tudo o que o Néctar pode organizar para você.",
        icon = Icons.Rounded.Dashboard,
        accentColor = NectarPrimary,
        highlights = listOf(
            "Veja seus módulos em um relance",
            "Acesse atalhos rápidos",
            "Inicie conversas instantaneamente"
        )
    )

    data object Chat : AppDestination(
        route = "chat",
        title = "Chat IA",
        subtitle = "Converse com o Néctar, faça perguntas e obtenha respostas inteligentes.",
        icon = Icons.Rounded.Chat,
        accentColor = ChatAccent,
        highlights = listOf(
            "Responder dúvidas do dia a dia",
            "Criar planos personalizados",
            "Conectar com seus dados pessoais"
        )
    )

    data object Tasks : AppDestination(
        route = "tasks",
        title = "Tarefas",
        subtitle = "Organize tudo o que precisa fazer em um único lugar.",
        icon = Icons.Rounded.CheckCircle,
        accentColor = TasksAccent,
        highlights = listOf(
            "Mostrar tarefas simples",
            "Criar tarefa: Comprar leite",
            "Marcar tarefa como concluída"
        )
    )

    data object Habits : AppDestination(
        route = "habits",
        title = "Hábitos",
        subtitle = "Crie rotinas saudáveis com lembretes inteligentes.",
        icon = Icons.Rounded.Task,
        accentColor = HabitsAccent,
        highlights = listOf(
            "Registrar exercício hoje",
            "Criar hábito: Beber água",
            "Visualizar sequência semanal"
        )
    )

    data object Projects : AppDestination(
        route = "projects",
        title = "Projetos",
        subtitle = "Planeje etapas, acompanhe entregas e monitore o progresso.",
        icon = Icons.Rounded.Assessment,
        accentColor = ProjectsAccent,
        highlights = listOf(
            "Listar meus projetos",
            "Criar projeto novo",
            "Ver progresso dos objetivos"
        )
    )

    data object Finances : AppDestination(
        route = "finances",
        title = "Finanças",
        subtitle = "Controle gastos, receitas e tenha uma visão clara das finanças.",
        icon = Icons.Rounded.Money,
        accentColor = FinanceAccent,
        highlights = listOf(
            "Ver gastos do mês",
            "Registrar gasto: Almoço R$ 25",
            "Ver relatório financeiro"
        )
    )

    data object Diary : AppDestination(
        route = "diary",
        title = "Diário",
        subtitle = "Capture seus pensamentos e sentimentos diariamente.",
        icon = Icons.Rounded.AutoStories,
        accentColor = DiaryAccent,
        highlights = listOf(
            "Ver entradas do diário",
            "Escrever sobre hoje",
            "Como me senti essa semana"
        )
    )
}
