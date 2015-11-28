package com.temppler;

import java.util.LinkedList;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class TempplerActivity extends Activity {

	private Button buttonEmit, buttonReceive;
	
	private ApplicationState state = ApplicationState.START;
	
	private CardView cardEmit, cardReceive;
	private View detailsEmit, detailsReceive;
	
	private LinkedList<Device> devices;
	private Emitter e;
	private Receiver r;
	
	{
		devices = new LinkedList<Device>();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.layout_main);

        cardEmit    = (CardView) findViewById(R.id.layout_emit);
        cardReceive = (CardView) findViewById(R.id.layout_receive);

        TOnClickListener onclick = this.new TOnClickListener();
        buttonEmit = (Button) findViewById(R.id.button_emit);
        buttonReceive = (Button) findViewById(R.id.button_receive);
        buttonEmit.setOnClickListener(onclick);
        buttonReceive.setOnClickListener(onclick);
                
        detailsEmit = findViewById(R.id.details_emit);
        detailsReceive = findViewById(R.id.details_receive);

        e = new Emitter(this);
        r = new Receiver(this);
        devices.add(e);
        devices.add(r);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    protected void onStop(){
		super.onStop();
    	for(Device d : devices) {
    		d.stop();
    		d.destroy();
    	}
    	//System.exit(0);
    }
  
    @Override
    public void onBackPressed() {
    	if(state != ApplicationState.START) {
    		if(state == ApplicationState.EMIT) {
    			// (stop &) destroy Emitter
    			e.stop();
    			e.destroy();
    			// restore Emit Card
    			detailsEmit.setVisibility(View.GONE);
				buttonEmit.setVisibility(View.VISIBLE);
    		} else if (state == ApplicationState.RECEIVE) {
    			// (stop &) destroy Receiver
    			r.stop();
    			r.destroy();
    			// restore Receive Card
    			detailsReceive.setVisibility(View.GONE);
				buttonReceive.setVisibility(View.VISIBLE);
    		}
    		// make both Cards Visible
			cardEmit.setVisibility(View.VISIBLE);
    		cardReceive.setVisibility(View.VISIBLE);
    		
    		// set Application State
    		state = ApplicationState.START;
    	} else {
    		super.onBackPressed();
    	}
    };
    
    private class TOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if(null == v)
				return;
			if(v.getId() == R.id.button_emit && state == ApplicationState.START) {
				// hide receive card
				cardReceive.setVisibility(View.GONE);
				// extend emit card
				detailsEmit.setVisibility(View.VISIBLE);
				buttonEmit.setVisibility(View.GONE);
				// set App State
				state = ApplicationState.EMIT;
				// initialize Emitter
				e.init();
				// start Emitter (for now)
				e.start();
				
			} else if(v.getId() == R.id.button_receive && state == ApplicationState.START) {
				// hide emit card
				cardEmit.setVisibility(View.GONE);
				// extend receive card
				detailsReceive.setVisibility(View.VISIBLE);
				buttonReceive.setVisibility(View.GONE);
				// set App State
				state = ApplicationState.RECEIVE;
				// initialize Receiver
				r.init();
				// start Receiver (for now)
				r.start();
			}
		}
    }
}
