package com.umutbey.otplistener

import android.app.Activity
import android.content.*
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class OTPListener(val activity: Activity) : LifecycleObserver {
    val CREDENTIAL_PICKER_REQUEST = 1
    val SMS_CONSENT_REQUEST = 2
    var isSubscribeSMS: Boolean = false

    fun build(): OTPListener {
        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(
                this
            )
        return this
    }

    private val smsVerificationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
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
                            activity.startActivityForResult(
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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun startSMSBroadcast() {
        if (GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(activity) == ConnectionResult.SUCCESS) {
            val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            activity.registerReceiver(smsVerificationReceiver, intentFilter)
            isSubscribeSMS = true
            val task =
                SmsRetriever.getClient(activity).startSmsUserConsent(null)
            task.addOnSuccessListener { Log.d("OTPListener", "Task Running") }
            Log.d("OTPListener", "Value:$task")
        } else {
            Log.d("OTPListener", "The phone not installed google api services")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stopSMSBroadcast() {
        if (isSubscribeSMS) {
            activity.unregisterReceiver(smsVerificationReceiver)
            isSubscribeSMS = false
        }
    }
}