package ioio.rd.vespidrone;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
//import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


//import ioio.rd.vespidrone.MainController.MotorsPowers;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIO.VersionType;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
//import ioio.lib.api.PwmOutput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.Sequencer;
import ioio.lib.api.Sequencer.ChannelConfig;
import ioio.lib.api.Sequencer.ChannelConfigPwmPosition;
//import ioio.lib.api.Sequencer.ChannelConfigBinary;
import ioio.lib.api.Sequencer.ChannelConfigPwmSpeed;
//import ioio.lib.api.Sequencer.ChannelConfigSteps;
//import ioio.lib.api.Sequencer.ChannelCueBinary;
import ioio.lib.api.Sequencer.ChannelCuePwmSpeed;
import ioio.lib.api.Sequencer.ChannelCuePwmPosition;
//import ioio.lib.api.Sequencer.ChannelCueSteps;
//import ioio.lib.api.Sequencer.Clock;
//import android.util.Log;

//import ioio.lib.util.BaseIOIOLooper;
//import ioio.lib.util.IOIOLooper;
//import android.content.BroadcastReceiver;

public class QuadcopterActivity extends IOIOActivity
{
    public static final long UI_REFRESH_PERIOD_MS = 250;
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREFS_ID_LAST_IP = "lastServerIP";
    
    
    //private volatile int uno_varValue;
	//private volatile int due_varValue;
	//private volatile int tre_varValue;
	//private volatile int qua_varValue;

	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Get UI elements.
        serverIpEditText = (EditText)findViewById(R.id.serverIpEditText);
        connectToServerCheckBox = (CheckBox)findViewById(R.id.connectToServerCheckBox);

        // In the "server IP" field, insert the last used IP address.
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String lastIP = settings.getString(PREFS_ID_LAST_IP, "192.168.0.8");
        serverIpEditText.setText(lastIP);

        // Create the main controller.
        mainController = new MainController(this);
        //powers = mainController.getMotorsPowers();
        
        //uno_varValue = (int) (1060 + (powers.nw * 2.9));
    	//due_varValue = (int) (1060 + (powers.ne * 2.9));
    	//tre_varValue = (int) (1060 + (powers.sw * 2.9));
    	//qua_varValue = (int) (1060 + (powers.se * 2.9));
    	//Log.d("AndroCopter", "Motor=" + powers.nw);
    }

    // Deactivate some buttons.
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_CALL)
            return true;
        else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Prevent sleep mode.
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Start the main controller.
        try
        {
            mainController.start();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "The USB transmission could not start.",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // Allow the user starting the TCP client.
        serverIpEditText.setEnabled(true);

        // Connect automatically to the computer.
        // This way, it is possible to start the communication just by plugging
        // the ADK (if the auto-start of this application is checked).
        connectToServerCheckBox.setChecked(true);
        onConnectToServerCheckBoxToggled(null);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // Reallow sleep mode.
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Stop the main controller.
        mainController.stop();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        // Save the server IP.
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_ID_LAST_IP, serverIpEditText.getText().toString());
        editor.commit();
    }

    public void onConnectToServerCheckBoxToggled(View v)
    {
        if(connectToServerCheckBox.isChecked()) // Connect.
        {
            mainController.startClient(serverIpEditText.getText().toString());

            serverIpEditText.setEnabled(false);
        }
        else // Disconnect.
        {
            serverIpEditText.setEnabled(true);

            mainController.stopClient();
        }
    }
    
    
    
    
        
        
   
    class Looper extends BaseIOIOLooper {
		/** The on-board LED. */

		
		
		private Sequencer.ChannelCuePwmSpeed dcMotorNECue_ = new ChannelCuePwmSpeed();
		private Sequencer.ChannelCuePwmSpeed dcMotorNWCue_ = new ChannelCuePwmSpeed();
		private Sequencer.ChannelCuePwmSpeed dcMotorSECue_ = new ChannelCuePwmSpeed();
		private Sequencer.ChannelCuePwmSpeed dcMotorSWCue_ = new ChannelCuePwmSpeed();
		

		private Sequencer.ChannelCue[] cue_ = new Sequencer.ChannelCue[] { dcMotorNECue_,dcMotorNWCue_,dcMotorSECue_,dcMotorSWCue_ };
		private Sequencer sequencer_;
		
		

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 *
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * @throws InterruptedException 
		 *
		 * @see ioio.lib.util.IOIOLooper#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException, InterruptedException {
			showVersions(ioio_, "IOIO connected!");
			enableUi(true);
			//led_ = ioio_.openDigitalOutput(0, true);
			
			final ChannelConfigPwmSpeed dcMotorNEConfig = new Sequencer.ChannelConfigPwmSpeed(
					Sequencer.Clock.CLK_2M, 6111, 0, new DigitalOutput.Spec(2));
			final ChannelConfigPwmSpeed dcMotorNWConfig = new Sequencer.ChannelConfigPwmSpeed(
					Sequencer.Clock.CLK_2M, 611, 0, new DigitalOutput.Spec(3));
			final ChannelConfigPwmSpeed dcMotorSEConfig = new Sequencer.ChannelConfigPwmSpeed(
					Sequencer.Clock.CLK_2M, 6111, 0, new DigitalOutput.Spec(4));
			final ChannelConfigPwmSpeed dcMotorSWConfig = new Sequencer.ChannelConfigPwmSpeed(
					Sequencer.Clock.CLK_2M, 6111, 0, new DigitalOutput.Spec(5));
			

			
			final ChannelConfig[] config = new ChannelConfig[] { dcMotorNEConfig,dcMotorNWConfig,dcMotorSEConfig,dcMotorSWConfig };
			sequencer_= ioio_.openSequencer(config);
			
			
			sequencer_.waitEventType(Sequencer.Event.Type.STOPPED);
			while (sequencer_.available() > 0) {
				push();
			}
			sequencer_.start();
			
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 *
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * @throws InterruptedException
		 * 				When the IOIO thread has been interrupted.
		 *
		 * @see ioio.lib.util.IOIOLooper#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			//led_.write(!button_.isChecked());
						
			push();
			//servo_.setDutyCycle(0.05f + _varValue * 0.05f);
			//led_.setDutyCycle(0.05f + _varValue * 0.05f);
			//led_.setPulseWidth(1500);
			
		}
		
		private void push() throws ConnectionLostException, InterruptedException {
			
						
			dcMotorNECue_.pulseWidth = mainController.getNEMotorsPowers();
			dcMotorNWCue_.pulseWidth = mainController.getNWMotorsPowers();
			dcMotorSECue_.pulseWidth = mainController.getSEMotorsPowers();
			dcMotorSWCue_.pulseWidth = mainController.getSWMotorsPowers();
			
			sequencer_.push(cue_, 10);
		}

		/**
		 * Called when the IOIO is disconnected.
		 *rone.
		 * @see ioio.lib.util.IOIOLooper#disconnected()
		 */
		@Override
		public void disconnected() {
			enableUi(false);
			toast("IOIO disconnected");
		}

		/**
		 * Called when the IOIO is connected, but has an incompatible firmware version.
		 *
		 * @see ioio.lib.util.IOIOLooper#incompatible(IOIO)
		 */
		@Override
		public void incompatible() {
			showVersions(ioio_, "Incompatible firmware version!");
		}
}
    
    protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
    
    public void showVersions(IOIO ioio, String title) {
		toast(String.format("%s\n" +
				"IOIOLib: %s\n" +
				"Application firmware: %s\n" +
				"Bootloader firmware: %s\n" +
				"Hardware: %s",
				title,
				ioio.getImplVersion(VersionType.IOIOLIB_VER),
				ioio.getImplVersion(VersionType.APP_FIRMWARE_VER),
				ioio.getImplVersion(VersionType.BOOTLOADER_VER),
				ioio.getImplVersion(VersionType.HARDWARE_VER)));
	}

	public void toast(final String message) {
		final Context context = this;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
		});
	}

	//private int numConnected_ = 0;

	public void enableUi(final boolean enable) {
		// This is slightly trickier than expected to support a multi-IOIO use-case.
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (enable) {
					/*if (numConnected_++ == 0) {
						//button_.setEnabled(true);
					}
				} else {
					if (--numConnected_ == 0) {
						//button_.setEnabled(false);
					}*/
				}
			}
		});
	}
    
    private CheckBox connectToServerCheckBox;
    private EditText serverIpEditText;
    private MainController mainController;
}