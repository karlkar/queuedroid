package com.kksionek.queuedroid.view.keyboard;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.model.keyboard.KeyboardViewAdapter;

public class KeyboardView extends LinearLayout {
    private static final String TAG = "KEYBOARDVIEW";

    private static final int MAX_LENGTH = 10;

    private final TextView mCurPointsTextView;
    private final Button mClearButton;
    private final Button mBackspaceButton;
    private final RecyclerView mButtonRecylerView;

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(
                R.layout.keyboard, this);

        mCurPointsTextView = (TextView) findViewById(R.id.cur_points);
        mClearButton = (Button) findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurPointsTextView.setText("0");
            }
        });
        mBackspaceButton = (Button) findViewById(R.id.backspace_button);
        mBackspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence text = mCurPointsTextView.getText();
                if (text.length() == 1)
                    mCurPointsTextView.setText("0");
                else
                    mCurPointsTextView.setText(text.subSequence(0, text.length() - 1));
            }
        });

        mButtonRecylerView = (RecyclerView) findViewById(R.id.button_grid_view);
        KeyboardViewAdapter keyboardViewAdapter = new KeyboardViewAdapter(getContext());
        keyboardViewAdapter.setOnKeyboardItemClickListener(new KeyboardListener());
        mButtonRecylerView.setAdapter(keyboardViewAdapter);
        mButtonRecylerView.setLayoutManager(new GridLayoutManager(getContext(), 5, GridLayoutManager.VERTICAL, false));
        mButtonRecylerView.addItemDecoration(new SpacesItemDecoration(getContext(), R.dimen.keyboard_item_gap_size));
    }

    class KeyboardListener implements OnKeyboardItemClickListener {

        public void onClick(int position) {
            CharSequence text = mCurPointsTextView.getText();
            if (text.charAt(0) == '0')
                mCurPointsTextView.setText(String.valueOf(position));
            else if (text.length() < MAX_LENGTH)
                mCurPointsTextView.setText(text + String.valueOf(position));
            else
                Toast.makeText(getContext(), "Not allowed number.", Toast.LENGTH_SHORT).show();
        }
    }
}
