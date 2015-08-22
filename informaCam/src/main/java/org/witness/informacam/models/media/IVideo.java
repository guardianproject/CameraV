package org.witness.informacam.models.media;

import java.io.IOException;
import java.util.ArrayList;

import org.witness.informacam.InformaCam;
import org.witness.informacam.informa.embed.VideoConstructor;
import org.witness.informacam.models.j3m.IGenealogy;
import org.witness.informacam.storage.IOUtility;

import android.graphics.Bitmap;

public class IVideo extends IMedia {
	public IAsset video = null;
	
	public IVideo() {
		super();
	}
	
	public IVideo(IMedia media) throws InstantiationException, IllegalAccessException {
		super();
		inflate(media.asJson());
	}
	
	@Override
	public Bitmap getBitmap(IAsset bitmapAsset) {
		try
		{
			return IOUtility.getBitmapFromFile(dcimEntry.thumbnail.path, dcimEntry.thumbnail.source);
		}
		catch (IOException ioe)
		{
			return null;
		}
	}
	
	@Override
	public boolean analyze() throws IOException {
		super.analyze();

		InformaCam informaCam = InformaCam.getInstance();

		height = dcimEntry.exif.height;
		width = dcimEntry.exif.width;
		
		// 1. hash
		VideoConstructor vc = new VideoConstructor(informaCam);
		if(genealogy == null) {
			genealogy = new IGenealogy();
		}
		
		genealogy.hashes = new ArrayList<String>();
		
		String hash = vc.hashVideo(dcimEntry.fileAsset.path, dcimEntry.fileAsset.source, "mp4");		
		
		if (hash != null)
			genealogy.hashes.add(hash);
		
		// 2. copy over video
		video = dcimEntry.fileAsset;	
		return true;
	}
}