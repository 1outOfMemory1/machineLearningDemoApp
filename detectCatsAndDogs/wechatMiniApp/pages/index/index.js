// index.js
// 获取应用实例
const app = getApp()

Page({
  data: {
    motto: 'Hello World',
    userInfo: {},
    hasUserInfo: false,
    canIUse: wx.canIUse('button.open-type.getUserInfo')
  },
  // 事件处理函数
  takePhoto(){	
    wx.navigateTo({
      url: '/pages/takePhoto/takePhoto',	//跳转到自定义的一个拍照页面
    })
  },

  onLoad() {
   

  },
  
})
