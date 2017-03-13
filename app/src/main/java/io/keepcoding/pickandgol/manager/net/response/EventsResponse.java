package io.keepcoding.pickandgol.manager.net.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.ParsedResponse;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JSON_RESULT_OK;


public class EventsResponse implements ParsedResponse {
    @SerializedName("result") private String result;
    @SerializedName("data") private EventsData data;

    public boolean resultIsOK() {
        return (result != null && result.equals(JSON_RESULT_OK));
    }

    public @NonNull
    EventsData getData() {
        return data;
    }

    public class EventsData implements ParsedData {

        // These fields exist only in case of 'ERROR' result
        @SerializedName("code")         private String errorCode;
        @SerializedName("description")  private String errorDescription;

        // These fields exist only in case of 'OK' result
        @SerializedName("_id")  private String id;
        @SerializedName("name") private String name;
        @SerializedName("date") private String date;
        // XXX @SerializedName("description") private String description;
        @SerializedName("photo_url") private String photoUrl;
        @SerializedName("category") private List<String> category;
        @SerializedName("pubs") private List<String> pubs;

        @Nullable
        @Override
        public String getErrorCode() {
            return errorCode;
        }

        @Nullable
        @Override
        public String getErrorDescription() {
            return errorDescription;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        /* XXX public String getDescription() {
            return description;
        }*/

        public String getPhotoUrl() {
            return photoUrl;
        }

        public String getCategory() {
            return category.get(0);
        }

        public List<String> getPubs() {
            return pubs;
        }
    }

    @Override
    public String debugString() {
        if (data == null) {
            return "";
        }

        String str = "result: " + Utils.safeString(result) + "\n"
            + "code: " + (data.errorCode == null ? "null" : Utils.safeString(data.errorCode)) + "\n"
            + "description: " + (data.errorDescription == null ? "null" : Utils.safeString(data.errorDescription)) + "\n"
            + "id: " + (data.id == null ? "null" : Utils.safeString(data.id)) + "\n"
            + "name: " + (data.name == null ? "null" : Utils.safeString(data.name)) + "\n"
            + "date: " + (data.date == null ? "null" : Utils.safeString(data.date.toString())) + "\n"
            + "photoUrl: " + (data.photoUrl == null ? "null" : Utils.safeString(data.photoUrl)) + "\n"
            + "category" + (data.category == null ? "null" : Utils.safeString(data.category.toString())) + "\n"
            + "pubs" + (data.pubs == null ? "null" : Utils.safeString(data.pubs.get(0)));

        return str;
    }
}
