// Empty constructor
function InAppGalleryPlugin() {}

// The function that passes work along to native shells
InAppGalleryPlugin.prototype.getMedia = function (
  rawOptions,
  successCallback,
  errorCallback
) {
  let options = {};
  options.pageIndex = rawOptions.pageIndex || 0;
  options.pageSize = rawOptions.pageSize || 20;
  options.includeVideos = rawOptions.includeVideos || false;

  cordova.exec(successCallback, errorCallback, 'InAppGalleryPlugin', 'getMedia', [
    options,
  ]);
};

// Installation constructor that binds InAppGalleryPlugin to window
InAppGalleryPlugin.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.inAppGalleryPlugin = new InAppGalleryPlugin();
  return window.plugins.inAppGalleryPlugin;
};
cordova.addConstructor(InAppGalleryPlugin.install);
