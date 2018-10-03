package me.blog.korn123.easyphotomap.helper

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import java.util.*

/**
 *  * Created by Mpho Kobola 2018-10-01.
 */

object PhotoMapDbHelper {
    private val mConfig: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .name("easyphotomap.realm")
                .schemaVersion(1)
                .migration(PhotoMapMigration())
                .modules(Realm.getDefaultModule())
                .build()
    }

    fun getInstance(): Realm = Realm.getInstance(mConfig)

    fun insertPhotoMapItem(photoMapItem: PhotoMapItem) {
        val realmInstance = getInstance()
        realmInstance.beginTransaction()
        var sequence = 1
        realmInstance.where(PhotoMapItem::class.java)?.max("sequence")?.let { max ->
            sequence = max.toInt() + 1
        }
        photoMapItem.sequence = sequence
        realmInstance.insert(photoMapItem)
        realmInstance.commitTransaction()
        realmInstance.close()
    }

    fun selectPhotoMapItemAll(realmInstance: Realm): ArrayList<PhotoMapItem> {
        val realmResults = realmInstance.where(PhotoMapItem::class.java).findAllSorted("sequence", Sort.DESCENDING)
        val list = ArrayList<PhotoMapItem>()
        list.addAll(realmResults.subList(0, realmResults.size))
        return list
    }

    fun selectTimeLineItemAll(realmInstance: Realm, excludeDate: String): ArrayList<PhotoMapItem> {
        val realmResults = realmInstance.where(PhotoMapItem::class.java).notEqualTo(COLUMN_DATE, excludeDate).findAllSorted(COLUMN_DATE, Sort.ASCENDING)
        val list = ArrayList<PhotoMapItem>()
        list.addAll(realmResults.subList(0, realmResults.size))
        realmInstance.beginTransaction()
        for (item in list) {
            item.dateWithoutTime = getSimpleDate(item.date)
        }
        realmInstance.commitTransaction()
        return list
    }

    private fun getSimpleDate(date: String): String = when(date.contains("(")) {
        true -> date.substring(0, date.lastIndexOf("("))
        false -> date
    }

    fun selectPhotoMapItemBy(realmInstance: Realm, targetColumn: String, value: String): ArrayList<PhotoMapItem> {
        val realmResults = realmInstance.where(PhotoMapItem::class.java).equalTo(targetColumn, value).findAllSorted("sequence", Sort.DESCENDING)
        val list = ArrayList<PhotoMapItem>()
        list.addAll(realmResults.subList(0, realmResults.size))
        return list
    }

    fun containsPhotoMapItemBy(realmInstance: Realm, targetColumn: String, value: String): ArrayList<PhotoMapItem> {
        val realmResults = realmInstance.where(PhotoMapItem::class.java).contains(targetColumn, value).findAllSorted("sequence", Sort.DESCENDING)
        val list = ArrayList<PhotoMapItem>()
        list.addAll(realmResults.subList(0, realmResults.size))
        return list
    }

    fun deletePhotoMapItemBy(realmInstance: Realm, sequence: Int) {
        val item: PhotoMapItem? = realmInstance.where(PhotoMapItem::class.java).equalTo("sequence", sequence).findFirst()
        item?.let {
            realmInstance.beginTransaction()
            it.deleteFromRealm()
            realmInstance.commitTransaction()
        }
    }

    fun deletePhotoMapItemBy(realmInstance: Realm, query: String) {
        val realmResults = realmInstance.where(PhotoMapItem::class.java).contains(COLUMN_INFO, query).findAll()
        realmResults?.let {
            realmInstance.beginTransaction()
            realmResults.deleteAllFromRealm()
            realmInstance.commitTransaction()
        }
    }
}
