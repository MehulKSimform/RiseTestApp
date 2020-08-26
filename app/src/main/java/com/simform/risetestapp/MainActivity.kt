package com.simform.risetestapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.simform.risetestapp.base.BaseActivity
import com.simform.risetestapp.databinding.ActivityMainBinding
import com.simform.risetestapp.util.extention.ActivityExtensions.launchActivity
import com.simform.risetestapp.view.activity.BrightnessActivity
import com.simform.risetestapp.view.activity.ConnectBluetoothEarphoneActivity
import com.simform.risetestapp.view.activity.RecordAudioActivity
import com.simform.risetestapp.view.activity.SpeechRecogniseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindObject.clickHandler = this
    }

    override fun getLayoutResId(): Int = R.layout.activity_main

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.audioRecord -> {
                launchActivity<RecordAudioActivity>()
            }

            R.id.btnBrightness -> {
                launchActivity<BrightnessActivity>()
            }

            R.id.btnBluetoothConnect -> {
                launchActivity<ConnectBluetoothEarphoneActivity>()
            }

            R.id.btnSpeechRecognise -> {
                launchActivity<SpeechRecogniseActivity>()
            }

            R.id.btnDisplayVideo -> {

            }
        }
    }
}