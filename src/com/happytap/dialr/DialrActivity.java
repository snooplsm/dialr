package com.happytap.dialr;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.happytap.jumper.JumpDialog;
import com.happytap.jumper.JumpListener;

public class DialrActivity extends ListActivity {

	ContactListAdapter adapter;

	LinkedHashSet<Character> enabledCharacters;
	
	private Map<String,Integer> phoneNumberToType = new HashMap<String,Integer>();
	
	private Map<MenuItem, String> menuItemToNumber = new HashMap<MenuItem,String>();

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == 1) {
			JumpDialog d = new JumpDialog(this, new JumpListener() {

				@Override
				public void onJump(Character c) {
					Cursor cursor = adapter.getCursor();
					int pos = cursor
							.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
					boolean found = false;
					for (int i = 0; i < adapter.getCount(); i++) {
						Cursor o = (Cursor) adapter.getItem(i);
						String str = o.getString(pos).trim();
						if (str.length() > 0) {
							Character character = str.charAt(0);
							if (c.equals(character)) {
								if (!found) {
									getListView().setSelectionFromTop(i, 0);
									found = true;
								}
							}
						}
						System.out.println(o);
					}
				}

			});
			d.setEnabledCharacters(enabledCharacters);
			return d;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		Uri lookupUri = ContactsContract.Contacts.CONTENT_URI;
		Cursor cursor = getContentResolver().query(
				lookupUri,
				new String[] { ContactsContract.Contacts._ID,
						ContactsContract.Contacts.LOOKUP_KEY,
						ContactsContract.Contacts.DISPLAY_NAME },
				ContactsContract.Contacts.HAS_PHONE_NUMBER + "> ?",
				new String[] { "0" }, ContactsContract.Contacts.DISPLAY_NAME);

		int index = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
		enabledCharacters = new LinkedHashSet<Character>();
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext();
			String name = cursor.getString(index).trim();
			if (name.length() > 0) {
				enabledCharacters.add(name.charAt(0));
			}
		}

		setListAdapter((adapter = new ContactListAdapter(this, cursor)));

		if (savedInstanceState == null) {
			showDialog(1);
		}
		registerForContextMenu(getListView());
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				Cursor contact = (Cursor) adapter.getItem(position);
				String id = contact.getString(contact
						.getColumnIndex(ContactsContract.Contacts._ID));
				Cursor phones = getContentResolver().query(Phone.CONTENT_URI,
						null, Phone.CONTACT_ID + " = ?", new String[] { id },
						null);
				phoneNumberToType.clear();
				String number = null;
				while (phones.moveToNext()) {
					number = phones.getString(phones
							.getColumnIndex(Phone.NUMBER));
					int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
					if (phones.getCount() == 1) {
						break;
					} else {
						phoneNumberToType.put(number, type);
					}
				}
				phones.close();
				if(phoneNumberToType.isEmpty() && number!=null) {
					Intent intent = new Intent(Intent.ACTION_CALL, Uri
							.parse("tel:" + number));
					startActivity(intent);
				} else {
					openContextMenu(arg1);
				}
			}

		});
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String number = "tel:" + menuItemToNumber.get(item);
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
		startActivity(intent);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menuItemToNumber.clear();
		for(Map.Entry<String, Integer> entry : phoneNumberToType.entrySet()) {
			menuItemToNumber.put(menu.add(Menu.NONE, entry.getValue(), entry.getValue(), entry.getKey()),entry.getKey());
		}
	}
	@Override
	public boolean onSearchRequested() {
		showDialog(1);
		return false;
	}
}