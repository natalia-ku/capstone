package com.example.user.android.capstone.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by nataliakuleniuk on 7/26/17.
 */

public class TextViewMerriweather extends android.support.v7.widget.AppCompatTextView{


    public TextViewMerriweather(Context context) {
        super(context);
        setFont(context);
    }

    public TextViewMerriweather(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setFont(context);

    }

    public TextViewMerriweather(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFont(context);

    }


    private void setFont(Context context){
        this.setTypeface(TypefaceHelper.getMontserrat(context));
        this.setTextColor(Color.BLUE);
    }
}
