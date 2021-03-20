package haonan.tech.util

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.IOException

class imageUtil{
    companion object {
        fun readPictureDegree(path: String): Int {
            var degree = 0
            try {
                val exifInterface = ExifInterface(path)
                val orientation: Int = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return degree
        }

        fun rotaingImageView(angle: Int, bitmap: Bitmap): Bitmap? {
            var returnBm: Bitmap? = null
            // 根据旋转角度，生成旋转矩阵
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            try {
                // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
                returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } catch (e: OutOfMemoryError) {
            }
            if (returnBm == null) {
                returnBm = bitmap
            }
            if (bitmap != returnBm) {
                bitmap.recycle()
            }
            return returnBm
        }

    }


}