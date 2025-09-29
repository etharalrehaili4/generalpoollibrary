package com.example.auth.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavController
import com.example.auth.ui.model.CardUi
import com.example.auth.ui.model.LoginUiState
import com.example.auth.ui.viewmodel.LoginViewModel

@Composable
fun phoneLoginLayout(
    navController: NavController,
    ui: LoginUiState,
    viewModel: LoginViewModel,
    focusManager: FocusManager,
    cardUi: CardUi,
) {
    loginScaffold(
        card = cardUi,
        messageRes = ui.message,
        messageText = ui.errorMessage,
    ) {
        authFields(
            navController = navController,
            ui = ui,
            onUsername = viewModel::updateUsername,
            onPassword = viewModel::updatePassword,
            onSubmit = {
                focusManager.clearFocus()
                viewModel.submit()
            },
        )
    }
}