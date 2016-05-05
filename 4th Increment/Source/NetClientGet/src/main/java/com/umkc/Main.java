package com.umkc;

import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Main {

    public static void main(String[] args) {
     SocketConn soc = new SocketConn();

        String Artist = soc.initialize();
        String[] TrackList = new String[500];

        try {

            URL url = new URL("http://ws.audioscrobbler.com/2.0/?method=track.search&track="+Artist+"&api_key=75acfb39d4896f48c41697c3e5a10222&format=json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            File file = new File("C:/Users/sowmy/Documents/json/json.txt");
            FileReader reader = new FileReader(file);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(br.readLine());
            bw.close();
            try
            {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
                JSONObject results =(JSONObject) jsonObject.get("results");
                JSONObject trackmatches = (JSONObject)results.get("trackmatches");
                JSONArray track =(JSONArray) trackmatches.get("track");
                for(int i=0; i<track.size(); i++){
                    System.out.println("The " + i + " element of the array: "+track.get(i));
                }
                Iterator i = track.iterator();
                int j = 0;
               //String resulttrack = "null";
                // take each value from the json array separately

                while (i.hasNext()) {

                    JSONObject innerObj = (JSONObject) i.next();

                    System.out.println("track link "+ innerObj.get("url") );
                   // resulttrack=(String)innerObj.get("url");
                    TrackList[j] = innerObj.get("url").toString();
                    System.out.println("track list in array "+  TrackList[j]  );
                    socket.sendCommandToRobot("You look so happy! Would you like to listen to some songs? Here are the songs Recommended for you!!"+Artist +TrackList[j]);
                    j=j+1;
                }
                //socket.sendCommandToRobot("You look so happy! Would you like to listen to some songs? Here are the songs Recommended for you!!" +resulttrack);
               /* for(int k=0;k<TrackList.length;k++) {
                    socket.sendCommandToRobot("You look so happy! Would you like to listen to some songs? Here are the songs Recommended for you!!"+Artist +TrackList[k]);
                }*/
            }
            catch (FileNotFoundException ex) {

                ex.printStackTrace();

            } catch (IOException ex) {

                ex.printStackTrace();

            } catch (ParseException ex) {

                ex.printStackTrace();

            } catch (NullPointerException ex) {

                ex.printStackTrace();

            }

            String output;
            System.out.println("Output from Server .... \n"+Artist);
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}
