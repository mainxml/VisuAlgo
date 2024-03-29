package com.mainxml.visualgo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mainxml.visualgo.base.BaseActivity
import com.mainxml.visualgo.base.BaseViewModel
import com.mainxml.visualgo.databinding.ActivityBinaryTreeBinding

/**
 * @author zcp
 */
class BinaryTreeActivity : BaseActivity<BaseViewModel, ActivityBinaryTreeBinding>() {

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, BinaryTreeActivity::class.java))
        }
    }

    override fun getLayoutId() = R.layout.activity_binary_tree

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binaryTreeArray = intArrayOf(1, 2, 3)
        binding.binaryTree.setTreeArray(binaryTreeArray)

        binding.binaryTree.setOnClickListener {
            binding.binaryTree.add()
        }
    }
}