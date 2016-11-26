package com.budmo.drivealive;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

import com.budmo.drivealive.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.concurrent.atomic.AtomicInteger;

public class GraphicFaceTracker extends Tracker<Face> {
    private static final String TAG = "FaceTracker";
    private static final double EYE_CLOSED_THRESHOLD = 0.4;

    private static AtomicInteger faceNumber = new AtomicInteger(0);

    private GraphicOverlay mOverlay;
    private FaceGraphic mFaceGraphic;
    private ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
    private int faceId;

    GraphicFaceTracker(GraphicOverlay overlay) {
        mOverlay = overlay;
        mFaceGraphic = new FaceGraphic(overlay);
    }

    @Override
    public void onNewItem(int faceId, Face item) {
        Log.w(TAG, "TRACKER: NEW FACE " + faceId);
        faceNumber.incrementAndGet();
        Log.w(TAG, "NUMBER OF FACES " + faceNumber.get());
        this.faceId = faceId;
    }

    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
        mOverlay.add(mFaceGraphic);

        boolean oneFaceOnly = faceNumber.get() == 1;
        if (oneFaceOnly) {
            float leftEye = face.getIsLeftEyeOpenProbability();
            float rightEye = face.getIsRightEyeOpenProbability();
            if (leftEye < EYE_CLOSED_THRESHOLD && rightEye < EYE_CLOSED_THRESHOLD) {
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
                mFaceGraphic.updateFaceFrame(face, false);
            } else {
                mFaceGraphic.updateFaceFrame(face, true);
            }
        } else {
            mFaceGraphic.updateFaceFrame(face, false);
        }
    }

    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        Log.w(TAG,"TRACKER: MISSING FACE " + faceId);
        faceNumber.decrementAndGet();
        Log.w(TAG, "NUMBER OF FACES " + faceNumber.get());
        mOverlay.remove(mFaceGraphic);
    }

    @Override
    public void onDone() {
        Log.w(TAG,"TRACKER: REMOVED FACE " + faceId);
        faceNumber.decrementAndGet();
        Log.w(TAG, "NUMBER OF FACES " + faceNumber.get());
        mOverlay.remove(mFaceGraphic);
    }

    public static int getFaceNumber() {
        return GraphicFaceTracker.faceNumber.get();
    }

}