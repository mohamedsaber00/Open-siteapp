package com.msaber.openapiapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.lifecycle.Observer
import com.msaber.openapiapp.R
import com.msaber.openapiapp.ui.BaseActivity
import com.msaber.openapiapp.ui.auth.AuthActivity
import com.msaber.openapiapp.ui.hide
import com.msaber.openapiapp.ui.visible
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_main)
        subscribeObservers()
    }

    fun subscribeObservers() {
        sessionManager.cachedToken.observe(this, Observer { authtoken ->
            Log.d(TAG, "subscribeObservers: AuthToken : $authtoken")
            if (authtoken == null || authtoken.account_pk == -1 || authtoken.token == null) {
                navAuthActivity()
            }
        })
    }

    private fun navAuthActivity() {
        val intent = Intent(this,AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun displayProgressBar(bool: Boolean) {
        if (bool) {
            progress_bar.visible()
        } else {
            progress_bar.hide()
        }
    }
}