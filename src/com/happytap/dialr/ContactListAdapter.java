package com.happytap.dialr;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;

public class ContactListAdapter extends SimpleCursorAdapter {

	
	public ContactListAdapter(Context context, Cursor c) {
		super(context, android.R.layout.simple_list_item_1, c, new String[] {ContactsContract.Contacts.DISPLAY_NAME}, new int[] {android.R.id.text1});
	}

}
