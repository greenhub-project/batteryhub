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

package hmatalonga.greenhub.models;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

import hmatalonga.greenhub.models.data.StorageDetails;

/**
 * Storage.
 */
public class Storage {

    private static final String TAG = "Storage";

    /**
     * Storage details for internal, external, secondary and system partitions.
     * External and secondary storage details are not exactly reliable.
     * @return Thrift-compatible StorageDetails object
     */
    public static StorageDetails getStorageDetails(){
        StorageDetails sd = new StorageDetails();

        // Internal
        File path = Environment.getDataDirectory();
        long[] internal = getStorageDetailsForPath(path);
        if(internal.length == 2){
            sd.setFree((int)internal[0]);
            sd.setTotal((int)internal[1]);
        }

        // External
        long[] external = getExternalStorageDetails();
        if(external.length == 2){
            sd.setFreeExternal((int)external[0]);
            sd.setTotalExternal((int)external[1]);
        }

        // Secondary
        long[] secondary = getSecondaryStorageDetails();
        if(secondary.length == 2){
            sd.setFreeSecondary((int)secondary[0]);
            sd.setTotalSecondary((int)secondary[1]);
        }

        // System
        path = Environment.getRootDirectory();
        long[] system = getStorageDetailsForPath(path);
        if(system.length == 2){
            sd.setFreeSystem((int)system[0]);
            sd.setTotalSystem((int)system[1]);
        }

        return sd;
    }

    /**
     * Returns free and total external storage space
     * @return Two values as a pair or none
     */
    private static long[] getExternalStorageDetails(){
        File path = getStoragePathFromEnv("EXTERNAL_STORAGE");
        if(path != null && path.exists()){
            long[] storage = getStorageDetailsForPath(path);
            if(storage.length == 2) return storage;
        }

        // Make sure external storage isn't a secondary device
        if(!isExternalStorageRemovable() || isExternalStorageEmulated()){
            path = Environment.getExternalStorageDirectory();
            if(path != null && path.exists()){
                long[] storage = getStorageDetailsForPath(path);
                return storage;
            }
        }
        return new long[]{};
    }

    /**
     * Returns free and total secondary storage space
     * @return Two values as a pair or none
     */
    private static long[] getSecondaryStorageDetails(){
        File path = getStoragePathFromEnv("SECONDARY_STORAGE");
        if(path != null && path.exists()){
            long[] storage = getStorageDetailsForPath(path);
            return storage;
        }
        // Make sure external storage is a secondary device
        if(isExternalStorageRemovable() && !isExternalStorageEmulated()){
            path = Environment.getExternalStorageDirectory();
            if(path != null && path.exists()){
                long[] storage = getStorageDetailsForPath(path);
                return storage;
            }
        }
        return new long[]{};
    }

    /**
     * Returns a storage path from an environment variable, if supported.
     * @param variable Variable name
     * @return Storage path or null if not found
     */
    private static File getStoragePathFromEnv(String variable){
        String path;
        try{
            path = System.getenv(variable);
            return new File(path);
        } catch (Exception e){
            return null;
        }
    }

    private static boolean isExternalStorageRemovable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Environment.isExternalStorageRemovable();
    }

    /**
     * Checks if external storage is emulated, works on API level 11+.
     * @return True if method is supported and storage is emulated
     */
    private static boolean isExternalStorageEmulated() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Environment.isExternalStorageEmulated();
    }

    /**
     * Returns free and total storage space in bytes
     * @param path Path to the storage medium
     * @return Free and total space in long[]
     */
    @Deprecated
    private static long[] getStorageDetailsForPath(File path){
        if(path == null) return new long[]{};
        final int KB = 1024;
        final int MB = KB*1024;
        long free;
        long total;
        long blockSize;
        try {
            StatFs stats = new StatFs(path.getAbsolutePath());
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
                free = stats.getAvailableBytes()/MB;
                total = stats.getTotalBytes()/MB;
                return new long[]{free, total};
            } else {
                blockSize = (long)stats.getBlockSize();
                free = ((long)stats.getAvailableBlocks()*blockSize)/MB;
                total = ((long)stats.getBlockCount()*blockSize)/MB;
                if(free < 0 || total < 0) return new long[]{};
                return new long[]{free, total};
            }
        } catch(Exception e){
            return new long[]{};
        }
    }
}
