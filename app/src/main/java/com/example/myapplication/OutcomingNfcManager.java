//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.myapplication;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcEvent;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;

public class OutcomingNfcManager implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private OutcomingNfcManager.NfcActivity activity;

    public OutcomingNfcManager(OutcomingNfcManager.NfcActivity activity) {
        this.activity = activity;
    }

    public NdefMessage createNdefMessage(NfcEvent event) {
        String outString = this.activity.getOutcomingMessage();
        byte[] outBytes = outString.getBytes();
        NdefRecord outRecord = NdefRecord.createMime("text/plain", outBytes);
        return new NdefMessage(outRecord, new NdefRecord[0]);
    }

    public void onNdefPushComplete(NfcEvent event) {
        this.activity.signalResult();
    }

    public interface NfcActivity {
        String getOutcomingMessage();

        void signalResult();
    }
}
