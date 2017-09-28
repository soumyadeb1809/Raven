package soumyadeb.raven.utility;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Soumya Deb on 27-06-2017.
 */

public class Tools {
    private static AlertDialog.Builder mAlert;

    public static void showAlert(Activity activity, String title, String message){
        mAlert = new AlertDialog.Builder(activity);
        if(!title.equals(null))
            mAlert.setTitle(title);
        mAlert.setMessage(message);
        mAlert.setCancelable(false);
        mAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            //Empty handler
            }
        });
        mAlert.show();
    }

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    public static void showFailureError(Context context){
        Toast.makeText(context, "Error occurred. Please try again.", Toast.LENGTH_LONG).show();
    }

    public static String getCurrentTimeStamp(){
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return ts;
    }

    public static String getDateFromTS(String ts){
        long tsLong = Long.parseLong(ts);
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(tsLong);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

}
