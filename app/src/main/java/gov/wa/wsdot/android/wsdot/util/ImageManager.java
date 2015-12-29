package gov.wa.wsdot.android.wsdot.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import gov.wa.wsdot.android.wsdot.R;

public class ImageManager {

	private HashMap<String, SoftReference<Bitmap>> imageMap = new HashMap<String, SoftReference<Bitmap>>();

	private File cacheDir;
	private ImageQueue imageQueue = new ImageQueue();
	private Thread imageLoaderThread = new Thread(new ImageQueueManager());
	private long mCacheDuration;

	public ImageManager(Context context, long cacheDuration) {
		mCacheDuration = cacheDuration;
		// Make background thread low priority, to avoid affecting UI performance
		imageLoaderThread.setPriority(Thread.NORM_PRIORITY-1);
		cacheDir = context.getCacheDir();
	}

	public void displayImage(String url, Activity activity, ImageView imageView) {
		Bitmap bitmapToShow = null;
		
		if(imageMap.containsKey(url)) {
			bitmapToShow = imageMap.get(url).get();
			if(bitmapToShow != null) {
				imageView.setImageBitmap(bitmapToShow);
			} else {
				imageView.setImageResource(R.drawable.image_placeholder);
				queueImage(url, activity, imageView);
			}
		} else {
			queueImage(url, activity, imageView);
			imageView.setImageResource(R.drawable.image_placeholder);
		}
	}


	private void queueImage(String url, Activity activity, ImageView imageView) {
		// This ImageView might have been used for other images, so we clear 
		// the queue of old tasks before starting.
		imageQueue.Clean(imageView);
		ImageRef p=new ImageRef(url, imageView);

		synchronized(imageQueue.imageRefs) {
			imageQueue.imageRefs.push(p);
			imageQueue.imageRefs.notifyAll();
		}

		// Start thread if it's not started yet
		if(imageLoaderThread.getState() == Thread.State.NEW)
			imageLoaderThread.start();
	}

	private Bitmap getBitmap(String url) {
		String filename = String.valueOf(url.hashCode());
		File bitmapFile = new File(cacheDir, filename);
		
		// Is the bitmap in our cache?
		Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath());

		if(bitmap != null) {
			long currentTimeMillis = System.currentTimeMillis();
			// Has it expired?
			long bitmapTimeMillis = bitmapFile.lastModified();
			if (mCacheDuration == 0) { // Don't update. Always return the cached version.
				return bitmap;
			} else if ((currentTimeMillis - bitmapTimeMillis) < mCacheDuration) { // Within our cache time.
				return bitmap;				
			}
		}

		// Nope, have to download it
		try {
			if (url.matches("(.*)twitpic.com(.*)")) {
				url = getTwitPicImage(url);
				if (url == null) return null;
			}			
			
		    // First decode with inJustDecodeBounds=true to check dimensions
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream(), null, options);
		    
		    // Calculate inSampleSize
		    options.inSampleSize = calculateInSampleSize(options, 320, 240); // no bigger than this.
		    
		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    bitmap = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream(), null, options);
			
			// save bitmap to cache for later
			writeFile(bitmap, bitmapFile);

			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private String getTwitPicImage(String url) {
		try {
			Document doc = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1309.0 Safari/537.17")
					.get();
			Elements metas = doc.getElementsByTag("meta");
			for (Element meta : metas) {
				if (meta.attr("name").equals("twitter:image")) {
					return meta.attr("value");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of original image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    
	    return inSampleSize;
	}
	
	private void writeFile(Bitmap bmp, File f) {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(f);
			bmp.compress(Bitmap.CompressFormat.JPEG, 75, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally { 
			try { if (out != null ) out.close(); }
			catch(Exception ex) {} 
		}
	}

	/** Classes **/

	private class ImageRef {
		public String url;
		public ImageView imageView;

		public ImageRef(String u, ImageView i) {
			url=u;
			imageView=i;
		}
	}

	//stores list of images to download
	private class ImageQueue {
		private Stack<ImageRef> imageRefs = 
				new Stack<ImageRef>();

		//removes all instances of this ImageView
		public void Clean(ImageView view) {

			for(int i = 0 ;i < imageRefs.size();) {
				if(imageRefs.get(i).imageView == view)
					imageRefs.remove(i);
				else ++i;
			}
		}
	}

	private class ImageQueueManager implements Runnable {
		public void run() {
			try {
				while(true) {
					// Thread waits until there are images in the 
					// queue to be retrieved
					if(imageQueue.imageRefs.size() == 0) {
						synchronized(imageQueue.imageRefs) {
							imageQueue.imageRefs.wait();
						}
					}

					// When we have images to be loaded
					if(imageQueue.imageRefs.size() != 0) {
						ImageRef imageToLoad;

						synchronized(imageQueue.imageRefs) {
							imageToLoad = imageQueue.imageRefs.pop();
						}

						Bitmap bmp = getBitmap(imageToLoad.url);
						imageMap.put(imageToLoad.url, new SoftReference<Bitmap>(bmp));
						Object tag = imageToLoad.imageView.getTag();

						// Make sure we have the right view - thread safety defender
						if(tag != null && ((String)tag).equals(imageToLoad.url)) {
							BitmapDisplayer bmpDisplayer = 
									new BitmapDisplayer(bmp, imageToLoad.imageView);

							Activity a = 
									(Activity)imageToLoad.imageView.getContext();

							a.runOnUiThread(bmpDisplayer);
						}
					}

					if(Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {}
		}
	}

	//Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;

		public BitmapDisplayer(Bitmap b, ImageView i) {
			bitmap=b;
			imageView=i;
		}

		public void run() {
			if(bitmap != null)
				imageView.setImageBitmap(bitmap);
			else
				imageView.setImageResource(R.drawable.image_placeholder);
		}
	}
}
