package com.kksionek.queuedroid.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.kksionek.queuedroid.data.Player;

import java.util.ArrayList;
import java.util.List;

public class ContactsController {

    private static final String TAG = "ContactsController";

    private ContactsController() {}

    public static List<Player> loadContacts(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor == null) {
            Log.e(TAG, "loadContacts: Cannot obtain contact list");
            return null;
        }
        List<Player> list = new ArrayList<>(cursor.getCount());
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String photo = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                if (name.contains("@"))
                    continue;

                list.add(new Player(id, name, photo, Player.Type.CONTACTS));
            }
        }
        cursor.close();
        return list;
    }
}
