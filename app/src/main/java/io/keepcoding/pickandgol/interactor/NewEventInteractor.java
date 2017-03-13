package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.EventsResponse;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.mapper.EventsDataToEventMapper;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.EVENTS;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_EVENTS;

public class NewEventInteractor {

    private static final String REQUEST_PARAM_KEY_NAME = "name";
    private static final String REQUEST_PARAM_KEY_DATE = "date";
    private static final String REQUEST_PARAM_KEY_CATEGORY = "category";
    private static final String REQUEST_PARAM_KEY_PUB = "pub";
    private static final String REQUEST_PARAM_KEY_DESCRIPTION = "description";
    private static final String REQUEST_PARAM_KEY_PHOTO_URL = "photo_url";
    private static final String REQUEST_PARAM_KEY_TOKEN = "token";

    public interface Listener {
        void onNewEventSuccess(final Event event);
        void onNewEventFailed(final String message);
    }

    public void execute(final @NonNull Context context,
                        final @NonNull String token,
                        final @NonNull Event event,
                        final Listener listener) {
        NetworkManager manager = new NetworkManager(context);
        RequestParams params = getRequestParams(token, event);

        manager.launchPOSTStringRequest(URL_EVENTS, params, EVENTS, new NetworkManager.NetworkRequestListener() {
            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {
                Event event = new EventsDataToEventMapper().map((EventsResponse.EventsData) parsedData);
                if (listener != null) {
                    listener.onNewEventSuccess(event);
                }
            }

            @Override
            public void onNetworkRequestFail(Exception e) {
                if (listener != null) {
                    listener.onNewEventFailed(e.getMessage());
                }
            }
        });
    }

    @NonNull
    private RequestParams getRequestParams(final @NonNull String token, final @NonNull Event event) {
        RequestParams params = new RequestParams();
        params.addParam(REQUEST_PARAM_KEY_TOKEN, token);

        if (event.getName() != null) {
            params.addParam(REQUEST_PARAM_KEY_NAME, event.getName());
        }

        if (event.getDate() != null) {
            params.addParam(REQUEST_PARAM_KEY_DATE, event.getDate());
        }

        if (event.getCategory() != null) {
            params.addParam(REQUEST_PARAM_KEY_CATEGORY, event.getCategory());
        }

        if (event.getPubs() != null) {
            params.addParam(REQUEST_PARAM_KEY_PUB, event.getPubs().get(0));
        }

        if (event.getDescription() != null) {
            params.addParam(REQUEST_PARAM_KEY_DESCRIPTION, event.getDescription());
        }

        if (event.getPhotoUrl() != null) {
            params.addParam(REQUEST_PARAM_KEY_PHOTO_URL, event.getPhotoUrl());
        }

        return params;
    }
}
