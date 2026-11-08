package com.avengers.ai.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avengers.ai.AppContainer
import com.avengers.ai.AvengersApp
import com.avengers.ai.vm.vmFactory

@Composable
inline fun <reified T : ViewModel> tonyVm(key: String? = null, crossinline create: (AppContainer) -> T): T {
    val c = (LocalContext.current.applicationContext as AvengersApp).container
    return viewModel<T>(key = key, factory = vmFactory { create(c) })
}

@Composable
fun themeState(): androidx.compose.runtime.State<String> {
    val c = (LocalContext.current.applicationContext as AvengersApp).container
    return c.settings.theme.collectAsState()
}
