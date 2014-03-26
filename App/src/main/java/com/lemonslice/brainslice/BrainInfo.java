package com.lemonslice.brainslice;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.lemonslice.brainslice.event.Event;
import android.media.MediaPlayer;

import com.threed.jpct.SimpleVector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

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
            Event.trigger("data:loaded");
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

    public static void loadData()
    {
        new BrainApiTask().execute();
    }

    public static boolean parseJSON(JsonReader reader) throws IOException
    {
        Log.d("BRAININFO", "opened reader");

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
                    else if (key.equals("audio"))
                    {
                        reader.nextBoolean();
                    }
                }
            }
            catch (IllegalStateException e)
            {
                Log.d("BRAININFO", "error parsing JSON");
                e.printStackTrace();
                return true;
            }

            segments.put(segmentName, segment);

            reader.endObject();
        }
        reader.endObject();
        return false;
    }

    private static class BrainApiTask extends AsyncTask<String, String, String> {

        private void getData(String apiUrl) throws IOException
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

            parseJSON(reader);

            Log.d("BRAININFO", "done reading");

            conn.disconnect();
            in.close();
            reader.close();

            Log.d("BRAININFO", "closed connections");

        }

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {
                getData(API_ENDPOINT);
                return "success";
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return "error";
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            BrainInfo.setDataIsLoaded(true);
        }

    }
}
