package com.simform.risetestapp.view.activity

import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.simform.risetestapp.R
import com.simform.risetestapp.databinding.ActivityBrightnessBinding
import com.simform.risetestapp.util.extention.ActivityExtensions.allowWritePermission
import com.simform.risetestapp.util.extention.ActivityExtensions.canWrite
import com.simform.risetestapp.util.extention.ActivityExtensions.setBrightness
import kotlinx.android.synthetic.main.activity_brightness.seek_bar
import kotlinx.android.synthetic.main.activity_brightness.text_view

class BrightnessActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityBrightnessBinding
    private var brightness = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_brightness)
        binding.clickHandler = this
        init()
    }

    private fun init() {
        // Set the SeekBar initial progress from screen current brightness
        brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0);
        val brightness = brightness
        seek_bar.progress = brightness

        text_view.text = "Screen Brightness : $brightness"

        // If app has no permission to write system settings
        if (!canWrite) {
            seek_bar.isEnabled = false
            allowWritePermission()
        }

        // Set a SeekBar change listener
        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                text_view.text = "Screen Brightness : $i"

                // Change the screen brightness
                if (canWrite) {
                    setBrightness(i)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}