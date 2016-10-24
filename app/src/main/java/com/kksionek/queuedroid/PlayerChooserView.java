package com.kksionek.queuedroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerChooserView extends LinearLayout {

    private final TextView mStaticName;
    private final Button mPointsView;
    private final ImageView mPlayerThumbnail;
    private final ViewGroup mRoot;
    private final AutoCompleteTextView mPlayerName;
    private Activity mActivity;
    private boolean mWaitingForPhoto = false;
    private Player mPlayer = null;

    private final OnThumbnailClickListener mOnThumbClickListener = new OnThumbnailClickListener();

    public PlayerChooserView(Context context, ViewGroup root) {
        super(context, null);

        mRoot = root;

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.player_chooser_view, this, true);

        mPlayerThumbnail = (ImageView) findViewById(R.id.thumbnail);
        initThumbnail();

        mPlayerName = (AutoCompleteTextView) findViewById(R.id.text);
        mPlayerName.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && mPlayer != null) {
                    mPlayer = null;
                    initThumbnail();
                }
                return false;
            }
        });
        mPlayerName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    View nextFocus = focusSearch(FOCUS_DOWN);
                    if (nextFocus instanceof AutoCompleteTextView)
                        nextFocus.requestFocus();
                    else {
                        InputMethodManager imm =  (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mPlayerName.getWindowToken(), 0);
                        v.clearFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        mPlayerName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPlayer = (Player) parent.getItemAtPosition(position);
                mPlayerThumbnail.setImageDrawable(mPlayer.getDrawable());
                mPlayerThumbnail.setOnClickListener(null);
                View nextFocus = focusSearch(FOCUS_DOWN);
                if (nextFocus instanceof AutoCompleteTextView)
                    nextFocus.requestFocus();
                else {
                    mPlayerName.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mPlayerName.getWindowToken(), 0);
                }
            }
        });

        mStaticName = (TextView) findViewById(R.id.staticText);
        mPointsView = (Button) findViewById(R.id.pointsView);
    }

    public void setAdapter(@Nullable PlayerChooserAdapter adapter) {
        mPlayerName.setAdapter(adapter);
    }

    public void setActivity(@NonNull Activity activity) {
        mActivity = activity;
    }

    public boolean onPhotoCreated(@Nullable Intent data) {
        if (!mWaitingForPhoto)
            return false;
        mWaitingForPhoto = false;
        if (data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mPlayerThumbnail.setImageBitmap(imageBitmap);
        }
        return true;
    }

    public void setEditable(boolean editable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(mRoot);
        mPointsView.setVisibility(editable ? GONE : VISIBLE);

        mPlayerName.setVisibility(editable ? VISIBLE : GONE);
        mStaticName.setText(mPlayerName.getText().toString());
        mStaticName.setVisibility(editable ? GONE : VISIBLE);
        mPlayerThumbnail.setOnClickListener(editable && mPlayer == null ? mOnThumbClickListener : null);
    }

    public Player getPlayer() {
        if (mPlayerName.getText().toString().isEmpty())
            return null;
        if (mPlayer == null)
            return new Player(mPlayerName.getText().toString(), mPlayerThumbnail.getDrawable());
        return mPlayer;
    }

    public void setCurrentTurn(boolean current) {
        mStaticName.setTypeface(mStaticName.getTypeface(), current ? Typeface.BOLD : Typeface.NORMAL);
    }

    public void reset(boolean hardReset) {
        setEditable(true);
        mPointsView.setText("0");
        setCurrentTurn(false);
        if (hardReset) {
            mPlayer = null;
            initThumbnail();
            mPlayerName.setText("");
            mStaticName.setText("");
        }
    }

    private void initThumbnail() {
        mPlayerThumbnail.setImageResource(R.drawable.ic_contact_picture);
        mPlayerThumbnail.setOnClickListener(mOnThumbClickListener);
    }

    public void setPoints(int points) {
        mPointsView.setText(String.valueOf(points));
    }

    private class OnThumbnailClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (mPlayerName.getText().length() > 0) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
                    mActivity.startActivityForResult(takePictureIntent, MainActivity.REQUEST_IMAGE_CAPTURE);
                    mWaitingForPhoto = true;
                }
            }
        }
    }
}
