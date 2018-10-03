package me.blog.korn123.easyphotomap.helper

import android.support.multidex.MultiDexApplication

import io.realm.Realm

/**
 *  * Created by Mpho Kobola 2018-10-01.
 */

class EasyPhotoMapApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}
