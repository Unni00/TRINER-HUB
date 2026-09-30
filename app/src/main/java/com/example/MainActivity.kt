package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CoachFitViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.client.ClientMainScreen
import com.example.ui.theme.CoachFitTheme
import com.example.ui.trainer.TrainerMainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoachFitTheme {
                val viewModel: CoachFitViewModel = viewModel()
                CoachFitApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CoachFitApp(
    viewModel: CoachFitViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Crossfade(
        targetState = currentUser,
        label = "auth_routing",
        modifier = modifier.fillMaxSize()
    ) { user ->
        when {
            user == null -> {
                AuthScreen(viewModel = viewModel)
            }
            user.isTrainer -> {
                TrainerMainScreen(viewModel = viewModel)
            }
            else -> {
                ClientMainScreen(viewModel = viewModel)
            }
        }
    }
}
