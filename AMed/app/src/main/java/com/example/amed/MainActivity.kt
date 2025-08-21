package com.example.amed

import android.app.Application
import androidx.lifecycle.viewmodel.compose.viewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.amed.p1.PacienteScreen
import com.example.amed.p1.PacienteViewModel
import com.example.amed.p1.PruebaScreen
import com.example.amed.ui.theme.AMedTheme
import com.example.amed.p1.BleViewModel
import com.example.amed.p1.HistorialScreen
import com.example.amed.p1.PruebaDetalleScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AMedTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

object AppDestinations {
    const val PACIENTE_SCREEN_ROUTE = "paciente_screen"
    const val PRUEBA_SCREEN_ROUTE = "prueba_screen"
    const val HISTORIAL_SCREEN_ROUTE = "historial_screen"
    const val PRUEBA_DETALLE_ROUTE = "prueba_detalle_screen"
    const val PACIENTE_ID_ARG = "pacienteId"
    const val PRUEBA_ID_ARG = "pruebaId"
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val pacienteViewModel: PacienteViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
    )
    val bleViewModel: BleViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
    )
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.PACIENTE_SCREEN_ROUTE,
        modifier = modifier
    ) {
        composable(route = AppDestinations.PACIENTE_SCREEN_ROUTE) {
            PacienteScreen(
                viewModel = pacienteViewModel,
                onVerHistorial = { paciente ->
                    navController.navigate("${AppDestinations.HISTORIAL_SCREEN_ROUTE}/${paciente.id}")
                },
                onEditar = { },
                onHacerPrueba = { paciente ->
                    navController.navigate("${AppDestinations.PRUEBA_SCREEN_ROUTE}/${paciente.id}")
                }
            )
        }

        composable(
            route = "${AppDestinations.PRUEBA_SCREEN_ROUTE}/{${AppDestinations.PACIENTE_ID_ARG}}",
            arguments = listOf(navArgument(AppDestinations.PACIENTE_ID_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getInt(AppDestinations.PACIENTE_ID_ARG) ?: -1
            PruebaScreen(
                pacienteViewModel = pacienteViewModel,
                bleViewModel = bleViewModel,
                pacienteId = pacienteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${AppDestinations.HISTORIAL_SCREEN_ROUTE}/{${AppDestinations.PACIENTE_ID_ARG}}",
            arguments = listOf(navArgument(AppDestinations.PACIENTE_ID_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getInt(AppDestinations.PACIENTE_ID_ARG) ?: -1
            HistorialScreen(
                pacienteViewModel = pacienteViewModel,
                pacienteId = pacienteId,
                onNavigateBack = { navController.popBackStack() },
                onVerDetalle = { prueba ->
                    navController.navigate("${AppDestinations.PRUEBA_DETALLE_ROUTE}/${prueba.id}")
                }
            )
        }

        composable(
            route = "${AppDestinations.PRUEBA_DETALLE_ROUTE}/{${AppDestinations.PRUEBA_ID_ARG}}",
            arguments = listOf(navArgument(AppDestinations.PRUEBA_ID_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val pruebaId = backStackEntry.arguments?.getInt(AppDestinations.PRUEBA_ID_ARG) ?: -1
            PruebaDetalleScreen(
                pacienteViewModel = pacienteViewModel,
                pruebaId = pruebaId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}