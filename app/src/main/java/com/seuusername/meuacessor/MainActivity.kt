package com.seuusername.meuacessor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        CalculatorScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
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

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .padding(24.dp)
        ) {
            Text(
                text = displayValue,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val rows = listOf(
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "-"),
            listOf("C", "0", ".", "+")
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { symbol ->
                        val isOperation = symbol in listOf("÷", "×", "-", "+")
                        val buttonModifier = Modifier
                            .weight(1f)
                            .height(64.dp)

                        CalculatorButton(
                            symbol = symbol,
                            modifier = buttonModifier,
                            containerColor = if (isOperation) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        ) {
                            when (symbol) {
                                "C" -> resetAll()
                                "." -> handleDecimal()
                                "÷", "×", "-", "+" -> handleOperation(symbol)
                                else -> handleNumberInput(symbol)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalculatorButton(
                    symbol = "=",
                    modifier = Modifier
                        .weight(4f)
                        .height(64.dp),
                    containerColor = MaterialTheme.colorScheme.primary
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
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
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
