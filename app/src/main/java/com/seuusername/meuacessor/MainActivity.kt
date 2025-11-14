package com.seuusername.meuacessor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seuusername.meuacessor.ui.theme.MeuAcessorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeuAcessorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    val (displayValue, setDisplayValue) = remember { mutableStateOf("0") }
    val (firstOperand, setFirstOperand) = remember { mutableStateOf<Double?>(null) }
    val (pendingOperation, setPendingOperation) = remember { mutableStateOf<String?>(null) }
    val (shouldResetInput, setShouldResetInput) = remember { mutableStateOf(false) }

    fun resetAll() {
        setDisplayValue("0")
        setFirstOperand(null)
        setPendingOperation(null)
        setShouldResetInput(false)
    }

    fun formatResult(value: Double): String {
        val text = value.toString()
        return if (text.contains(".")) {
            text.trimEnd('0').trimEnd('.')
        } else {
            text
        }
    }

    fun performOperation(operation: String?) {
        val currentValue = displayValue.toDoubleOrNull()
        val storedValue = firstOperand
        if (operation == null || storedValue == null || currentValue == null) {
            setFirstOperand(currentValue ?: storedValue)
            return
        }

        val result = when (operation) {
            "+" -> storedValue + currentValue
            "-" -> storedValue - currentValue
            "×" -> storedValue * currentValue
            "÷" -> if (currentValue == 0.0) null else storedValue / currentValue
            else -> null
        }

        if (result == null) {
            setDisplayValue("Erro")
            setFirstOperand(null)
            setPendingOperation(null)
            setShouldResetInput(true)
        } else {
            val formatted = formatResult(result)
            setDisplayValue(formatted)
            setFirstOperand(result)
            setShouldResetInput(true)
        }
    }

    fun handleNumberInput(number: String) {
        if (displayValue == "Erro" || shouldResetInput) {
            setDisplayValue(number)
            setShouldResetInput(false)
        } else {
            val nextValue = if (displayValue == "0") number else displayValue + number
            setDisplayValue(nextValue)
        }
    }

    fun handleDecimal() {
        if (displayValue == "Erro" || shouldResetInput) {
            setDisplayValue("0.")
            setShouldResetInput(false)
        } else if (!displayValue.contains('.')) {
            setDisplayValue(displayValue + ".")
        }
    }

    fun handleOperation(symbol: String) {
        val currentValue = displayValue.toDoubleOrNull()
        if (displayValue == "Erro") {
            resetAll()
            return
        }

        if (currentValue != null) {
            if (pendingOperation != null && !shouldResetInput) {
                performOperation(pendingOperation)
            } else {
                setFirstOperand(currentValue)
                setShouldResetInput(true)
            }
            setPendingOperation(symbol)
        }
    }

    fun handleEquals() {
        if (displayValue == "Erro") {
            resetAll()
            return
        }

        if (pendingOperation != null) {
            performOperation(pendingOperation)
            setPendingOperation(null)
        }
    }

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        )
    )

    val helperText = when {
        displayValue == "Erro" -> "Operação inválida"
        firstOperand != null && pendingOperation != null && !shouldResetInput ->
            "${formatResult(firstOperand!!)} $pendingOperation $displayValue"
        firstOperand != null && pendingOperation != null ->
            "${formatResult(firstOperand!!)} $pendingOperation"
        else -> ""
    }

    Column(
        modifier = modifier
            .background(gradient)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Calculadora",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (helperText.isNotEmpty()) {
                        Text(
                            text = helperText,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = displayValue,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 48.sp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val rows = listOf(
            listOf("AC", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            rows.dropLast(1).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    row.forEach { symbol ->
                        val isOperation = symbol in listOf("÷", "×", "-", "+")
                        val buttonModifier = Modifier
                            .weight(1f)
                            .height(68.dp)

                        CalculatorButton(
                            symbol = symbol,
                            modifier = buttonModifier,
                            containerColor = when {
                                symbol == "AC" -> MaterialTheme.colorScheme.errorContainer
                                isOperation -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = when {
                                symbol == "AC" -> MaterialTheme.colorScheme.onErrorContainer
                                isOperation -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ) {
                            when (symbol) {
                                "AC" -> resetAll()
                                "." -> handleDecimal()
                                "÷", "×", "-", "+" -> handleOperation(symbol)
                                "±" -> if (displayValue != "Erro") {
                                    val toggled = displayValue.toDoubleOrNull()?.times(-1)
                                    if (toggled != null) {
                                        setDisplayValue(formatResult(toggled))
                                        setShouldResetInput(false)
                                    }
                                }
                                "%" -> if (displayValue != "Erro") {
                                    val percent = displayValue.toDoubleOrNull()?.div(100)
                                    if (percent != null) {
                                        setDisplayValue(formatResult(percent))
                                        setShouldResetInput(false)
                                    }
                                }
                                "=" -> handleEquals()
                                else -> handleNumberInput(symbol)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CalculatorButton(
                    symbol = "0",
                    modifier = Modifier
                        .weight(2f)
                        .height(68.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    handleNumberInput("0")
                }

                CalculatorButton(
                    symbol = ".",
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    handleDecimal()
                }

                CalculatorButton(
                    symbol = "=",
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    handleEquals()
                }
            }
        }
    }
}

@Composable
private fun CalculatorButton(
    symbol: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = symbol,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    MeuAcessorTheme {
        CalculatorScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}
