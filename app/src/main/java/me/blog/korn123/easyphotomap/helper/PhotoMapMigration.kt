package me.blog.korn123.easyphotomap.helper

import io.realm.DynamicRealm
import io.realm.RealmMigration

/**
 *  * Created by Mpho Kobola 2018-10-01.
 */

class PhotoMapMigration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var currentVersion = oldVersion as Int
        val schema = realm.schema
        if (currentVersion == 1) {
            val diarySchema = schema.get("PhotoMapItem")
            currentVersion++
        }
    }

}
