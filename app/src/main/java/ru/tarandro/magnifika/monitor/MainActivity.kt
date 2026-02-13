package ru.tarandro.magnifika.monitor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start the service when the UI is opened
        startMonitorService()

        setContent {
            MaterialTheme {
                MonitorSettingsScreen(context = this)
            }
        }
    }
}

@Composable
fun MonitorSettingsScreen(context: Context) {
    val prefs = AppSettings.getPrefs(context)
    
    var zone by remember { mutableStateOf(prefs.getInt(AppSettings.DEFAULT_ZONE, 1).toString()) }
    var coordX by remember { mutableStateOf(prefs.getFloat(AppSettings.CLICK_X, 0f).toString()) }
    var coordY by remember { mutableStateOf(prefs.getFloat(AppSettings.CLICK_Y, 0f).toString()) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Настройки Sampi Bridge", style = MaterialTheme.typography.h5)

        OutlinedTextField(
            value = zone,
            onValueChange = { zone = it },
            label = { Text("Номер GPIO зоны") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = coordX,
                onValueChange = { coordX = it },
                label = { Text("X") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = coordY,
                onValueChange = { coordY = it },
                label = { Text("Y") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(onClick = {
            val zoneInt = zone.toIntOrNull() ?: 1
            AppSettings.saveConfig(context, zoneInt, coordX.toFloatOrNull() ?: 0f, coordY.toFloatOrNull() ?: 0f)
            Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()
        }) {
            Text("Сохранить")
        }

        Button(onClick = {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }) {
            Text("Открыть Спец. возможности")
        }
    }
}
