package org.witness.iwitness.utils.adapters;

import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.TimeUtility;
import org.witness.iwitness.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NotificationsListAdapter extends BaseAdapter {
	List<INotification> notifications;
	int currentSort = Models.INotificationManifest.Sort.DATE_DESC;
	
	private static final String LOG = App.LOG;
	
	public NotificationsListAdapter(List<INotification> notifications) {
		this.notifications = notifications;
	}
	
	public void update(List<INotification> newNotifications) {
		notifications = newNotifications;
		notifyDataSetChanged();
	}
	
	public void update(INotification newNotification) {		
		notifications.add(newNotification);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return notifications.size();
	}

	@Override
	public Object getItem(int position) {
		return notifications.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		INotification notification = notifications.get(position);
		
		convertView = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.adapter_notification_list_item, null);
		
		TextView metadata = (TextView) convertView.findViewById(R.id.notification_metadata);
		StringBuffer sb = new StringBuffer();
		sb.append(TimeUtility.millisecondsToDatestamp(notification.timestamp));
		if(notification.from != null) {
			sb.append(System.getProperty("line.separator")).append(notification.from);
		}
		metadata.setText(sb.toString());
		
		if(notification.label != null) {
			TextView label = (TextView) convertView.findViewById(R.id.notification_label);
			label.setText(notification.label);
		}
		
		if(notification.content != null) {
			TextView content = (TextView) convertView.findViewById(R.id.notification_content);
			content.setText(notification.content);
		}
		
		if(notification.icon != null) {
			byte[] iconBytes = null;
			if(notification.iconSource == Type.IOCIPHER) {
				iconBytes = InformaCam.getInstance().ioService.getBytes(notification.icon, Type.IOCIPHER);
			}
			
			if(iconBytes != null) {
				ImageView icon = (ImageView) convertView.findViewById(R.id.notification_icon);
				Bitmap b = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
				icon.setImageBitmap(b);
			}
		}
		
		if(notification.type == Models.INotification.Type.EXPORTED_MEDIA) {
			ImageView statusIcon = (ImageView) convertView.findViewById(R.id.notification_status);
			
			int d = R.drawable.ic_notification_waiting;
			if(notification.taskComplete) {
				d = R.drawable.ic_notification_accepted;
			}
			
			statusIcon.setImageDrawable(parent.getContext().getApplicationContext().getResources().getDrawable(d));
			statusIcon.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}
}
