package com.htetwill.portier.launcher

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.htetwill.portier.launcher.activity.AppActivity
import com.htetwill.portier.launcher.databinding.ActivityHomeBinding
import com.htetwill.portier.launcher.model.Config
import com.htetwill.portier.launcher.state.ResultOf
import com.htetwill.portier.launcher.viewmodel.HomeViewModel

class HomeActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private lateinit var btnProceed: Button
    private lateinit var indicator: LinearProgressIndicator
    private lateinit var binding: ActivityHomeBinding
    private lateinit var tvGreeting: TextView
    private val viewModel by lazy {
        ViewModelProvider(this, HomeViewModel.Factory())
            .get(HomeViewModel::class.java)
    }
    private val hideHandler = Handler()
    private var isFullscreen: Boolean = false

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        tvGreeting.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

//         Set up the user interaction to manually show or hide the system UI.
        tvGreeting = binding.tvGreeting
        indicator = binding.busyIndicator
        btnProceed = binding.btnProceed

        btnProceed.setOnClickListener(View.OnClickListener {
            showPermissionPreview()
        })

        Toast.makeText(this, "param is " + intent.getStringExtra("param"), Toast.LENGTH_SHORT)
            .show()

        intent.getStringExtra("param")?.let { viewModel.fetchResponse(it) }
        viewModel.isLoadingLiveData().observe(this, { setLoadingState(it) })
        viewModel.configLiveData().observe(this, { result -> setWelcomeUI(result) })


    }

    private fun showPermissionPreview() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(Intent(this,AppActivity::class.java))
        } else {
            // Permission is missing and must be requested.
            requestPermission()
        }
    }

    private fun setWelcomeUI(result: ResultOf<Config>?) {
        when (result) {
            is ResultOf.Success -> {
                binding.tvGreeting.text = getString(
                    R.string.dummy_content,
                    result.value.hotel!!.name, result.value.hotel!!.city
                )
            }
            is ResultOf.Failure -> {
                result.message?.let { msg -> getSnackbar(msg).show() }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        when (isLoading) {
            true -> {
                indicator.show()
                tvGreeting.visibility = View.INVISIBLE
                btnProceed.visibility = View.INVISIBLE
            }
            false -> {
                indicator.hide()
                tvGreeting.visibility = View.VISIBLE
                btnProceed.visibility = View.VISIBLE
            }
        }
    }

    private fun getSnackbar(msg: String): Snackbar {
        val snackbar = Snackbar.make(binding.layoutCoordinator, msg, LENGTH_SHORT)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_primary))
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.snackbar_text))
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.snackbar_text))
        return snackbar
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        hideHandler.postDelayed(hidePart2Runnable, 3000.toLong())
    }

    private fun requestPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this,AppActivity::class.java))
            } else {
                getSnackbar("Permission Denied !")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}