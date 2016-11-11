package com.projects.codeyasam.threatmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by codeyasam on 8/31/16.
 */
public class CYM_UTILITY {

    //public static final String THREAT_MAP_ROOT_URL = "http://codeyasam.com/threatmap/";
    public static final String THREAT_MAP_ROOT_URL = "http://192.168.42.68/threatmap/";

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
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

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static Bitmap loadImageFromServer(String url, int reqWidth, int reqHeight) {
        try {
            URL urlConnection = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            //Bitmap myBitmap = BitmapFactory.decodeStream(input);
            BitmapFactory.decodeStream(input, null, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            input.close();
            connection = (HttpURLConnection) urlConnection.openConnection();
            connection.connect();
            input = connection.getInputStream();
            options.inJustDecodeBounds = false;
            Bitmap myBitmap = BitmapFactory.decodeStream(input, null, options);
            input.close();
            input = null;
            return myBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void displayText(Activity activity, int id, String text) {
        TextView tv = (TextView) activity.findViewById(id);
        tv.setText(text);
    }

    public static void displayText(View view, int id, String text) {
        TextView tv = (TextView) view.findViewById(id);
        tv.setText(text);
    }

    public static void setImageOnView(Activity activity, int id, Bitmap bmp) {
        ImageView iv = (ImageView) activity.findViewById(id);
        iv.setImageBitmap(bmp);
    }

    public static void setImageOnView(View view, int id, Bitmap bmp) {
        ImageView iv = (ImageView) view.findViewById(id);
        iv.setImageBitmap(bmp);
    }

    public static String getText(Activity activity, int id) {
        EditText et = (EditText) activity.findViewById(id);
        return et.getText().toString().trim();
    }

    public static void setText(Activity activity, int id, String text) {
        EditText et = (EditText) activity.findViewById(id);
        et.setText(text);
    }

    public static void setImageOnView(Activity activity, int id, byte[] resource) {
        Bitmap bmp = BitmapFactory.decodeByteArray(resource, 0, resource.length);
        ImageView image = (ImageView) activity.findViewById(id);
        image.setImageBitmap(getRoundedCornerBitmap(bmp));
    }

    public static String getPath(Uri imageURI, Activity activity) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        CursorLoader cursorLoader = new CursorLoader(activity, imageURI, projection, null, null,
                null);
        Cursor cursor =cursorLoader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String selectedImagePath = cursor.getString(column_index);
        return selectedImagePath;
    }

    public static Matrix getMatrixAngle(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            //Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }

            return matrix;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public static void callYesNoMessage(String message, Activity context, DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setNegativeButton("CANCEL", null);
        builder.setPositiveButton("OK", clickListener);
        builder.show();
    }

    public static void mAlertDialog(String message, Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setNegativeButton("OK", null);
        builder.show();
    }

    public static void setRatingBarRate(Activity activity, int id, float rating) {
        RatingBar ratingBar = (RatingBar) activity.findViewById(id);
        ratingBar.setRating(rating);
    }

    public static void setRatingBarRate(View view, int id, float rating) {
        RatingBar ratingBar = (RatingBar) view.findViewById(id);
        ratingBar.setRating(rating);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap getBitmapFromUrl(String url) {
        try {
            URL pictureURL = new URL(url);
            Bitmap bitmap = BitmapFactory.decodeStream(pictureURL.openConnection().getInputStream());
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        //bm.recycle();
        return resizedBitmap;
    }

}
