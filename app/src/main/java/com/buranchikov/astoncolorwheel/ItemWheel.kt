package com.buranchikov.astoncolorwheel

sealed class ItemWheel {
    data class StringItem(val value: String) : ItemWheel()
    data class IntItem(val value:Int): ItemWheel()
}
