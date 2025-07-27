package com.nullpointer.squad.presentation.splashScreen

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nullpointer.squad.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Option 1: Using XML layout (Recommended)
        // return inflater.inflate(R.layout.fragment_splash, container, false)

        // Option 2: Programmatic approach with logo
        val root = FrameLayout(requireContext()).apply {
            setBackgroundColor(Color.WHITE) // White background
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Container for logo and text
        val logoContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        // Logo ImageView
        val logoImageView = ImageView(requireContext()).apply {
            setImageResource(R.drawable.trendora) // Your logo drawable
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(340), // Width in dp
                dpToPx(340)  // Height in dp
            ).apply {
                bottomMargin = dpToPx(16)
            }
        }


        // Add views to container
        logoContainer.addView(logoImageView)


        // Add container to root
        root.addView(logoContainer)

        // Navigate after delay
        handler.postDelayed({
            if (isAdded) { // Check if fragment is still attached
                findNavController().navigate(R.id.action_splashFragment_to_productListFragment)
            }
        }, 2500) // Increased to 2.5 seconds for better logo visibility

        return root
    }

    // Helper function to convert dp to pixels
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}