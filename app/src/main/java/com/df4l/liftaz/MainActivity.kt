package com.df4l.liftaz

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.df4l.liftaz.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val fragmentsAvecNav = listOf(
            R.id.navigation_soulever,
            R.id.navigation_manger,
            R.id.navigation_stats
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            navView.visibility = if (destination.id !in fragmentsAvecNav) View.GONE else View.VISIBLE
        }

        navView.setupWithNavController(navController)
    }
}