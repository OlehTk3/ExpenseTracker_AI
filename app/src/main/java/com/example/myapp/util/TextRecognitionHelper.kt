package com.example.myapp.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object TextRecognitionHelper {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun recognizeText(
        context: Context,
        uri: Uri,
        onSuccess: (String, Double?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text
                    val amount = findAmount(fullText)
                    
                    // Просто возвращаем первую строку как название (как было раньше)
                    val title = visionText.textBlocks.firstOrNull()?.text ?: "Новый расход"
                    
                    onSuccess(title, amount)
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun findAmount(text: String): Double? {
        val regex = Regex("""\d+[.,]\d{2}""")
        val matches = regex.findAll(text)
        
        return matches.map { 
            it.value.replace(",", ".").toDoubleOrNull() 
        }.filterNotNull().maxOrNull()
    }
}
