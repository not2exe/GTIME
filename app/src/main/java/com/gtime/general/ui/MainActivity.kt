package com.gtime.general.ui


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.antip.R
import com.example.antip.databinding.ActivityMainBinding
import com.example.antip.databinding.NavHeaderMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gtime.general.Cache
import com.gtime.general.Constants
import com.gtime.general.app.App
import com.gtime.online_mode.data.model.AccountInfoModel
import com.gtime.online_mode.data.AccountRepository
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var idIntent: Intent

    @Inject
    lateinit var launcher: ActivityResultLauncher<Intent>

    @Inject
    lateinit var cache: Cache


    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as App).appComponent.activity().create(this).inject(this)
        if (cache.isOnline()) {
            setTheme(R.style.Theme_GTIME_Online)
        } else {
            setTheme(R.style.Theme_GTIME_Offline)
        }
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        val navController = findNavController(R.id.fcvMainContainer)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navigationView = binding.navView
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)
        val headerBinding = NavHeaderMainBinding.bind(navigationView.getHeaderView(0))

        setObservers(headerBinding, binding)
        setListeners(headerBinding)
        setSwitch(headerBinding)
    }

    private fun setSwitch(binding: NavHeaderMainBinding) = with(binding) {
        switchOnlineMode.isChecked = cache.isOnline()
        switchOnlineMode.setOnCheckedChangeListener { _, b ->
            cache.setOnline(b)
            animRecreate()
        }
    }

    private fun setObservers(
        binding: NavHeaderMainBinding,
        activityMainBinding: ActivityMainBinding,
    ) = with(binding) {
        accountRepository.accountInfoModel.observe(this@MainActivity) { acc ->
            if (acc.name == "" || !acc.isFirebaseAuth) {
                userInfoUnsaved(this)
            } else {
                userInfoSaved(this, acc)
            }
        }
        cache.onlineLiveData.observe(this@MainActivity) { isOnline ->
            if (isOnline) {
                onlineMenu(activityMainBinding)
            } else {
                offlineMenu(activityMainBinding, this)
            }
        }
    }

    private fun setListeners(binding: NavHeaderMainBinding) {
        binding.authWithIDButton.setOnClickListener {
            launcher.launch(idIntent)
        }
        binding.loginOutButton.setOnClickListener {
            accountRepository.clearAccountInfo()
            Firebase.auth.signOut()
        }
    }

    private fun animRecreate() {
        val tempBundle = Bundle()
        intent.putExtra(Constants.BUNDLE, tempBundle)
        finish()
        overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )
        startActivity(intent)
    }

    private fun onlineMenu(binding: ActivityMainBinding) = with(binding) {
        navView.apply {
            menu.clear()
            inflateMenu(R.menu.menu_online)
        }
    }

    private fun offlineMenu(activityBinding: ActivityMainBinding, binding: NavHeaderMainBinding) =
        with(binding) {
            activityBinding.navView.apply {
                menu.clear()
                inflateMenu(R.menu.menu_offline)
            }
            loginOutButton.visibility = View.GONE
            emailTv.visibility = View.GONE
            authWithIDButton.visibility = View.GONE
        }

    private fun userInfoUnsaved(binding: NavHeaderMainBinding) = with(binding) {
        nameTv.visibility = View.GONE
        emailTv.visibility = View.GONE
        iconIv.visibility = View.GONE
        loginOutButton.visibility = View.GONE
        authWithIDButton.visibility = View.VISIBLE
    }

    private fun userInfoSaved(binding: NavHeaderMainBinding, accountInfoModel: AccountInfoModel) =
        with(binding) {
            nameTv.text = accountInfoModel.name
            emailTv.text = accountInfoModel.email
            Glide.with(iconIv).load(accountInfoModel.urlAvatar + Constants.AVATAR_URL_68_END)
                .centerCrop().into(iconIv)
            nameTv.visibility = View.VISIBLE
            emailTv.visibility = View.VISIBLE
            iconIv.visibility = View.VISIBLE
            authWithIDButton.visibility = View.GONE
            loginOutButton.visibility = View.VISIBLE
        }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fcvMainContainer)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
