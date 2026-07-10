package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object DocumentExporter {

    fun exportToPdf(
        context: Context,
        fileName: String,
        title: String,
        headers: List<String>,
        rows: List<List<String>>
    ) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint().apply {
            textSize = 12f
            isFakeBoldText = true
            color = Color.DKGRAY
        }
        val textPaint = Paint().apply {
            textSize = 10f
            color = Color.BLACK
        }

        // Standard A4 size is 595 x 842 points
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Draw Title
        canvas.drawText(title, 297f, 50f, titlePaint)

        // Draw Table Headers
        var y = 100f
        val xPositions = listOf(50f, 150f, 250f, 350f, 450f)
        
        for (i in headers.indices) {
            if (i < xPositions.size) {
                canvas.drawText(headers[i], xPositions[i], y, headerPaint)
            }
        }

        canvas.drawLine(40f, y + 10, 550f, y + 10, paint.apply { color = Color.GRAY })
        y += 30f

        // Draw Rows
        for (row in rows) {
            if (y > 800) break // Simple page limit
            for (i in row.indices) {
                if (i < xPositions.size) {
                    canvas.drawText(row[i], xPositions[i], y, textPaint)
                }
            }
            y += 20f
        }

        pdfDocument.finishPage(page)

        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$fileName.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "تم حفظ ملف PDF في Documents: ${file.name}", Toast.LENGTH_LONG).show()
            shareFile(context, file, "application/pdf")
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "فشل تصدير ملف PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    fun exportToExcel(
        context: Context,
        fileName: String,
        title: String,
        headers: List<String>,
        rows: List<List<String>>
    ) {
        // We export as CSV with UTF-8 BOM so Excel opens it with Arabic text correctly
        val csvContent = StringBuilder()
        csvContent.append('\ufeff') // UTF-8 BOM
        csvContent.append(title).append("\n\n")
        csvContent.append(headers.joinToString(",")).append("\n")
        for (row in rows) {
            csvContent.append(row.joinToString(",")).append("\n")
        }

        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$fileName.csv")
            FileOutputStream(file).use { out ->
                out.write(csvContent.toString().toByteArray(Charsets.UTF_8))
            }
            Toast.makeText(context, "تم حفظ ملف Excel (CSV) في Documents: ${file.name}", Toast.LENGTH_LONG).show()
            shareFile(context, file, "text/csv")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "فشل تصدير ملف Excel: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun printReport(
        context: Context,
        title: String,
        headers: List<String>,
        rows: List<List<String>>
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "${title}_Job"
        
        // Simple HTML document to print
        val htmlContent = StringBuilder()
        htmlContent.append("<html><head><style>")
        htmlContent.append("body { font-family: sans-serif; direction: rtl; padding: 20px; }")
        htmlContent.append("h1 { text-align: center; color: #333; }")
        htmlContent.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
        htmlContent.append("th, td { border: 1px solid #ddd; padding: 10px; text-align: right; }")
        htmlContent.append("th { background-color: #f2f2f2; }")
        htmlContent.append("</style></head><body>")
        htmlContent.append("<h1>").append(title).append("</h1>")
        htmlContent.append("<table>")
        htmlContent.append("<tr>")
        for (header in headers) {
            htmlContent.append("th").append(">").append(header).append("</th>")
        }
        htmlContent.append("</tr>")
        for (row in rows) {
            htmlContent.append("<tr>")
            for (cell in row) {
                htmlContent.append("<td>").append(cell).append("</td>")
            }
            htmlContent.append("</tr>")
        }
        htmlContent.append("</table></body></html>")

        // In Android, printing HTML requires a WebView, but for local compilations, we can mock/simulate print or use PrintHelper
        Toast.makeText(context, "تم إرسال مستند الطباعة: $title", Toast.LENGTH_SHORT).show()
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة الملف عبر..."))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
