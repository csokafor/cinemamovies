package com.chinedusokafor.silverbirdmoviis.rottentomatoes;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cokafor on 1/21/2015.
 */
public class RottentomatoesAPI {
    public static final String LOG_TAG = RottentomatoesAPI.class.getSimpleName();
    public static final String UTF = "UTF-8";
    public static final String GIDIMO_API_KEY = "brd9f8habdmrufwh6wtf9nfu";
    public static final String API_URL = "http://api.rottentomatoes.com/api/public/v1.0/movies.json";

    public JSONObject searchForMovie(String movieTitle) {
        JSONObject requiredJson = null;
        try {
            String urlEncodedMovieTitle = URLEncoder.encode(movieTitle, UTF);
            String fullUrlToUse = API_URL + "?" + "q=" + urlEncodedMovieTitle
                    + "&page_limit=" + 1 + "&page=1" + "&apikey=" + GIDIMO_API_KEY;

            String serverResponse = getResponseAtUrl(fullUrlToUse);
            JSONObject theServerResponseJson = new JSONObject(serverResponse);

            JSONArray moviesJsonArray = (JSONArray) theServerResponseJson.get("movies");
            if (moviesJsonArray.length() > 0) {
                if (moviesJsonArray.length() == 1) {
                    JSONObject aMovieJsonObj = (JSONObject) moviesJsonArray.get(0);
                    String aMovieTitle = (String) aMovieJsonObj.get("title");
                    requiredJson = aMovieJsonObj;

                } else {
                    for (int i = 0; i < moviesJsonArray.length(); i++) {
                        JSONObject aMovieJsonObj = (JSONObject) moviesJsonArray.get(i);
                        String aMovieTitle = (String) aMovieJsonObj.get("title");
                        Log.d(LOG_TAG, "search result" + aMovieTitle);
                        if (aMovieTitle.equalsIgnoreCase(movieTitle)) {
                            requiredJson = aMovieJsonObj;
                            break;
                        }
                    }
                }
            } else {
                //No movies match the search parameter
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "searchForMovie " + e.getMessage(), e);
        }
        return requiredJson;
    }

    public JSONObject getMovieJson(String movieUrl) {
        JSONObject movieJson = new JSONObject();
        try {
            String jsonUrl = movieUrl + "?apikey=" + GIDIMO_API_KEY;
            String serverResponse = getResponseAtUrl(jsonUrl);
            movieJson =  new JSONObject(serverResponse);
        } catch (Exception e) {
            Log.e(LOG_TAG, "getMovieJson " + e.getMessage(), e);
        }
        return movieJson;
    }

    public JSONArray getMovieReviewsJson(JSONObject theMovieJson) {
        JSONArray reviewJson = new JSONArray();
        try {
            JSONObject links = (JSONObject) theMovieJson.get("links");
            String reviewUrl = (String) links.get("reviews");
            String jsonUrl = reviewUrl + "?apikey=" + GIDIMO_API_KEY;
            String serverResponse = getResponseAtUrl(jsonUrl);

            JSONObject responseJson = new JSONObject(serverResponse);
            Log.d(LOG_TAG, responseJson.toString());
            reviewJson = (JSONArray)responseJson.get("reviews");

        } catch (Exception e) {
            Log.e(LOG_TAG, "getMovieReviewsJson " + e.getMessage(), e);
        }
        return reviewJson;
    }

    public String getMovieJsonUrl(JSONObject theMovieJson) {
        String jsonUrl = "";
        try {
            JSONObject links = (JSONObject) theMovieJson.get("links");
            jsonUrl = (String) links.get("self");
        } catch (Exception e) {
            Log.e(LOG_TAG, "getMovieJsonUrl " + e.getMessage(), e);
        }
        return jsonUrl;
    }


    public String getMovieGenres(JSONObject theMovieJson) {
        String genres = "";
        try {
            JSONArray genresArray = (JSONArray) theMovieJson.get("genres");

            for(int i = 0; i < genresArray.length(); i++) {
                String genre = (String) genresArray.get(i);
                genres += genre + " ";
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "getMovieGenres " + e.getMessage(), e);
        }
        return genres;
    }

    public Map<String,String> getMovieCast(JSONObject theMovieJson) {
        Map<String,String> theCast = new HashMap<String,String>();
        try {
            JSONArray abridgedCast = (JSONArray) theMovieJson.get("abridged_cast");

            for(int i = 0; i < abridgedCast.length(); i++) {
                JSONObject aMovieStar = (JSONObject) abridgedCast.get(i);
                String movieStarName = (String) aMovieStar.get("name");
                JSONArray characterArray = (JSONArray)aMovieStar.get("characters");
                String character = "";
                for(int j = 0; j < characterArray.length(); j++) {
                    String genre = (String) characterArray.get(j);
                    character += genre + " ";
                }
                theCast.put(movieStarName, character);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "getMovieCast " + e.getMessage(), e);
        }
        return theCast;
    }


    public String[] getMovieDirectors(JSONObject theMovieJson) {
        String[] theDirectors = null;
        try {
            JSONArray movieDirectorsArray = (JSONArray) theMovieJson.get("abridged_directors");
            theDirectors = new String[movieDirectorsArray.length()];

            for(int i = 0; i < theDirectors.length; i++) {
                JSONObject aDirectorObj = (JSONObject) movieDirectorsArray.get(i);
                String directorName = (String) aDirectorObj.get("name");
                theDirectors[i] = directorName;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "getMovieDirectors " + e.getMessage(), e);
        }
        return theDirectors;
    }

    public ArrayList<String> getCurrentlyInTheatre() {
        ArrayList<String> movieList = new ArrayList<String>();

        try {
            String apiUrl = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?"
                    + "&page_limit=16&page=1" + "&apikey=" + GIDIMO_API_KEY;

            String serverResponse = getResponseAtUrl(apiUrl);
            JSONObject theServerResponseJson = new JSONObject(serverResponse);

            JSONArray moviesJsonArray = (JSONArray) theServerResponseJson.get("movies");
            if (moviesJsonArray.length() > 0) {
                for (int i = 0; i < moviesJsonArray.length(); i++) {
                    JSONObject movieJsonObj = (JSONObject) moviesJsonArray.get(i);
                    String movieTitle = (String) movieJsonObj.get("title");
                    movieList.add(movieTitle);
                    Log.d(LOG_TAG, "movieTitle " + movieTitle);
                }

            } else {
                //No movies match the search parameter
            }
        } catch(Exception e) {
            Log.e(LOG_TAG, "getCurrentlyShowingMovies " + e.getMessage(), e);
        }
        return movieList;
    }




    private String getResponseAtUrl(String urlConnString) {
        String serverResponse = "";
        try {
            java.net.URL urlConn = new java.net.URL(urlConnString);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlConn.openConnection();
            conn.addRequestProperty("Content-Type", "Application/json");

            if(conn.getResponseCode() == 200){
                java.io.InputStream stream = conn.getInputStream();
                java.io.InputStreamReader reader = new java.io.InputStreamReader(stream);
                java.io.BufferedReader ireader = new java.io.BufferedReader(reader);

                String x = "";
                while((x = ireader.readLine()) != null){
                    //System.out.println("response x: " + x);
                    if(!x.isEmpty()) {
                        serverResponse = x;
                    }
                }
            }
        } catch(IOException e) {
            Log.e(LOG_TAG, "Why No internet access.", e);
        }
        return serverResponse;
    }


}
