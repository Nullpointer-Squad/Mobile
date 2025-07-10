package com.nullpointer.squad

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import com.nullpointer.squad.ui.theme.NullpointerSquadTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val navHostFragmentId = View.generateViewId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This sets the XML layout containing the FragmentContainerView
        setContentView(R.layout.activity_main)

        // Only create NavHostFragment once
        if (savedInstanceState == null) {
            val navHost = NavHostFragment.create(R.navigation.nav_graph)
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, navHost)
                .setPrimaryNavigationFragment(navHost)
                .commitNow()
        }
    }
}
