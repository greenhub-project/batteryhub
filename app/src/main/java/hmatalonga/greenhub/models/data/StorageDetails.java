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
 * Storage Details data definition.
 * Free and total storage space in megabytes
 */
@Table(name = "StorageDetails")
public class StorageDetails extends Model {

    @Column(name = "Free")
    public int free;

    @Column(name = "Total")
    public int total;

    @Column(name = "FreeExternal")
    public int freeExternal;

    @Column(name = "TotalExternal")
    public int totalExternal;

    @Column(name = "FreeSystem")
    public int freeSystem;

    @Column(name = "TotalSystem")
    public int totalSystem;

    @Column(name = "FreeSecondary")
    public int freeSecondary;

    @Column(name = "TotalSecondary")
    public int totalSecondary;
    
    public StorageDetails() {
        super();
    }
}
