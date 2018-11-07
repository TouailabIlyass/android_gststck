package de.codecrafters.tableview.toolkit;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.codecrafters.tableview.TableHeaderAdapter;

public final class SimpleTableHeaderAdapter extends TableHeaderAdapter {
    private final String[] headers;
    private int paddingBottom = 30;
    private int paddingLeft = 20;
    private int paddingRight = 20;
    private int paddingTop = 30;
    private int textColor = -1728053248;
    private int textSize = 18;
    private int typeface = 1;

    public SimpleTableHeaderAdapter(Context context, String... headers) {
        super(context);
        this.headers = headers;
    }

    public SimpleTableHeaderAdapter(Context context, int... headerStringResources) {
        super(context);
        this.headers = new String[headerStringResources.length];
        for (int i = 0; i < headerStringResources.length; i++) {
            this.headers[i] = context.getString(headerStringResources[i]);
        }
    }

    public void setPaddings(int left, int top, int right, int bottom) {
        this.paddingLeft = left;
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
    }

    public void setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
    }

    public void setPaddingBottom(int paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setTypeface(int typeface) {
        this.typeface = typeface;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public View getHeaderView(int columnIndex, ViewGroup parentView) {
        TextView textView = new TextView(getContext());
        if (columnIndex < this.headers.length) {
            textView.setText(this.headers[columnIndex]);
        }
        textView.setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, this.paddingBottom);
        textView.setTypeface(textView.getTypeface(), this.typeface);
        textView.setTextSize((float) this.textSize);
        textView.setTextColor(this.textColor);
        textView.setSingleLine();
        textView.setEllipsize(TruncateAt.END);
        return textView;
    }
}
