package com.ram.nativesocialsharing.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.FileProvider;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.ShareDialog;
import com.ram.projectlib.interfaces.PermissionResultCallback;
import com.ram.projectlib.utils.CGlobal_lib;
import com.ram.projectlib.utils.PermissionUtils;
import com.ram.nativesocialsharing.R;
import com.ram.nativesocialsharing.utility.FileNameCreation;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PermissionResultCallback, OnRequestPermissionsResultCallback {

    private static String TAG = MainActivity.class.getSimpleName();

    ImageButton imgBtn_facebook, imgBtn_twitter, imgBtn_instgram, imgBtn_whatsapp, imgBtn_choose;
    ImageView imgPreview;
    VideoView videoPreview;
    EditText et_message, et_mobileNumber;

    private Context context;
    private static final int GALLERY_REQUEST_CODE = 332;
    private static final int CAMERA_REQUEST_CODE = 333;
    private Uri cameraFileURI;
    String simageuri;
    File fileImagepath;
    String sShareType, sharing_message;

    private static String authority = null;
    PermissionUtils permissionUtils;
    ArrayList<String> permissions = new ArrayList<>();
    boolean isAllPermissionGranted = false;
    String imageType = "image/*";
    String videoType = "video/*";
    String textType = "text/plan";

    private CallbackManager callbackManager;
    private ShareDialog shareDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;

        // Initialize facebook SDK.
        FacebookSdk.sdkInitialize(getApplicationContext());
        // Create a callbackManager to handle the login responses.

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(MainActivity.this);

        initView();
        initListner();

        check_permission();
    }

    private void initView() {

        imgBtn_facebook = findViewById(R.id.btn_facebook);
        imgBtn_instgram = findViewById(R.id.btn_instagram);
        imgBtn_twitter = findViewById(R.id.btn_twitter);
        imgBtn_whatsapp = findViewById(R.id.btn_whatapp);
        imgPreview = findViewById(R.id.imageView);
        videoPreview = findViewById(R.id.videoView);
        imgBtn_choose = findViewById(R.id.btn_choose);
        et_message = findViewById(R.id.et_message);
        et_mobileNumber = findViewById(R.id.et_mobileNumber);

    }

    private void initListner() {

        imgBtn_facebook.setOnClickListener(this);
        imgBtn_instgram.setOnClickListener(this);
        imgBtn_twitter.setOnClickListener(this);
        imgBtn_whatsapp.setOnClickListener(this);
        imgBtn_choose.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        sharing_message = et_message.getText().toString().trim();


        switch (v.getId()) {

            case R.id.btn_choose:
                if (isAllPermissionGranted)
                    cameraFileURI = null;
                onPermissionGranted();
                /*selectImageFromGallery();*/

                break;

            case R.id.btn_facebook:

                /*facebook package name ="com.facebook.katana */
                /*facebook sharing activity name = "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias"*/
                //shareContent_on_Facebook_native(cameraFileURI, sharing_message, sShareType, "com.facebook.katana", "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias");
                if (cameraFileURI != null && !cameraFileURI.equals(Uri.EMPTY)) {
                    if (sShareType.equalsIgnoreCase(imageType)) {
                        shareImage_on_facebook_sdk(cameraFileURI, sharing_message);

                    } else if (sShareType.equalsIgnoreCase(videoType)) {
                        shareVideo_on_facebook_sdk(cameraFileURI, sharing_message);
                    } else {
                        CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.selectcontenttoshare));
                    }
                } else {
                    CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.selectcontenttoshare));
                }

                break;

            case R.id.btn_twitter:
                /*twitter package name = "com.twitter.android"*/
                /*twitter sharing activity name = "com.twitter.composer.ComposerActivity*/
                if (cameraFileURI != null && !cameraFileURI.equals(Uri.EMPTY)) {
                    shareContenton_Twitter(cameraFileURI, sharing_message, sShareType, "com.twitter.android", "com.twitter.composer.ComposerActivity");
                } else {
                    CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.selectcontenttoshare));

                }

                break;

            case R.id.btn_instagram:
                /*instagarm package name = "com.instagram.android*/
                /*instagram story activity name = "com.instagram.share.handleractivity.StoryShareHandlerActivity*/
                /*instagram feed actuvuty name = "com.instagram.share.handleractivity.ShareHandlerActivity*/

                if (cameraFileURI != null && !cameraFileURI.equals(Uri.EMPTY)) {
                    shareContent_on_instgram(cameraFileURI, sharing_message, sShareType, "com.instagram.android", "");
                } else {
                    CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.selectcontenttoshare));
                }
                break;

            case R.id.btn_whatapp:
                /*whatsapp package name = "com.whatsapp"*/
                /*whatsapp sharing activity name = "@s.whatsapp.net" */
                if (cameraFileURI != null && !cameraFileURI.equals(Uri.EMPTY)) {
                    String mobile_number = et_mobileNumber.getText().toString().trim();
                    if (!TextUtils.isEmpty(mobile_number))
                        shareContent_on_Whatsapp(cameraFileURI, mobile_number, sharing_message, sShareType, "com.whatsapp", "@s.whatsapp.net");
                    else
                        CGlobal_lib.getInstance(MainActivity.this).showMessage("Please enter mobile number to share on whatsapp");
                } else {
                    CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.selectcontenttoshare));

                }
                break;
        }
    }

    public void check_permission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                permissionUtils = new PermissionUtils(MainActivity.this);

            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.CAMERA);
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionUtils.check_permission(permissions, "To use CAMERA " + "requires Permissions", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // check_permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // check_permission denied, boo! Disable the
                    // functionality that depends on this check_permission.
                    CGlobal_lib.getInstance(MainActivity.this).showMessage("You need to grant check_permission to access app functionality.");

                }
                return;
            }
        }
    }


    @Override
    public void PermissionGranted(int request_code) {
        Log.i("PERMISSION", "GRANTED");
        isAllPermissionGranted = true;
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        Log.i("PERMISSION PARTIALLY", " Permission GRANTED");
        isAllPermissionGranted = false;
    }

    @Override
    public void PermissionDenied(int request_code) {
        Log.i("PERMISSION", "DENIED");
        isAllPermissionGranted = false;
    }

    @Override
    public void NeverAskAgain(int request_code) {
        Log.i("PERMISSION", "NEVER ASK AGAIN");
        isAllPermissionGranted = false;
    }


    private void onPermissionGranted() {

        new AlertDialog.Builder(this)
                .setTitle("Select Option")
                .setItems(new String[]{"Image", "Video"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                //Gallery
                                selectImageFromGallery();
                                break;
                            case 1:
                                //Camera
                                //captureImageFormCamera();

                                //video
                                selectVideoFromGallery();
                                break;
                        }
                    }
                })
                .setCancelable(true)
                .create()
                .show();
    }


    /**
     * start activity to pick image from gallery
     */
    private void selectImageFromGallery() {
        Intent in = new Intent(Intent.ACTION_PICK);
        in.setType(imageType);
        sShareType = imageType;
        startActivityForResult(in, GALLERY_REQUEST_CODE);
    }

    /**
     * start activity to pick Video from gallery
     */
    private void selectVideoFromGallery() {
        Intent in = new Intent(Intent.ACTION_PICK);
        in.setType(videoType);
        sShareType = videoType;
        startActivityForResult(in, GALLERY_REQUEST_CODE);
    }

    /**
     * start activity to capture image
     */
    private void captureImageFormCamera() {
        //check if device support camera or not if not then don't do anything
        if (!CameraUtils.isDeviceSupportCamera(this)) {
            CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.devicedoesnotsupportcamer));
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Get the file URI using the below code
        //here in place of AUTHORITY you have to pass <package_name>.file_provider
        fileImagepath = FileNameCreation.createImageFile(this);
        cameraFileURI = FileProvider.getUriForFile(this, authority, fileImagepath);

        //after getting image URI pass it via Intent
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileURI);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //grant URI check_permission to access the create image URI
        for (ResolveInfo resolveInfo : getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
            try {
                grantUriPermission(resolveInfo.activityInfo.packageName, cameraFileURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //here check if there ia any app available to perform camera task or not if not then show toast
        //NOTE : This condition is not required because every device has Camera app but in rare cases some device don't have camera
        //so to avoid that thing we have to add this condition
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } else {
            CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.noapptocaptureimage));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GALLERY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    this.cameraFileURI = imageUri;

                    if (sShareType.equalsIgnoreCase(imageType)) {
                        displayImage(cameraFileURI);
                    } else if (sShareType.equalsIgnoreCase(videoType)) {
                        displayVideo(cameraFileURI);
                    }
                    simageuri = cameraFileURI.toString();
                } else {
                    CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.failedtopickupcontentfromgallary));
                }
                break;

            case CAMERA_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (cameraFileURI != null) {
                        simageuri = cameraFileURI.getPath();
                        displayImage(cameraFileURI);
                    } else {
                        CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.failedtocaptureimage));
                    }
                } else {
                    CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.failedtocaptureimage));
                }
                break;
            default:
                break;
        }

    }

    private void displayImage(Uri imageUri) {
        imgPreview.setVisibility(View.VISIBLE);
        videoPreview.setVisibility(View.GONE);
        Picasso.with(this).load(imageUri).into(imgPreview);
    }

    private void displayVideo(Uri videoUri) {
        videoPreview.setVisibility(View.VISIBLE);
        imgPreview.setVisibility(View.GONE);
        videoPreview.setVideoURI(videoUri);
        videoPreview.start();
    }

    /*To share image and video with text and hashtag on twitter wall(Code Tested/working)*/
    public void shareContenton_Twitter(Uri imageUri, String msg, String type, String packageName, String sharing_activityName) {

        try {
            if (imageUri != null && !imageUri.equals(Uri.EMPTY)) {
                List<Intent> targetShareIntents = new ArrayList<Intent>();
                if (CGlobal_lib.getInstance(MainActivity.this).appInstalledOrNot(packageName)) {

                    if (CGlobal_lib.getInstance(MainActivity.this).GetAuthority(MainActivity.this) == null) {
                        CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.somthingwentwrong));
                        return;
                    }

                    Log.d(TAG, getResources().getString(R.string.appinstalled));
                    PackageManager packageManager = context.getPackageManager();
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType(type);
                    List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(sendIntent, 0);
                    for (int j = 0; j < resolveInfoList.size(); j++) {
                        ResolveInfo resInfo = resolveInfoList.get(j);
                        String system_packageName = resInfo.activityInfo.packageName;
                        Log.i(TAG, system_packageName);
                        if (system_packageName.contains(packageName)) {
                            if (resInfo.activityInfo.name.equalsIgnoreCase(sharing_activityName)) {

                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName(system_packageName, resInfo.activityInfo.name));
                                intent.setAction(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_TEXT, msg);
                                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.sharecontentonsocialmedia));
                                intent.setType(type);//Set MIME Type
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.putExtra(Intent.EXTRA_STREAM, imageUri);// Pur Image to intent
                                intent.setPackage(system_packageName);
                                targetShareIntents.add(intent);

                                if (!targetShareIntents.isEmpty()) {
                                    Log.d(TAG, "Have Intent");
                                    Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Choose app to share");
                                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                                    context.startActivity(chooserIntent);
                                } else {
                                    Log.d(TAG, "Do not Have Intent");
                                }
                            }
                        }
                    }
                } else
                    CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.appnotinstalled));
            } else {
                CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.nocontenttoshare));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*To share image and video without text and hashtag on Facebook(Working/Tested code)*/
    public void shareContent_on_Facebook_native(Uri imageUri, String msg, String type, String packageName, String sharing_activityName) {

        try {
            if (imageUri != null && !imageUri.equals(Uri.EMPTY)) {
                List<Intent> targetShareIntents = new ArrayList<Intent>();
                if (CGlobal_lib.getInstance(MainActivity.this).appInstalledOrNot(packageName)) {
                    Log.d(TAG, getResources().getString(R.string.appinstalled));

                    if (CGlobal_lib.getInstance(MainActivity.this).GetAuthority(MainActivity.this) == null) {
                        CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.somthingwentwrong));
                        return;
                    }


                    PackageManager packageManager = context.getPackageManager();
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType(type);

                    List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(sendIntent, 0);
                    for (int j = 0; j < resolveInfoList.size(); j++) {
                        ResolveInfo resInfo = resolveInfoList.get(j);
                        String system_packageName = resInfo.activityInfo.packageName;
                        Log.i(TAG, system_packageName);

                        if (system_packageName.contains(packageName)) {
                            if (resInfo.activityInfo.name.equalsIgnoreCase(sharing_activityName)) {

                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName(system_packageName, resInfo.activityInfo.name));
                                intent.setAction(Intent.ACTION_SEND);
                                intent.setType(type);//Set MIME Type
                                intent.putExtra(Intent.EXTRA_TEXT, msg);
                                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.sharecontentonsocialmedia));

                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.putExtra(Intent.EXTRA_STREAM, imageUri);// Pur Image to intent
                                intent.setPackage(system_packageName);
                                targetShareIntents.add(intent);

                                if (!targetShareIntents.isEmpty()) {
                                    Log.d(TAG, "Have Intent");
                                    Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Choose app to share");
                                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                                    context.startActivity(chooserIntent);
                                } else {
                                    Log.d(TAG, "Do not Have Intent");
                                }
                            }
                        }
                    }
                } else
                    CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.appnotinstalled));
            } else {
                CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.nocontenttoshare));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void shareImage_on_facebook_sdk(Uri imageUri, String message) {
        final InputStream imageStream;
        final Bitmap selectedImage;
        try {
            imageStream = context.getContentResolver().openInputStream(imageUri);
            selectedImage = BitmapFactory.decodeStream(imageStream);

            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(selectedImage)
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .setShareHashtag(new ShareHashtag.Builder().setHashtag(message).build())
                    .build();

            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
            MessageDialog.show((Activity) context, content);

            if (shareDialog.canShow(SharePhotoContent.class)) {
                shareDialog.show(content);
            } else {
                Toast.makeText(this.context, "Please install Facebook to share this content", Toast.LENGTH_SHORT).show();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void shareVideo_on_facebook_sdk(Uri imageUri, String message) {
        if (imageUri != null && !imageUri.equals(Uri.EMPTY)) {

            ShareVideo shareVideo = new ShareVideo.Builder()
                    .setLocalUrl(imageUri)
                    .build();

            ShareVideoContent content = new ShareVideoContent.Builder()
                    .setVideo(shareVideo)
                    .setShareHashtag(new ShareHashtag.Builder().setHashtag(message).build())
                    .build();

            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
            MessageDialog.show((Activity) context, content);

            if (shareDialog.canShow(ShareVideoContent.class)) {
                shareDialog.show(content);
            } else {
                CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.appnotinstalled));
            }
        } else {
            Toast.makeText(context, "Video is not selected", Toast.LENGTH_SHORT).show();
        }
    }

    /*To share image and video without text on Instagram*/
    private void shareContent_on_instgram(Uri imageUri, String message, String type, String packageName, String subPackageActivity) {
        try {
            if (imageUri != null && !imageUri.equals(Uri.EMPTY)) {
                if (CGlobal_lib.getInstance(MainActivity.this).appInstalledOrNot(packageName)) {

                    if (CGlobal_lib.getInstance(MainActivity.this).GetAuthority(MainActivity.this) == null) {
                        CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.somthingwentwrong));
                        return;
                    }


                    List<Intent> targetShareIntents = new ArrayList<Intent>();

                    PackageManager packageManager = context.getPackageManager();
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType(type);

                    List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(sendIntent, 0);
                    for (int j = 0; j < resolveInfoList.size(); j++) {
                        ResolveInfo resInfo = resolveInfoList.get(j);
                        String system_packageName = resInfo.activityInfo.packageName;
                        Log.i(TAG, system_packageName);

                        if (system_packageName.contains(packageName)) {

                            /*if (resInfo.activityInfo.name.equalsIgnoreCase("com.instagram.share.handleractivity.StoryShareHandlerActivity")) {*/

                            Intent intent = new Intent();
                            if (!TextUtils.isEmpty(subPackageActivity))
                                intent.setComponent(new ComponentName(system_packageName, subPackageActivity));
                            intent.setAction(Intent.ACTION_SEND);
                            intent.setType(type);//Set MIME Type
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(Intent.EXTRA_STREAM, imageUri);// Pur Image to intent
                            intent.putExtra(Intent.EXTRA_TEXT, message);
                            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.sharecontentonsocialmedia));

                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setPackage(system_packageName);
                            targetShareIntents.add(intent);

                            if (!targetShareIntents.isEmpty()) {
                                Log.d(TAG, "Have Intent");
                                Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Choose app to share");
                                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                                context.startActivity(chooserIntent);
                            } else {
                                Log.d(TAG, "Do not Have Intent");
                            }
                            break;
                            // }
                        }

                    }

                } else {
                    CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.appnotinstalled));
                }
            } else {
                CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.nocontenttoshare));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*To share image and video with text and hashtag on Whatsapp (Working/Tested code)*/
    public void shareContent_on_Whatsapp(Uri imageUri, String phone_number, String message, String type, String packageName, String sharing_activityname) {
        try {
            if (imageUri != null && !imageUri.equals(Uri.EMPTY)) {
                if (CGlobal_lib.getInstance(MainActivity.this).appInstalledOrNot(packageName)) {

                    if (CGlobal_lib.getInstance(MainActivity.this).GetAuthority(MainActivity.this) == null) {
                        CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.somthingwentwrong));
                        return;
                    }

                    String toNumber = phone_number;
                    Intent sendIntent = new Intent("android.intent.action.MAIN");
                    sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    sendIntent.putExtra("jid", toNumber + sharing_activityname);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, message);

                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setPackage(packageName);
                    sendIntent.setType(type);
                    context.startActivity(sendIntent);
                } else {
                    CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.appnotinstalled));
                }
            } else {
                CGlobal_lib.getInstance(MainActivity.this).showMessage(getResources().getString(R.string.nocontenttoshare));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
