package com.unavify.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class SmsReceiverJava extends BroadcastReceiver {
    public interface OtpListener {
        void onOtpReceived(String otp);
    }
    private static OtpListener otpListener;
    public static void setOtpListener(OtpListener listener) {
        otpListener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages != null) {
                for (SmsMessage sms : messages) {
                    String messageBody = sms.getMessageBody();
                    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b\\d{6}\\b").matcher(messageBody);
                    if (matcher.find() && otpListener != null) {
                        otpListener.onOtpReceived(matcher.group());
                    }
                }
            }
        }
    }
} 