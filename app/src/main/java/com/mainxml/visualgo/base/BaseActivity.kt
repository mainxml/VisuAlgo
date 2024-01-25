package com.mainxml.visualgo.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.mainxml.visualgo.BR
import java.lang.reflect.ParameterizedType

/**
 * @author zcp
 */
abstract class BaseActivity<VM: BaseViewModel, BD: ViewDataBinding> : AppCompatActivity() {

    /** ViewModel */
    protected lateinit var vm: VM

    /** ViewDataBinding */
    protected lateinit var binging: BD

    /**
     * 视图id
     */
    abstract val layoutId: Int

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        initViewDataBinding()
    }

    /**
     * 初始化泛型ViewModel
     *
     * 例如实际类是：MainActivity<MainVM, ActivityMainBinding>，
     * 先获取到带泛型的超类 BaseActivity<MainVM, ActivityMainBinding>，
     * 再获取超类泛型列表的第一个参数得到实际VM类型。
     */
    private fun initViewModel() {
        val parameterizedType = javaClass.genericSuperclass as ParameterizedType
        @Suppress("UNCHECKED_CAST")
        val vmClass = parameterizedType.actualTypeArguments[0] as Class<VM>
        vm = ViewModelProvider(this)[vmClass]

        lifecycle.addObserver(vm)
    }

    /**
     * 初始化泛型ViewDataBinding
     */
    private fun initViewDataBinding() {
        binging = DataBindingUtil.setContentView(this, layoutId)
        binging.lifecycleOwner = this
        binging.setVariable(BR.vm, vm)
    }
}