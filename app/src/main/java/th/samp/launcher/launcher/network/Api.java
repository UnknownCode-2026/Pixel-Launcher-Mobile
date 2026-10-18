package th.samp.launcher.launcher.network;

import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.http.GET;

public interface Api {

    @GET("api.json")
    Call<Links> getLinks();

    @GET("servers.json") // servers.json
    Call<ArrayList<Server>> getServers();

    @GET("news.json")
    Call<ArrayList<Story>> getStories();
}
