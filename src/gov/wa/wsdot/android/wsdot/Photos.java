package gov.wa.wsdot.android.wsdot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery.LayoutParams;

public class Photos extends Activity {

	private static final int IO_BUFFER_SIZE = 4 * 1024;
	private static final String DEBUG_TAG = "Photos";
    ArrayList<Drawable> bitmapImages = new ArrayList<Drawable>();
    ArrayList<String> remoteImages = new ArrayList<String>();
    private ArrayList<PhotoItem> photoItems = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displayview);
        ((TextView)findViewById(R.id.sub_section)).setText("Photos");      
        new GetRSSItems().execute();
    }
   
    private class GetRSSItems extends AsyncTask<String, Integer, String> {
    	private final ProgressDialog dialog = new ProgressDialog(Photos.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving photos from Flickr ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setCancelable(true);
	        this.dialog.setMax(20);
	        this.dialog.show();
		}
    	
		@Override
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected String doInBackground(String... params) {
	    	String patternStr = "http://farm.*jpg";
	    	Pattern pattern = Pattern.compile(patternStr);
	    	BufferedInputStream in;
	        BufferedOutputStream out;
	    	
	        try {
	            URL text = new URL("http://api.flickr.com/services/feeds/photos_public.gne?id=7821771@N05&amp;lang=en-us&amp;format=atom");
	            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
	            XmlPullParser parser = parserCreator.newPullParser();
	            parser.setInput(text.openStream(), null);
	            int parserEvent = parser.getEventType();
				String tag;
				String title;
				String published;
				String link;
				String content;
				boolean inTitle = false;
				boolean inPublished = false;
				boolean inLink = false;
				boolean inContent = false;
				boolean inEntry = false;
				photoItems = new ArrayList<PhotoItem>();
				PhotoItem i = null;
	            
	            while (parserEvent != XmlPullParser.END_DOCUMENT) {
	                switch (parserEvent) {
	                case XmlPullParser.TEXT:
						if (inEntry & inTitle) {
							title = parser.getText();
							i.setTitle(title);
						}
						if (inEntry & inPublished) {
							published = parser.getText();
							i.setPublished(published);
						}
						if (inEntry & inLink) {
							link = parser.getText();
							i.setLink(link);
						}
	                    if (inEntry & inContent) {
	                    	String tmpContent = parser.getText();
	                    	content = tmpContent.replace("<p><a href=\"http://www.flickr.com/people/wsdot/\">WSDOT</a> posted a photo:</p>", "");
							i.setContent(content);                  
		                	Matcher matcher = pattern.matcher(content);
		                	boolean matchFound = matcher.find();

		                	if (matchFound) {
		                		String tmpString = matcher.group();
		                		String imageSrc = tmpString.replace("_m", "_s"); // We want the small 75x75 images
		                		remoteImages.add(imageSrc);
	                            in = new BufferedInputStream(new URL(imageSrc).openStream(), IO_BUFFER_SIZE);
	                            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
	                            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
	                            copy(in, out);
	                            out.flush();
	                            final byte[] data = dataStream.toByteArray();
	                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);                        
		                        final Drawable image = new BitmapDrawable(bitmap);
		                        bitmapImages.add(image);
		                	}
	                    }
	                    break;

	                case XmlPullParser.END_TAG:
						tag = parser.getName();
						if (tag.compareTo("entry") == 0) {
							inEntry = false;
							photoItems.add(i);
							publishProgress(1);
						}
						if (tag.compareTo("title") == 0) {
							inTitle = false;
						}
						if (tag.compareTo("published") == 0) {
							inPublished = false;
						}
						if (tag.compareTo("link") == 0) {
							inLink = false;
						}
						if (tag.compareTo("content") == 0) {
							inContent = false;
						}
	                    break;

	                case XmlPullParser.START_TAG:
						tag = parser.getName();
						if (tag.compareTo("entry") == 0) {
							inEntry = true;
							i = new PhotoItem();
						}
						if (tag.compareTo("title") == 0) {
							inTitle = true;
						}
						if (tag.compareTo("published") == 0) {
							inPublished = true;
						}
						if (tag.compareTo("link") == 0) {
							inLink = true;
						}					
						if (tag.compareTo("content") == 0) {
							inContent = true;
						}
						break;
	                }
	                parserEvent = parser.next();
	            }
	        } catch (Exception e) {
	            Log.e(DEBUG_TAG, "Error parsing Flickr RSS feed", e);
	        }
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			showImages();
		}   
    }
    
    private void showImages() {
    	try {
            GridView gridView = (GridView) findViewById(R.id.gridview);
            gridView.setAdapter(new ImageAdapter(this));    
            gridView.setOnItemClickListener(new OnItemClickListener() {
            	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        		Bundle b = new Bundle();
	        		Intent intent = new Intent(Photos.this, PhotoItemDetails.class);
	        		b.putString("heading", photoItems.get(position).getTitle());
	        		b.putString("content", photoItems.get(position).getContent());
	        		intent.putExtras(b);
	        		startActivity(intent);
	            }
	        });

    	} catch (Exception e) {
            Log.e("DEBUG_TAG", "Error getting images", e);
        }	        
    }  
    
    public View makeView() {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundColor(0xFFFFFFFF);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setLayoutParams(new 
                ImageSwitcher.LayoutParams(
                        LayoutParams.FILL_PARENT,
                        LayoutParams.FILL_PARENT));
        return imageView;
    }
    
    public class ImageAdapter extends BaseAdapter {
        private Context context;
      
        public ImageAdapter(Context c) {
        	context = c;       	
        }

        public int getCount() { return remoteImages.size(); }
        public Object getItem(int position) { return position; }
        public long getItemId(int position) { return position; }
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(context);

            if (convertView == null) {
                imageView.setLayoutParams(new GridView.LayoutParams(75, 75));
                imageView.setPadding(5, 5, 5, 5);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageDrawable(bitmapImages.get(position));

            return imageView;
        }
    }
    
    /**
     * Copy the content of the input stream into the output stream, using a
     * temporary byte array buffer whose size is defined by
     * {@link #IO_BUFFER_SIZE}.
     * 
     * @param in The input stream to copy from.
     * @param out The output stream to copy to.
     * @throws IOException If any error occurs during the copy.
     */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }    
}
