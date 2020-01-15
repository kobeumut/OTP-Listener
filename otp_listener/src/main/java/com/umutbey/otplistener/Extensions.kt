package com.umutbey.otplistener

import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever

fun Intent.getMessage():String? = this.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)

/**
 * This function only return numbers
 *
 * @param char count for catch up in messages
 * @return
 */
fun Intent.getOTP(char:Int):String?{
    val regexForNumber = "\\b\\d{$char}\\b".toRegex()
    return getMessage()?.let { regexForNumber.find(it)?.groupValues?.get(0) }
}