package com.example.kindcafe

import android.app.Application
import com.google.firebase.auth.FirebaseAuth

class KindCafeApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        myAuth = FirebaseAuth.getInstance()
    }

    companion object{
        lateinit var myAuth: FirebaseAuth
    }
}