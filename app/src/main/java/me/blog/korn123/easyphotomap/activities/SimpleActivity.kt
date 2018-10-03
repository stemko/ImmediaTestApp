package me.blog.korn123.easyphotomap.activities

import io.github.aafactory.commons.activities.BaseSimpleActivity
import me.blog.korn123.easyphotomap.extensions.initTextSize

/**

 * Created by Mpho Kobola on 2018-10-01.
 * This code based 'Simple-Commons' package
*/

open class SimpleActivity : BaseSimpleActivity() {

    override fun onResume() {
        super.onResume()
        initTextSize(findViewById(android.R.id.content), this@SimpleActivity);
    }
}