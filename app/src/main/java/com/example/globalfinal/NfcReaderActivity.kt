package com.example.globalfinal

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import java.io.IOException

private const val TAG = "NFCREAD"
class NfcReaderActivity : Activity() {

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "NFC CREATE INSTANCE")

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC não é suportado neste dispositivo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "Por favor, habilite o NFC", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "2NFC CREATE INSTANCE")
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val techList = arrayOf(arrayOf(NfcAdapter::class.java.name, MifareUltralight::class.java.name))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, techList)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    // Método onNewIntent corretamente sobrescrito
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "10000NFC CREATE INSTANCE")
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            //val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            tag?.let {
                val mifare = MifareUltralight.get(it)
                readFromTag(mifare) // Chamando o método readFromTag corretamente
            }
        }
    }

    // Método readFromTag definido corretamente
    private fun readFromTag(mifare: MifareUltralight?) {
        Log.d(TAG, "TENTANDO LER SA DISSGRASSA")
        try {
            mifare?.connect()
            val payload = mifare?.readPages(0) // Lê as primeiras 4 páginas da tag
            if (payload != null) {
                val data = String(payload, Charsets.UTF_8)
                Toast.makeText(this, "Tag Data: $data", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                mifare?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
