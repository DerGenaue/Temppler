package com.temppler;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class Emitter extends Device {
	Thread t;
    private AudioIn aIn;
	
	public boolean active = false; 
    
    private SeekBar valueIn;
    private TextView valueOut, loudnessTOut;
    private ProgressBar loudnessOut;
    private GraphView graph;
    private boolean done = false;
    private double average = 0;

    
	public Emitter(TempplerActivity context) {
		valueIn = (SeekBar) context.findViewById(R.id.valueIn);
        valueOut = (TextView) context.findViewById(R.id.valueOut);
        loudnessTOut = (TextView) context.findViewById(R.id.loudnessText);
        loudnessOut = (ProgressBar) context.findViewById(R.id.loudness);
        graph = (GraphView) context.findViewById(R.id.graph); 

        valueIn.setOnSeekBarChangeListener(OSBCL);
        valueIn.setProgress(19000);
        valueOut.setText("19000 Hz");
	}
	
	public void init() {
		aIn = AudioIn.getInstance(aRec);

		t = new Thread(new OutDACrunnable());
        t.setPriority(Thread.MAX_PRIORITY);
	}
	
	public void start() {
		done = false;
        t.start();
        aIn.start();
        // (new Thread(doFreqTest)).start();
	}
	
	public void stop() {
		done = true;
        if(aIn != null && aIn.isRecording())
            aIn.stop();
	}
	
	public void destroy() {
		// close streams, whatever
	}
	
	private Runnable doFreqTest = new Runnable(){

        int step = 20, samples = 10;
        int val = 0;
        private double[] vals = new double[20000 / step];
        public void run(){
            for(int i = 0; i < vals.length; i++){
                vals[i] = 0;
            }
            graph.setVals(vals);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            for(int i = 0; i < vals.length; i++){
                val = step * i;
                valueIn.post(new Runnable(){public void run(){valueIn.setProgress(val);}});
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {}
                for(int j = 0; j < samples; j++) {
                    vals[i] += average;
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {}
                }
                vals[i] /= samples;
                graph.postInvalidate();
            }
        }
    };
	
	private AudioReceiverListener aRec = new AudioReceiverListener(){
	        @Override
	        public void capturedAudioReceived(short[] buffer) {
	            int maxGrad = 0;
	            for (int i = 1; i < buffer.length; i++){
	                if(Math.abs(buffer[i] - buffer[i-1]) > maxGrad){
	                    maxGrad = Math.abs(buffer[i] - buffer[i-1]);
	                }
	            }

	            average = (average + maxGrad) / 2;

	            loudnessOut.post(new Runnable() {
	                public void run() {
	                    loudnessOut.setProgress((int) average);
	                    loudnessTOut.setText("VolIn: " + (int) average);
	                }
	            });
	        }
	    };
	
	class OutDACrunnable implements Runnable {

        private short[] generatedSnd;
        private int sampleR = 44100;
        private double ph = 0;
        private int amp = 10000;


        public void run() {
            generatedSnd = new short[AudioTrack.getMinBufferSize(sampleR, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)];
            System.out.println("Buffer-length: "+ generatedSnd.length);
            for(int i = 0; i < generatedSnd.length; i++){
                generatedSnd[i] = 0;
            }

            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleR,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    generatedSnd.length, AudioTrack.MODE_STREAM);

            audioTrack.play();

            while (! done) {
                genTone(valueIn.getProgress()); //;valueIn.getProgress());

                //System.out.println(val);


                audioTrack.write(generatedSnd, 0, generatedSnd.length);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            audioTrack.release();
        }


        private void genTone(float freq) {
            for (int i = 0; i < generatedSnd.length; i++) {
                generatedSnd[i] = (short) (Math.sin(ph) * (double)amp);


                ph += 2*Math.PI*freq/sampleR;
                //ph %= 2*Math.PI;

            }
            // Log.d(MyTag, "genTone: done");
        }
    }
	
    private SeekBar.OnSeekBarChangeListener OSBCL = new SeekBar.OnSeekBarChangeListener(){
        public void onProgressChanged(SeekBar sb, int progress, boolean u){
            valueOut.setText(progress+" Hz");
        }
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
    
}
