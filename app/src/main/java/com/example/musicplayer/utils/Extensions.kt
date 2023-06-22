package com.example.musicplayer.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import com.google.gson.Gson

fun showViews(vararg view: View) = view.map {
    it.show()
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun goneViews(vararg view: View) = view.map {
    it.gone()
}

fun String.toToast(context: Context) = android.widget.Toast.makeText(
    context, this,
    android.widget.Toast.LENGTH_SHORT
).show()

@Suppress("DEPRECATION")
fun Activity.setFullScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.window.insetsController?.hide(WindowInsets.Type.statusBars())
    } else {
        this.window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}

fun prettyPrint(data: Any?, tag: String = "kanaku") {
    data?.let {
        Log.d(tag, Gson().toJson(it))
    } ?: run {
        Log.d(tag, "null")
    }
}