package com.example.globalfinal


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import java.io.IOException
import android.widget.Button
import android.widget.ProgressBar
import android.view.MotionEvent


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var tts: TextToSpeech
    private var rightBtnPress = false
    private var leftBtnPress = false
    private var currentSpeechRate: Float = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val rightButton = findViewById<Button>(R.id.rightBtn)
        val leftButton = findViewById<Button>(R.id.leftBtn)


        rightButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Inicia o temporizador quando o botão for pressionado
                    rightBtnPress = true
                    rightButton.postDelayed({
                        if (rightBtnPress) {
                            increase() // Função chamada após 2 segundos de pressão
                        }
                    }, 2000) // 2000 ms = 2 segundos
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Cancela o temporizador se o botão for solto antes de 2 segundos
                    rightBtnPress = false
                    true
                }
                else -> false
            }
        }

        leftButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    leftBtnPress = true
                    leftButton.postDelayed({
                        if (leftBtnPress) {
                            decrease() // Função chamada após 2 segundos de pressão
                        }
                    }, 2000) // 2000 ms = 2 segundos
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Cancela o temporizador se o botão for solto antes de 2 segundos
                    leftBtnPress = false
                    true
                }
                else -> false
            }
        }

    }

    private fun increase() {
        currentSpeechRate += 0.5f

        tts.setSpeechRate(currentSpeechRate)
        speak("Velocidade aumentada")
        Toast.makeText(this, "$currentSpeechRate", Toast.LENGTH_LONG).show()
    }

    private fun decrease() {
        currentSpeechRate -= 0.5f

        tts.setSpeechRate(currentSpeechRate)
        speak("Velocidade reduzida")
        Toast.makeText(this, "$currentSpeechRate", Toast.LENGTH_LONG).show()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {

            val result = tts.setLanguage(Locale("pt", "BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Idioma não suportado ou dados ausentes.")
                Toast.makeText(this, "Idioma não suportado ou dados ausentes.", Toast.LENGTH_LONG).show()
            } else {

                Log.d(TAG, "TextToSpeech inicializado com sucesso.")
                speak("mãe do renan iniciada!")
            }
        } else {
            Log.e(TAG, "Falha ao inicializar o TextToSpeech.")
            Toast.makeText(this, "Falha ao inicializar o TextToSpeech.", Toast.LENGTH_LONG).show()
        }
    }

    private fun speak(text: String) {
        if (this::tts.isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e(TAG, "TextToSpeech não está inicializado.")
        }
    }

    override fun onDestroy() {
        if (this::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "ON RESUME.")

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val techList = arrayOf(arrayOf(NfcAdapter::class.java.name, MifareUltralight::class.java.name))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, techList)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.e(TAG, "ON NEW INTENTA.")
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        tag?.let {
            val mifare = MifareUltralight.get(it)
            readFromTag(mifare)
        }
    }
//progressBar
    private fun readFromTag(mifare: MifareUltralight) {
        Log.e(TAG, "ON READ FROM TAG.")



        val numPages = 16
        val stringBuilder = StringBuilder()
        try {
            mifare.connect()
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
            Toast.makeText(this, "Tag Data: $concatenatedData", Toast.LENGTH_LONG).show()


            speak(concatenatedData)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                mifare.close()
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
