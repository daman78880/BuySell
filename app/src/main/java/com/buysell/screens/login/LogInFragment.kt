package com.buysell.screens.login

import SharedPref
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.databinding.FragmentLogInBinding
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.facebook.*
import com.facebook.FacebookSdk.getApplicationContext
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import hideKeyBoard
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import setToolBar


@AndroidEntryPoint
class LogInFragment : Fragment() {
    private val RC_SIGN_IN: Int = 211
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var binding: FragmentLogInBinding
    lateinit var callbackManager: CallbackManager
    private val viewModelLogin: ViewModelLogin by viewModels()
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // firebase auth
        auth = Firebase.auth
        // Intilize facebook login or google login
        FacebookSdk.sdkInitialize(getApplicationContext())
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        // Initialize facebook login or google login
        AppEventsLogger.activateApp(requireActivity().application)
        callbackManager = CallbackManager.Factory.create()
        setToolBar(binding.appBar, requireContext(), false, false, "", 0f)
        setSpanText()
        clickListeners()
        fbIntegrationCallBack()
        binding.apply {
            model = viewModelLogin
            // For request focus change
            viewModelLogin.requestFouce.observe(viewLifecycleOwner, Observer {
                val value = it
                if (value != null) {
                    when (value) {
                        0 -> {
                            etEmailLogin.requestFocus()
                        }
                        1 -> {
                            etPasswordLogin.requestFocus()
                        }
                        else -> {
                            Toast.makeText(requireContext(), "Else part 4", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            })
            // For getting login with email api response
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModelLogin.logInResponse.collect { data ->
                    when (data) {
                        is ApiState.Loading -> {
                            Extentions.showProgress(requireContext())
                        }
                        is ApiState.Failure -> {
                            Extentions.stopProgress()
                            if (data.msg is HttpException) {
                                val res = Extentions.getFailedMsg(data.msg)
                                Log.i(TAG, "Error ${res?.message}")
                                Toast.makeText(
                                    requireContext(),
                                    "${res?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.i(TAG, "else Error ${data.msg}")
                                Toast.makeText(
                                    requireContext(),
                                    "Failed due to ${data.msg}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is ApiState.Success -> {
                            Log.i(TAG, "Login success ${data.data}")
                            val response = data.data as JsonObject
                            val data: PojoLogin = Gson().fromJson(response, PojoLogin::class.java)
                            if (data.status == 200) {
                                if (data.success) {
                                    Log.i(TAG, "login data is $data")
                                    auth.signInAnonymously()
                                        .addOnCompleteListener(requireActivity()) { task ->
                                            if (task.isSuccessful) {
                                                Extentions.stopProgress()
                                                val user = auth.currentUser
                                                Log.i(TAG, "Anomous Sign successfull $user")
                                                val topicId = "topic_" + data.data.id.toString()
                                                SharedPref(requireContext()).saveBoolean(
                                                    Constant.LOGIN_STATUS,
                                                    true
                                                )
                                                SharedPref(requireContext()).saveBoolean(
                                                    Constant.GUEST_LOGIN_STATUS,
                                                    false
                                                )
                                                SharedPref(requireContext()).saveString(
                                                    Constant.TOKEN_kEY,
                                                    "bearer " + data.token
                                                )
                                                Extentions.saveLoginData(
                                                    requireContext(),
                                                    data.data
                                                )
                                                lifecycleScope.launchWhenResumed {
                                                    if (findNavController().currentDestination?.id == R.id.logInFragment) {
                                                        findNavController().navigate(R.id.action_logInFragment_to_homeFragment)
                                                    }
                                                }
                                            } else {
                                                Extentions.stopProgress()
                                                Log.w(
                                                    TAG,
                                                    "signInAnonymously:failure" + task.exception
                                                )
                                                Toast.makeText(
                                                    requireContext(), "Authentication failed.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                            } else if (data.status == 201) {
                                Extentions.stopProgress()
                                Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        is ApiState.Empty -> {

                        }
                    }
                }
            }
            // For getting login with social media api response
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModelLogin.facebookResponse.collect { data ->
                    when (data) {
                        is ApiState.Loading -> {
                            Extentions.showProgress(requireContext())
                        }
                        is ApiState.Failure -> {
                            Extentions.stopProgress()
                            if (data.msg is HttpException) {
                                val res = Extentions.getFailedMsg(data.msg)
                                Log.i(TAG, "Error ${res?.message}")
                                Toast.makeText(
                                    requireContext(),
                                    "${res?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.i(TAG, "else Error ${data.msg}")
                                Toast.makeText(
                                    requireContext(),
                                    "Failed due to ${data.msg}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is ApiState.Success -> {
                            val dataa = data.data as JsonObject
                            val response: PojoLogin = Gson().fromJson(dataa, PojoLogin::class.java)
                            if (response.success) {
                                Log.i(TAG, "Response is $response")
                                auth.signInAnonymously()
                                    .addOnCompleteListener(requireActivity()) { task ->
                                        if (task.isSuccessful) {
                                            Extentions.stopProgress()
                                            val user = auth.currentUser
                                            Log.i(TAG, "Anomous Sign successfull $user")
                                            SharedPref(requireContext()).saveBoolean(
                                                Constant.GUEST_LOGIN_STATUS,
                                                false
                                            )
                                            SharedPref(requireContext()).saveString(
                                                Constant.TOKEN_kEY,
                                                "bearer " + response.token
                                            )
                                            SharedPref(requireContext()).saveBoolean(
                                                Constant.LOGIN_STATUS,
                                                true
                                            )
                                            Extentions.saveLoginData(
                                                requireContext(),
                                                response.data
                                            )
                                            lifecycleScope.launchWhenResumed {
                                                if (findNavController().currentDestination?.id == R.id.logInFragment) {
                                                    findNavController().navigate(R.id.action_logInFragment_to_homeFragment)
                                                }
                                            }
                                        } else {
                                            Extentions.stopProgress()
                                            Log.w(TAG, "signInAnonymously:failure" + task.exception)
                                            Toast.makeText(
                                                requireContext(), "Authentication failed.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                            } else {
                                Extentions.stopProgress()
                            }
                        }
                        is ApiState.Empty -> {

                        }

                    }
                }
            }
        }
    }


    private fun clickListeners() {
        binding.apply {

            logInFragment.setOnClickListener {
                requireActivity().hideKeyBoard()
            }
            imgGoogle.setOnClickListener {
                googleSignUp()
            }
            imgFacebook.setOnClickListener {
                LoginManager.getInstance().loginBehavior.allowsWebViewAuth()
                LoginManager.getInstance().loginBehavior.allowsFacebookLiteAuth()
                LoginManager.getInstance().loginBehavior.allowsDeviceAuth()
                LoginManager.getInstance().loginBehavior.allowsCustomTabAuth()
                LoginManager.getInstance().loginBehavior.allowsKatanaAuth()
                // If want to fb login token expired or not then use this function below
//                val flag=isLoggedIn()
//                if(flag ) {
                LoginManager.getInstance().logInWithReadPermissions(
                    this@LogInFragment,
                    listOf("openid,public_profile", "email")
                )
//                }
//                else{
//                    LoginManager.getInstance().logOut()
//                }
            }

        }
    }

    // Call back of facebook integation response
    private fun fbIntegrationCallBack() {
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val request = GraphRequest.newMeRequest(
                    loginResult.accessToken, object : GraphRequest.GraphJSONObjectCallback {
                        override fun onCompleted(obj: JSONObject?, response: GraphResponse?) {
                            if (obj != null) {
                                try {
                                    val jsonObject = JsonObject()
                                    jsonObject.addProperty("loginType", 2)
                                    jsonObject.addProperty("deviceType", "android")
                                    if (obj.has("id")) {
                                        val facebookId = obj.getString("id")
                                        jsonObject.addProperty("socialmediaId", facebookId)
                                    } else {
                                        Log.i(TAG, "No id")
                                    }
                                    if (obj.has("name")) {
                                        val facebookName = obj.getString("name")
                                        jsonObject.addProperty("name", facebookName)
                                    } else {
                                        Log.i(TAG, "No full name")
                                    }
                                    if (obj.has("picture")) {
                                        val facebookPictureObject = obj.getJSONObject("picture")
                                        if (facebookPictureObject.has("data")) {
                                            val facebookDataObject =
                                                facebookPictureObject.getJSONObject("data")
                                            if (facebookDataObject.has("url")) {
                                                val facebookProfilePicURL =
                                                    facebookDataObject.getString("url")
                                                jsonObject.addProperty(
                                                    "profileUrl",
                                                    facebookProfilePicURL
                                                )
                                            }
                                        }
                                    } else {
                                        Log.i(TAG, "No profile picture")
                                    }
                                    if (obj.has("email")) {
                                        val facebookEmail = obj.getString("email")
                                        jsonObject.addProperty("email", facebookEmail)
                                    } else {
                                        Log.i(TAG, "No email")
                                    }
                                    viewModelLogin.hitLoginWithSocialMediaApi(jsonObject)
                                } catch (e: JSONException) {
                                    Log.i(TAG, "Error : ${e.message}")
                                    e.printStackTrace()
                                } catch (e: NullPointerException) {
                                    Log.i(TAG, "Error : ${e.message}")
                                    e.printStackTrace()
                                } catch (e: Exception) {
                                    Log.i(TAG, "Error : ${e.message}")
                                    e.printStackTrace()
                                }
                            } else {
                                Log.i(TAG, "null")
                            }
                        }
                    })
                val parameters = Bundle()
                parameters.putString(
                    "fields",
                    "id, first_name, middle_name, last_name, name, picture.type(large), email"
                )
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                Toast.makeText(requireContext(), "Login Cancel", Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: FacebookException) {
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    // for set spannable text
    private fun setSpanText() {
        val str = requireContext().resources.getString(R.string.newUserSignUpTxt)
        val ss = SpannableString(str)
        val span2: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                Log.i(TAG, "Click Second")
                (textView as AppCompatTextView).highlightColor = ContextCompat.getColor(
                    requireActivity(),
                    android.R.color.transparent
                )
                findNavController().navigate(R.id.action_logInFragment_to_signUpFragment)
            }

            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = requireActivity().resources.getColor(R.color.black)
                textPaint.isFakeBoldText = true
            }
        }
        ss.setSpan(span2, 9, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.txtSignUpLoginn.text = ss
        binding.txtSignUpLoginn.highlightColor = ContextCompat.getColor(
            requireActivity(),
            android.R.color.transparent
        )
        binding.txtSignUpLoginn.movementMethod = LinkMovementMethod.getInstance()
    }

    // for check if facebook login is token expired or not
    fun isLoggedIn(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null && !accessToken.isExpired
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        // super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    // For getting response of google login
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("socialmediaId", account.id)
            jsonObject.addProperty("email", account.email)
            jsonObject.addProperty("name", account.displayName)
            if (account.photoUrl != null)
                jsonObject.addProperty("profileUrl", account.photoUrl.toString())
            jsonObject.addProperty("loginType", 3)
            jsonObject.addProperty("deviceType", "android")
            if (account.idToken != null)
                jsonObject.addProperty("socialToken", account.idToken)
            viewModelLogin.hitLoginWithSocialMediaApi(jsonObject)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed whole content=" + e)
            Log.w(TAG, "signInResult:failed message=" + e.localizedMessage)
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun googleSignUp() {
        // if always want select id of gmail then use this other wise below function use
        signOutGmail()
//        val signInIntent = mGoogleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
//        updateUI(account)
    }

    // for gmail SignOut then login
    private fun signOutGmail() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(requireActivity(), object : OnCompleteListener<Void> {
                override fun onComplete(p0: Task<Void>) {
                    if (p0.isSuccessful) {
                        val signInIntent = mGoogleSignInClient.signInIntent
                        startActivityForResult(signInIntent, RC_SIGN_IN)
                    } else {
                        val signInIntent = mGoogleSignInClient.signInIntent
                        startActivityForResult(signInIntent, RC_SIGN_IN)
                    }
                }
            })
    }


}