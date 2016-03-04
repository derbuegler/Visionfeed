package jdengel.com.visionfeed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    /**
     * a public variable.
     * String twitteraccount Visionfeed
     */
    public static String webpage ="https://twitter.com/contextfeed";

    /**
     * a public variable.
     * String URL of the server
     */
    public static final String SERVER_ADRESS = "http://visionfeed.net23.net";

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;

    /**
     * a public variable.
     * select image button
     */
    Button button;

    /**
     * a public variable.
     * upload image button
     */
    Button button2;

    /**
     * a public variable.
     * twitter button
     */
    Button button3;

    /**
     * a public variable.
     * shows the image when selected
     */
    ImageView imageView;

    /**
     * a public variable.
     * String path of the image
     */
    String imagePath;

    /**
     * a public variable.
     * File path of /Pictures
     */
    File path;

    /**
     * a public variable.
     * File path of /Pictures/Visionfeed
     */
    File filePath;

    /**
     * a public variable.
     * string path of /Visionfeed
     */
    String URL = "";


    /**
     * runs when the app is started and sets all variables
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        filePath = new File(path, "Visionfeed");
        imagePath = filePath.getName();
        URL = filePath.getPath();
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.selectButton);
        button2 = (Button) findViewById(R.id.uploadButton);
        button3 = (Button) findViewById(R.id.webButton);
        imageView = (ImageView) findViewById(R.id.imageUploaded);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                selectImage();
            }
        });
        button2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                upload();

            }
        });
        button3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openPage();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        closeCamera();
        super.onPause();
    }

    /**
     * selects between taking and loading a picture
     *
     */
    public void selectImage() {
        final CharSequence[] options = { "Take Picture", "Picture from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Picture")) {
                    takePhoto();
                } else if (options[item].equals("Picture from Gallery")) {
                    openPicture();
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * loads the picture onto imageView
     *
     */
    public void openPicture() {
        imageView.setImageURI(Uri.fromFile(new File(URL + "/visionfeed.jpg")));
    }

    /**
     * opens twitteraccount of Visionfeed on the internet
     *
     */
    public void openPage() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webpage));
        startActivity(browserIntent);
    }

    /**
     * saves a picture in filePath
     * returns a File
     */
    File createImageFile() throws IOException {
        String imageFileName = "visionfeed";
        return new File(filePath, imageFileName + ".jpg");
    }

    /**
     * opens the camera
     *
     */
    private void takePhoto() {
        Intent callCameraApplicationIntent = new Intent();
        callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivity(callCameraApplicationIntent);
    }

    /**
     * executes UploadImage
     *
     */
    private void upload() {
        if (imageView.getDrawable() != null) {
            Bitmap bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            new UploadImage(bm, imagePath).execute();
        } else {
            Toast.makeText(MainActivity.this, "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * a class
     * to upload a bitmap on SERVER_ADRESS with visionfeed.php
     */
    private class UploadImage extends AsyncTask<Void, Void, Void> {

        Bitmap image;
        String name;

        /**
         * a constructor
         * creates variables for the php file
         * @param image
         * @param name
         */
        public UploadImage(Bitmap image, String name) {
            this.image = image;
            this.name = name;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image", encodedImage));
            dataToSend.add(new BasicNameValuePair("name", name));

            HttpParams httpRequestParams = getHttpRequestParams();

            HttpClient client = new DefaultHttpClient((httpRequestParams));
            HttpPost post = new HttpPost(SERVER_ADRESS + "/visionfeed.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        /**
         * shows a message after doInBackground()
         *
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
        }

        /**
         * gets request parameters of the server
         * returns httpRequestParams
         */
        private HttpParams getHttpRequestParams() {
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, 1000 * 30);
            HttpConnectionParams.setSoTimeout(httpRequestParams, 1000 * 30);
            return httpRequestParams;
        }
    }

    /**
     * closes the camera
     *
     */
    private void closeCamera() {
        if(mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if(mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }
}