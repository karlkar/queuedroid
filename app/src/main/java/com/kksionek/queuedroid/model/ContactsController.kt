package com.kksionek.queuedroid.model

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.kksionek.queuedroid.data.Player
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class ContactsController @Inject constructor() {

    companion object {
        private const val TAG = "ContactsController"
    }

    fun loadContacts(context: Context): Single<List<Player>> {
        return Single.fromCallable {
            val cr = context.contentResolver
            val cursor = cr.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            if (cursor == null) {
                Log.e(TAG, "loadContacts: Cannot obtain contact list")
                return@fromCallable emptyList()
            }
            cursor.use {
                return@fromCallable if (cursor.count > 0) {
                    generateSequence { if (cursor.moveToNext()) cursor else null }
                        .map {
                            val id = getId(cursor)
                            val name = getName(cursor)
                            val photo = getPhoto(cursor)
                            Player(id, name, photo, Player.Type.CONTACTS)
                        }
                        .filter { !it.name.contains("@") }
                        .toList()
                } else {
                    emptyList()
                }
            }
        }
    }

    private fun getId(cursor: Cursor): String =
        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))

    private fun getName(cursor: Cursor): String =
        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

    private fun getPhoto(cursor: Cursor): String =
        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
}