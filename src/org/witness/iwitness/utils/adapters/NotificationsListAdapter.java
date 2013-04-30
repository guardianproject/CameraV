package org.witness.iwitness.utils.adapters;

import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.Model;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.TimeUtility;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.ListAdapterListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

public class NotificationsListAdapter extends BaseAdapter implements ListAdapterListener {
	List<INotification> notifications;
	int currentSort = Models.INotificationManifest.Sort.DATE_DESC;
	
	private static final String LOG = App.LOG;
	
	public NotificationsListAdapter(List<INotification> notifications) {
		this.notifications = notifications;
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
		
		convertView = LayoutInflater.from(InformaCam.getInstance().a).inflate(R.layout.adapter_notification_list_item, null);
		
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
		
		if(notification.type == Models.INotification.Type.SHARED_MEDIA) {
			View progress = LayoutInflater.from(InformaCam.getInstance().a).inflate(R.layout.extras_notification_progress, null);
			((LinearLayout) convertView.findViewById(R.id.notification_view_root)).addView(progress);
		}
		
		return convertView;
	}

	@Override
	public void addData(Model newData) {
		notifications.add((INotification) newData);
		
	}

	@Override
	public void updateData(int position, Message message) {
		// TODO Auto-generated method stub
		
	}

}
