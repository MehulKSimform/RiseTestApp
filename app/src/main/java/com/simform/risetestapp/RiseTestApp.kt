package com.simform.risetestapp

import android.app.Application
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothClassicService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.simform.risetestapp.di.generalModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.UUID

class RiseTestApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        startKoin {
            androidContext(this@RiseTestApp)
            modules(listOf(generalModule))
        }

        initBluetoothClassic()
    }

    private fun initBluetoothClassic() {
        val config = BluetoothConfiguration()
        config.bluetoothServiceClass = BluetoothClassicService::class.java //  BluetoothClassicService.class or BluetoothLeService.class
        config.context = applicationContext
        config.bufferSize = 1024
        config.characterDelimiter = '\n'
        config.deviceName = "Carbon Trainer"
        config.callListenersInMainThread = true

        //config.uuid = null; // When using BluetoothLeService.class set null to show all devices on scan.
        config.uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // For Classic
        BluetoothService.init(config)
    }
}