package com.lemonslice.brainslice;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.lemonslice.brainslice.event.Events;
import android.media.MediaPlayer;

import com.threed.jpct.SimpleVector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by James on 29/01/14.
 */
public class BrainInfo
{
    public static final String API_ENDPOINT = "http://162.243.46.77:3333/api/all";

    private static HashMap<String, BrainSegment> segments = new HashMap<String, BrainSegment>();

    public static boolean isDataIsLoaded()
    {
        return dataIsLoaded;
    }

    public static MediaPlayer speaker = new MediaPlayer();

    private static boolean dataIsLoaded = false;

    public static void setDataIsLoaded(boolean dataIsLoaded)
    {
        BrainInfo.dataIsLoaded = dataIsLoaded;
        if (dataIsLoaded)
            Events.trigger("data:loaded");
    }

    public static HashMap<String, BrainSegment> getSegments()
    {
        if (!dataIsLoaded) return null;
        return segments;
    }

    static BrainSegment getSegment(String segment) {
        if (!dataIsLoaded) return null;
        return segments.get(segment);
    }

    // downloads data from the server in a separate thread.
    public static boolean loadData()
    {
        AsyncTask<String, String, Boolean> task = new BrainApiTask().execute();
        try
        {
            return task.get();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    // reads data from the 'backup copy' on the device
    public static void readData(Resources resources)
    {
        Log.d("BRAININFO", "Loading data from file");
        InputStream data = resources.openRawResource(R.raw.data);

        JsonReader reader = null;
        try
        {
            reader = new JsonReader(new InputStreamReader(data, "UTF-8"));
            BrainInfo.parseJSON(reader);
            data.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unable to load segment data, aborting.");
        }

        BrainInfo.setDataIsLoaded(true);
    }

    public static boolean parseJSON(JsonReader reader) throws IOException
    {
        Log.d("BRAININFO", "opened JSON reader");

        // this section parses the data based on what it expects to find.
        reader.beginObject();
        while (reader.hasNext())
        {
            String segmentName = reader.nextName();
            Log.d("BRAININFO", "Processing segment " + segmentName);
            BrainSegment segment = new BrainSegment(segmentName);
            try
            {
                reader.beginObject();
                while (reader.hasNext())
                {

                    String key = reader.nextName();
                    assert key != null;
                    if (key.equals("name"))
                    {
                        String title = reader.nextString();
                        Log.d("JSONIFY", title);
                        segment.setTitle(title);
                    }
                    else if (key.equals("description"))
                        segment.setDescription(reader.nextString());
                    else if (key.equals("position"))
                    {
                        JsonToken token = reader.peek();
                        assert token != null;
                        if (token.compareTo(JsonToken.BEGIN_ARRAY) == 0)
                        {
                            reader.beginArray();
                            segment.setPosition(SimpleVector.create(
                                    (float) reader.nextDouble(),
                                    (float) reader.nextDouble(),
                                    (float) reader.nextDouble()
                            ));
                            reader.endArray();
                        }
                        else
                        {
                            reader.nextNull();
                        }
                    }
                    else if (key.equals("tasks"))
                    {
                        reader.beginArray();
                        while (reader.hasNext())
                            segment.addTask(reader.nextString());
                        reader.endArray();
                    }
                    else
                    {
                        reader.skipValue();
                    }
                }
            }
            catch (IllegalStateException e)
            {
                Log.d("BRAININFO", "error parsing JSON");
                e.printStackTrace();
                return false;
            }

            segments.put(segmentName, segment);

            reader.endObject();
        }
        reader.endObject();
        reader.close();
        return true;
    }

    private static class BrainApiTask extends AsyncTask<String, String, Boolean> {

        private boolean getData(String apiUrl) throws IOException
        {
            // initialise the data request (assumes connectivity has already been checked)
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            Log.d("BRAININFO", "Connecting...");

            conn.connect();

            Log.d("BRAININFO", "Connected");

            InputStream in = conn.getInputStream();
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

            boolean parsed = parseJSON(reader);

            Log.d("BRAININFO", "done reading");

            conn.disconnect();
            in.close();

            Log.d("BRAININFO", "closed connections");

            return parsed;

        }

        @Override
        protected Boolean doInBackground(String... strings)
        {
            try
            {
                return getData(API_ENDPOINT);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean b)
        {
            super.onPostExecute(b);
            BrainInfo.setDataIsLoaded(b);
        }

    }
}
