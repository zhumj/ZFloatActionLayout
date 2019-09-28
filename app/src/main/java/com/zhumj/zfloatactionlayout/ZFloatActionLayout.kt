package com.zhumj.zfloatactionlayout

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import android.os.CountDownTimer




class ZFloatActionLayout(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    //是否开启X轴吸边
    private var isAdsorbX: Boolean = false
    //X轴吸边之后与边缘的距离
    private var adsorbXMargin: Float = 0f
    //是否开启Y轴吸边
    private var isAdsorbY: Boolean = false
    //Y轴吸边之后与边缘的距离
    private var adsorbYMargin: Float = 0f
    //是否开启半隐藏
    private var isHalfHidden: Boolean = false
    //显示多少时间后进行隐藏（单位：毫秒）
    private var displayDuration: Int = 10*1000
    //计时间隔(单位：毫秒)
    private var displayStep: Int = 1000

    private var parentHeight: Int = 0//父布局高度
    private var parentWidth: Int = 0//父布局宽度
    private var lastX: Int = 0
    private var lastY: Int = 0

    private var isDrag: Boolean = false//是否滑动
    private var isHided: Boolean = false//记录是否已半隐藏
    private var isHiding: Boolean = false//正在隐藏
    private var isShowing: Boolean = false//正在显示
    private var isHideX: Boolean = false//X轴半隐藏
    private var isHideY: Boolean = false//Y轴半隐藏

    private var timer: CountDownTimer? = null

    init {
        val array = context?.obtainStyledAttributes(attrs, R.styleable.ZFloatActionLayout)
        isAdsorbX = (array?.getBoolean(R.styleable.ZFloatActionLayout_ZFloatActionLayout_isAdsorbX, isAdsorbX)) ?: isAdsorbX
        adsorbXMargin = (array?.getDimension(R.styleable.ZFloatActionLayout_ZFloatActionLayout_adsorbXMargin, adsorbXMargin)) ?: adsorbXMargin
        isAdsorbY = (array?.getBoolean(R.styleable.ZFloatActionLayout_ZFloatActionLayout_isAdsorbY, isAdsorbY)) ?: isAdsorbY
        adsorbYMargin = (array?.getDimension(R.styleable.ZFloatActionLayout_ZFloatActionLayout_adsorbYMargin, adsorbYMargin)) ?: adsorbYMargin
        isHalfHidden = (array?.getBoolean(R.styleable.ZFloatActionLayout_ZFloatActionLayout_isHalfHidden, isHalfHidden)) ?: isHalfHidden
        displayDuration = (array?.getInt(R.styleable.ZFloatActionLayout_ZFloatActionLayout_displayDuration, displayDuration)) ?: displayDuration
        displayStep = (array?.getInt(R.styleable.ZFloatActionLayout_ZFloatActionLayout_displayStep, displayStep)) ?: displayStep
        array?.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(b: Boolean, i: Int, i1: Int, i2: Int, i3: Int) {
        val view = getChildAt(0)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val rawX = ev.rawX.toInt()
        val rawY = ev.rawY.toInt()

        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                if (isHided) {//半隐藏状态
                    isPressed = false//默认是点击事件
                    isDrag = false//默认是非拖动而是点击事件
                    return true
                } else {
                    isPressed = true//默认是点击事件
                    isDrag = false//默认是非拖动而是点击事件

                    parent.requestDisallowInterceptTouchEvent(true)//父布局不要拦截子布局的监听
                    lastX = rawX
                    lastY = rawY
                    if (parent != null) {
                        val zParent = parent as ViewGroup
                        parentHeight = zParent.height
                        parentWidth = zParent.width
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isHided) {
                    val hasParent = parentHeight > 0 && parentWidth > 0//只有父布局存在才可以拖动

                    val dx = rawX - lastX
                    val dy = rawY - lastY
                    //这里修复一些华为手机无法触发点击事件
                    val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toInt()
                    isDrag = distance > 0//位移大于0说明拖动了

                    if (hasParent && isDrag) {
                        timer?.cancel()
                        var x = x + dx
                        var y = y + dy
                        //检测是否到达边缘 左上右下
                        x = if (x < 0) 0f else if (x > parentWidth - width) (parentWidth - width).toFloat() else x
                        y = if (y < 0) 0f else if (y > parentHeight - height) (parentHeight - height).toFloat() else y
                        setX(x)
                        setY(y)
                        lastX = rawX
                        lastY = rawY
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!isHided) {
                    //判断是否为点击事件
                    val hasParent = parentHeight > 0 && parentWidth > 0//只有父布局存在才可以拖动
                    //自动贴边
                    if (hasParent && isDrag && (isAdsorbX || isAdsorbY)) {
                        moveToEdge()
                    } else {//如果没有设置吸边，但设置了可以半隐藏，则X轴或者Y轴到达边缘的时候自动半隐藏
                        if (isHalfHidden) {
                            val dx = rawX - lastX
                            val dy = rawY - lastY
                            val x = x + dx
                            val y = y + dy
                            //检测是否到达边缘 左上右下
                            isHideX = x <= 0 || x >= parentWidth - width
                            isHideY = y <= 0 || y >= parentHeight - height
                            if (isHideX || isHideY) {
                                startHalfHidden()
                            }
                        }
                    }

                    //如果是拖动状态下即非点击按压事件
                    isPressed = !isDrag
                } else {
                    stopHalfHidden()
                }
            }
        }

        //如果不是拖拽，那么就不消费这个事件，以免影响点击事件的处理
        //拖拽事件要自己消费
        return isDrag || super.dispatchTouchEvent(ev)
    }

    //倒计时：倒计时 displayDuration 后启动半隐藏
    private fun startTimer() {
        if (displayDuration <= 0) {
            startHalfHidden()
        } else {
            if (timer == null) {
                timer = object : CountDownTimer(displayDuration.toLong(), displayStep.toLong()) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        startHalfHidden()
                    }
                }
            }
            timer?.start()
        }
    }

    //自动贴边
    private fun moveToEdge() {
        val adsorbAnim = this.animate()
            .setInterpolator(BounceInterpolator())
            .setDuration(500)
            .setStartDelay(0)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    if (isHalfHidden) {
                        isHideX = isAdsorbX
                        isHideY = isAdsorbY
                        startTimer()
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationStart(animation: Animator?) {}
            })
        //X轴自动贴边
        if (isAdsorbX) {
            val centerX =  parentWidth/ 2
            adsorbAnim.x(if (lastX < centerX) {//向左贴边
                adsorbXMargin
            } else {//向右贴边
                parentWidth - width - adsorbXMargin
            })
        }
        //Y轴自动贴边
        if (isAdsorbY) {
            val centerY =  parentHeight/ 2
            adsorbAnim.y(if (lastY < centerY){//向上贴边
                adsorbYMargin
            } else {//向下贴边
                parentHeight - height - adsorbYMargin
            })
        }
        adsorbAnim.start()
    }

    //启动半隐藏
    private fun startHalfHidden() {
        if (isShowing || isHiding || !isHalfHidden) return
        val halfHiddenAnim = this.animate()
            .setInterpolator(LinearInterpolator())
            .setDuration(200)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    isHided = true
                    isHiding = false
                }

                override fun onAnimationCancel(animation: Animator?) {
                    isHided = false
                    isHiding = false
                }

                override fun onAnimationStart(animation: Animator?) {
                    isHiding = true
                }
            })
        //X轴自动贴边
        if (isHideX) {
            val centerX =  parentWidth/ 2
            halfHiddenAnim.x(if (lastX < centerX) {//左贴边
                -(width/2).toFloat()
            } else {//右贴边
                (parentWidth - width/2).toFloat()
            })
        }
        //Y轴自动贴边
        if (isHideY) {
            val centerY =  parentHeight/ 2
            halfHiddenAnim.y(if (lastY < centerY){//上贴边
                -(height/2).toFloat()
            } else {//下贴边
                (parentHeight - height/2).toFloat()
            })
        }
        halfHiddenAnim.start()
    }

    //结束半隐藏
    private fun stopHalfHidden() {
        if (isShowing || isHiding) return
        val halfHiddenAnim = this.animate()
            .setInterpolator(LinearInterpolator())
            .setDuration(200)
            .setStartDelay(0)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    isHided = false
                    isShowing = false
                    startTimer()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    isHided = false
                    isHiding = false
                }

                override fun onAnimationStart(animation: Animator?) {
                    isShowing = true
                }
            })
        //X轴自动贴边
        if (isHideX) {
            val centerX =  parentWidth/ 2
            halfHiddenAnim.x(if (lastX < centerX) {//左贴边
                adsorbXMargin
            } else {//右贴边
                parentWidth - width - adsorbXMargin
            })
        }
        //Y轴自动贴边
        if (isHideY) {
            val centerY =  parentHeight/ 2
            halfHiddenAnim.y(if (lastY < centerY){//上贴边
                adsorbYMargin
            } else {//下贴边
                parentHeight - height - adsorbYMargin
            })
        }
        halfHiddenAnim.start()
    }

}