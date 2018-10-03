package me.blog.korn123.easyphotomap.models

import com.drew.lang.GeoLocation
import java.util.*

/**
 *  * Created by Mpho Kobola 2018-10-01.
 */

data class ExifModel(val imagePath: String) {
    var tagOrientation: Int = 1
    var date: Date? = null
    var geoLocation: GeoLocation? = null
}