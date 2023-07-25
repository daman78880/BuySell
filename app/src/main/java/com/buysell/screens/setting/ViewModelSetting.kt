package com.buysell.screens.setting

import SharedPref
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.repository.RepositoryImplementation
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ViewModelSetting @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    private val deleteAccountResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var deleteAccountResponse: StateFlow<ApiState> = deleteAccountResponseMutable

    fun onClick(view: View) {
        when (view.id) {
            R.id.cLayoutChangePasswordSetting -> {
                view.findNavController().navigate(R.id.action_settingFragment_to_changePasswordFragment)
            }
            R.id.cLayoutLogOutAccountSetting->{
                logoutUserDialog(view,view.context)
            }
            R.id.cLayoutDeleteAccountSetting->{
                deleteUserDialog(view.context)
            }
            R.id.cLayoutPrivacyPolicySetting->{
                Toast.makeText(view.context, "Clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logoutUserDialog(view:View,context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_logout_accout)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(context,
            android.R.color.transparent))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(false)
        val notNowBtn = dialog.findViewById<AppCompatButton>(R.id.btnNotNowLogoutDialog)
        val deleteAccountBtn = dialog.findViewById<AppCompatButton>(R.id.btnLogoutDialog)
        notNowBtn.setOnClickListener {
            dialog.dismiss()
        }
        deleteAccountBtn.setOnClickListener {
            Extentions.unSubscribedTopic(context)
            dialog.dismiss()
            SharedPref(context).clearData()
            view.findNavController().navigate(R.id.action_settingFragment_to_welcomeFragment)
        }
        dialog.show()
    }
    private fun deleteUserDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_delete_accout)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(context,
            android.R.color.transparent))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(false)
        val notNowBtn = dialog.findViewById<AppCompatButton>(R.id.btnNotNowDeleteDialog)
        val deleteAccountBtn = dialog.findViewById<AppCompatButton>(R.id.btnDeleteDeleteDialog)
        notNowBtn.setOnClickListener {
            dialog.dismiss()
        }
        deleteAccountBtn.setOnClickListener {
            val token=SharedPref(context).getString(Constant.TOKEN_kEY)
            hitDeleteAccountApi(token)
            dialog.dismiss()
        }
        dialog.show()
    }
    fun hitDeleteAccountApi(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            deleteAccountResponseMutable.value = ApiState.Loading
            repositoryImplementation.deleteAccount(token)
                .catch { e ->
                    deleteAccountResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    deleteAccountResponseMutable.value = ApiState.Success(data)
                }
        }
    }


}