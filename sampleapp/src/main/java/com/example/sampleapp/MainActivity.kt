package com.example.sampleapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.umutbey.otplistener.OTPListener
import com.umutbey.otplistener.getMessage
import com.umutbey.otplistener.getOTP

class MainActivity : AppCompatActivity() {
    private var tv: TextView? = null
    val otpListener: OTPListener by lazy { OTPListener(this@MainActivity) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.textview2)
        otpListener.build()
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            otpListener.SMS_CONSENT_REQUEST -> if (resultCode == Activity.RESULT_OK) { // Get SMS message content
                Log.d("A", "message:->${data?.getMessage()}")
                data?.getOTP(6)?.let {
                    tv!!.text = it
                }
            } else if (resultCode == Activity.RESULT_CANCELED) { // Consent canceled, handle the error ...
                val task =
                    SmsRetriever.getClient(this).startSmsUserConsent(null)
                Log.d("OTPListener", "cancelled")
            }
        }
    }


}

