package com.buranchikov.astoncolorwheel

sealed class ItemWheel {
    data class StringItem(val value: String) : ItemWheel()
    data class ImgItem(val url:String): ItemWheel()
}
