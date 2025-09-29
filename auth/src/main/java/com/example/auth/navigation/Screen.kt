package com.example.auth.navigation

sealed class Screen(
    val route: String,
) {
    object Login : Screen("login_screen")

    // ---------- Main Screen ----------

    object Drawer : Screen("root_drawer")


    data object Logout : Screen("logout_screen")

    // ---------- Notification & Settings ----------

    object Notifications : Screen("notifications_screen")

    object Settings : Screen("settings_screen")

    // ---------- Details ----------
    object OrderDetails : Screen("order_details/{orderId}") {
        fun route(orderId: Long) = "order_details/$orderId"
    }
}
