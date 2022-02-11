package com.umutbey.otplistener

import android.app.Activity
import android.content.*
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class OTPListener : DefaultLifecycleObserver {
    val CREDENTIAL_PICKER_REQUEST = 1
    val SMS_CONSENT_REQUEST = 2
    var isSubscribeSMS: Boolean = false

    private var smsVerificationReceiver: BroadcastReceiver? = null

    fun Activity.startSMSBroadcast() {
         smsVerificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                    val extras = intent.extras
                    val smsRetrieverStatus: Status? = extras!![SmsRetriever.EXTRA_STATUS] as Status?
                    when (smsRetrieverStatus?.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            val consentIntent =
                                extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                            try {
                                // 5 minutes later TIMEOUT intent
                                this@startSMSBroadcast.startActivityForResult(
                                    consentIntent,
                                    SMS_CONSENT_REQUEST
                                )
                            } catch (e: ActivityNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                        CommonStatusCodes.TIMEOUT ->  // Time out occurred, handle the error.
                            Log.d("OTPListener", "TIME OUT")
                    }
                }
            }
        }
            if (GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
                val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
                this.registerReceiver(smsVerificationReceiver, intentFilter)
                isSubscribeSMS = true
                val task =
                    SmsRetriever.getClient(this).startSmsUserConsent(null)
                task.addOnSuccessListener { Log.d("OTPListener", "Task Running") }
                Log.d("OTPListener", "Value:$task")
            } else {
                Log.d("OTPListener", "The phone not installed google api services")
            }
    }

    fun Activity.stopSMSBroadcast() {
        if (isSubscribeSMS) {
            this.unregisterReceiver(smsVerificationReceiver)
            isSubscribeSMS = false
        }
    }
}
