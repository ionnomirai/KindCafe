package com.example.kindcafe.firebase

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.firebase.firebaseInterfaces.DefinitionOfStatus
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser

class AccountHelper(val activity: MainActivity, @IdRes val currentView: Int) {
    val myAuth = KindCafeApplication.myAuth
    private val MY_TAG = "AccountHelperTag"

    /* Registration */
    fun signUpWithEmail(name: String, email: String, password: String, status: DefinitionOfStatus? = null) {

        if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
            myAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        /* if it is ok, then send verification to user */
                        sendEmailVerification(task.result?.user!!)
                        status?.onSuccess()
                        activity.mainViewModel.setData(task.result?.user!!.email!!)
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

    /* Enter into account */
    fun signInWithEmail(email: String, password: String): Boolean{
        var isSuccess = false
        /* Checking the correctness of the input  */
        if (email.isNotEmpty() && password.isNotEmpty()){
            myAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        isSuccess = true
                        showSnackBar(R.string.success_login)
                    }
                    else {
                        Log.d(MY_TAG, "Enter (Login) -- Global exception -- ${task.exception}")
                        isSuccess = false
                        showSnackBar(R.string.failed_login)
                    }
                }
        } else {
            showSnackBar(R.string.failed_login)
        }
        return isSuccess
    }

    /* Exit this account */
    fun signOut(): Boolean{
        if (myAuth.currentUser != null){
            myAuth.signOut()
            showSnackBar(R.string.home_success_log_out)
        } else {
            showSnackBar(R.string.home_not_logged)
            return false
        }
        return true
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

    fun getUserEmail(): String?{
        myAuth.currentUser?.let {
            return it.email.toString()
        }
        return null
    }

    fun isUserLogin(showMessage: Boolean = true): Boolean{
        myAuth.currentUser?.let {
            if(showMessage){
                showSnackBar(R.string.already_login)
            }
            return true
        }
        return false
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


