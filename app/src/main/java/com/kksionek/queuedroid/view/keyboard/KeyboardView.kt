package com.kksionek.queuedroid.view.keyboard;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.model.Settings;
import com.kksionek.queuedroid.model.keyboard.KeyboardViewAdapter;

public class KeyboardView extends LinearLayout {
    private static final String TAG = "KEYBOARDVIEW";

    private static final int MAX_LENGTH = 10;

    private final TextView mCurPointsTextView;
    private final RecyclerView mButtonRecylerView;
    private int mColsNum;

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.keyboard, this);

        mCurPointsTextView = (TextView) findViewById(R.id.cur_points);
        Button clearButton = (Button) findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPoints();
            }
        });
        Button backspaceButton = (Button) findViewById(R.id.backspace_button);
        backspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence text = mCurPointsTextView.getText();
                if (text.length() == 1)
                    clearPoints();
                else
                    mCurPointsTextView.setText(text.subSequence(0, text.length() - 1));
            }
        });

        mButtonRecylerView = (RecyclerView) findViewById(R.id.button_grid_view);
        KeyboardViewAdapter keyboardViewAdapter = new KeyboardViewAdapter(getContext());
        keyboardViewAdapter.setOnKeyboardItemClickListener(new KeyboardListener());
        mButtonRecylerView.setAdapter(keyboardViewAdapter);
        if (isInEditMode())
            mColsNum = 5;
        else
            mColsNum = Settings.getKeyboardColumnsCount(getContext());
        mButtonRecylerView.setLayoutManager(
                new GridLayoutManager(
                        getContext(),
                        mColsNum,
                        GridLayoutManager.VERTICAL,
                        false));
        mButtonRecylerView.addItemDecoration(
                new SpacesItemDecoration(getContext(), R.dimen.keyboard_item_gap_size));
    }

    public void setColumnCount(int columnCount) {
        if (mColsNum != columnCount) {
            mColsNum = columnCount;
            mButtonRecylerView.setLayoutManager(new GridLayoutManager(
                    getContext(),
                    mColsNum,
                    GridLayoutManager.VERTICAL,
                    false));
        }
    }

    public int getPoints() {
        return Integer.valueOf(mCurPointsTextView.getText().toString());
    }

    public void clearPoints() {
        mCurPointsTextView.setText("0");
    }

    class KeyboardListener implements OnKeyboardItemClickListener {

        public void onClick(int position) {
            CharSequence text = mCurPointsTextView.getText();
            if (text.charAt(0) == '0')
                mCurPointsTextView.setText(String.valueOf(position));
            else if (text.length() < MAX_LENGTH)
                mCurPointsTextView.setText(text + String.valueOf(position));
            else
                Toast.makeText(getContext(), R.string.view_keyboard_too_long_input_message, Toast.LENGTH_SHORT).show();
        }
    }
}
