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
 * Call Month data definition.
 */
@Table(name = "CallsMonth")
public class CallMonth extends Model {

    @Column(name = "TotalCallInNum")
    public int totalCallInNum = 0;

    @Column(name = "TotalCallOutNum")
    public int totalCallOutNum = 0;

    @Column(name = "TotalMissedCallNum")
    public int totalMissedCallNum = 0;

    @Column(name = "TotalCallInDur")
    public long totalCallInDur = 0;

    @Column(name = "TotalCallOutDur")
    public long totalCallOutDur = 0;

    public CallMonth() {
        super();
    }
}