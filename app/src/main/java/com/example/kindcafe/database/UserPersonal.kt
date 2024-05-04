package com.example.kindcafe.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserPersonal(
    @PrimaryKey(autoGenerate = false)
    val name: String = "a",
    val surname: String? = null,
    val birthday: String? = null,
    val email: String? = null,
    val signZodiac: String? = null,
    val phoneNumber: String? = null,
    val location: String? = null
)
