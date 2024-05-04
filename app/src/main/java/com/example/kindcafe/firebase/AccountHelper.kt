package com.example.kindcafe.firebase

import android.util.Log
import androidx.annotation.IdRes
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.database.UserPersonal
import com.example.kindcafe.firebase.firebaseInterfaces.DefinitionOfStatus
import com.example.kindcafe.utils.AuxillaryFunctions
import com.google.firebase.auth.FirebaseUser

class AccountHelper(val activity: MainActivity, @IdRes val currentView: Int) {
    val myAuth = KindCafeApplication.myAuth
    private val dbManager = DbManager()
    private val MY_TAG = "AccountHelperTag"

    /* Registration
    * - return 0 - if password ok or error not related to password..
    * - return 1 - if password bad */
    fun signUpWithEmail(name: String, email: String, password: String, status: DefinitionOfStatus? = null) {
        if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
            myAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        /* if it is ok, then send verification to user */
                        sendEmailVerification(task.result?.user!!)
                        status?.onSuccess()
                        //activity.mainVM.setData(task.result?.user!!.email!!)
                        activity.mainVM.setData(name)
                        dbManager.setPrimaryData(myAuth.currentUser, UserPersonal(name = name, email = email))
                    } else {
                        /* Get the current error */
                        Log.d(MY_TAG, "Global Exception: ${task.exception}")
                        AuxillaryFunctions.showSnackBar(R.string.can_not_contact_the_server, activity)
                        /* ... */
                    }
                }
        } else {
            AuxillaryFunctions.showSnackBar(R.string.incomplete_fields, activity)
        }
    }

    /* Enter into account */
    fun signInWithEmail(email: String, password: String, status: DefinitionOfStatus? = null){
        /* Checking the correctness of the input  */
        if (email.isNotEmpty() && password.isNotEmpty()){
            myAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        AuxillaryFunctions.showSnackBar(R.string.success_login, activity)
                        //activity.mainVM.setData(task.result?.user!!.email!!)
                        activity.mainVM.setData(task.result?.user!!.email!!)
                        // read data from db
                        status?.onSuccess()
                    }
                    else {
                        Log.d(MY_TAG, "Enter (Login) -- Global exception -- ${task.exception}")
                        AuxillaryFunctions.showSnackBar(R.string.failed_login, activity)

                        /* Do if wrong password (realise later) */
                        activity.mainVM.incAttempt()
                        activity.mainVM.setNumberOfAttempts()
                    }
                }
        } else {
            AuxillaryFunctions.showSnackBar(R.string.incomplete_fields, activity)
        }
    }

    /* Exit this account */
    fun signOut(): Boolean{
        if (myAuth.currentUser != null){
            myAuth.signOut()
            AuxillaryFunctions.showSnackBar(R.string.home_success_log_out, activity)
            //showSnackBar(R.string.home_success_log_out)
        } else {
            AuxillaryFunctions.showSnackBar(R.string.home_not_logged, activity)
            //showSnackBar(R.string.home_not_logged)
            return false
        }
        return true
    }

    /* Restore account (reset password by email) */
    fun restoreAccount(email: String, status: DefinitionOfStatus? = null){
        if(email.isNotEmpty()){
            myAuth
                .sendPasswordResetEmail(email)
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        AuxillaryFunctions.showSnackBar(R.string.send_restore_message_successfully, activity)
                        status?.onSuccess()
                    } else {
                        AuxillaryFunctions.showSnackBar(R.string.send_restore_message_failed, activity)
                    }
                }
        } else {
            AuxillaryFunctions.showSnackBar(R.string.email_not_entered, activity)
        }
    }

    /*------------------------------------ Auxilary ------------------------------------*/
    private fun sendEmailVerification(user: FirebaseUser) {
        user
            .sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { AuxillaryFunctions.showSnackBar(R.string.send_verification_email_done, activity) }
                else { AuxillaryFunctions.showSnackBar(R.string.send_verification_email_error, activity) }
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
                AuxillaryFunctions.showSnackBar(R.string.already_login, activity)
            }
            return true
        }
        return false
    }



    /* Show message on the screen */
/*    private fun showSnackBar(
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
    }*/
}


