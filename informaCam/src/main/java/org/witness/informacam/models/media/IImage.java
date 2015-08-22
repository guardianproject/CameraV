package org.witness.informacam.models.media;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.witness.informacam.Debug;
import org.witness.informacam.InformaCam;
import org.witness.informacam.models.j3m.IGenealogy;
import org.witness.informacam.storage.IOUtility;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models.IUser;
import org.witness.informacam.utils.ImageUtility;
import org.witness.informacam.utils.MediaHasher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class IImage extends IMedia {
	public IImage() {
		super();
	}
	
	public IImage(IMedia media) throws InstantiationException, IllegalAccessException {
		super();
		inflate(media.asJson());
	}

	@Override
	public boolean analyze() throws IOException {
		super.analyze();
		
		InformaCam informaCam = InformaCam.getInstance();


		BitmapFactory.Options opts = new BitmapFactory.Options();	
		opts.inJustDecodeBounds = true;

		InputStream isImage = informaCam.ioService.getStream(dcimEntry.fileAsset.path, dcimEntry.fileAsset.source);
		BitmapFactory.decodeStream(isImage, null, opts);
		height = opts.outHeight;
		width = opts.outWidth;	
		isImage.close();
		
		// hash
		if(genealogy == null) {
			genealogy = new IGenealogy();
		}
		

		genealogy.hashes = new ArrayList<String>();
		
		String hash = null;
		
		try
		{
			isImage = informaCam.ioService.getStream(dcimEntry.fileAsset.path, dcimEntry.fileAsset.source);
			hash = MediaHasher.getJpegHash(isImage);
			isImage.close();
			
		}
		catch (Exception e)
		{
			Log.e(LOG,"error media hash",e);
		}
		if (hash != null)
			genealogy.hashes.add(hash);
		
		return true;
	}
}