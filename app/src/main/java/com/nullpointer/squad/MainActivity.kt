package com.nullpointer.squad

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
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
