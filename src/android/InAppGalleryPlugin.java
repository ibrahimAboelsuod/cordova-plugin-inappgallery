package com.cordova.plugin.inappgalleryplugin;

import android.content.Context;
import android.database.Cursor;
import android.content.ContentUris;
import android.provider.MediaStore;
import android.util.Log;

import java.lang.Exception;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

public class InAppGalleryPlugin extends CordovaPlugin {
    private static final String TAG = "InAppGalleryPlugin";

    public void initialize(CordovaInterface cordova,
                           CordovaWebView webView) {
        super.initialize(cordova, webView);

        Log.d(TAG, "Initializing MyCordovaPlugin");
    }

    @Override
    public boolean execute(String action,
                           JSONArray args,
                           final CallbackContext callbackContext) {
        if (!action.equals("getMedia")) {
            callbackContext.error("\"" + action + "\" is not a recognized action.");
            return false;
        }
        String message;
        String duration;
        try {
            JSONObject rawOptions = args.getJSONObject(0);
            MediaOptions mediaOptions = new MediaOptions(rawOptions.getInt("pageIndex"), rawOptions.getInt("pageSize"),
                    rawOptions.getBoolean("includeVideos"));

            this.getMedia(mediaOptions, callbackContext);

        } catch (JSONException e) {
            callbackContext.error("Error encountered: " + e.getMessage());
            return false;
        }

        // Send a positive result to the callbackContext
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
        callbackContext.sendPluginResult(pluginResult);
        return true;
    }

    public void getMedia(final MediaOptions options,
                         final CallbackContext callbackContext) {
        final Context context = this.getContext();
        cordova.getThreadPool().execute(new Runnable() {
            Context runnableContext = context;
            
            public void run() {
                String[] projection = new String[]{
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.DATE_TAKEN,
                        MediaStore.MediaColumns.MIME_TYPE,
                        MediaStore.MediaColumns.SIZE,
                        MediaStore.MediaColumns.DATA
                };

                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                if (options.includeVideos) {
                    selection += " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                            + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                }

                String sortOrder = MediaStore.MediaColumns.DATE_TAKEN + " DESC LIMIT " + options.pageSize + " OFFSET "
                        + options.pageIndex;

                try (Cursor cursor = this.runnableContext.getContentResolver().query(MediaStore.Files.getContentUri("external"),
                        projection, selection, null, sortOrder)) {
                    // Cache column indices.
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                    int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

                    JSONArray mediaList = new JSONArray();

                    while (cursor.moveToNext()) {
                        // Get values of columns for a given video.
                        long id = cursor.getLong(idColumn);
                        String name = cursor.getString(nameColumn);
                        String data = cursor.getString(dataColumn);

                        Log.d(TAG, name);
                        JSONObject media = new JSONObject();
                        media.put("url", data);
                        media.put("name", name);

                        mediaList.put(media);
                    }
                    
                    Log.d(TAG, String.valueOf(mediaList));

                    JSONObject response = new JSONObject();
                    response.put("data", mediaList);
                    callbackContext.success(response);

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private Context getContext() {
        return this.cordova.getActivity().getApplicationContext();
    }
}