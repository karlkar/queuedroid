package com.kksionek.queuedroid.model

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.kksionek.queuedroid.data.Player

object ContactsController {

    private const val TAG = "ContactsController"

    fun loadContacts(context: Context, list: MutableList<Player>) {
        val cr = context.contentResolver
        val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if (cursor == null) {
            Log.e(TAG, "loadContacts: Cannot obtain contact list")
            return
        }
        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val photo =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                if (name.contains("@")) continue
                list.add(Player(id, name, photo, Player.Type.CONTACTS))
            }
        }
        cursor.close()
    }
}