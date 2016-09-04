package com.kksionek.queuedroid;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactsController {

    private static final String TAG = "ContactsController";

    public void loadContacts(Activity activity, PlayerChooserAdapter adapter) {
        ContentResolver cr = activity.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor == null) {
            Log.e(TAG, "loadContacts: Cannot obtain contact list");
            return;
        }
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String photo = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

                if (!name.contains("@"))
                    adapter.add(new Player(id, name, photo, Player.Type.CONTACTS));
            }
        }
        cursor.close();
    }
}
