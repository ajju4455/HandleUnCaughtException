package aze.samples.handleuncaughtexception;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

/**
 * 
 * <p>
 * Name : <b> MyActivity </b>
 * <p>
 * Description : <b> This Activity a holder activity of all fragments and only parent activity </b>
 * 
 * @author SilentKiller
 * 
 */
@SuppressLint("SimpleDateFormat")
public class MyActivity extends FragmentActivity {

	private String TAG = "MyActivity";
	private SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(MyActivity.this, TAG, formatter.format(new Date()) + ".txt"));
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

}