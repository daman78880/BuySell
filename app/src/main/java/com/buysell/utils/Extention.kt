import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.buysell.R
import com.buysell.databinding.ToolbarLayoutOneBinding
import com.buysell.utils.Extentions
import dagger.hilt.android.internal.managers.FragmentComponentManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.hideKeyBoard() {
    Extentions.hideKeyboardd(this)
}

fun Fragment.hideKeyboard() {
    Extentions.hideKeyboardd(requireContext())
}


fun setToolBar(
    toolBarView: ToolbarLayoutOneBinding,
    contextt: Context,
    titleVisibleStatus: Boolean,
    clearBtnStatus: Boolean,
    title: String?,
    cardElevation: Float,
    backButtonGoneStatus: Boolean? = null,
) {
    val context= FragmentComponentManager.findActivity(contextt) as Activity

    if (titleVisibleStatus) {
        toolBarView.tbTitle.visibility = View.VISIBLE
        toolBarView.tbTitle.text = title
    } else {
        toolBarView.tbTitle.visibility = View.GONE
    }
    if (clearBtnStatus) {
        toolBarView.tbLastTxt.text = "Clear"
        toolBarView.tbLastTxt.visibility = View.VISIBLE
    } else {
        toolBarView.tbLastTxt.visibility = View.GONE
    }
    if (null != backButtonGoneStatus && backButtonGoneStatus == true) {
        toolBarView.tbBackBtn.visibility = View.GONE
    } else {
        toolBarView.tbBackBtn.visibility = View.VISIBLE
    }
    toolBarView.cardViewToolbar.cardElevation = cardElevation

    toolBarView.tbBackBtn.setOnClickListener {
        it.findNavController().navigateUp()
    }
    toolBarView.toolBarLayout.setOnClickListener {
        Extentions.hideKeyboardd(context)
    }
    if(clearBtnStatus){
        toolBarView.tbLastTxt.setOnClickListener {
            Toast.makeText(context, "Cleared", Toast.LENGTH_SHORT).show()
        }
    }

}


