package com.ryk.tzviewswidget;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

public class ContactListRow extends TableRow {
	private ImageView ctImage;
	private TextView tvFullName;
	private TextView tvLastMsg;
	
	public ContactListRow(Context context) {
		super(context);
		
		ctImage = new ImageView(context);
		tvFullName = new TextView(context);
		tvLastMsg = new TextView(context);
	}
	
	public ContactListRow(Context context, String fullname, String lastMessage, Uri imgUri) {
		this(context);
		setContact(fullname, lastMessage, imgUri);
	}	
	
	public void setContact(String fullname, String lastMessage, Uri imgUri) {
		ctImage.setImageURI(imgUri);
		tvFullName.setText(fullname);
		tvLastMsg.setText(lastMessage);
	}
}
