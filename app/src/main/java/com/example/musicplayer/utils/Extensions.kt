package com.example.musicplayer.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

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

fun Any.prettyPrint(tag: String = "kanaku") {
    this.let {
        Log.d(tag, Gson().toJson(it))
    }
}

fun isNetConnected(context: Context): Boolean {
    val conMgr = context
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return conMgr.isDefaultNetworkActive
}

fun getFormattedDurationTime(timeInMilliseconds: Long): String {
    return if (timeInMilliseconds < 1000) {
        Constants.EMPTY
    } else {
        /* if call duration greater than one hour change duration format */
        if (timeInMilliseconds >= 3600 * 1000) {
            String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(timeInMilliseconds),
                TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMilliseconds)),
                TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                timeInMilliseconds
                            )
                        )
            )
        } else {
            String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMilliseconds)),
                TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                timeInMilliseconds
                            )
                        )
            )
        }
    }
}