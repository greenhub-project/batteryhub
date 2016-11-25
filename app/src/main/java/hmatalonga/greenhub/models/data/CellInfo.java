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
 * Cell Info data definition.
 */
@Table(name = "CellInfos")
public class CellInfo extends Model {

    @Column(name = "Mcc")
    public int mcc = 0;

    @Column(name = "Mnc")
    public int mnc = 0;

    @Column(name = "Lac")
    public int lac = 0;

    @Column(name = "Cid")
    public int cid = 0;

    @Column(name = "RadioType")
    public String radioType = null;

    public CellInfo() {
        super();
    }
}