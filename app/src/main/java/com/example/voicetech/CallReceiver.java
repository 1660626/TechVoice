package com.example.voicetech;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        MyPhonestatreListener customPhoneListener = new MyPhonestatreListener();

        telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        Bundle bundle = intent.getExtras();
        String phone_number = bundle.getString("incoming_number");

        String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
        int state = 0;
        if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            state = TelephonyManager.CALL_STATE_IDLE;
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            state = TelephonyManager.CALL_STATE_OFFHOOK;
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            state = TelephonyManager.CALL_STATE_RINGING;
        }
        if (phone_number == null || "".equals(phone_number)) {
            return;
        }
        customPhoneListener.onCallStateChanged(context, state, phone_number);
    }
}
