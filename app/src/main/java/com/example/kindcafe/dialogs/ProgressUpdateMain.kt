package com.example.kindcafe.dialogs


import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.example.kindcafe.databinding.DialogUpdateBinding

object ProgressUpdateMain {
    fun createProgressDialog(act: Activity): AlertDialog {
        val builder = AlertDialog.Builder(act)

        val binding = DialogUpdateBinding.inflate(act.layoutInflater)

        builder.setView(binding.root)

        val dialog = builder.create()

        dialog.setCancelable(false)
        dialog.show()

        return dialog
    }
}