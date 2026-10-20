package com.example.konspekt2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.konspekt2.ui.theme.Konspekt2Theme
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Konspekt2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LengthConverterScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Definicja jednostek oparta na tabeli przeliczników[cite: 9]
enum class LengthUnit(val label: String, val symbol: String, val metersInUnit: Double) {
    CENTIMETER("Centymetry", "cm", 0.01),
    KILOMETER("Kilometry", "km", 1000.0),
    MILE("Mile (międzynarodowe)", "mi", 1609.344),
    LIGHT_YEAR("Lata świetlne", "ly", 9_460_730_472_580_800.0)
}

// Czysta funkcja przeliczająca metry na daną jednostkę
fun convert(meters: Double, unit: LengthUnit): Double {
    return meters / unit.metersInUnit
}

// Czysta funkcja formatująca wynik (obsługa bardzo małych i dużych rzędów wielkości)[cite: 7, 8]
fun formatResult(value: Double): String {
    return if (value != 0.0 && (abs(value) < 1e-4 || abs(value) > 1e7)) {
        String.format(Locale.getDefault(), "%.4e", value)
    } else {
        val numberFormat = NumberFormat.getInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 4
        }
        numberFormat.format(value)
    }
}

@Composable
fun LengthConverterScreen(modifier: Modifier = Modifier) {
    // Stan zachowany przy obrocie ekranu (zgodnie z zaleceniem)[cite: 9]
    var inputString by rememberSaveable { mutableStateOf("") }

    // Zamiana tekstu na liczbę z obsługą błędnego wejścia[cite: 9]
    val metersValue = inputString.toDoubleOrNull()
    val isError = inputString.isNotEmpty() && metersValue == null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Przelicznik miar",
            style = MaterialTheme.typography.headlineMedium
        )

        // Pole tekstowe z klawiaturą numeryczną[cite: 9]
        OutlinedTextField(
            value = inputString,
            onValueChange = { inputString = it },
            label = { Text("Length in metres") },
            suffix = { Text("m") },
            isError = isError,
            supportingText = {
                if (isError) {
                    Text("Wpisz poprawną liczbę (np. przecinek lub kropka)")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Wyświetlanie wyników dla wszystkich jednostek za pomocą iteracji po enum[cite: 9]
        LengthUnit.values().forEach { unit ->
            ResultCard(
                unitName = unit.label,
                symbol = unit.symbol,
                resultText = if (metersValue != null) {
                    formatResult(convert(metersValue, unit))
                } else {
                    "—" // Czytelny stan początkowy lub przy błędzie
                }
            )
        }
    }
}

@Composable
fun ResultCard(unitName: String, symbol: String, resultText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = unitName, style = MaterialTheme.typography.bodyMedium)
                Text(text = symbol, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = resultText,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}