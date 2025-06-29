package com.unavify.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    
    companion object {
        private val _otpFlow = MutableSharedFlow<String>()
        val otpFlow = _otpFlow.asSharedFlow()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.forEach { sms ->
                val messageBody = sms.messageBody
                // Extract OTP from message (assuming it's a 6-digit number)
                val otpRegex = Regex("\\b\\d{6}\\b")
                val matchResult = otpRegex.find(messageBody)
                matchResult?.value?.let { otp ->
                    GlobalScope.launch {
                        _otpFlow.emit(otp)
                    }
                }
            }
        }
    }
} 