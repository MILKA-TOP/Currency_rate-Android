package proj.stocks

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import proj.stocks.databinding.ActivityMainBinding
import proj.stocks.util.getUserLanguage
import proj.stocks.util.getUserTheme


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferencesUpdate()

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        fragmentNavigationSetup()

    }

    private fun sharedPreferencesUpdate() {
        getUserTheme(this)
        getUserLanguage(this)
    }

    private fun fragmentNavigationSetup() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)
        appBarConfiguration =
            AppBarConfiguration(
                setOf(
                    R.id.full_list_fragment,
                    R.id.favourite_fragment,
                    R.id.settings_fragment
                )
            )
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
}