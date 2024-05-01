package com.example.kindcafe

import android.app.Application
import com.example.kindcafe.database.KindCafeRepository
import com.google.firebase.auth.FirebaseAuth

class KindCafeApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        myAuth = FirebaseAuth.getInstance()

        KindCafeRepository.initialize(this)
    }

    companion object{
        lateinit var myAuth: FirebaseAuth
    }
}