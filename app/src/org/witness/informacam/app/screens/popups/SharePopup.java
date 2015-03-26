package org.witness.informacam.app.screens.popups;

import info.guardianproject.onionkit.ui.OrbotHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.services.Logger;
import org.spongycastle.openpgp.PGPException;
import org.witness.informacam.InformaCam;
import org.witness.informacam.app.R;
import org.witness.informacam.app.utils.Constants.App;
import org.witness.informacam.app.utils.UIHelpers;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.organizations.IInstalledOrganizations;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.Models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SharePopup {
	LayoutInflater li;
	IMedia media;

	protected final static String LOG = App.Home.LOG;

	InformaCam informaCam;

	View tvTitleShareOrg, divider1, tvTitleShare, divider2;
	ListView mLvItems;
	ListView mLvItemsOrg;
	ProgressBar inProgressBar;
	LinearLayout inProgressRoot;

	List<IOrganization> organizations;

	HandlerIntent sendTo = null;
	IOrganization encryptTo = null;

	Activity a;
	Handler h;
	private final Dialog alert;

	private boolean shareJ3MOnly = false;
	
	public SharePopup(Activity a, final IMedia media) {
		this(a, media, false, false);
	}

	@SuppressLint("HandlerLeak")
	public SharePopup(final Activity a, final IMedia media, boolean startsInforma, boolean shareJ3MOnly) {
		this.a = a;

		alert = new Dialog(a);
		alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
		alert.setContentView(R.layout.popup_share);

		this.media = media;
		this.shareJ3MOnly = shareJ3MOnly;
		
		informaCam = InformaCam.getInstance();

		mLvItems = (ListView) alert.findViewById(R.id.lvItems);
		mLvItemsOrg = (ListView) alert.findViewById(R.id.lvItemsOrg);
		inProgressRoot = (LinearLayout) alert
				.findViewById(R.id.share_in_progress_root);
		inProgressBar = (ProgressBar) alert
				.findViewById(R.id.share_in_progress_bar);
		
		tvTitleShare = alert.findViewById(R.id.tvTitleShare);
		tvTitleShareOrg = alert.findViewById(R.id.tvTitleShareOrg);
		divider1 = alert.findViewById(R.id.divider1);
		divider2 = alert.findViewById(R.id.divider2);
		
		li = LayoutInflater.from(a);
		
		try
		{
			initLayout();
		}
		catch (Exception re)
		{
			Log.e("Setup","Could not init data and layout",re);
			throw new RuntimeException("Could not init data");
		}
		
		h = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Bundle b = msg.getData();
				if(b.containsKey(Models.IMedia.VERSION)) {
					inProgressBar.setProgress(100);
					alert.cancel();

					if (sendTo != null
							&& b.getString(Models.IMedia.VERSION) != null) {

						sendTo.intent.setClassName(
								sendTo.resolveInfo.activityInfo.packageName,
								sendTo.resolveInfo.activityInfo.name);
						
						/*
						try
						{
							String j3mText = generateJ3M(a, media._id);
							
							if (j3mText != null)
								sendTo.intent.putExtra(Intent.EXTRA_TEXT, j3mText);
						}
						catch (Exception e)
						{
							//unable to generate j3m
							e.printStackTrace();
						}*/
						
						String title = a.getString(R.string.share_from_) + a.getString(R.string.app_name);
						sendTo.intent.putExtra(Intent.EXTRA_TITLE, title);
					//	sendTo.intent.putExtra(Intent.EXTRA_SUBJECT, title);
						
						Uri uriShare = Uri
								.fromFile(new java.io.File(b
										.getString(Models.IMedia.VERSION)));
						sendTo.intent.setType("*/*");
						
						sendTo.intent.putExtra(Intent.EXTRA_STREAM, uriShare);
						a.startActivity(sendTo.intent);
						
					}
				} else if(b.containsKey(Codes.Keys.UI.PROGRESS)) {
					inProgressBar.setProgress(b.getInt(Codes.Keys.UI.PROGRESS));
				}
				else if (msg.what == -1)
				{
					inProgressBar.setProgress(100);
					alert.cancel();
					
					String errMsg = b.getString("msg");
					Toast.makeText(a,errMsg, Toast.LENGTH_LONG).show();
				}
				else if (msg.what == Codes.Messages.Transport.ORBOT_UNINSTALLED)
				{
					OrbotHelper oh = new OrbotHelper(a);
					oh.promptToInstall(a);
				}
				else if (msg.what == Codes.Messages.Transport.ORBOT_NOT_RUNNING)
				{
					OrbotHelper oh = new OrbotHelper(a);
					oh.requestOrbotStart(a);
				}
			}
		};

		alert.show();
		alert.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
	}

	public String generateJ3M (Context context, String id) throws InstantiationException, IllegalAccessException, NoSuchAlgorithmException, SignatureException, PGPException, IOException
	{
		boolean signData = false;
		IMedia media = informaCam.mediaManifest.getById(id);
		String j3m = ((IMedia) media).buildJ3M(context, signData, null);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(j3m);
		String prettyJsonString = gson.toJson(je);
		
		byte[] sig = informaCam.signatureService.signData(prettyJsonString.getBytes());
	
		StringBuffer sbLog = new StringBuffer();
		sbLog.append("-----BEGIN PGP SIGNED MESSAGE-----\n");
		sbLog.append("Hash: SHA1\n\n");
		sbLog.append(prettyJsonString);
		
		String sigString = new String(sig);
		
		sbLog.append(sigString.replace("PGP MESSAGE", "PGP SIGNATURE"));
	
		return sbLog.toString();
	}
		
	private void initLayout() throws IllegalAccessException, InstantiationException, IOException {
		initData();
	}

	private void initData() throws IllegalAccessException, InstantiationException, IOException {
		informaCam = InformaCam.getInstance();
		IInstalledOrganizations installedOrganizations = (IInstalledOrganizations) informaCam.getModel(new IInstalledOrganizations());

		// Add organizations
		ArrayList<Object> shareDestinations = new ArrayList<Object>();
		if(installedOrganizations.organizations != null && installedOrganizations.organizations.size() > 0) {
			organizations = installedOrganizations.organizations;
			
			for(IOrganization org : organizations) {
				shareDestinations.add(org);
			}
		}

		ListAdapter adapter = new HandlerIntentListAdapter(a,
				shareDestinations.toArray(new Object[shareDestinations.size()]));
		mLvItemsOrg.setAdapter(adapter);

		// Add other handlers
		shareDestinations.clear();
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("*/*");
		getHandlersForIntent(intent, shareDestinations);

		adapter = new HandlerIntentListAdapter(a,
				shareDestinations.toArray(new Object[shareDestinations.size()]));
		mLvItems.setAdapter(adapter);

		OnItemClickListener listener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Object handler = parent.getAdapter().getItem(position);
				if (handler instanceof IOrganization) {
					encryptTo = organizations.get(position);
					sendTo = null;
					Log.d(LOG, "now exporting to " + encryptTo.organizationName);
					export();
				}
				else if (handler instanceof HandlerIntent) {
					encryptTo = null;
					sendTo = (HandlerIntent) handler;
					Log.d(LOG, "now sending to " + sendTo.toString());
					export();
				}
			}
		};

		mLvItems.setOnItemClickListener(listener);
		mLvItemsOrg.setOnItemClickListener(listener);
	}

	private void export() {
		mLvItemsOrg.setVisibility(View.GONE);
		mLvItems.setVisibility(View.GONE);
		inProgressRoot.setVisibility(View.VISIBLE);
		if (sendTo != null)
		{
			tvTitleShareOrg.setVisibility(View.GONE);
			divider1.setVisibility(View.GONE);
		}
		else
		{
			tvTitleShare.setVisibility(View.GONE);
			divider2.setVisibility(View.GONE);
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				try
				{
					boolean doSendTo = sendTo != null;
					
					if (shareJ3MOnly)
						media.exportJ3M(a, h, encryptTo, doSendTo);
					else
						media.export(a, h, encryptTo);
				}
				catch (Exception e)
				{
					Logger.exception(LOG, e);
					Message msg = new Message();
					msg.what = -1;
					msg.getData().putString("msg", "Error exporting metadata: " + e.getMessage());
					h.sendMessage(msg);
				}
			}
		}).start();
	}

	private class HandlerIntent {
		public final Intent intent;
		public final ResolveInfo resolveInfo;

		public HandlerIntent(Intent intent, ResolveInfo resolveInfo) {
			this.intent = intent;
			this.resolveInfo = resolveInfo;
		}
	}

	private class HandlerIntentListAdapter extends ArrayAdapter<Object> {
		public HandlerIntentListAdapter(Context context, Object[] intents) {
			super(context, android.R.layout.select_dialog_item,
					android.R.id.text1, intents);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// User super class to create the View
			View v = super.getView(position, convertView, parent);
			TextView tv = (TextView) v.findViewById(android.R.id.text1);

			Object handler = getItem(position);
			int iconSize = UIHelpers.dpToPx(32, getContext());
			if (handler instanceof IOrganization) {

				IOrganization org = (IOrganization) handler;
				tv.setText(org.organizationName);
				
				//todo load the org's icon from bundle or remote site
				//Drawable icon = a.getResources().getDrawable(
				//		R.drawable.ic_share_iba);
				//icon.setBounds(0, 0, iconSize, iconSize);
				//tv.setCompoundDrawables(icon, null, null, null);
				
				tv.setCompoundDrawablePadding(UIHelpers.dpToPx(10, getContext()));
			} else if (handler instanceof HandlerIntent) {
				HandlerIntent handlerIntent = (HandlerIntent) handler;
				ResolveInfo info = handlerIntent.resolveInfo;
				PackageManager pm = getContext().getPackageManager();
				tv.setText(info.loadLabel(pm));

				Drawable icon = info.loadIcon(pm);
				icon.setBounds(0, 0, iconSize, iconSize);

				// Put the image on the TextView
				tv.setCompoundDrawables(icon, null, null, null);
				tv.setCompoundDrawablePadding(UIHelpers.dpToPx(10, getContext()));
			}

			// Add margin between image and text (support various screen
			// densities)
			int dp5 = (int) (5 * a.getResources().getDisplayMetrics().density + 0.5f);
			tv.setCompoundDrawablePadding(dp5);

			return v;
		}
	};

	private void getHandlersForIntent(Intent intent,
			ArrayList<Object> rgIntents) {
		PackageManager pm = a.getPackageManager();
		List<ResolveInfo> resInfos = pm.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);

		for (ResolveInfo resInfo : resInfos) {
			rgIntents.add(new HandlerIntent(intent, resInfo));
		}
	}
}
