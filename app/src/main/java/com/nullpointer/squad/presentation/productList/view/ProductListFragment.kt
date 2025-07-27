package com.nullpointer.squad.presentation.productList.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.nullpointer.squad.presentation.productList.ProductListViewModel
import com.nullpointer.squad.util.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductListFragment : Fragment() {
    private lateinit var composeView: ComposeView
    private val viewModel: ProductListViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        composeView = ComposeView(requireContext()).apply {
            id = View.generateViewId()
        }
        return composeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        composeView.setContent {
            MaterialTheme {
                AppNavHost(viewModel = viewModel)
            }
        }
    }
}

