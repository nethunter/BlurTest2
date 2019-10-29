package com.vitapm.blurtest2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import com.daasuu.gpuv.camerarecorder.LensFacing
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorderBuilder
import android.opengl.GLSurfaceView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorder
import com.daasuu.gpuv.egl.filter.GlBoxBlurFilter
import com.daasuu.gpuv.egl.filter.GlGaussianBlurFilter
import kotlinx.android.synthetic.main.content_main.*

const val CAMERA_REQUEST_ID: Int = 101

class MainActivity : AppCompatActivity(), OnSeekBarChangeListener {
    lateinit var gpuCameraRecorder: GPUCameraRecorder
    lateinit var sampleGLView: GLSurfaceView
    lateinit var blurFilter: GlGaussianBlurFilter
    private var cameraPermission: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_ID)
        } else {
            cameraPermission = true
        }

        blurValue.setOnSeekBarChangeListener(this)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (cameraPermission) blurFilter.blurSize = progress.toFloat() / 100
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            CAMERA_REQUEST_ID -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    cameraPermission = true
                    startCamera()
                }
                return
            }
        }
    }

    fun startCamera() {
        sampleGLView = GLSurfaceView(applicationContext)
        cameraFrame.addView(sampleGLView)

        gpuCameraRecorder = GPUCameraRecorderBuilder(this, sampleGLView)
            .lensFacing(LensFacing.BACK)
            .build()

        blurFilter = GlGaussianBlurFilter()
        blurFilter.blurSize = 0f
        gpuCameraRecorder.setFilter(blurFilter);
    }

    fun stopCamera() {
        gpuCameraRecorder.stop()
        gpuCameraRecorder.release()

        cameraFrame.removeView(sampleGLView)

    }

    override fun onResume() {
        super.onResume()

        if (cameraPermission) startCamera()
    }

    override fun onPause() {
        super.onPause()
        if (cameraPermission) stopCamera()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
