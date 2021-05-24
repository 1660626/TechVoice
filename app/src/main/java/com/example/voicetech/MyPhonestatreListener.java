package com.example.voicetech;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MyPhonestatreListener extends PhoneStateListener {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isIncoming;

    public void onCallStateChanged(Context context, int state, String phoneNumber) {
        if (lastState == state) {
            return;
        }

        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                RecordFragment.getInstance().stopRecording(false);
                RecordFragment.getInstance().setRecordPauseBtn();
                if (AudioListFragment.getInstance() != null) {
                    AudioListFragment.getInstance().setPauseAudio();
                }

                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    RecordFragment.getInstance().stopRecording(false);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                } else if (isIncoming) {
                } else {
                }
                break;
        }
        lastState = state;
    }
}
