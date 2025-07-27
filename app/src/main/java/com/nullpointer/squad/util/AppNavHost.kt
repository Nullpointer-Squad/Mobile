package com.nullpointer.squad.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nullpointer.squad.presentation.productDetail.view.ProductDetailRoot
import com.nullpointer.squad.presentation.productList.ProductListViewModel
import com.nullpointer.squad.presentation.productList.intent.NavigationEvent
import com.nullpointer.squad.presentation.productList.intent.ProductListAction
import com.nullpointer.squad.presentation.productList.view.ProductListScreen

@Composable
fun AppNavHost(viewModel: ProductListViewModel) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()



    LaunchedEffect(state.navigationEvent) {
        when (val event = state.navigationEvent) {
            is NavigationEvent.NavigateToProductDetail -> {
                navController.navigate("product_detail/${event.productId}")
                viewModel.clearNavigationEvent()
            }
            NavigationEvent.NavigateBack -> {
                navController.popBackStack()
                viewModel.clearNavigationEvent()
            }
            null -> { /* No navigation event */ }
        }
    }
    NavHost(
        navController = navController,
        startDestination = "product_list"
    ) {
        composable("product_list") {
            ProductListScreen(
                viewModel = viewModel,
                onAction = {
                    when (it) {
                        is ProductListAction.NavigateToProductDetail -> {
                            navController.navigate("product_detail/${it.productId}")
                        }
                        else -> viewModel.onAction(it)
                    }
                }
            )
        }

        composable(
            route = "product_detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailRoot(
                viewModel = viewModel,
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

