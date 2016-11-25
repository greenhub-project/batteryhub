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

package hmatalonga.greenhub.models.data;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * CallInfo data definition.
 */
@Table(name = "CallsInfo")
public class CallInfo extends Model {

    // Incoming call time sum since boot
    @Column(name = "IncomingCallTime")
    public double incomingCallTime;

    // Outgoing call time sum since boot
    @Column(name = "OutgoingCallTime")
    public double outgoingCallTime;

    // Non-call time sum since boot
    @Column(name = "NonCallTime")
    public double nonCallTime;

    // Idle, offhook or ringing
    @Column(name = "CallStatus")
    public String callStatus;

    public CallInfo() {
        super();
    }
}
