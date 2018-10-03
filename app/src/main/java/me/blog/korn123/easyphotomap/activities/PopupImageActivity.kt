package me.blog.korn123.easyphotomap.activities

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import com.github.chrisbanes.photoview.PhotoView
import io.github.aafactory.commons.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_popup_image.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.helper.COLUMN_IMAGE_PATH
import java.io.File

/**
 *  * Created by Mpho Kobola 2018-10-01.
 */
class PopupImageActivity : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup_image)
        val imageView = findViewById<PhotoView>(R.id.imageView)

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false
        options.inSampleSize = 2
        val imagePath = intent.getStringExtra(COLUMN_IMAGE_PATH)
        val bitmap: Bitmap = if (File(imagePath).exists()) BitmapUtils.decodeFile(this@PopupImageActivity, imagePath, options) else BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_gallery) 
        imageView.setImageBitmap(bitmap)
        finish.setOnClickListener { finish() }
        rotateLeft.setOnClickListener { imageView.setRotationBy(-90F) }
        rotateRight.setOnClickListener { imageView.setRotationBy(90F) }
    }
}
