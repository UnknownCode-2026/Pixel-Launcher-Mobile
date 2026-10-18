package th.samp.launcher.launcher.network;

import com.google.gson.annotations.SerializedName;
import th.samp.launcher.BuildConfig;

public class Links {
    @SerializedName("URL_CLIENT")
    private String URL_CLIENT;
    @SerializedName("URL_CLIENTSAMPAPK")
    private String URL_CLIENT_SAMPAPK;
    @SerializedName("URL_GAME_FILES")
    private String URL_GAME_FILES;
    @SerializedName("URL_GAME_FILES_UPD")
    private String URL_GAME_FILES_UPDATE;
    @SerializedName("URL_VK")
    private String URL_VK;
    @SerializedName("URL_DISCORD")
    private String URL_DISCORD;
    @SerializedName("URL_YOUTUBE")
    private String URL_YOUTUBE;
    @SerializedName("URL_FORUM")
    private String URL_FORUM;
    @SerializedName("URL_DONATE")
    private String URL_DONATE;
    @SerializedName("clientVersionCode")
    private Integer targetClientVersion = BuildConfig.VERSION_CODE;
    @SerializedName("gameFilesVersionCode")
    private Integer targetGameFilesVersion;
    @SerializedName("clientSampVersionCode")
    private Integer targetClientSampVersion;

    public Links() { }

    public final Integer getTargetClientVersion() {
        return targetClientVersion;
    }

    public final Integer getTargetClientSampVersion() {
        return targetClientSampVersion;
    }

    public final Integer getTargetGameFilesVersion() {
        return targetGameFilesVersion;
    }

    public final String getUrlClient() {
        return URL_CLIENT;
    }
     public final String getUrlClientSampapk() {
        return URL_CLIENT_SAMPAPK;
    }
    public final String getUrlFiles() {
        return URL_GAME_FILES;
    }
    public final String getUrlFilesUpdate() {
        return URL_GAME_FILES_UPDATE;
    }
    public final String getUrlForum() {
        return URL_FORUM;
    }
    public final String getUrlDonate() {
        return URL_DONATE;
    }
    public final String getUrlVKontakte() {
        return URL_VK;
    }
    public final String getUrlDiscord() {
        return URL_DISCORD;
    }
    public final String getUrlYoutube() {
        return URL_YOUTUBE;
    }

}
