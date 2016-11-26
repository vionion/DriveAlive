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
    private static final double ONE_EYE_CLOSED_THRESHOLD = 0.4;
    private static final double BOTH_EYES_CLOSED_THRESHOLD = 0.6;
    private static int SLEEPY_FRAMES = 0;
    private static int BLINKING_FRAMES = 180;

    private static AtomicInteger faceNumber = new AtomicInteger(0);

    private GraphicOverlay mOverlay;
    private FaceGraphic mFaceGraphic;
    private ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
    private float leftEyeOpened;
    private float rightEyeOpened;
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
            leftEyeOpened = face.getIsLeftEyeOpenProbability();
            rightEyeOpened = face.getIsRightEyeOpenProbability();
            if ((leftEyeOpened < ONE_EYE_CLOSED_THRESHOLD && rightEyeOpened < ONE_EYE_CLOSED_THRESHOLD) || (leftEyeOpened + rightEyeOpened < BOTH_EYES_CLOSED_THRESHOLD)) {
                SLEEPY_FRAMES++;
                BLINKING_FRAMES -= 180;
                if (SLEEPY_FRAMES > 3) {
                    toneG.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_SLS, 100); // 100 is duration in ms
                    mFaceGraphic.updateFaceFrame(face, false);
                }
                if (BLINKING_FRAMES < 150) {
                    // TODO: show notification
                    toneG.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_SLS, 100); // 100 is duration in ms
                    mFaceGraphic.updateFaceFrame(face, false);
                }
            } else {
                mFaceGraphic.updateFaceFrame(face, true);
                SLEEPY_FRAMES = 0;
                if (BLINKING_FRAMES < 180) {
                    BLINKING_FRAMES++;
                }
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