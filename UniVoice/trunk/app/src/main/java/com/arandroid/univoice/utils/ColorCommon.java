package com.arandroid.univoice.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.arandroid.univoice.R;

import java.util.ArrayList;
import java.util.Collections;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class ColorCommon {
    private static ArrayList<Integer> randomColors;
    private static int currentIndexRandomColors = 0;

    private static ArrayList<Drawable> randomQuoteBackgrounds;
    private static int currentIndexQuoteBacgkround = 0;

    /*
     * Questo metodo usa il ColorFilter (PorterDuffColorFilter) per "moltiplicare" il bianco dell'immagine originale con qualsiasi altro colore.
     * In questo caso lo limitiamo al primary o all'accent color tramite il boolean primary
     */
    public static void changeColor(Context activity, EditText editText, boolean primary) {
        Drawable drawable = editText.getCompoundDrawables()[0];
        TypedValue colorPrimary = new TypedValue();
        if (primary) {
            activity.getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);
        } else {
            activity.getTheme().resolveAttribute(R.attr.colorAccent, colorPrimary, true);
        }
        drawable.setColorFilter(new PorterDuffColorFilter(colorPrimary.data, Mode.MULTIPLY));

        editText.setCompoundDrawables(drawable, null, null, null);

    }

    public static void changeColor(Context activity, ImageView view, boolean primary) {
        Drawable drawable = view.getDrawable();
        TypedValue colorPrimary = new TypedValue();
        if (primary) {
            activity.getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);
        } else {
            activity.getTheme().resolveAttribute(R.attr.colorAccent, colorPrimary, true);
        }
        drawable.setColorFilter(new PorterDuffColorFilter(colorPrimary.data, Mode.MULTIPLY));
        view.setImageDrawable(drawable);
    }

    public static void whiteColor(Context activity, ImageView view) {
        Drawable drawable = view.getDrawable();
        drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(activity, android.R.color.white), Mode.MULTIPLY));
        view.setImageDrawable(drawable);
    }

    public static void whiteColor(Context activity, MenuItem view) {
        Drawable drawable = view.getIcon();
        if (drawable != null) {
            drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(activity, android.R.color.white), Mode.MULTIPLY));
            view.setIcon(drawable);
        }
    }

    public static void changeColor(Context activity, MenuItem view, boolean primary) {
        Drawable drawable = view.getIcon();
        TypedValue colorPrimary = new TypedValue();
        if (primary) {
            activity.getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);
        } else {
            activity.getTheme().resolveAttribute(R.attr.colorAccent, colorPrimary, true);
        }
        drawable.setColorFilter(new PorterDuffColorFilter(colorPrimary.data, Mode.MULTIPLY));
        view.setIcon(drawable);
    }

    public static void changeColor(Context activity, SwitchCompat switchCompat) {
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked},
        };

        TypedValue colorPrimary = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);

        if (colorPrimary.data == ContextCompat.getColor(activity, android.R.color.white)) {
            activity.getTheme().resolveAttribute(R.attr.colorAccent, colorPrimary, true);
        }

        int[] trackColors = new int[]{
                Color.LTGRAY,
                colorPrimary.data,
        };

        DrawableCompat.setTintList(DrawableCompat.wrap(switchCompat.getTrackDrawable()), new ColorStateList(states, trackColors));
    }

    public static int nextRandomColor(Context context) {
        if (randomColors == null) {
            initRandomColors(context);
        }
        currentIndexRandomColors = (currentIndexRandomColors + 1) % randomColors.size();
        return randomColors.get(currentIndexRandomColors);
    }

    private static void initRandomColors(Context context) {
        TypedArray ta = context.getResources().obtainTypedArray(R.array.material_colors_array);

        randomColors = new ArrayList<>();
        for (int i = 0; i < ta.length(); i++) {
            int value = ta.getColor(i, -1);
            randomColors.add(value);
        }

        Collections.shuffle(randomColors);
        ta.recycle();
    }

    private static int getDominantColor(ImageView imageView) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    public static boolean isBright(ImageView imageView) {
        int color = getDominantColor(imageView);
        if (android.R.color.transparent == color)
            return true;

        boolean rtnValue = false;

        int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};

        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .299 + rgb[1]
                * rgb[1] * .587 + rgb[2] * rgb[2] * .114);

        if (brightness >= 200) {    // light color
            rtnValue = true;
        }

        return rtnValue;
    }
}
