package com.example.kindcafe.firebase

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser

class AccountHelper(val activity: Activity, @IdRes val currentView: Int) {
    val myAuth = KindCafeApplication.myAuth

    /* Registration */
    fun signUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            myAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    } else {

                    }
                }
        }
    }

    /*------------------------------------ Auxilary ------------------------------------*/
    private fun sendEmailVerification(user: FirebaseUser) {
        var snackbar: Snackbar
        user
            .sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    snackbar = Snackbar.make(
                        activity.findViewById(currentView),
                        activity.resources.getText(R.string.send_verification_email_done),
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackbar
                        .setAction("Close") {
                            snackbar.dismiss()
                        }
                        .show()

                } else {

                }
            }
    }


}


