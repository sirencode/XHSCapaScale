package com.yan.xhscapascale

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.View.inflate
import android.view.animation.OvershootInterpolator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv: RecyclerView = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder =
                object : RecyclerView.ViewHolder(layoutInflater.inflate(R.layout.item_main, p0, false)) {}

            override fun getItemCount(): Int = 1

            override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
                val csv = p0.itemView.findViewById<CapaScaleView>(R.id.csv)
                val tv = p0.itemView.findViewById<View>(R.id.tv)
                tv.post {
//                    onCapaSet(csv, tv)
                    csv.setBottomView(tv)
                }
            }
        }
    }


}
