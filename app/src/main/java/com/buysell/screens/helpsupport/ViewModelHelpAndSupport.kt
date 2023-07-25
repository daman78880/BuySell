package com.buysell.screens.helpsupport

import SharedPref
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.repository.RepositoryImplementation
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions.TAG
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ViewModelHelpAndSupport @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    val email = ObservableField<String>("")
    val subject = ObservableField<String>("")
    val description = ObservableField<String>("")
    private val helpAndSupportResponseMutable: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Empty)
    var helpAndSupportResponse: StateFlow<ApiState> = helpAndSupportResponseMutable

    fun onClick(view: View) {
        when (view.id) {
            R.id.btnHelpAndSupport -> {
                val email = email.get()
                val subject = subject.get()
                val description = description.get()
                if (email.toString().isNotEmpty()) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(email!!).matches()) {
                    if (subject.toString().isNotEmpty()) {
                        if (description.toString().isNotEmpty()) {
                            val jsonToken = JsonObject()
                            jsonToken.addProperty("email", email.trim())
                            jsonToken.addProperty("subject", subject?.trim())
                            jsonToken.addProperty("Description", description)
                            hitHelpAndSupportApi(
                                SharedPref(view.context).getString(Constant.TOKEN_kEY),
                                jsonToken
                            )
                        } else {

                            Toast.makeText(
                                view.context,
                                "Please enter description",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(view.context, "Please enter subject", Toast.LENGTH_SHORT)
                            .show()

                    }
                }
                    else{
                        Toast.makeText(view.context, "Please enter valid email", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(view.context, "Please enter email", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }


    private fun hitHelpAndSupportApi(token: String, jsonObject: JsonObject) {
        viewModelScope.launch {
            helpAndSupportResponseMutable.value = ApiState.Loading
            repositoryImplementation.helpAndSupport(token, jsonObject)
                .catch { e ->
                    helpAndSupportResponseMutable.value = ApiState.Failure(e)
                }
                .collect { data ->
                    helpAndSupportResponseMutable.value = ApiState.Success(data)
                }
        }
    }

}