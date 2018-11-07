package de.codecrafters.tableview.toolkit;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.codecrafters.tableview.TableDataAdapter;
import java.util.List;

public final class SimpleTableDataAdapter extends TableDataAdapter<String[]> {
    private static final String LOG_TAG = SimpleTableDataAdapter.class.getName();
    private int paddingBottom = 15;
    private int paddingLeft = 20;
    private int paddingRight = 20;
    private int paddingTop = 15;
    private int textColor = -1728053248;
    private int textSize = 18;
    private int typeface = 0;

    public SimpleTableDataAdapter(Context context, String[][] data) {
        super(context, (Object[]) data);
    }

    public SimpleTableDataAdapter(Context context, List<String[]> data) {
        super(context, (List) data);
    }

    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        TextView textView = new TextView(getContext());
        textView.setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, this.paddingBottom);
        textView.setTypeface(textView.getTypeface(), this.typeface);
        textView.setTextSize((float) this.textSize);
        textView.setTextColor(this.textColor);
        textView.setSingleLine();
        textView.setEllipsize(TruncateAt.END);
        try {
            textView.setText(((String[]) getItem(rowIndex))[columnIndex]);
        } catch (IndexOutOfBoundsException e) {
            String str = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No Sting given for row ");
            stringBuilder.append(rowIndex);
            stringBuilder.append(", column ");
            stringBuilder.append(columnIndex);
            stringBuilder.append(". ");
            stringBuilder.append("Caught exception: ");
            stringBuilder.append(e.toString());
            Log.w(str, stringBuilder.toString());
        }
        return textView;
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
}
