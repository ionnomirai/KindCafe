package com.example.kindcafe.firebase

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser

class AccountHelper(val activity: Activity, @IdRes val currentView: Int) {
    val myAuth = KindCafeApplication.myAuth
    private val MY_TAG = "AccountHelperTag"

    /* Registration */
    fun signUpWithEmail(name: String, email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
            myAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        /* if it is ok, then send verification to user */
                        sendEmailVerification(task.result?.user!!)
                    } else {
                        /* Get the current error */
                        Log.d(MY_TAG, "Global Exception: ${task.exception}")
                        showSnackBar(R.string.can_not_contact_the_server)
                        /* ... */
                    }
                }
        } else {
            showSnackBar(R.string.incomplete_fields)
        }
    }

    /*------------------------------------ Auxilary ------------------------------------*/
    private fun sendEmailVerification(user: FirebaseUser) {
        user
            .sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { showSnackBar(R.string.send_verification_email_done) }
                else { showSnackBar(R.string.send_verification_email_error) }
            }
    }

    fun showUser(){
        Log.d(MY_TAG, "current user: ${myAuth.currentUser.toString()}")
    }

    /* Show message on the screen */
    private fun showSnackBar(
        @StringRes messageText: Int,
        @StringRes closeButton: Int = R.string.close
    ){
        val snackbar: Snackbar = Snackbar.make(
            activity.findViewById(currentView),
            activity.resources.getText(messageText),
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar
            .setAction(closeButton) {
                snackbar.dismiss()
            }
            .show()
    }
}


