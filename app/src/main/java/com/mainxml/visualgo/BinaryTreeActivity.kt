package com.mainxml.visualgo

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.mainxml.visualgo.R
import com.mainxml.visualgo.base.BaseActivity
import com.mainxml.visualgo.base.BaseViewModel
import com.mainxml.visualgo.widget.BinaryTreeView

/**
 * @author zcp
 */
class BinaryTreeActivity : BaseActivity<BaseViewModel, ViewDataBinding>() {

    override fun getLayoutId(): Int {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val binaryTreeArray = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        //val binaryTreeView = findViewById<BinaryTreeView>(R.id.binaryTreeView)
        //binaryTreeView.setTreeArray(binaryTreeArray)
    }
}