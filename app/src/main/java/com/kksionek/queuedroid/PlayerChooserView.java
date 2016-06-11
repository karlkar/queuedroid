package com.kksionek.queuedroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerChooserView extends LinearLayout {

    private ImageView mPlayerThumbnail;
    private AutoCompleteTextView mPlayerName;
    private Activity mActivity;
    private boolean mWaitingForPhoto = false;
    private Player mPlayer = new Player("-", "", "", Player.Type.CUSTOM);

    public PlayerChooserView(Context context) {
        this(context, null);
    }

    public PlayerChooserView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.player_chooser_view, this, true);

        setBackgroundResource(R.drawable.btn_big);

        mPlayerThumbnail = (ImageView) getChildAt(0);
        mPlayerThumbnail.setImageResource(R.drawable.ic_contact_picture);
        mPlayerThumbnail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerName != null && mPlayerName.getText().length() > 0) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
                        mActivity.startActivityForResult(takePictureIntent, MainActivity.REQUEST_IMAGE_CAPTURE);
                        mWaitingForPhoto = true;
                    }
                }
            }
        });

        mPlayerName = (AutoCompleteTextView) getChildAt(1);
        mPlayerName.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                mPlayerThumbnail.setImageResource(R.drawable.ic_contact_picture);
                if (!mPlayer.isCustom())
                    mPlayer = new Player("-", mPlayerName.getText().toString(), null, Player.Type.CUSTOM);
                else
                    mPlayer.setName(mPlayerName.getText().toString());
                mPlayer.setDrawable(mPlayerThumbnail.getDrawable());
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
                View nextFocus = focusSearch(FOCUS_DOWN);
                if (nextFocus instanceof AutoCompleteTextView)
                    nextFocus.requestFocus();
                else {
                    mPlayerName.clearFocus();
                    InputMethodManager imm =  (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mPlayerName.getWindowToken(), 0);
                }
            }
        });
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
            mPlayer.setDrawable(mPlayerThumbnail.getDrawable());
        }
        return true;
    }
}
