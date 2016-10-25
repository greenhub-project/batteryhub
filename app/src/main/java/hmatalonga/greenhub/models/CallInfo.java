/*
 * Copyright (C) 2016 Hugo Matalonga & João Paulo Fernandes
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

package hmatalonga.greenhub.models;

import hmatalonga.greenhub.util.StringHelper;

/**
 * Created by hugo on 09-04-2016.
 */
public class CallInfo {
    private static final int fieldNum = 4;
    private double incomingCallTime; // optional
    private double outgoingCallTime; // optional
    private double nonCallTime; // optional
    private String callStatus; // optional

    public CallInfo() {}

    public double getIncomingCallTime() {
        return incomingCallTime;
    }

    public void setIncomingCallTime(double incomingCallTime) {
        this.incomingCallTime = incomingCallTime;
    }

    public double getOutgoingCallTime() {
        return outgoingCallTime;
    }

    public void setOutgoingCallTime(double outgoingCallTime) {
        this.outgoingCallTime = outgoingCallTime;
    }

    public double getNonCallTime() {
        return nonCallTime;
    }

    public void setNonCallTime(double nonCallTime) {
        this.nonCallTime = nonCallTime;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public void parseString(String s) {
        String[] values = StringHelper.trimArray(s.split(";"));
        if (values.length == fieldNum) {
            try {
                setIncomingCallTime(Double.parseDouble(values[0]));
                setOutgoingCallTime(Double.parseDouble(values[1]));
                setNonCallTime(Double.parseDouble(values[2]));
                setCallStatus(values[3]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return String.valueOf(incomingCallTime) + ";" + String.valueOf(outgoingCallTime) + ";" +
                String.valueOf(nonCallTime) + ";" + callStatus;
    }
}
