package com.hmatalonga.greenhub.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.JsonObject;
import com.hmatalonga.greenhub.BuildConfig;
import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.events.StatusEvent;
import com.hmatalonga.greenhub.models.Specifications;
import com.hmatalonga.greenhub.models.data.Message;
import com.hmatalonga.greenhub.network.services.GreenHubAPIService;
import com.hmatalonga.greenhub.util.Notifier;
import com.hmatalonga.greenhub.util.SettingsUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.realm.Realm;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CheckNewMessagesTask extends AsyncTask<Context, Void, Void> {

    protected Void doInBackground(final Context... params) {
        String url = SettingsUtils.fetchServerUrl(params[0]);

        if (BuildConfig.DEBUG) {
            url = Config.SERVER_URL_DEVELOPMENT;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        GreenHubAPIService service = retrofit.create(GreenHubAPIService.class);

        final int last = SettingsUtils.fetchLastMessageId(params[0]);

        Call<List<JsonObject>> call = service.getMessages(
                Specifications.getAndroidId(params[0]),
                last
        );
        call.enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response == null) {
                    EventBus.getDefault().post(new StatusEvent("Server response has failed..."));
                    return;
                }
                if (response.body() != null && response.body().size() > 0) {
                    final Realm realm = Realm.getDefaultInstance();
                    Message message = null;

                    for (JsonObject el : response.body()) {
                        message = new Message(
                                el.get("id").getAsInt(),
                                el.get("title").getAsString(),
                                el.get("body").getAsString(),
                                el.get("created_at").getAsString()
                        );
                        if (realm.where(Message.class).equalTo("id", message.id).count() == 0) {
                            try {
                                realm.beginTransaction();
                                realm.copyToRealm(message);
                                realm.commitTransaction();
                            } catch (RealmPrimaryKeyConstraintException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    realm.close();

                    if (message != null) {
                        SettingsUtils.saveLastMessageId(params[0], message.id);
                    }
                    if (SettingsUtils.isMessageAlertsOn(params[0])) {
                        Notifier.newMessageAlert(params[0]);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                t.printStackTrace();
                EventBus.getDefault().post(new StatusEvent("Server is not responding..."));
            }
        });
        return null;
    }
}
