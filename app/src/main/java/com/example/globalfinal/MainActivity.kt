package com.example.globalfinal

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.os.Parcelable
import android.content.Context
import android.provider.Settings

import android.app.Activity
import android.nfc.tech.MifareUltralight
import java.io.IOException

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.util.Log
import android.widget.Toast

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

    }
    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val techList = arrayOf(arrayOf(NfcAdapter::class.java.name, MifareUltralight::class.java.name))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, techList)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        //val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        tag?.let {
            val mifare = MifareUltralight.get(it)
            readFromTag(mifare)
        }

    }

    private fun readFromTag(mifare: MifareUltralight) {
        val numPages = 16
        val stringBuilder = StringBuilder()
        try {
            mifare?.connect()

            for (i in 6 until numPages step 4) {
                val payload = mifare.readPages(i)
                if (payload != null) {
                    val pageData = String(payload, Charsets.UTF_8)
                    val cleanPageData = cleanText(pageData)
                    stringBuilder.append(cleanPageData)
                } else {
                    Log.d(TAG, "Page $i is null")
                }
            }

            val concatenatedData = stringBuilder.toString().trim().drop(1)
            Log.d(TAG, "Concatenated Data: $concatenatedData")

            Toast.makeText(this, "Tag Data: $concatenatedData", Toast.LENGTH_SHORT).show()

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

    private fun isPrintableCharacter(c: Char): Boolean {
        return c.isLetterOrDigit() || c.isWhitespace() || c.isISOControl()
    }

    private fun cleanText(data: String): String {
        return data.filter { isPrintableCharacter(it) }
    }


}