package org.crypticplank.smp.Util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

public class Twitch {
    private final String ClientId;
    private final String ClientSecret;
    public String OAuth;
    private int OAuthExpiresIn;
    private boolean IsFirstRun = true;

    public class TwitchStream {
        public int ViewCount;
        public boolean IsOnline;
        public String ProfilePictureUrl;

        public TwitchStream(int ViewCount, boolean IsOnline, String ProfilePictureUrl) {
            this.ViewCount = ViewCount;
            this.IsOnline = IsOnline;
            this.ProfilePictureUrl = ProfilePictureUrl;
        }
    }

    public Twitch(String ClientId, String ClientSecret) {
        this.ClientId = ClientId;
        this.ClientSecret = ClientSecret;
        try {
            GetAuthToken();
        } catch (InterruptedException | IOException ignored) {
        }
        StartOAuthUpdateTimer();
    }

    public JSONObject CallApi(String endpoint) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.twitch.tv/helix/" + endpoint))
                .header("Client-ID", this.ClientId)
                .header("Authorization", "Bearer " + this.OAuth)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new JSONObject(response.body());
    }

    public String GetTwitchPicture(String name) throws IOException, InterruptedException {
        JSONObject json = CallApi("users?login=" + name);
        JSONArray array = null;
        try {
            array = json.getJSONArray("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(array == null || array.isEmpty()) {
            return null;
        }
        Iterator<Object> iterator = array.iterator();
        JSONObject data = (JSONObject)iterator.next();
        return data.getString("profile_image_url");
    }

    /**
     * Returns a twitch stream containing the users view count and if they are online.
     * @param name
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public TwitchStream GetStream(String name, boolean getProfilePicture) throws IOException, InterruptedException {
        JSONObject json = CallApi("streams?user_login=" + name);
        JSONArray array = null;
        try {
            array = json.getJSONArray("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(array == null || array.isEmpty()) {
            return new TwitchStream(0, false, null);
        }
        Iterator<Object> iterator = array.iterator();
        JSONObject data = (JSONObject)iterator.next();
        if(data == null || !data.has("viewer_count")) {
            return new TwitchStream(0, false, null);
        }
        int viewerCount = data.getInt("viewer_count");
        if(getProfilePicture) {
            return new TwitchStream(viewerCount, true, GetTwitchPicture(name));
        }
        return new TwitchStream(viewerCount, true, null);
    }

    /**
     * Gets an auth token based on the ClientID and ClientSecret.
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private String GetAuthToken() throws IOException, InterruptedException {
        String payload = "{\"client_id\": \""+ ClientId +"\", \"client_secret\": \""+ ClientSecret +"\", \"grant_type\": \"client_credentials\"}";
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://id.twitch.tv/oauth2/token"))
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        this.OAuth = json.get("access_token").toString();
        this.OAuthExpiresIn = Integer.parseInt(json.get("expires_in").toString());
        return this.OAuth;
    }

    /**
     * Starts updating the OAuth token based on the time it returns - ~ 20 seconds.
     */
    private void StartOAuthUpdateTimer() {
        Thread thread = new Thread(() -> {
            try {
                if(!this.IsFirstRun) {
                    GetAuthToken();
                }
                Thread.sleep((OAuthExpiresIn * 1000L) - (20 * 1000L));
                this.IsFirstRun = false;
                StartOAuthUpdateTimer();
            } catch (InterruptedException | IOException ignored) {
            }
        });
        thread.start();
    }
}
