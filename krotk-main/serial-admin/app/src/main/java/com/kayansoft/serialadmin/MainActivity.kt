package com.kayansoft.serialadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kayansoft.serialadmin.ui.DashboardScreen
import com.kayansoft.serialadmin.ui.LoginScreen
import com.kayansoft.serialadmin.ui.theme.SerialAdminTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SerialAdminTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AdminApp()
                }
            }
        }
    }
}

@Composable
fun AdminApp(viewModel: AdminViewModel = viewModel()) {
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val serials by viewModel.serials.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(successMessage) {
        successMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessages()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (authState) {
                is AuthState.CheckingSession -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AuthState.LoggedOut -> {
                    LoginScreen(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onLogin = { u, p -> viewModel.login(u, p) }
                    )
                }
                is AuthState.LoggedIn -> {
                    DashboardScreen(
                        clients = clients,
                        serials = serials,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshDashboard() },
                        onLogout = { viewModel.logout() },
                        onCreateSerial = { name, network, phone, months, notes, deviceId ->
                            viewModel.createSerial(name, network, phone, months, notes, deviceId)
                        },
                        onToggle = { viewModel.toggleSerial(it) },
                        onReset = { viewModel.resetDeviceLock(it) },
                        onDelete = { viewModel.deleteSerial(it) }
                    )
                }
            }
        }
    }
}
