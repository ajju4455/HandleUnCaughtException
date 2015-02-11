package aze.samples.handleuncaughtexception;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;

/**
 * {@link UncaughtExceptionHandler} Store Log to SD-Card or to Email with some debug information to the developer. For E-mail you need to uncomment code inside
 * "showAlertToReport"
 * 
 * @author SilentKiller
 */
@SuppressWarnings("deprecation")
public class UnCaughtException implements UncaughtExceptionHandler {

	private Context context;
	private FileWriter crashReport = null;
	private File mLogFolder;

	// TODO Uncomment to send mail
	// private static Context context1;
	// private static final String RECIPIENT = "RECIPENT's MAIL ID";

	public UnCaughtException(Context ctx, String mDirectoryName, String mFileName) {
		context = ctx;
		// context1 = ctx;
		mLogFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "LOG_" + mDirectoryName);
		mLogFolder.mkdirs();
		File logFile = new File(mLogFolder, mFileName);
		try {
			crashReport = new FileWriter(logFile, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private StatFs getStatFs() {
		File path = Environment.getDataDirectory();
		return new StatFs(path.getPath());
	}

	private long getAvailableInternalMemorySize(StatFs stat) {
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	private long getTotalInternalMemorySize(StatFs stat) {
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	private void addInformation(StringBuilder message) {
		message.append("Locale: ").append(Locale.getDefault()).append('\n');
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi;
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			message.append("Version: ").append(pi.versionName).append('\n');
			message.append("Package: ").append(pi.packageName).append('\n');
		} catch (Exception e) {
			Log.e("CustomExceptionHandler", "Error", e);
			message.append("Could not get Version information for ").append(context.getPackageName());
		}
		message.append("Phone Model: ").append(android.os.Build.MODEL).append('\n');
		message.append("Android Version: ").append(android.os.Build.VERSION.RELEASE).append('\n');
		message.append("Board: ").append(android.os.Build.BOARD).append('\n');
		message.append("Brand: ").append(android.os.Build.BRAND).append('\n');
		message.append("Device: ").append(android.os.Build.DEVICE).append('\n');
		message.append("Host: ").append(android.os.Build.HOST).append('\n');
		message.append("ID: ").append(android.os.Build.ID).append('\n');
		message.append("Model: ").append(android.os.Build.MODEL).append('\n');
		message.append("Product: ").append(android.os.Build.PRODUCT).append('\n');
		message.append("Type: ").append(android.os.Build.TYPE).append('\n');
		StatFs stat = getStatFs();
		message.append("Total Internal memory: ").append(getTotalInternalMemorySize(stat)).append('\n');
		message.append("Available Internal memory: ").append(getAvailableInternalMemorySize(stat)).append('\n');
	}

	public void uncaughtException(Thread t, Throwable mThrowableException) {
		try {
			StringBuilder report = new StringBuilder();
			Date curDate = new Date();
			report.append("Error Report collected on : ").append(curDate.toString()).append('\n').append('\n');
			report.append("Informations :").append('\n');
			addInformation(report);
			report.append('\n').append('\n');
			report.append("Stack:\n");
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			// Exception will write all stack trace to string builder
			mThrowableException.printStackTrace(printWriter);
			report.append(result.toString());
			printWriter.close();
			report.append('\n');
			report.append("**** End of current Report ***");
			// Log.e(UnCaughtException.class.getName(), "Error while sendErrorMail" + report);
			showAlertToReport(report);
		} catch (Throwable ignore) {
			// Log.e(UnCaughtException.class.getName(), "Error while sending error e-mail", ignore);
		}
		// previousHandler.uncaughtException(t, e);
	}

	/**
	 * This method for call alert dialog when application crashed!
	 * 
	 * @author Azharahmed
	 */
	public void showAlertToReport(final StringBuilder errorContent) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				builder.setTitle(context.getApplicationInfo().getClass().getName());
				builder.setMessage("Application crash report is saved");
				builder.create();
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						System.exit(0);
					}
				});
				builder.setPositiveButton("Report", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						// ------- Write Crash Report to External Storage
						try {
							System.out.println(errorContent.toString());
							BufferedWriter buf = new BufferedWriter(crashReport);
							try {
								buf.write(errorContent.toString());
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								if (buf != null) {
									buf.flush();
									buf.close();
								}
							}
						} catch (Exception ex) {
							Log.e("TAG", "IO ERROR", ex);
						}

						// ------- TODO Uncomment to send mail of Error Log
						// Intent sendIntent = new Intent(Intent.ACTION_SEND);
						// String subject = "Your App is crassing, Please fix it!";
						// StringBuilder body = new StringBuilder("Error Log : ");
						// body.append('\n').append('\n');
						// body.append(errorContent).append('\n').append('\n');
						// // sendIntent.setType("text/plain");
						// sendIntent.setType("message/rfc822");
						// sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { RECIPIENT });
						// sendIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
						// sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
						// sendIntent.setType("message/rfc822");
						// // context.startActivity(Intent.createChooser(sendIntent, "Error Report"));
						// context1.startActivity(sendIntent);
						System.exit(0);
					}
				});
				builder.setMessage("Unfortunately,This application has stopped");
				builder.show();
				Looper.loop();
			}
		}.start();
	}
}