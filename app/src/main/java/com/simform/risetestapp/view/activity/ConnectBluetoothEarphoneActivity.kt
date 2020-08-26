package com.simform.risetestapp.view.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothDeviceDecorator
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import com.simform.risetestapp.R
import com.simform.risetestapp.base.BaseActivity
import com.simform.risetestapp.databinding.ActivityConnectBluetoothEarphoneBinding
import com.simform.risetestapp.util.extention.ActivityExtensions.startPermissionActivity
import com.simform.risetestapp.view.adapter.DeviceItemAdapter
import kotlinx.android.synthetic.main.activity_connect_bluetooth_earphone.pg_bar
import kotlinx.android.synthetic.main.activity_connect_bluetooth_earphone.rv
import kotlinx.android.synthetic.main.activity_connect_bluetooth_earphone.txtScan
import java.util.Arrays

class ConnectBluetoothEarphoneActivity : BaseActivity<ActivityConnectBluetoothEarphoneBinding>(), BluetoothService.OnBluetoothScanCallback,
        BluetoothService.OnBluetoothEventCallback, DeviceItemAdapter.OnAdapterItemClickListener {

    private var mAdapter: DeviceItemAdapter? = null

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mService: BluetoothService? = null
    private var mScanning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pg_bar?.visibility = View.GONE

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothAdapter?.let {
            if (!it.isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            } else {
                startStopScan()
            }
        }

        rv?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mAdapter = DeviceItemAdapter(this, mBluetoothAdapter?.bondedDevices)
        mAdapter?.setOnAdapterItemClickListener(this)
        rv?.adapter = mAdapter

        mService = BluetoothService.getDefaultInstance()

        mService?.setOnScanCallback(this)
        mService?.setOnEventCallback(this)
        txtScan.setOnClickListener(this)
    }

    override fun getLayoutResId(): Int = R.layout.activity_connect_bluetooth_earphone

    override fun onResume() {
        super.onResume()
        mService?.setOnEventCallback(this)
        mBluetoothAdapter?.let {
            if (checkLocationPermission() && it.isEnabled) {
                startStopScan()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mService?.let {
            it.stopScan()
        }
    }

    private fun startStopScan() {
        if (!mScanning) {
            mService?.startScan()
        } else {
            mService?.stopScan()
        }
    }

    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
        Log.d(TAG, "onDeviceDiscovered: " + device.name + " - " + device.address + " - " + Arrays.toString(device.uuids))
        val dv = BluetoothDeviceDecorator(device, rssi)
        mAdapter?.let { adapter ->
            val index = adapter.devices.indexOf(dv)
            if (index < 0) {
                adapter.devices.add(dv)
                adapter.notifyItemInserted(adapter.devices.size - 1)
            } else {
                adapter.devices[index].device = device
                adapter.devices[index].rssi = rssi
                adapter.notifyItemChanged(index)
            }
        }
    }

    override fun onStartScan() {
        Log.d(TAG, "onStartScan")
        mScanning = true
        pg_bar?.visibility = View.VISIBLE
        txtScan.text = getString(R.string.txt_scan_stop)
    }

    override fun onStopScan() {
        Log.d(TAG, "onStopScan")
        mScanning = false
        pg_bar?.visibility = View.GONE
        txtScan.text = getString(R.string.txt_scan)
    }

    override fun onDataRead(buffer: ByteArray, length: Int) {
        Log.d(TAG, "onDataRead")
    }

    override fun onStatusChange(status: BluetoothStatus) {
        Log.d(TAG, "onStatusChange: $status")
        Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show()

        if (status == BluetoothStatus.CONNECTED) {
            val colors = arrayOf<CharSequence>("Try text", "Try picture")

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select")
            builder.setItems(colors) { dialog, which ->
                /*if (which == 0) {
                    startActivity(Intent(this@MainActivity, DeviceActivity::class.java))
                } else {
                    startActivity(Intent(this@MainActivity, BitmapActivity::class.java))
                }*/
            }
            builder.setCancelable(false)
            //builder.show()
        }

    }

    override fun onDeviceName(deviceName: String) {
        Log.d(TAG, "onDeviceName: $deviceName")
    }

    override fun onToast(message: String) {
        Log.d(TAG, "onToast")
    }

    override fun onDataWrite(buffer: ByteArray) {
        Log.d(TAG, "onDataWrite")
    }

    override fun onItemClick(device: BluetoothDeviceDecorator, position: Int) {
        //mService?.stopScan()
        if (!device.device.isDeviceBonded) {
            device.device.createBond()
            Toast.makeText(this, getString(R.string.txt_device_paired), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.txt_device_already_paired), Toast.LENGTH_SHORT).show()
        }
        //mService?.connect(device.device)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    // Error
                    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
                } else {
                    startStopScan()
                }
            }

            REQ_LOCATION_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    startStopScan()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        val TAG = "BluetoothExampleKotlin"
        private const val REQUEST_ENABLE_BT = 2002
        private const val PERMISSION_REQUEST_COARSE_LOCATION = 2001
        private const val REQ_LOCATION_SETTINGS = 2004
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.txtScan -> {
                startStopScan()
            }
        }
    }

    private val BluetoothDevice.isDeviceBonded get() = bondState == BluetoothDevice.BOND_BONDED

    private fun checkLocationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // Error
                    // permission was not granted
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        //Show Information about why you need the permission
                        android.app.AlertDialog.Builder(this, R.style.AlertDialogCustom)
                                .setTitle(getString(R.string.txt_err_permission_required_title))
                                .setPositiveButton(R.string.txt_give_permission) { dialog, id -> this.startPermissionActivity() }
                                .setCancelable(false)
                                .create()
                                .show()
                    }
                }
            }
        }
    }
}