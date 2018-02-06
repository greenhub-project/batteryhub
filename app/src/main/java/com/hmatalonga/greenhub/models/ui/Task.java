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

package com.hmatalonga.greenhub.models.ui;

import android.content.pm.PackageInfo;

import java.util.TreeSet;

/**
 * Task.
 */
public class Task {

    private int mUid;

    private String mName;

    private double mMemory;

    private String mLabel;

    private PackageInfo mPackageInfo;

    private TreeSet<Integer> mProcesses;

    private boolean mIsChecked;

    private boolean mIsAutoStart;

    private boolean mHasBackgroundService;

    public Task(int uid, String name) {
        mUid = uid;
        mName = name;
        mProcesses = new TreeSet<>();
        mIsChecked = true;
    }

    // ---------------------------------------------------------------------------------------------

    public int getUid() {
        return mUid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName= name;
    }

    public double getMemory() {
        return mMemory;
    }

    public void setMemory(double memory) {
        this.mMemory = Math.round(memory * 100.0) / 100.0;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    public PackageInfo getPackageInfo() {
        return mPackageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.mPackageInfo = packageInfo;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    public boolean isAutoStart() {
        return mIsAutoStart;
    }

    public void setIsAutoStart(boolean isAutoStart) {
        this.mIsAutoStart = isAutoStart;
    }

    public boolean hasBackgroundService() {
        return mHasBackgroundService;
    }

    public void setHasBackgroundService(boolean hasBackgroundService) {
        this.mHasBackgroundService = hasBackgroundService;
    }

    public TreeSet<Integer> getProcesses() {
        return mProcesses;
    }
}