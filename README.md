# ZFloatActionLayout

 自定义 随意拖动 + 吸边 + 半隐藏 布局，继承自 RelativeLayout ，可装载任何子控件
 
 ![image](./gif/11111.gif) 
 
 属性：
 
  <?xml version="1.0" encoding="utf-8"?>
  <resources>

      <declare-styleable name="ZFloatActionLayout">
          //是否开启X轴吸边
          <attr name="ZFloatActionLayout_isAdsorbX" format="boolean"/>
          //X轴吸边之后与边缘的距离
          <attr name="ZFloatActionLayout_adsorbXMargin" format="dimension"/>
          //是否开启Y轴吸边
          <attr name="ZFloatActionLayout_isAdsorbY" format="boolean"/>
          //Y轴吸边之后与边缘的距离
          <attr name="ZFloatActionLayout_adsorbYMargin" format="dimension"/>
          //是否开启半隐藏
          <attr name="ZFloatActionLayout_isHalfHidden" format="boolean"/>
          //显示多少时间后进行隐藏（单位：毫秒）
          <attr name="ZFloatActionLayout_displayDuration" format="integer"/>
          //计时间隔（单位：毫秒）
          <attr name="ZFloatActionLayout_displayStep" format="integer"/>
      </declare-styleable>

  </resources>

 使用：
   
    <com.zhumj.zfloatactionlayout.ZFloatActionLayout
           android:layout_width="90dp"
           android:layout_height="90dp"
           android:background="@color/colorPrimary"
           app:ZFloatActionLayout_isAdsorbX="true"
           app:ZFloatActionLayout_adsorbXMargin="12dp"
           app:ZFloatActionLayout_isAdsorbY="true"
           app:ZFloatActionLayout_adsorbYMargin="12dp"
           app:ZFloatActionLayout_isHalfHidden="true"
           app:ZFloatActionLayout_displayDuration="3000"
           app:ZFloatActionLayout_displayStep="100">

           <Button
                   android:layout_width="match_parent"
                   android:layout_height="match_parent"
                   android:onClick="onButtonClick"
                   android:text="看看能不能拖动"/>

    </com.zhumj.zfloatactionlayout.ZFloatActionLayout>
