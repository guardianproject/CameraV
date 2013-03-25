package org.witness.iwitness.models;

import org.witness.informacam.utils.models.IMedia;

import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.app.TextFormatter;
import org.witness.iwitness.utils.media.ImageInfo;

import android.app.Activity;

public class Media extends IMedia {	
	public Media(Activity a) {
		this(a, false);
	}
	
	public Media(Activity a, boolean isDummy) {
		if(isDummy) {
			int drawable, drawable_thumb, drawable_list;
			
			drawable = R.drawable.test_img;
			bitmap = ImageInfo.drawableToBitmap(a.getResources().getDrawable(drawable));
			
			drawable_list = R.drawable.test_img_list;
			bitmapList = ImageInfo.drawableToBitmap(a.getResources().getDrawable(drawable_list));
			
			drawable_thumb = R.drawable.test_img_thumb;
			bitmapThumb = ImageInfo.drawableToBitmap(a.getResources().getDrawable(drawable_thumb));
			bitmapPreview = ImageInfo.drawableToBitmap(a.getResources().getDrawable(drawable_list));
			
			_id = "aoeitj25029ti2fqkg4t98";
			_rev = "2-10asfdbqp3h35hha322tgmurls";
			alias = "Gordon Bennett (Williamsburg, NYC)";
			
			mediaType = Codes.Media.TYPE_IMAGE;
			orientation = Codes.Media.ORIENTATION_LANDSCAPE;
			
			detailsAsText = renderDetailsAsText(1);
		}
	}	
	
	private CharSequence renderDetailsAsText(int depth) {
		StringBuffer details = new StringBuffer();
		switch(depth) {
		case 1:
			details.append(TextFormatter.wrap(this._id, TextFormatter.BoldGrey.token) + System.getProperty("line.separator"));
			details.append(TextFormatter.wrap(this.alias, TextFormatter.BoldBlack.token) + System.getProperty("line.separator"));
			break;
		}
		
		return TextFormatter.formatSpan(details.toString());
	}
}
