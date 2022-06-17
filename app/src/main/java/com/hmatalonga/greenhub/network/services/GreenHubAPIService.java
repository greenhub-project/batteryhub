/*
 * Copyright (c) 2017 Hugo Matalonga & João Paulo Fernandes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmatalonga.greenhub.network.services;

import com.google.gson.JsonObject;
import com.hmatalonga.greenhub.models.data.Device;
import com.hmatalonga.greenhub.models.data.Upload;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * GreenHub API Interface.
 */
public interface GreenHubAPIService {
    @GET("api/mobile/messages")
    Call<List<JsonObject>> getMessages(@Query("uuid") String uuid, @Query("message") int message);

    @POST("api/mobile/register")
    Call<Integer> createDevice(@Body Device device);

    @POST("api/mobile/upload")
    Call<Integer> createSample(@Body Upload upload);
}
