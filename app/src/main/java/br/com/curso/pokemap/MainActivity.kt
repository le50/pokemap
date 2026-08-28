package br.com.curso.pokemap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokeMapTheme {
                PokeMapApp()
            }
        }
    }
}

/**
 * Tema do PokeMap com paleta verde neon e fundo escuro
 */
@Composable
fun PokeMapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF00E676),
            secondary = Color(0xFF00B0FF),
            background = Color(0xFF121212)
        ),
        content = content
    )
}

/**
 * Gerenciador reativo de permissão de localização (GPS)
 */
@Composable
fun PokeMapApp() {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Registra callback de resposta da permissão
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasLocationPermission = granted
        }
    )

    // Lança a solicitação ao iniciar
    LaunchedEffect(key1 = true) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (hasLocationPermission) {
        PokeMapScreen()
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Permissão de Localização Necessária",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                    Text("Conceder Permissão")
                }
            }
        }
    }
}

/**
 * Tela principal do mapa com marcadores de ginásios e painel de desafio
 */
@Composable
fun PokeMapScreen() {
    val context = LocalContext.current
    val saoPaulo = LatLng(-23.5505, -46.6333)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(saoPaulo, 15f)
    }

    // Estado para controlar qual marcador foi clicado
    var selectedGym by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true), // Ativa o ponto azul GPS
            uiSettings = MapUiSettings(myLocationButtonEnabled = true)
        ) {
            // Ginásio 1
            Marker(
                state = MarkerState(position = LatLng(-23.5505, -46.6333)),
                title = "Ginásio Central",
                snippet = "Líder: Brock | Tipo: Rocha",
                onClick = {
                    selectedGym = "Ginásio Central"
                    false // false mantém o comportamento padrão de abrir o info window
                }
            )

            // Ginásio 2
            Marker(
                state = MarkerState(position = LatLng(-23.5520, -46.6350)),
                title = "Ginásio Aquático",
                snippet = "Líder: Misty | Tipo: Água",
                onClick = {
                    selectedGym = "Ginásio Aquático"
                    false
                }
            )
        }

        // Overlay de Informação do Ginásio Selecionado
        if (selectedGym != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = selectedGym!!,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color(0xFF00E676)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Deseja desafiar este ginásio?",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { selectedGym = null }) {
                            Text("CANCELAR", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                // Sem sistema de batalha real neste projeto (fica para o P10);
                                // o Toast garante feedback visível em vez de um clique silencioso
                                Toast.makeText(
                                    context,
                                    "Batalha contra $selectedGym iniciada!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                selectedGym = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                        ) {
                            Text("BATALHAR", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}