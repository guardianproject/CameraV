package info.guardianproject.iocipher.player;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

public class OverlayHandler {
        public final static int POSITION_UPPER_LEFT  = 9;
        public final static int POSITION_UPPER_RIGHT = 3;
        public final static int POSITION_LOWER_LEFT  = 12;
        public final static int POSITION_LOWER_RIGHT = 6;
    	
        private Canvas canvas;
        private Paint paint;
        private Paint overlayPaint;
        private int overlayTextColor;
        private int overlayBackgroundColor;
        private int ovlPos;
        
        private int ovltop = 5;

		public OverlayHandler(Canvas c, Paint p) {
			this.canvas = c;
			this.paint = p;
            overlayPaint = new Paint();
            overlayPaint.setTextAlign(Paint.Align.LEFT);
            overlayPaint.setTextSize(12);
            overlayPaint.setTypeface(Typeface.DEFAULT);
            overlayTextColor = Color.WHITE;
            overlayBackgroundColor = Color.BLACK;
            ovlPos = POSITION_UPPER_RIGHT;
        }
		
    	public Bitmap makeOverlay(String text) {
            Rect b = new Rect();
            paint.getTextBounds(text, 0, text.length(), b);
            int bwidth  = b.width()+2;
            int bheight = b.height()+2;
            Bitmap bm = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bm);
            paint.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, bwidth, bheight, paint);
            paint.setColor(overlayTextColor);
            c.drawText(text, -b.left+1, (bheight/2)-((paint.ascent()+paint.descent())/2)+1, paint);
            return bm;           
        }

    	public void drawOverlay(Bitmap overlay) {
    		if (overlay == null) {
    			Log.v("OverlayHandler", "NONONO the overlay is null");
    		}
    		canvas.drawBitmap(overlay, 5, ovltop, paint);
    		ovltop += 5 + overlay.getHeight();
    	}
    	
        public int getOvltop() {
			return ovltop;
		}

		public void setOvltop(int ovltop) {
			this.ovltop = ovltop;
		}
    	
        public void setOverlayPaint(Paint p) { 
            overlayPaint = p; 
        }
        public void setOverlayTextColor(int c) { 
            overlayTextColor = c; 
        }
        public void setOverlayBackgroundColor(int c) { 
            overlayBackgroundColor = c; 
        }
        public void setOverlayPosition(int p) { 
            ovlPos = p; 
        }
    }