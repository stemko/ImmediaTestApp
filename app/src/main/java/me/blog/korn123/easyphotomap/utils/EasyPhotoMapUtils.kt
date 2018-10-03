package me.blog.korn123.easyphotomap.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.TextView
import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.imaging.jpeg.JpegProcessingException
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import me.blog.korn123.easyphotomap.helper.WORKING_DIRECTORY
import me.blog.korn123.easyphotomap.models.ExifModel
import me.blog.korn123.easyphotomap.models.ThumbnailItem
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 *  * Created by Mpho Kobola 2018-10-01.
 */
class EasyPhotoMapUtils {
    
    companion object {
        @Volatile private var mGeoCoder: Geocoder? = null

        val dateTimePattern = SimpleDateFormat("yyyy-MM-dd(EEE) HH:mm", Locale.getDefault())

        private val MAX_RETRY = 5

        private fun getGeoCoderInstance(context: Context): Geocoder = mGeoCoder?.let { it } ?: Geocoder(context, Locale.getDefault())

        fun initWorkingDirectory() {
            if (!File(WORKING_DIRECTORY).exists()) {
                File(WORKING_DIRECTORY).mkdirs()
            }
        }

        fun getFromLocation(context: Context, latitude: Double, longitude: Double, maxResults: Int, retryCount: Int): List<Address>? {
            val lat = java.lang.Double.parseDouble(String.format("%.6f", latitude))
            val lon = java.lang.Double.parseDouble(String.format("%.7f", longitude))
            val listAddress: List<Address>?
            try {
                listAddress = getGeoCoderInstance(context).getFromLocation(lat, lon, maxResults)
            } catch (e: Exception) {
                if (retryCount < MAX_RETRY) {
                    return getFromLocation(context, lat, lon, maxResults, retryCount + 1)
                }
                throw Exception(e.message)
            }

            return listAddress
        }

        fun getFromLocationName(context: Context, locationName: String, maxResults: Int, retryCount: Int): List<Address>? {
            var count = retryCount
            val geoCoder = Geocoder(context, Locale.getDefault())
            val listAddress: List<Address>?
            try {
                listAddress = geoCoder.getFromLocationName(locationName, maxResults)
            } catch (e: Exception) {
                if (count < MAX_RETRY) {
                    return getFromLocationName(context, locationName, maxResults, ++count)
                }
                throw Exception(e.message)
            }

            return listAddress
        }

        fun <K, V : Comparable<V>> entriesSortedByValues(map: Map<K, V>): List<Map.Entry<K, V>> {
            val sortedEntries = ArrayList(map.entries)
            Collections.sort(sortedEntries) { e1, e2 -> e2.value.compareTo(e1.value) }
            return sortedEntries
        }

        fun <K, V : Comparable<V>> entriesSortedByKeys(map: Map<K, V>): List<Map.Entry<K, V>> {
            val sortedEntries = ArrayList(map.entries)
            Collections.sort(sortedEntries) { e1, e2 -> e2.key.toString().compareTo(e1.key.toString()) }
            return sortedEntries
        }

        fun fetchAllThumbnail(context: Context): List<ThumbnailItem> {
            val projection = arrayOf(MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID)
            val imageCursor = context.contentResolver.query(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                    projection, null, null,
                    MediaStore.Images.Thumbnails.IMAGE_ID + " desc")
            val result = ArrayList<ThumbnailItem>()

            when (imageCursor.moveToFirst()) {
                true -> {
                    val dataColumnIndex = imageCursor.getColumnIndex(projection[0])
                    val idColumnIndex = imageCursor.getColumnIndex(projection[1])
                    do {
                        val filePath = imageCursor.getString(dataColumnIndex)
                        val imageId = imageCursor.getString(idColumnIndex)
                        //                Uri thumbnailUri = uriToThumbnail(context, imageId);
                        //                Uri imageUri = Uri.parse(filePath);
                        //                Log.i("fetchAllImages", imageUri.toString());
                        // 원본 이미지와 썸네일 이미지의 uri를 모두 담을 수 있는 클래스를 선언합니다.
                        val photo = ThumbnailItem(imageId, "", filePath)
                        result.add(photo)
                    } while (imageCursor.moveToNext())
                    imageCursor.close()
                }
                false -> {
                    // imageCursor is empty
                }
            }

            return result.filter {
                File(it.thumbnailPath).exists()
            }
        }

        fun fetchAllImages(context: Context): List<ThumbnailItem> {
            // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
            val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)
            val imageCursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                    projection, null, null,
                    MediaStore.Images.Media.DATA + " asc")        // DATA, _ID를 출력
            val result = ArrayList<ThumbnailItem>(imageCursor!!.count)
            when(imageCursor.moveToFirst()) {
                true -> {
                    val dataColumnIndex = imageCursor.getColumnIndex(projection[0])
                    val idColumnIndex = imageCursor.getColumnIndex(projection[1])
                    do {
                        val filePath = imageCursor.getString(dataColumnIndex)
                        val imageId = imageCursor.getString(idColumnIndex)

                        //                Uri thumbnailUri = uriToThumbnail(context, imageId);
                        //                Uri imageUri = Uri.parse(filePath);
                        //                Log.i("fetchAllImages", imageUri.toString());
                        // 원본 이미지와 썸네일 이미지의 uri를 모두 담을 수 있는 클래스를 선언합니다.
                        val photo = ThumbnailItem(imageId, filePath, "")
                        result.add(photo)
                    } while (imageCursor.moveToNext())
                    imageCursor.close()
                }
                false -> {
                    // imageCursor is empty
                }
            }
            return result
        }

        fun getOriginImagePath(context: Context, imageId: String): String? {
            // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val contentResolver = context.contentResolver

            // 원본 이미지의 _ID가 매개변수 imageId인 썸네일을 출력
            val cursor = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Images.Media._ID + "=?",
                    arrayOf(imageId), null)
            if (cursor == null) {
                // Error 발생
                // 적절하게 handling 해주세요
            } else if (cursor.moveToFirst()) {
                val thumbnailColumnIndex = cursor.getColumnIndex(projection[0])
                val path = cursor.getString(thumbnailColumnIndex)
                cursor.close()
                return path
            }
            return null
        }

        fun getGPSDirectory(filePath: String): GpsDirectory? {
            var gpsDirectory: GpsDirectory? = null
            try {
                val metadata = JpegMetadataReader.readMetadata(File(filePath))
                gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
            } catch (e: JpegProcessingException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return gpsDirectory
        }

        fun fullAddress(address: Address): String {
            val sb = StringBuilder()
            if (address.countryName != null) sb.append(address.countryName).append(" ")
            if (address.adminArea != null) sb.append(address.adminArea).append(" ")
            if (address.locality != null) sb.append(address.locality).append(" ")
            if (address.subLocality != null) sb.append(address.subLocality).append(" ")
            if (address.thoroughfare != null) sb.append(address.thoroughfare).append(" ")
            if (address.featureName != null) sb.append(address.featureName).append(" ")
            return sb.toString()
        }

        fun readDataFile(targetPath: String): List<String>? {
            val inputStream: InputStream?
            var listData: List<String>? = null
            try {
                inputStream = FileUtils.openInputStream(File(targetPath))
                listData = IOUtils.readLines(inputStream, "UTF-8")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return listData
        }

        fun getDefaultDisplay(activity: Activity): Point {
            val display = activity.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            return size
        }
        
        fun getDateFromJpegMetaData(targetFile: File): String {
            val metadata = JpegMetadataReader.readMetadata(targetFile)
            val exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            var date: Date? = null
            try {
                date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault())    
            } catch (e: Exception) {}
            
            return when (date != null) {
                true -> EasyPhotoMapUtils.dateTimePattern.format(date)
                false -> ""
            } 
        }

        fun setChildViewTypeface(viewGroup: ViewGroup) {
            repeat(viewGroup.childCount) { i ->
                if (viewGroup.getChildAt(i) is ViewGroup) {
                    setChildViewTypeface(viewGroup.getChildAt(i) as ViewGroup)
                } else {
                    if (viewGroup.getChildAt(i) is TextView) {
                        val tv = viewGroup.getChildAt(i) as TextView
                        tv.typeface = Typeface.DEFAULT
                    }
                }
            }
        }
        
        fun parseExifDescription(imageFilePath: String): ExifModel {
            val metadata = JpegMetadataReader.readMetadata(File(imageFilePath))
            val exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            val exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
            val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
            
            val exifModel = ExifModel(imageFilePath)
            exifIFD0Directory?.let {
                exifModel.tagOrientation = it.getInt(ExifIFD0Directory.TAG_ORIENTATION)    
            }
            exifSubIFDDirectory?.let {
                exifModel.date = it.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault())
            }
            gpsDirectory?.geoLocation?.let {
                exifModel.geoLocation = it 
            }
            return exifModel
        }
    }
}
