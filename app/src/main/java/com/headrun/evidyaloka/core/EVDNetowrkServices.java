package com.headrun.evidyaloka.core;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.headrun.evidyaloka.config.ApiEndpoints;
import com.headrun.evidyaloka.core.BaseService;
import com.headrun.evidyaloka.core.ResponseListener;
import com.headrun.evidyaloka.dto.ChangeSessionStatus;
import com.headrun.evidyaloka.dto.DemandResponse;
import com.headrun.evidyaloka.dto.FiltersDataResponse;
import com.headrun.evidyaloka.dto.IntialHandShakeResponse;
import com.headrun.evidyaloka.dto.SchoolDetailResponse;
import com.headrun.evidyaloka.dto.SessionResponse;
import com.headrun.evidyaloka.event.SlotConfirmEvent;
import com.headrun.evidyaloka.model.BlockReleaseDemand;
import com.headrun.evidyaloka.model.LoginResponse;
import com.headrun.evidyaloka.model.SchoolDetails;
import com.headrun.evidyaloka.utils.UserSession;
import com.headrun.evidyaloka.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sujith on 10/1/17.
 */

public class EVDNetowrkServices extends BaseService {

    String PLATFORM = "platform";
    String ANDROID = "android";

    public void getDeamnds(Context context, ResponseListener<DemandResponse> listener, Map<String, String> params) {

        params.put(PLATFORM, ANDROID);
        addCsrfparam(context, params);
        executePostRequest(context, ApiEndpoints.GET_DEMANDS, getSessionHeaders(context), params, new TypeToken<DemandResponse>() {
        }, listener);
    }

    public void getFiltersData(Context context, ResponseListener<FiltersDataResponse> listener) {
        executeGetRequest(context, ApiEndpoints.GET_FILTERS, getSessionHeaders(context), null, new TypeToken<FiltersDataResponse>() {
        }, listener);
    }

    public void getDemandDetails(Context context, ResponseListener<SchoolDetailResponse> listener, String id) {
        executeGetRequest(context, ApiEndpoints.GET_DEMAND_DEATILS + id + "/", getSessionHeaders(context), null, new TypeToken<SchoolDetailResponse>() {
        }, listener);
    }

    public void blockDemand(Context context, ResponseListener<BlockReleaseDemand> listener, SlotConfirmEvent slots_data) {
        Map<String, String> params = new HashMap<>();
        params.put("center_id", slots_data.center_id);
        params.put("offer_id", slots_data.offer_id);
        params.put(PLATFORM, ANDROID);

        StringBuffer sel_slot_ids = new StringBuffer();
        int length = slots_data.slot_ids.size();
        int count = 0;
        for (Map.Entry<String, SchoolDetails.ClassTimings> entry : slots_data.slot_ids.entrySet()) {
            count++;
            sel_slot_ids.append(entry.getValue().class_id);
            if (count != length)
                sel_slot_ids.append(";");

        }
        params.put("slot_ids", sel_slot_ids.toString());
        addCsrfparam(context, params);

        executePostRequest(context, ApiEndpoints.BLOCAK_DEMANDS, getSessionHeaders(context), params, new TypeToken<BlockReleaseDemand>() {
        }, listener);
    }

    public void releaseDemand(Context context, ResponseListener<BlockReleaseDemand> listener) {
        HashMap<String, String> params = new HashMap<>();
        params.put(PLATFORM, ANDROID);
        addCsrfparam(context, params);
        executePostRequest(context, ApiEndpoints.RELEASE_DEMANDS, getSessionHeaders(context), params, new TypeToken<BlockReleaseDemand>() {
        }, listener);
    }

    public void login_authentication(Context context, ResponseListener<LoginResponse> listener, HashMap<String, String> params) {

        addCsrfparam(context, params);

        executePostRequest(context, ApiEndpoints.REST_AUTH, getSessionHeaders(context), params, new TypeToken<LoginResponse>() {
        }, listener);
    }

    public void sigup_auth(Context context, ResponseListener<LoginResponse> listener, HashMap<String, String> params) {

        addCsrfparam(context, params);

        executePostRequest(context, ApiEndpoints.SIGNUP, getSessionHeaders(context), params, new TypeToken<LoginResponse>() {
        }, listener);
    }

    public void csrfTOken(Context context, ResponseListener<String> listener) {
        executeGetRequest(context, ApiEndpoints.CSRFTOKEN, getSessionHeaders(context), null, new TypeToken<SchoolDetailResponse>() {
        }, listener);
    }

    public void fcmTokenRefresh(Context context, ResponseListener<ChangeSessionStatus> listener, String prev_toekn, String token) {

        Map<String, String> params = new HashMap<>();
        params.put("key", prev_toekn);
        params.put("new_key", token);

        executeGetRequest(context, ApiEndpoints.UPDATE_FCMTOKEN, getSessionHeaders(context), params, new TypeToken<ChangeSessionStatus>() {
        }, listener);
    }

    public void intailHandShake(Context context, ResponseListener<IntialHandShakeResponse> listener) {

        executeGetRequest(context, ApiEndpoints.CSRFTOKEN, getSessionHeaders(context), null, new TypeToken<IntialHandShakeResponse>() {
        }, listener);
    }

    public void getUpComingSessions(Context context, ResponseListener<SessionResponse> listener, String mSessionType, int page_num) {

        HashMap<String, String> params = new HashMap<>();

        params.put("status", mSessionType);
        params.put("next_page", String.valueOf(page_num));
        //params.put("max_results", "10");
        addCsrfparam(context, params);
        executePostRequest(context, ApiEndpoints.UPCOMING_SESSIONS, getSessionHeaders(context), params, new TypeToken<SessionResponse>() {
        }, listener);
    }

    public void sessionStatusChange(Context context, ResponseListener<ChangeSessionStatus> listener, String session_id, String session_status) {

        String status = session_status.toLowerCase();
        String change_status = "";
        HashMap<String, String> params = new HashMap<>();

        params.put("class_id", session_id);
        if (status.contains("teaching"))
            change_status = "Started";
        else if (status.contains("cancelled"))
            change_status = "Cancelled";
        else
            change_status = status;

        params.put("status", status);

        addCsrfparam(context, params);
        executePostRequest(context, ApiEndpoints.CHANGE_SESSIONS, getSessionHeaders(context), params, new TypeToken<ChangeSessionStatus>() {
        }, listener);
    }

    public HashMap<String, String> getSessionHeaders(Context context) {

        String cookie = new UserSession(context).getCookie();

        HashMap<String, String> headersValue = new HashMap<>();

        if (!cookie.isEmpty()) {
            boolean iscookie_value = false;
            String[] cookie_data = cookie.split(";");
            for (String value : cookie_data) {
                String[] data = value.split("=");
                if (data.length > 0) {
                    if (data[0].contains("csrftoken")) {
                        iscookie_value = true;
                        break;
                    }
                }
            }

            if (iscookie_value) {
                headersValue.put("Cookie", cookie);
                return headersValue;
            } else {
                String value = "csrftoken=" + new UserSession(context).getCsrf() + ";" + cookie;
                headersValue.put("Cookie", value);
                return headersValue;
            }

        } else {
            headersValue.put("Cookie", "csrftoken=" + new UserSession(context).getCsrf() + ";" + cookie);
            return headersValue;
        }
    }

    private void addCsrfparam(Context context, Map<String, String> params) {
        String csrf_token = new Utils(context).userSession.getCsrf();
        if (!csrf_token.isEmpty())
            params.put("csrfmiddlewaretoken", csrf_token);

    }
}