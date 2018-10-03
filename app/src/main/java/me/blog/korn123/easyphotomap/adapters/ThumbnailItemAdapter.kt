package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.AsyncTask
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import io.github.aafactory.commons.extensions.dpToPixel
import io.github.aafactory.commons.utils.BitmapUtils
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.models.ThumbnailItem
import me.blog.korn123.easyphotomap.utils.EasyPhotoMapUtils
import java.io.File

/**
 *  * Created by Mpho Kobola 2018-10-01.
 */
class ThumbnailItemAdapter(private val mActivity: Activity,
                           private val mContext: Context,
                           private val mLayoutResourceId: Int,
                           private val mEntities: List<ThumbnailItem>
) : ArrayAdapter<ThumbnailItem>(mContext, mLayoutResourceId, mEntities) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row = convertView
        val holder: ViewHolder

        if (row == null) {
            val inflater = (mContext as Activity).layoutInflater
            row = inflater.inflate(mLayoutResourceId, parent, false)
            holder = ViewHolder()
            holder.imageView1 = row.findViewById<ImageView>(R.id.image1)
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }
        val widthHeight = ((EasyPhotoMapUtils.getDefaultDisplay(mActivity).x - mContext.dpToPixel(10F, 1)) / 3)
        val entity = mEntities[position]
        val thumbnailPath = entity.thumbnailPath
        holder.position = position
        holder.imageView1?.layoutParams?.height = widthHeight
        holder.imageView1?.setImageBitmap(BitmapFactory.decodeResource(mActivity.resources, R.drawable.ic_menu_gallery))
        ThumbnailTask(mActivity, position, holder).execute(thumbnailPath, widthHeight.toString())
        return row
    }

    private class ThumbnailTask(private val activity: Activity, private val mPosition: Int, private val mHolder: ViewHolder) : AsyncTask<String, Void, Bitmap>() {
        private var widthHeight = 0

        override fun doInBackground(vararg params: String): Bitmap? {
            val filePath = params[0]
            widthHeight = Integer.valueOf(params[1])!!
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            if (!File(filePath).exists()) return null
            var resized: Bitmap? = null
            if (mHolder.position == mPosition) {
                try {
                    val bitmap = BitmapUtils.decodeFile(activity, filePath, options)
                    resized = Bitmap.createScaledBitmap(bitmap, widthHeight, widthHeight, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                this.cancel(true)
            }
            return resized
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            if (mHolder.position == mPosition && bitmap != null) {
                val td = TransitionDrawable(arrayOf(ColorDrawable(Color.TRANSPARENT), BitmapDrawable(activity.resources, bitmap)))
                mHolder.imageView1!!.setImageDrawable(td)
                td.startTransition(1000)
            }
        }
    }

    private class ViewHolder {
        var imageView1: ImageView? = null
        var position: Int = 0
    }

}
