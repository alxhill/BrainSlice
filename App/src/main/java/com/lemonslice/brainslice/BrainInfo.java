package com.lemonslice.brainslice;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.lemonslice.brainslice.event.Event;
import com.threed.jpct.SimpleVector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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
    private static boolean dataIsLoaded = false;

    public static HashMap<String, BrainSegment> getSegments() {
        if (!dataIsLoaded) return null;
        return segments;
    }

    static
    {
        BrainSegment cerebellum = new BrainSegment(
            "Cerebellum",
            "This part of your brain helps you stay balanced and helps you move.",
            SimpleVector.create(0, 100.0f, -50.0f));

        BrainSegment cerebrum = new BrainSegment(
            "Cerebrum",
            "The cerebrum consists of the cerebral cortex and subcortical structures. It controls all voluntary actions in the body",
            null);

        BrainSegment cerebralCortex = new BrainSegment(
            "Cerebral cortex",
            "The cerebral cortex is the outermost layer of tissue, covering the cerebrum. It takes part in memory, attention, and thought.",
            null);

        BrainSegment brainStem = new BrainSegment(
            "Brainstem",
            "The brainstem is the back part of the brain, connected to the spinal cord. It keeps you breathing, sleeping, and eating.",
            SimpleVector.create(0, 15.0f, -40.0f));

        BrainSegment hippocampus = new BrainSegment(
            "Hippocampus",
            "The hippocampus is important in making short-term memories long-term. It looks a bit like a seahorse.",
            null);

        BrainSegment amygdala = new BrainSegment(
            "Amygdala",
            "The amygdalae are almond-shaped and help you process your memories and emotions.",
            SimpleVector.create(30.0f, 15.0f, -40.0f));

        BrainSegment medullaOblongata = new BrainSegment(
            "Medulla oblongata",
            "The medulla oblongata is the lower half of the brainstem, which helps regulate your bodily functions, like breathing and blood flow.",
            null);

        BrainSegment hypothalamus = new BrainSegment(
            "Hypothalamus",
            "The hypothalamus is a part of the brain that links your nervous system to your kidneys and other organs, as well as telling you when you're hungry or thirsty",
            null);

        BrainSegment frontalLobe = new BrainSegment(
            "Frontal lobe",
            "The frontal lobe is the front part of your brain, which helps you choose between good and bad actions, and understand what's going to happen next.",
            SimpleVector.create(0, -70.0f, 0));

        BrainSegment parietalLobe = new BrainSegment(
            "Parietal lobe",
            "The parietal lobe is the top part of your brain, which helps you understand your senses, like touch.",
            SimpleVector.create(0, 80.0f, 50.0f));

        BrainSegment occipitalLobe = new BrainSegment(
            "Occipital lobe",
            "The occipital lobe is connected directly to your eyes, and turns the picture from your eyes the right way up. It also helps you understand what you see.",
            SimpleVector.create(0, 100.0f, 0));

        BrainSegment temporalLobe = new BrainSegment(
            "Temporal lobe",
            "The temporal lobe lets you keep visual memories, understand languages, and stores new memories and emotion.",
            SimpleVector.create(-75.0f, 0, 0));


//        segments.put(cerebellum.getTitle(), cerebellum);
//        segments.put(cerebrum.getTitle(), cerebrum);
//        segments.put(cerebralCortex.getTitle(), cerebralCortex);
//        segments.put(brainStem.getTitle(), brainStem);
//        segments.put(hippocampus.getTitle(), hippocampus);
//        segments.put(amygdala.getTitle(), amygdala);
//        segments.put(medullaOblongata.getTitle(), medullaOblongata);
//        segments.put(hypothalamus.getTitle(), hypothalamus);
//        segments.put(frontalLobe.getTitle(), frontalLobe);
//        segments.put(parietalLobe.getTitle(), parietalLobe);
//        segments.put(occipitalLobe.getTitle(), occipitalLobe);
//        segments.put(temporalLobe.getTitle(), temporalLobe);
    }

    static BrainSegment getSegment(String segment) {
        if (!dataIsLoaded) return null;
        return segments.get(segment);
    }

    public static void loadData()
    {
        new BrainApiTask().execute();
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
                    return;
                }

                segments.put(segmentName, segment);

                reader.endObject();
            }
            reader.endObject();

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
                return null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            dataIsLoaded = true;
            Event.trigger("data:loaded");
        }
    }
}
