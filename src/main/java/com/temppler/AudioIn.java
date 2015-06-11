package com.temppler;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by Daniel on 11.06.2015.
 */
public class AudioIn implements Runnable {

    private AudioRecord audioRecorder = null;
    private int bufferSize;
    private int samplePerSec = 44100;
    private String LOG_TAG = "AudioCapturer";
    private Thread thread = null;

    private boolean isRecording;
    private static AudioIn audioIn;
    private AudioReceiverListener iAudioReceiver;

    private AudioIn(AudioReceiverListener audioReceiver) {
        this.iAudioReceiver = audioReceiver;
    }

    public static AudioIn getInstance(AudioReceiverListener audioReceiver) {
        if (audioIn == null) {
            audioIn = new AudioIn(audioReceiver);
        }
        return audioIn;
    }

    public void start() {

        bufferSize = AudioRecord.getMinBufferSize(samplePerSec, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize != AudioRecord.ERROR_BAD_VALUE && bufferSize != AudioRecord.ERROR) {

            audioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, this.samplePerSec, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, this.bufferSize * 10); // bufferSize
            // 10x
            Log.p(LOG_TAG, "BufSize: "+bufferSize);

            if (audioRecorder != null && audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                //Log.i(LOG_TAG, "Audio Recorder created");


                audioRecorder.startRecording();
                isRecording = true;
                thread = new Thread(this);
                thread.start();

            } else {
                Log.e(LOG_TAG, "Unable to create AudioRecord instance");
            }

        } else {
            Log.e(LOG_TAG, "Unable to get minimum buffer size");
        }
    }

    public void stop() {
        isRecording = false;
        if (audioRecorder != null) {
            if (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                // System.out
                // .println("Stopping the recorder inside AudioRecorder");
                audioRecorder.stop();
            }
            if (audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecorder.release();
            }
        }
    }

    public boolean isRecording() {
        return (audioRecorder != null) ? (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) : false;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        while (isRecording && audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            short[] tempBuf = new short[bufferSize];
            audioRecorder.read(tempBuf, 0, tempBuf.length);
            iAudioReceiver.capturedAudioReceived(tempBuf);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("AudioCapturer finalizer");
        if (audioRecorder != null && audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
            audioRecorder.stop();
            audioRecorder.release();
        }
        audioRecorder = null;
        iAudioReceiver = null;
        thread = null;
    }

}

class Log {
    public static void e(String name, String e){
        System.err.println(name + ": " + e);
    }
    public static void p(String name, String e){
        System.out.println(name + ": " + e);
    }
}