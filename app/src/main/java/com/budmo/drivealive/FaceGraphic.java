package com.budmo.drivealive;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.face.Face;

import com.budmo.drivealive.R;

import com.budmo.drivealive.ui.camera.GraphicOverlay;

class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float ID_TEXT_SIZE = 60.0f;
    private static final float LABEL_Y_OFFSET = 50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int VALID_COLOR = Color.GREEN;
    private static final int INVALID_COLOR = Color.RED;

    private Paint mPaint;

    private volatile Face mFace;

    private boolean mIsReady = false;
    private final String mNotReadyMessage;
    private final String mReadyMessage;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);
        mNotReadyMessage = overlay.getContext().getResources().getString(R.string.not_ready_message);
        mReadyMessage = overlay.getContext().getResources().getString(R.string.ready_message);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(BOX_STROKE_WIDTH);
        mPaint.setTextSize(ID_TEXT_SIZE);
    }

    void updateFace(Face face, boolean isValid) {
        this.mFace = face;
        this.mIsReady = isValid;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;

        canvas.drawText(mIsReady ? mReadyMessage : mNotReadyMessage, left, top - LABEL_Y_OFFSET, mPaint);

        mPaint.setColor(mIsReady ? VALID_COLOR : INVALID_COLOR);
        canvas.drawRect(left, top, right, bottom, mPaint);
    }
}
