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
 * Device data definition.
 */
@Table(name = "Devices")
public class Device extends Model {

    @Column(name = "UuId")
    public String uuId;

    @Column(name = "Timestamp")
    public double timestamp;

    @Column(name = "Model")
    public String model;

    @Column(name = "Manufacturer")
    public String manufacturer;

    @Column(name = "Brand")
    public String brand;

    @Column(name = "Product")
    public String product;

    @Column(name = "OsVersion")
    public String osVersion;

    @Column(name = "KernelVersion")
    public String kernelVersion;

    @Column(name = "SerialNumber")
    public String serialNumber;

    @Column(name = "IsRoot")
    public int isRoot;

    public Device() {
        super();
    }
}
