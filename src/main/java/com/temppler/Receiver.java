package com.temppler;

import java.util.ArrayList;

public class Receiver extends Device {
	private AudioIn aIn;
	private FFT fft;
	private GraphView graph;
	private double[] x = new double[1024], y = new double[x.length], display = new double[x.length/2];
    boolean run = false, gotToDo = false;

	private ShortArrayBuffer inputBuffer;

	public Receiver(TempplerActivity context) {
		graph = (GraphView) context.findViewById(R.id.spectrumView);
		fft = new FFT(x.length);
		inputBuffer = new ShortArrayBuffer();
		graph.setScale(10);
	}

	@Override
	public void init() {
		aIn = AudioIn.getInstance(aRec);
	}

	@Override
	public void start() {
		aIn.start();
        run = true;
        (new Thread(fftDoer)).start();
	}

	@Override
	public void stop() {
		if(aIn.isRecording())
			aIn.stop();
        run = false;
	}

	@Override
	public void destroy() {

	}



	private void doFFT(){
		for(int i = 0; i < x.length; i++){
			x[i] = (double)inputBuffer.shift() / Short.MAX_VALUE;
			y[i] = 0;
		}
		fft.fft(x, y);
		for(int i = 0; i < display.length; i++){
			display[i] = x[i];
		}
		graph.setVals(display);
		graph.postInvalidate();
	}


    private Runnable fftDoer = new Runnable(){
        public void run(){
            while(run){
                while(!gotToDo)
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                doFFT();
                if (inputBuffer.size() < x.length)
                    gotToDo = false;
            }
        }
    };


	private AudioReceiverListener aRec = new AudioReceiverListener() {
		@Override
		public void capturedAudioReceived(short[] buffer) {

            // ggf. frames fallenlassen, dafuer echtzeit
			if(!gotToDo) {
                inputBuffer.addAll(buffer);

                if (inputBuffer.size() > x.length) {
                    gotToDo = true;
                }
            }
		}
	};
}



class ShortArrayBuffer{
	private ArrayList<short[]> list;
	private ArrayList<Integer> indizes;
	private int offset = 0;

	public ShortArrayBuffer(){
		list = new ArrayList<>();
		indizes = new ArrayList<>();
	}

	public synchronized void addAll(short[] ar){
		list.add(ar);
		indizes.add((indizes.size() > 0 ? indizes.get(indizes.size()-1) : 0) + ar.length);
	}

	public synchronized int size(){
		return indizes.get(indizes.size()-1) - offset;
	}

	public synchronized short shift(){
		if(offset >= indizes.get(0)){
			int min = indizes.get(0);
			offset -= min;
			indizes.remove(0);
			list.remove(0);
			for(int i = 0; i < indizes.size(); i++)
				indizes.set(i, indizes.get(i)-min);
		}
		offset++;

		return list.get(0)[offset-1];
	}
}