package com.example.user.android.capstone.view;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by nataliakuleniuk on 7/26/17.
 */

class TypefaceHelper {
    static Typeface tfMerriweather;
    static Typeface tfMontserrat;
    static Typeface tfOpenSans;

    public static Typeface getMerriweather(Context context){
        if(null == tfMerriweather){
            tfMerriweather = Typeface.createFromAsset(context.getAssets(), "Merriweather-Bold.ttf");
        }
        return tfMerriweather;
    }

    public static Typeface getMontserrat(Context context){
        if(null == tfMontserrat){
            tfMontserrat = Typeface.createFromAsset(context.getAssets(), "Montserrat-Medium.ttf");
        }
        return tfMontserrat;
    }

//    public static Typeface getOpenSansTf(Context context){
//        if(null == tfOpenSans){
//            tfOpenSans = Typeface.createFromAsset(context.getAssets(), "open_sans_light.ttf");
//        }
//        return tfOpenSans;
//    }

}
