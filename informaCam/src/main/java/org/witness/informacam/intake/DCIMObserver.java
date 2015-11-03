package org.witness.informacam.intake;

import java.util.List;
import java.util.Vector;

import org.witness.informacam.Debug;
import org.witness.informacam.InformaCam;
import org.witness.informacam.models.j3m.IDCIMDescriptor;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.Logger;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

public class DCIMObserver extends BroadcastReceiver {

    private final static String LOG = "DCIMObserver";

    public static IDCIMDescriptor dcimDescriptor;
    public ComponentName cameraComponent;

    List<ContentObserver> observers;
    InformaCam informaCam = InformaCam.getInstance();

    Handler h;
    private Context mContext;

    private boolean debug = false;

    public DCIMObserver () {}

    public DCIMObserver(Context context, String parentId, ComponentName cameraComponent) {

        mContext = context;
        this.cameraComponent = cameraComponent;

        h = new Handler();

        observers = new Vector<ContentObserver>();
        observers.add(new Observer(h, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        observers.add(new Observer(h, MediaStore.Images.Media.INTERNAL_CONTENT_URI));
        observers.add(new Observer(h, MediaStore.Video.Media.EXTERNAL_CONTENT_URI));
        observers.add(new Observer(h, MediaStore.Video.Media.INTERNAL_CONTENT_URI));
        observers.add(new Observer(h, MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI));
        observers.add(new Observer(h, MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI));
        observers.add(new Observer(h, MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI));
        observers.add(new Observer(h, MediaStore.Video.Thumbnails.INTERNAL_CONTENT_URI));

        for(ContentObserver o : observers) {
            mContext.getContentResolver().registerContentObserver(((Observer) o).authority, false, o);
        }

        dcimDescriptor = new IDCIMDescriptor(parentId, cameraComponent);
        dcimDescriptor.startSession();

        //Log.d(LOG, "DCIM OBSERVER INITED");
    }

    public void destroy() {
        dcimDescriptor.stopSession();
        dcimDescriptor = null;

        for(ContentObserver o : observers) {
            mContext.getContentResolver().unregisterContentObserver(o);
        }

        Log.d(LOG, "DCIM OBSERVER STOPPED");

    }

    class Observer extends ContentObserver {
        Uri authority;

        public Observer(Handler handler, Uri authority) {
            super(handler);
            this.authority = authority;
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.d(LOG, "ON CHANGE CALLED (no URI)");
            onChange(selfChange, null);

        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            boolean isThumbnail = false;

            if(Debug.DEBUG) {
                Logger.d(LOG, "AUTHORITY: " + authority.toString());

                if(uri != null) {
                    //Log.d(LOG, "ON CHANGE CALLED (with URI!)");
                    Logger.d(LOG, "URI: " + uri.toString());
                }
            }

            if(
                    authority.equals(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI) ||
                            authority.equals(MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI) ||
                            authority.equals(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI) ||
                            authority.equals(MediaStore.Video.Thumbnails.INTERNAL_CONTENT_URI)
                    ) {
                isThumbnail = true;
            }

            try
            {
                dcimDescriptor.addEntry(authority.toString(), isThumbnail,Storage.Type.FILE_SYSTEM);
            }
            catch (Exception e)
            {
                //Logger.d(LOG,"unable to add thumbnail");
                Logger.e(LOG, e);
            }
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Cursor cursor = context.getContentResolver().query(intent.getData(), null, null, null, null);

        try {

            if (cursor != null && cursor.isBeforeFirst())
            {
                cursor.moveToFirst();
                String media_path = cursor.getString(cursor.getColumnIndex("_data"));
                cursor.close();

                if (dcimDescriptor != null) {
                    try {
                        dcimDescriptor.addEntry(media_path, false, Storage.Type.FILE_SYSTEM);
                    } catch (Exception e) {
                        //Logger.d(LOG,"unable to add thumbnail");
                        Logger.e(LOG, e);
                    }

                    Logger.d(LOG, String.format("pulled media file: %s", media_path));
                }
            }
        }
        catch (Exception e)
        {
            Logger.e(LOG, e);
        }
    }

}
