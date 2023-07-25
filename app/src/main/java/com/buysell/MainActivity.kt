package com.buysell


import SharedPref
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.buysell.base.BaseActivity
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.ActivityMainBinding
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.facebook.share.Share
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: BaseActivity() {
    var bottom: CardView? = null
    private var navController: NavController? = null
    private lateinit var binding: ActivityMainBinding
    private var GUEST_USER: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        GUEST_USER = SharedPref(this).getBoolean(Constant.GUEST_LOGIN_STATUS)
        init()
//        1=appleId,2=facebook,3=googleId,4=normalUser,5= guest
    }
    companion object {
        // var navListener: NavigationListener? = null
        lateinit var context: WeakReference<Context>
        lateinit var activity: WeakReference<Activity>
    }

    override fun onStart() {
        super.onStart()
        context = WeakReference(this)
        activity=WeakReference(this)
    }


    private fun init() {

        getKeyhash()
        binding.apply {
            bottom = findViewById(R.id.bottomNavMain)
            navController = Navigation.findNavController(this@MainActivity, R.id.containerr)
            val loginStauts = SharedPref(this@MainActivity).getBoolean(Constant.LOGIN_STATUS)
            if (loginStauts) {
                navController?.popBackStack(R.id.welcomeFragment, true)
                navController?.navigate(R.id.homeFragment)
            }
            navController?.addOnDestinationChangedListener { controller, destination, arguments ->
                when (destination.id) {
                    R.id.homeFragment -> {
                        bottom?.visibility = View.VISIBLE
                        postIconMain.iconPostt.visibility = View.VISIBLE
                        selectHome(true)
                        selectChat(false)
                        selectMyAds(false)
                        selectAccount(false)
                    }

                    R.id.chatUserFragment -> {
                        bottom?.visibility = View.VISIBLE
                        postIconMain.iconPostt.visibility = View.VISIBLE
                        selectHome(false)
                        selectChat(true)
                        selectMyAds(false)
                        selectAccount(false)
                    }
                    R.id.adsFragment -> {
                        bottom?.visibility = View.VISIBLE
                        postIconMain.iconPostt.visibility = View.VISIBLE
                        selectHome(false)
                        selectChat(false)
                        selectMyAds(true)
                        selectAccount(false)
                    }
                    R.id.guestAccountFragment -> {
                        bottom?.visibility = View.VISIBLE
                        postIconMain.iconPostt.visibility = View.VISIBLE
                        selectHome(false)
                        selectChat(false)
                        selectMyAds(false)
                        selectAccount(true)
                    }
                    R.id.accountFragment -> {
                        bottom?.visibility = View.VISIBLE
                        postIconMain.iconPostt.visibility = View.VISIBLE
                        selectHome(false)
                        selectChat(false)
                        selectMyAds(false)
                        selectAccount(true)
                    }

                    else -> {
                        bottom?.visibility = View.GONE
                        postIconMain.iconPostt.visibility = View.GONE
                    }
                }
            }

            clickListener()
        }
    }

     fun clickListener() {

             binding.apply {
                 bottomNavMain.homeLinearLayout.setOnClickListener {
                     if(SharedPref(this@MainActivity).getBoolean(Constant.BOTTOM_BAR_CLICK)) {
                         lifecycleScope.launchWhenResumed {
                             if (navController?.currentDestination?.id != R.id.homeFragment) {
                                 navController?.popBackStack(R.id.homeFragment, false)
                             }
                         }
                     }
                 }
                 bottomNavMain.chatLinearLayout.setOnClickListener {
                             GUEST_USER = SharedPref(this@MainActivity).getBoolean(Constant.GUEST_LOGIN_STATUS)

                         if(SharedPref(this@MainActivity).getBoolean(Constant.BOTTOM_BAR_CLICK)) {
                         if(GUEST_USER){
                             Log.i("asdfnlasndf","guest clickc")
                             lifecycleScope.launchWhenResumed {
                                 if (navController?.currentDestination?.id != R.id.chatUserFragment) {
                                     navController?.popBackStack(R.id.homeFragment, false)
                                     navController?.navigate(R.id.guestAccountFragment)
                                 }
                             }
                         }else {
                             lifecycleScope.launchWhenResumed {
                                 if (navController?.currentDestination?.id != R.id.chatUserFragment) {
                                     navController?.popBackStack(R.id.homeFragment, false)
                                     navController?.navigate(R.id.chatUserFragment)
                                 }
                             }
                         }
                     }
                 }
                 bottomNavMain.myAdsLinearLayout.setOnClickListener {
                     if(SharedPref(this@MainActivity).getBoolean(Constant.BOTTOM_BAR_CLICK)) {
                         GUEST_USER = SharedPref(this@MainActivity).getBoolean(Constant.GUEST_LOGIN_STATUS)

                         if(GUEST_USER){
                             lifecycleScope.launchWhenResumed {
                                 if (navController?.currentDestination?.id != R.id.chatUserFragment) {
                                     navController?.popBackStack(R.id.homeFragment, false)
                                     navController?.navigate(R.id.guestAccountFragment)
                                 }
                             }
                         }else {
                             lifecycleScope.launchWhenResumed {
                                 if (navController?.currentDestination?.id != R.id.adsFragment) {
                                     navController?.popBackStack(R.id.homeFragment, false)
                                     navController?.navigate(R.id.adsFragment)
                                 }
                             }
                         }
                     }
                 }
                 bottomNavMain.accountLinearLayout.setOnClickListener {
                     GUEST_USER = SharedPref(this@MainActivity).getBoolean(Constant.GUEST_LOGIN_STATUS)

                     if(SharedPref(this@MainActivity).getBoolean(Constant.BOTTOM_BAR_CLICK)) {
                         lifecycleScope.launchWhenResumed {
                             if (navController?.currentDestination?.id != R.id.accountFragment) {

                                 if (GUEST_USER) {
                                     navController?.popBackStack(R.id.homeFragment, false)
                                     navController?.navigate(R.id.guestAccountFragment)

                                 } else {
                                     navController?.popBackStack(R.id.homeFragment, false)
                                     navController?.navigate(R.id.accountFragment)

                                 }
                             }
                         }
                     }
                 }
                 postIconMain.iconPostt.setOnClickListener {
                     GUEST_USER = SharedPref(this@MainActivity).getBoolean(Constant.GUEST_LOGIN_STATUS)

                     if(SharedPref(this@MainActivity).getBoolean(Constant.BOTTOM_BAR_CLICK)) {
                         if (GUEST_USER) {
                             lifecycleScope.launchWhenResumed {
                                 if (navController?.currentDestination?.id != R.id.chatUserFragment) {
                                     navController?.popBackStack(R.id.homeFragment, false)
                                     navController?.navigate(R.id.guestAccountFragment)
                                 }
                             }
                         } else {
                             lifecycleScope.launchWhenResumed {
                                 if (navController?.currentDestination?.id != R.id.categoryFragment) {

                                     navController?.popBackStack(R.id.homeFragment, false)
                                     navController?.navigate(R.id.categoryFragment)
                                 }
                             }
                         }
                     }
                 }
                 bottomNavMain.postLinearLayout.setOnClickListener {
                     GUEST_USER = SharedPref(this@MainActivity).getBoolean(Constant.GUEST_LOGIN_STATUS)

                     if (SharedPref(this@MainActivity).getBoolean(Constant.BOTTOM_BAR_CLICK)) {
                         if (GUEST_USER) {
                             lifecycleScope.launchWhenResumed {
                                 if (navController?.currentDestination?.id != R.id.chatUserFragment) {
                                     navController?.popBackStack(R.id.homeFragment, false)
                                     navController?.navigate(R.id.guestAccountFragment)
                                 }
                             }
                         } else {
                             lifecycleScope.launchWhenResumed {
                                 if (navController?.currentDestination?.id != R.id.categoryFragment) {
                                     navController?.popBackStack(R.id.homeFragment, false)
                                     navController?.navigate(R.id.categoryFragment)
                                 }
                             }
                         }
                     }
                 }
             }

    }

    private fun selectHome(state: Boolean) {
        if (state) {
            binding.bottomNavMain.imgHomeIcon.setColorFilter(ContextCompat.getColor(this@MainActivity,
                R.color.signUpBtnColor))
            binding.bottomNavMain.txtHome.setTextColor(ContextCompat.getColor(this@MainActivity,
                R.color.signUpBtnColor))

        } else {
            binding.bottomNavMain.imgHomeIcon.setColorFilter(ContextCompat.getColor(this@MainActivity,
                R.color.bottomNotSelected))
            binding.bottomNavMain.txtHome.setTextColor(ContextCompat.getColor(this@MainActivity,
                R.color.bottomNotSelected))
        }
    }

    private fun selectChat(state: Boolean) {
        if (state) {
            binding.bottomNavMain.imgChatIcon.setColorFilter(ContextCompat.getColor(this@MainActivity,
                R.color.signUpBtnColor))
            binding.bottomNavMain.txtChat.setTextColor(ContextCompat.getColor(this@MainActivity,
                R.color.signUpBtnColor))

        } else {
            binding.bottomNavMain.imgChatIcon.setColorFilter(ContextCompat.getColor(this@MainActivity,
                R.color.bottomNotSelected))
            binding.bottomNavMain.txtChat.setTextColor(ContextCompat.getColor(this@MainActivity,
                R.color.bottomNotSelected))
        }
    }

    private fun selectMyAds(state: Boolean) {
        if (state) {
            binding.bottomNavMain.imgHeartIcon.setColorFilter(ContextCompat.getColor(this@MainActivity,
                R.color.black))
            binding.bottomNavMain.txtMyAds.setTextColor(ContextCompat.getColor(this@MainActivity,
                R.color.signUpBtnColor))

        } else {
            binding.bottomNavMain.imgHeartIcon.setColorFilter(ContextCompat.getColor(this@MainActivity,
                R.color.bottomNotSelected))
            binding.bottomNavMain.txtMyAds.setTextColor(ContextCompat.getColor(this@MainActivity,
                R.color.bottomNotSelected))
        }
    }

    private fun selectAccount(state: Boolean) {
        if (state) {
            binding.bottomNavMain.imgAccountIcon.setColorFilter(ContextCompat.getColor(this@MainActivity,
                R.color.signUpBtnColor))
            binding.bottomNavMain.txtAccount.setTextColor(ContextCompat.getColor(this@MainActivity,
                R.color.signUpBtnColor))

        } else {
            binding.bottomNavMain.imgAccountIcon.setColorFilter(ContextCompat.getColor(this@MainActivity,
                R.color.bottomNotSelected))
            binding.bottomNavMain.txtAccount.setTextColor(ContextCompat.getColor(this@MainActivity,
                R.color.bottomNotSelected))
        }
        }


    private fun getKeyhash() {
        try {
            val info = packageManager.getPackageInfo(
                "com.buysell", PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }

    }


}