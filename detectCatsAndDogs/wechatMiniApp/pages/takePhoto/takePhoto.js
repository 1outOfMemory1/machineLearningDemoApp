// pages/takePhoto/takePhoto.js
Page({

  /**
   * Page initial data
   */
  data: {

  },

  /**
   * Lifecycle function--Called when page load
   */
  record(){
    this.data.cameraContext = wx.createCameraContext()
    this.data.cameraContext.takePhoto({
      quality:"high", //高质量的图片
      success: res => {
        //res.tempImagePath照片文件在手机内的的临时路径
        let tempImagePath = res.tempImagePath
        wx.saveFile({
          tempFilePath: tempImagePath,
          success: function (res) {
            //返回保存时的临时路径 res.savedFilePath
            const savedFilePath = res.savedFilePath
            // 保存到本地相册
            wx.saveImageToPhotosAlbum({
              filePath: savedFilePath,
            })
          },
          //保存失败回调（比如内存不足）
          fail: console.log
        })
      }
    })
  },

  onLoad: function (options) {

  },

  /**
   * Lifecycle function--Called when page is initially rendered
   */
  onReady: function () {

  },

  /**
   * Lifecycle function--Called when page show
   */
  onShow: function () {

  },

  /**
   * Lifecycle function--Called when page hide
   */
  onHide: function () {

  },

  /**
   * Lifecycle function--Called when page unload
   */
  onUnload: function () {

  },

  /**
   * Page event handler function--Called when user drop down
   */
  onPullDownRefresh: function () {

  },

  /**
   * Called when page reach bottom
   */
  onReachBottom: function () {

  },

  /**
   * Called when user click on the top right corner to share
   */
  onShareAppMessage: function () {

  }
})