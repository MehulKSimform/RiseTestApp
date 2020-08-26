package com.simform.risetestapp.view.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.simform.risetestapp.R
import com.simform.risetestapp.base.BaseActivity
import com.simform.risetestapp.databinding.ActivitySpeechRecogniseBinding
import com.simform.risetestapp.util.extention.ActivityExtensions.startPermissionActivity
import com.simform.risetestapp.util.extention.ActivityExtensions.toast
import kotlinx.android.synthetic.main.activity_speech_recognise.*
import timber.log.Timber

class SpeechRecogniseActivity : BaseActivity<ActivitySpeechRecogniseBinding>(), RecognitionListener {

    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mSpeechRecognizerIntent: Intent? = null
    private var mPreviousResult = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech_recognise)
    }

    override fun getLayoutResId(): Int = R.layout.activity_speech_recognise

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    override fun onResume() {
        super.onResume()
        resetSpeechRecognizer()
        if (checkPermission()) {
            setRecogniserIntent()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mSpeechRecognizer != null && SpeechRecognizer.isRecognitionAvailable(this)) {
            mSpeechRecognizer?.stopListening()
        }
    }

    override fun onStop() {
        super.onStop()

        if (mSpeechRecognizer != null && SpeechRecognizer.isRecognitionAvailable(this)) {
            mSpeechRecognizer?.destroy()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_RECORD -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // Error
                    // permission was not granted
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]!!)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder(this, R.style.AlertDialogCustom).setTitle(getString(R.string.txt_err_permission_required_title))
                                .setPositiveButton(R.string.txt_give_permission) { dialog, id -> startPermissionActivity() }
                                .setCancelable(false)
                                .create()
                                .show()
                    }
                } else {
                    resetSpeechRecognizer()
                    setRecogniserIntent()
                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_RECORD)
            return false
        }
        return true
    }

    private fun resetSpeechRecognizer() {
        if (mSpeechRecognizer != null && SpeechRecognizer.isRecognitionAvailable(this)) mSpeechRecognizer?.destroy()
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            mSpeechRecognizer?.setRecognitionListener(this)
        }
        else {
            toast("Speech Recognizer not available")
        }
    }

    private fun setRecogniserIntent() {
        mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mSpeechRecognizerIntent?.apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        if (mSpeechRecognizerIntent != null) mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
        // Change the stream to your stream of choice.
        //AudioUtils.muteAudio(true, requireContext());
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Timber.e("onReadyForSpeech")
        mPreviousResult = ""
    }

    override fun onRmsChanged(rmsdB: Float) {

    }

    override fun onBufferReceived(buffer: ByteArray?) {

    }

    override fun onPartialResults(partialResults: Bundle?) {

    }

    override fun onEvent(eventType: Int, params: Bundle?) {

    }

    override fun onBeginningOfSpeech() {

    }

    override fun onEndOfSpeech() {
        Timber.e("onEndOfSpeech")
        mSpeechRecognizer?.stopListening()
    }

    override fun onError(error: Int) {
        resetSpeechRecognizer()
        if (mSpeechRecognizerIntent != null) mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
        //AudioUtils.muteAudio(true, requireContext());
    }

    override fun onResults(results: Bundle?) {
        val matches = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (matches != null && matches.isNotEmpty()) {
            if (mPreviousResult != matches[0]) {
                Timber.e(matches[0])
                speechResult.text = matches[0]
                Toast.makeText(this, matches[0], Toast.LENGTH_SHORT).show()
                mPreviousResult = matches[0]
            }
        }
        if (mSpeechRecognizerIntent != null) mSpeechRecognizer!!.startListening(mSpeechRecognizerIntent)
    }

    companion object {
        private const val PERMISSION_REQUEST_RECORD = 0x11
    }
}