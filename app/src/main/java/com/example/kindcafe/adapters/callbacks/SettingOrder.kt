package com.example.kindcafe.adapters.callbacks

import com.example.kindcafe.data.NumberAdd
import com.example.kindcafe.data.Size

interface SettingOrder {
    fun setQuantity(id: String, name: String, quantity: String)

    fun setSize(id: String, name: String, size: Size)

    fun setAdds(id: String, name: String, addNumber: NumberAdd, value: Boolean)
}