package com.hooloovoochimico.badpiccollage

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.manzo.slang.extensions.getAppVersion
import com.manzo.slang.extensions.start
import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        actionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val versionText = "v.${getAppVersion()}"
        version_tv.text = versionText

        /*buy_me_coffee_button.setOnClickListener {
            showBrowser("https://ko-fi.com/angelomoroni")
        }*/

        credits_button.setOnClickListener {
            showBrowser("http://bit.ly/bpc_credits")
        }

        github_button.setOnClickListener {
            showBrowser("http://bit.ly/bpc_github")
        }

        license_button.setOnClickListener {
            showBrowser("http://bit.ly/bpc_licnse")
        }

    }

    fun showBrowser(url:String?) =
        url?.let {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }.start(this)
        }?: false

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if(item?.itemId == android.R.id.home ){
            super.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }


}

