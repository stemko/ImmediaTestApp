package me.blog.korn123.easyphotomap.models

import org.apache.commons.lang.StringUtils

/**
 *  * Created by Mpho Kobola 2018-10-01.
 */
class ThumbnailItem(imageId: String, imagePath: String, thumbnailPath: String) {

    var imageId: String = StringUtils.EMPTY
    var imagePath: String = StringUtils.EMPTY
    var thumbnailPath: String = StringUtils.EMPTY

    init {
        this.imageId = imageId
        this.imagePath = imagePath
        this.thumbnailPath = thumbnailPath
    }

}
