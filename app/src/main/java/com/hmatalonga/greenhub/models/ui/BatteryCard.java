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

package hmatalonga.greenhub.models.ui;

import android.graphics.Color;
import android.support.annotation.DrawableRes;

/**
 * Device Resource Card class model.
 *
 * Created by hugo on 05-04-2016.
 */
public class BatteryCard {
    public int icon;
    public String label;
    public String value;
    public int indicator;

    public BatteryCard(@DrawableRes int icon, String title, String value) {
        this.icon = icon;
        this.label = title;
        this.value = value;
        this.indicator = Color.GREEN;
    }

    public BatteryCard(@DrawableRes int icon, String title, String value, int indicator) {
        this.icon = icon;
        this.label = title;
        this.value = value;
        this.indicator = indicator;
    }
}
