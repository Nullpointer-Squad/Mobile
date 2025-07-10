package com.nullpointer.squad.presentation.splashScreen

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nullpointer.squad.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("splash Fragment", "onCreateView: done ")
        val root = FrameLayout(requireContext()).apply {
            setBackgroundColor(android.graphics.Color.WHITE)
            addView(TextView(context).apply {
                text = "NullPointer Squad"
                gravity = Gravity.CENTER
                textSize = 24f
            })
        }

        android.os.Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_splashFragment_to_productListFragment)
        }, 2000)

        return root
    }
}
