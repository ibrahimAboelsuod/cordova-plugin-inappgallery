package com.cordova.plugin.inappgalleryplugin;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InAppGalleryPlugin extends CordovaPlugin {
    private static final String TAG = "InAppGalleryPlugin";

    public void initialize(CordovaInterface cordova,
                           CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action,
                           JSONArray args,
                           final CallbackContext callbackContext) {
        if (!action.equals("getMedia")) {
            callbackContext.error("\"" + action + "\" is not a recognized action.");
            return false;
        }


        try {
            JSONObject rawOptions = args.getJSONObject(0);
            this.getMedia(new MediaOptions(rawOptions.getInt("pageIndex"), rawOptions.getInt("pageSize"),
                    rawOptions.getBoolean("includeVideos")), callbackContext);
            return true;

        } catch (JSONException e) {
            callbackContext.error("Error encountered: " + e.getMessage());
            return false;
        }
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
                        MediaStore.MediaColumns.DATA
                };

                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                if (options.includeVideos) {
                    selection += " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                            + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                }

                String query = MediaStore.MediaColumns.DATE_TAKEN + " DESC LIMIT " + options.pageSize + " OFFSET "
                        + options.pageIndex;

                try (Cursor cursor = this.runnableContext.getContentResolver().query(MediaStore.Files.getContentUri("external"),
                        projection, selection, null, query)) {
                    // Cache column indices.
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                    int typeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);
                    int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

                    JSONArray mediaList = new JSONArray();

                    while (cursor.moveToNext()) {
                        JSONObject media = new JSONObject();
                        media.put("id", cursor.getLong(idColumn));
                        media.put("name", cursor.getString(nameColumn));
                        media.put("type", cursor.getString(typeColumn));
                        media.put("url", "file:/" + cursor.getString(dataColumn));

                        mediaList.put(media);
                    }

                    JSONObject response = new JSONObject();
                    response.put("data", mediaList);
                    callbackContext.success(response);

                } catch (Exception e) {
                    callbackContext.error("Error encountered: " + e.getMessage());
                }
            }
        });
    }

    private Context getContext() {
        return this.cordova.getActivity().getApplicationContext();
    }
}