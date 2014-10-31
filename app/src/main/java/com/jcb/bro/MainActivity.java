package com.jcb.bro;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends Activity
{

    ArrayList<Course> courseList = new ArrayList<Course>();
    int getMoreCount = 0;
    CustomAdapter adapter;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        ListView listView = (ListView) findViewById( R.id.listView );

        adapter = new CustomAdapter(this, R.layout.custom_row, courseList);

        listView.setAdapter( adapter );

        getMore( 0, 30 );


    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if ( id == R.id.action_settings )
        {
            return true;
        }
        return super.onOptionsItemSelected( item );
    }

    public void getMore( final int start, final int size )
    {
        int count = 0;
        try
        {
            // Test Server
            //URL url = new URL( "https://stage.api.sympoz.com/ws/resource/course/?start=" + start + "&limit=" + 10 + "&siteId=1&appId=99" );

            //Production Server
            URL url = new URL( "https://api.sympoz.com/ws/resource/course/?start=" + start + "&limit=" + 10 + "&siteId=1&appId=99" );

            HttpURLConnection con = (HttpURLConnection) url
                    .openConnection();
            String result = readStream( con.getInputStream() );
            System.out.println(result);
            JSONArray jsonArray = new JSONArray( result );
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject( i );
                System.out.println( jsonObject.get("title") );
                Course course = new Course();
                course.id = jsonObject.getString( "id" );
                course.title = jsonObject.getString( "title" );
                course.imageUrl = jsonObject.getString( "imageUrlBig" );
                course.price = jsonObject.getString( "price" );
                courseList.add( course );

            }
            adapter.notifyDataSetChanged();
            count = jsonArray.length();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        // If we got less then 10 results then we're at the end of the line stop trying
        if(count == 10)
        {
            //Sometimes the loops indefinitely and I don't know why so getMoreCount will limit us to 20 loops
            if(getMoreCount < 20)
            {
                // Call next loop on background so view will finish creating
                Handler handler = new Handler();
                handler.post( new Runnable()
                {
                    public void run()
                    {
                        //Wait 3 seconds between loops so we don't hammer the server
                        try
                        {
                            Thread.sleep( 3000 );
                        }
                        catch ( InterruptedException e )
                        {
                            e.printStackTrace();
                        }
                        getMoreCount++;
                        getMore( size, size + 10 );
                    }
                } );


            }

        }
    }

    private String readStream( InputStream in )
    {
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer( "" );

        try
        {
            reader = new BufferedReader( new InputStreamReader( in ) );

            String line;
            while ( ( line = reader.readLine() ) != null )
            {
                sb.append( line );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            if ( reader != null )
            {
                try
                {
                    reader.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    private static class CustomAdapter extends ArrayAdapter<Course>
    {
        public CustomAdapter(Context context, int layout, List<Course> items)
        {
            super(context, layout, items);

        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
            View row = convertView;
            if(row == null)
            {
                row = LayoutInflater.from( getContext() ).inflate( R.layout.custom_row, parent, false );
            }

            TextView title = (TextView)row.findViewById( R.id.title );
            TextView price = (TextView)row.findViewById( R.id.price );
            ImageView image = (ImageView)row.findViewById( R.id.image );

            Course course = getItem( position );

            title.setText( course.title );
            price.setText( course.price );

            //Bitmap bitmap = getBitmapFromURL( course.imageUrl );

            //image.setImageDrawable( new BitmapDrawable(bitmap) );

            return row;
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream( input );
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
