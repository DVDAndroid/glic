package com.dvd.android.googlelaunchersicons.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dvd.android.googlelaunchersicons.R;

/**
 * @author dvdandroid
 */
public class RowItem extends LinearLayout {

    private final String title;
    private final Drawable icon;

    private final TextView mTitle;
    private final TextView mSummary;
    private final ImageView mIcon;

    public RowItem(Context context) {
        this(context, null);
    }

    public RowItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RowItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.info_item_row, this);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RowItem, 0, 0);

        try {
            title = a.getString(R.styleable.RowItem_text);
            icon = a.getDrawable(R.styleable.RowItem_icon);
        } finally {
            a.recycle();
        }

        mTitle = (TextView) findViewById(android.R.id.title);
        mSummary = (TextView) findViewById(android.R.id.summary);
        mIcon = (ImageView) findViewById(android.R.id.icon);

        mTitle.setText(title);
        mIcon.setImageDrawable(icon);
    }

    public void setSummary(String s) {
        mSummary.setText(s);
    }

    @SuppressLint("SetTextI18n")
    public void setSummary(long s) {
        mSummary.setText("" + s);
    }
}
