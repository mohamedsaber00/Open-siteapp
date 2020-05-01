package com.msaber.openapiapp.ui

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import com.afollestad.materialdialogs.MaterialDialog
import com.msaber.openapiapp.R


fun Context.displayToast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT)
}

fun Context.displayToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT)
}

fun Context.displaySuccessDialog(message: String?) {
    MaterialDialog(this).show {
        title(R.string.text_success)
        message(text = message)
        positiveButton(R.string.text_ok)
    }
}

fun Context.displayErrorDialog(message: String?) {
    MaterialDialog(this).show {
        title(R.string.text_error)
        message(text = message)
        positiveButton(R.string.text_ok)
    }
}


    /** makes visible a view. */
    fun View.visible() {
        visibility = View.VISIBLE
    }

    /** makes gone a view. */
    fun View.hide() {
        visibility = View.GONE
    }


