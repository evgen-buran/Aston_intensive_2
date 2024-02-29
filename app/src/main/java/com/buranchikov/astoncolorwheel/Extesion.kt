package com.buranchikov.astoncolorwheel

import android.view.View
import androidx.annotation.IdRes

fun <T : View> View.find(@IdRes idRes: Int): Lazy<T> {
    return lazy{findViewById(idRes)}

}