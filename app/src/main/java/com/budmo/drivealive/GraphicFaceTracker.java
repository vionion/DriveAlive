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
    private static final double BOTH_EYES_CLOSED_THRESHOLD = 1.2;

    private static AtomicInteger faceNumber = new AtomicInteger(0);

    private GraphicOverlay mOverlay;
    private FaceGraphic mFaceGraphic;
    private ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
    private int faceId;
    private int sleepyFrames = 0;
    private int totalFrames = 0;
    private long startTime;

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
        if (totalFrames == 0) {
            startTime = System.currentTimeMillis();
        }
        
        totalFrames++;
        if (totalFrames % 100 == 0) {
            double totalTimeInSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            Log.i(TAG, "FRAMES PER SECOND: " + (totalFrames / totalTimeInSeconds));
        }

        mOverlay.add(mFaceGraphic);
        boolean oneFaceOnly = faceNumber.get() == 1;
        if (oneFaceOnly) {
            float leftEyeOpened = face.getIsLeftEyeOpenProbability();
            float rightEyeOpened = face.getIsRightEyeOpenProbability();

            if (totalFrames % 10 == 0) {
                Log.i(TAG, "PROBABILITY OF OPEN EYES: " + leftEyeOpened + " " + rightEyeOpened);
            }

            if ((leftEyeOpened < ONE_EYE_CLOSED_THRESHOLD && rightEyeOpened < ONE_EYE_CLOSED_THRESHOLD) || (leftEyeOpened + rightEyeOpened < BOTH_EYES_CLOSED_THRESHOLD)) {
                sleepyFrames++;
                if (sleepyFrames > 3) {
                    toneG.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_SLS, 100); // 100 is duration in ms
                    mFaceGraphic.updateFaceFrame(face, false);
                    Log.w(TAG, "SLEEPY FRAMES: " + sleepyFrames);
                }
            } else {
                mFaceGraphic.updateFaceFrame(face, true);
                sleepyFrames = 0;
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