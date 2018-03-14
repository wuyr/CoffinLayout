# CoffinLayout
## 仿燃兔APP的游戏详情界面
## 详细见: http://blog.csdn.net/u011387817/article/details/79552699
## 只需一个CoffinLayout
``` 
 <com.test.viewtest.views.CoffinLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    app:bottom_bar="@layout/bottom_bar"
    app:header_view="@layout/header_view"
    app:lid_elevation="8dp"
    app:lid_offset="240dp"
    app:residual_view="@layout/residual_view"
    app:top_bar="@layout/top_bar"
    app:trigger_open_offset="100dp">
 
    <YourBottomView />
 
    <YourLidView />

 </com.test.viewtest.views.CoffinLayout>

``` 
![preview](https://github.com/wuyr/CoffinLayout/raw/master/preview.gif)
