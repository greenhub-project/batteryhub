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

package com.hmatalonga.greenhub.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by hugo on 05-07-2016.
 */
public class TextDrawable extends Drawable {

    private final String mText;
    private final Paint mPaint;

    public TextDrawable(String text) {

        this.mText = text;

        this.mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(22f);
        mPaint.setAntiAlias(true);
        mPaint.setFakeBoldText(true);
        mPaint.setShadowLayer(6f, 0, 0, Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawText(mText, 0, 0, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}