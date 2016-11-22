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

/**
 * Storage Details data definition.
 * Free and total storage space in megabytes
 */
public class StorageDetails {

    private static final int FIELD_NUM = 8;

    private int free;

    private int total;

    private int freeExternal;

    private int totalExternal;

    private int freeSystem;

    private int totalSystem;

    private int freeSecondary;

    private int totalSecondary;


    public int getFree() {
        return free;
    }

    public void setFree(int free) {
        this.free = free;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getFreeExternal() {
        return freeExternal;
    }

    public void setFreeExternal(int freeExternal) {
        this.freeExternal = freeExternal;
    }

    public int getTotalExternal() {
        return totalExternal;
    }

    public void setTotalExternal(int totalExternal) {
        this.totalExternal = totalExternal;
    }

    public int getFreeSystem() {
        return freeSystem;
    }

    public void setFreeSystem(int freeSystem) {
        this.freeSystem = freeSystem;
    }

    public int getTotalSystem() {
        return totalSystem;
    }

    public void setTotalSystem(int totalSystem) {
        this.totalSystem = totalSystem;
    }

    public int getFreeSecondary() {
        return freeSecondary;
    }

    public void setFreeSecondary(int freeSecondary) {
        this.freeSecondary = freeSecondary;
    }

    public int getTotalSecondary() {
        return totalSecondary;
    }

    public void setTotalSecondary(int totalSecondary) {
        this.totalSecondary = totalSecondary;
    }
}
