/*
 * Copyright (c) 2016 Hugo Matalonga & João Paulo Fernandes
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

package hmatalonga.greenhub.network;

import hmatalonga.greenhub.models.data.Device;
import hmatalonga.greenhub.models.data.Sample;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * GreenHub API Interface.
 */
public interface GreenHubAPIService {
    @POST("api/device")
    Call<Device> createDevice(@Body Device device);

    @POST("api/sample")
    Call<Sample> createSample(@Body Sample sample);
}
