package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.EventDetailResponse.EventDetailData;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.mapper.EventDetailDataToEventMapper;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.EVENT_DETAIL;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_CREATE_EVENT;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): request the creation of a new event in the system.
 * - Second (in the main thread): call the given listener with the result of the operation.
 */
public class CreateEventInteractor {

    // Param keynames for the event creation operation
    private static final String REQUEST_PARAM_KEY_TOKEN = "token";
    private static final String REQUEST_PARAM_KEY_NAME = "name";
    private static final String REQUEST_PARAM_KEY_DATE = "date";
    private static final String REQUEST_PARAM_KEY_PUB = "pub";
    private static final String REQUEST_PARAM_KEY_CATEGORY = "category";
    private static final String REQUEST_PARAM_KEY_DESCRIPTION = "description";
    private static final String REQUEST_PARAM_KEY_PHOTO_URL = "photo_url";


    // This interface describes the behavior of a listener waiting for the the async operation
    public interface CreateEventInteractorListener {
        void onCreateEventSuccess(Event event);
        void onCreateEventFail(Exception e);
    }

    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context       context for the operation.
     * @param name          name for the new event.
     * @param date          date for the new event.
     * @param pubId         id of the pub the new event is associated to.
     * @param categoryId    id of the category the new event belongs to.
     * @param description   description for the new event.
     * @param photoUrl      url of the picture for the new event.
     * @param token         session token to send to the server
     * @param listener      listener that will process the result of the operation.
     */
    public void execute(final @NonNull Context context,
                        final @NonNull String name,
                        final @NonNull Date date,
                        final @NonNull String pubId,
                        final @NonNull String categoryId,
                        final @Nullable String description,
                        final @Nullable String photoUrl,
                        final @NonNull String token,
                        final @NonNull CreateEventInteractorListener listener) {

        if (listener == null)
            return;

        if (token == null) {
            listener.onCreateEventFail(new Exception("This operation requires a session token."));
            return;
        }


        NetworkManager networkMgr = new NetworkManager(context);
        RequestParams createEventParams = new RequestParams();

        createEventParams
                .addParam(REQUEST_PARAM_KEY_NAME, name)
                .addParam(REQUEST_PARAM_KEY_DATE, Utils.getISODateString(date))
                .addParam(REQUEST_PARAM_KEY_PUB, pubId)
                .addParam(REQUEST_PARAM_KEY_CATEGORY, categoryId)
                .addParam(REQUEST_PARAM_KEY_TOKEN, token);

        if (description != null)
            createEventParams.addParam(REQUEST_PARAM_KEY_DESCRIPTION, description);

        if (photoUrl != null)
            createEventParams.addParam(REQUEST_PARAM_KEY_PHOTO_URL, photoUrl);

        String remoteUrl = getUrl();

        networkMgr.launchPOSTStringRequest(remoteUrl, createEventParams, EVENT_DETAIL, new NetworkManager.NetworkRequestListener() {

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onCreateEventFail(e);
            }

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {
                Event createdEvent = new EventDetailDataToEventMapper().map( (EventDetailData)parsedData );
                listener.onCreateEventSuccess(createdEvent);
            }
        });
    }


    // Gets the remote url for the operation
    private String getUrl() {
        return URL_CREATE_EVENT;
    }

}
