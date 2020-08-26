package com.simform.risetestapp.util.extention

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.koin.core.KoinComponent
import timber.log.Timber

object ActivityExtensions : KoinComponent {

    /**
     * Launch activity extension with optional bundles
     * @receiver Context
     * @param options Bundle?
     * @param init Intent.() -> Unit
     */
    inline fun <reified T : Any> Context.launchActivity(options: Bundle? = null, noinline init: Intent.() -> Unit = {}) {
        newIntent<T>(this).apply {
            init()
            startActivity(this, options)
        }
    }

    /**
     *
     * @receiver Context
     * @param options Bundle?
     * @param init Intent.() -> Unit
     */
    inline fun <reified T : Any> Activity.launchActivityWithResult(options: Bundle? = null, requestCode: Int, noinline init: Intent.() -> Unit = {}) {
        newIntent<T>(this).apply {
            init()
            startActivityForResult(this, requestCode, options)
        }
    }

    /**
     * Create T Type activity Intent
     * @param context Context
     * @return Intent
     */
    inline fun <reified T : Any> newIntent(context: Context): Intent = Intent(context, T::class.java)

    /**
     * This fun is used to hide keyboard.
     * @receiver AppCompatActivity
     */
    fun Activity.hideKeyBoard() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    /**
     * This fun is used to show keyboard.
     * @receiver AppCompatActivity
     */
    fun Activity.showKeyBoard() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.toggleSoftInput(
                InputMethodManager.SHOW_IMPLICIT, 0)
    }

    /**
     * Launch activity extension with optional bundles and finish current activity
     * @receiver Context
     * @param options Bundle?
     * @param init Intent.() -> Unit
     */
    inline fun <reified T : Any> Activity.launchActivityAndFinish(options: Bundle? = null, noinline init: Intent.() -> Unit = {}) {
        newIntent<T>(this).apply {
            init()
            startActivity(this, options)
        }
        finish()
    }

    /**
     * This fun is used to handle runtime permissions.
     * @receiver Fragment
     * @param permission List<String>
     * @param onPermissionGranted () -> Unit
     * @param onPermissionDeny () -> Unit
     */
    fun Activity.requestMultiplePermissions(permission: List<String>, onPermissionGranted: () -> Unit, onPermissionDeny: () -> Unit) {
        Dexter.withActivity(this)
                .withPermissions(permission)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            onPermissionGranted()
                        } else {
                            if (report.isAnyPermissionPermanentlyDenied) {
                                onPermissionDeny()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }
                }).check()

    }

    /**
     * This function is used to start permission activity.
     * @receiver Activity
     */
    fun Activity.startPermissionActivity() {
        Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }

    val Context.canWrite: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.System.canWrite(this)
            } else {
                true
            }
        }


    // Extension function to allow write system settings
    fun Context.allowWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Timber.e("Error")
            }
        }
    }


    // Extension property to get screen brightness programmatically
    val Context.brightness: Int
        get() {
            return Settings.System.getInt(
                    this.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    0
            )
        }


    // Extension method to set screen brightness programmatically
    fun Context.setBrightness(value: Int): Unit {
        Settings.System.putInt(
                this.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                value
        )
    }


    // Extension function to show toast message quickly
    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}