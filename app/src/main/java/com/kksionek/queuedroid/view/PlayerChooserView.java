package com.kksionek.queuedroid.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.data.Player;
import com.kksionek.queuedroid.model.PlayerChooserAdapter;
import com.kksionek.queuedroid.model.Settings;

public class PlayerChooserView extends LinearLayout {
    private static final String TAG = "PlayerChooserView";
    
    public interface PlayerChooserViewActionListener {
        void onPictureRequested(Intent takePictureIntent);
        void onPointsRequested();
    }

    public interface OnRemoveListener {
        void onRemoveClicked();
    }

    private PlayerChooserViewActionListener mPlayerChooserViewActionListener;

    private final TextView mStaticName;
    private final Button mPointsView;
    private final ImageView mPlayerThumbnail;
    private final AutoCompleteTextView mPlayerName;

    private boolean mIsCurrent = false;
    private boolean mWaitingForPhoto = false;
    private Player mPlayer = null;
    private boolean mEditable = true;

    private final OnThumbnailClickListener mOnThumbClickListener = new OnThumbnailClickListener();
    private OnClickListener mOnRemoveClickListener = null;
    private OnClickListener mOnPointsClickListener = null;

    public PlayerChooserView(Context context) {
        super(context, null);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        inflate(context, R.layout.player_chooser_view, this);

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
                Glide.with(getContext())
                        .load(mPlayer.getImage())
                        .placeholder(R.drawable.ic_contact_picture)
                        .into(mPlayerThumbnail);
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

    public void setPlayerChooserViewActionListener(
            @NonNull PlayerChooserViewActionListener playerChooserViewActionListener) {
        mPlayerChooserViewActionListener = playerChooserViewActionListener;
        mOnPointsClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsCurrent)
                    return;
                if (!Settings.shouldUseInAppKeyboard(getContext())) {
                    mPlayerChooserViewActionListener.onPointsRequested();
                }
            }
        };
        if (!mEditable)
            mPointsView.setOnClickListener(mOnPointsClickListener);
    }

    public void setOnRemoveListener(final OnRemoveListener onRemoveListener) {
        mOnRemoveClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRemoveListener.onRemoveClicked();
            }
        };
        if (mEditable)
            mPointsView.setOnClickListener(mOnRemoveClickListener);
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
        mEditable = editable;
        TransitionDrawable transitionDrawable = (TransitionDrawable) mPointsView.getBackground();
        int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        if (editable) {
            transitionDrawable.reverseTransition(duration);
            mPointsView.setText("");
        } else {
            transitionDrawable.startTransition(duration);
            mPointsView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPointsView.setText("0");
                }
            }, duration);
        }

        mPlayerName.setVisibility(editable ? VISIBLE : GONE);
        mStaticName.setText(mPlayerName.getText().toString());
        mStaticName.setVisibility(editable ? GONE : VISIBLE);
        mPlayerThumbnail.setOnClickListener(editable && mPlayer == null ? mOnThumbClickListener : null);
        if (editable)
            mPointsView.setOnClickListener(mOnRemoveClickListener);
        else
            mPointsView.setOnClickListener(mOnPointsClickListener);
    }

    public Player getPlayer() {
        if (mPlayerName.getText().toString().isEmpty())
            return null;
        if (mPlayer == null)
            return new Player(mPlayerName.getText().toString());
        return mPlayer;
    }

    public void setCurrentTurn(boolean current) {
        if (mIsCurrent || current) {
            float startSize = 15;
            float endSize = 30;

            if (mIsCurrent) {
                startSize = 30;
                endSize = 15;
            }

            int animationDuration = 600;

            ValueAnimator animator = ValueAnimator.ofFloat(startSize, endSize);
            animator.setDuration(animationDuration);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float animatedValue = (float) valueAnimator.getAnimatedValue();
                    mStaticName.setTextSize(animatedValue);
                }
            });

            animator.start();

//            mStaticName.setTypeface(mStaticName.getTypeface(), current ? Typeface.BOLD : Typeface.NORMAL);
            mIsCurrent = current;
        }
    }

    public void reset(boolean hardReset) {
        setEditable(true);
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
                    mPlayerChooserViewActionListener.onPictureRequested(takePictureIntent);
                    mWaitingForPhoto = true;
                }
            }
        }
    }
}
