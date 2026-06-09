package com.example.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.content.ContentValues
import android.widget.Toast

import com.example.data.User

object PdfHelper {
    fun copyToDownloads(context: Context, sourceUri: Uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "Order_${System.currentTimeMillis()}.pdf")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        resolver.openInputStream(sourceUri)?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not create file in Downloads", Toast.LENGTH_SHORT).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, "Order_${System.currentTimeMillis()}.pdf")
                FileOutputStream(destFile).use { outputStream ->
                    context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to download: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateOrderPdf(context: Context, items: List<Pair<String, String>>, user: User?): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = pdfDocument.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Title
        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Order Document", 50f, 80f, paint)

        // Date
        paint.textSize = 12f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        canvas.drawText("Date: $dateString", 50f, 110f, paint)
        
        var currentY = 130f
        
        if (user != null) {
            paint.isFakeBoldText = true
            canvas.drawText("Sender: ${user.name}", 50f, currentY, paint)
            currentY += 20f
            if (user.shopName.isNotBlank()) {
                canvas.drawText("Shop: ${user.shopName}", 50f, currentY, paint)
                currentY += 20f
            }
        }

        // Separator
        paint.strokeWidth = 2f
        currentY += 10f
        canvas.drawLine(50f, currentY, 545f, currentY, paint)

        // Table Header
        paint.textSize = 14f
        paint.isFakeBoldText = true
        currentY += 30f
        canvas.drawText("Item Name", 50f, currentY, paint)
        canvas.drawText("Quantity", 400f, currentY, paint)

        currentY += 15f
        canvas.drawLine(50f, currentY, 545f, currentY, paint)

        // Table Rows
        paint.textSize = 14f
        paint.isFakeBoldText = false
        currentY += 25f
        
        var currentPageNumber = 1
        var currentPage = page
        var currentCanvas = canvas

        for ((name, qty) in items) {
            if (currentY > 800f) {
                // simple pagination safety (create a new page)
                pdfDocument.finishPage(currentPage)
                currentPageNumber++
                
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNumber).create()
                currentPage = pdfDocument.startPage(newPageInfo)
                currentCanvas = currentPage.canvas
                currentY = 50f
                
                // Redraw table header on new page
                paint.textSize = 14f
                paint.isFakeBoldText = true
                currentCanvas.drawText("Item Name", 50f, currentY, paint)
                currentCanvas.drawText("Quantity", 400f, currentY, paint)
                
                currentY += 15f
                paint.strokeWidth = 2f
                currentCanvas.drawLine(50f, currentY, 545f, currentY, paint)
                
                paint.isFakeBoldText = false
                currentY += 25f
            }
            currentCanvas.drawText(name, 50f, currentY, paint)
            currentCanvas.drawText(qty, 400f, currentY, paint)
            currentY += 30f
        }

        // Footer
        currentCanvas.drawLine(50f, currentY, 545f, currentY, paint)
        currentY += 30f
        paint.isFakeBoldText = true
        currentCanvas.drawText("End of Order", 50f, currentY, paint)

        pdfDocument.finishPage(currentPage)

        // Write to file
        val fileDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Orders")
        if (!fileDir.exists()) fileDir.mkdirs()
        
        val file = File(fileDir, "Order_${System.currentTimeMillis()}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            
            // Return URI using FileProvider
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
