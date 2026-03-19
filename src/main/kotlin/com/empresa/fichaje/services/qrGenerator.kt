package com.empresa.fichaje.services

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import java.io.ByteArrayOutputStream

class QrGenerator {

    fun generateQrImage(text: String): ByteArray {

        val writer = QRCodeWriter()
        val matrix = writer.encode(text, BarcodeFormat.QR_CODE, 300, 300)

        val outputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream)

        return outputStream.toByteArray()
    }
}