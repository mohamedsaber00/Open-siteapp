package com.msaber.openapiapp.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import com.msaber.openapiapp.R
import com.msaber.openapiapp.ui.*
import com.msaber.openapiapp.util.constant.Constants
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ForgetPasswordFragment : BaseAuthFragment() {

    lateinit var webView: WebView

    lateinit var stateChangeListener: DataStateChangeListener

    val webInteractionCallback: WebAppInterface.OnWebInterfaceCallback =
        object : WebAppInterface.OnWebInterfaceCallback {
            override fun onSuccess(email: String) {
                Log.d(TAG, "onSuccess: a reset link sent $email")
                onPasswordResetLinkSent()
            }

            override fun onError(errorMessage: String) {
                Log.e(TAG, "onError: $errorMessage")

                val datatState =
                    DataState.error<Any>(response = (Response(errorMessage, ResponseType.Dialog())))

                stateChangeListener.onDataStateChange(dataState = datatState)
            }

            override fun onLoading(isLoading: Boolean) {
                Log.d(TAG, "onLoading: ")
                GlobalScope.launch(Main) {
                    stateChangeListener.onDataStateChange(
                        DataState.loading(
                            isLoading = isLoading,
                            cachedData = null
                        )
                    )
                }
            }

        }

    private fun onPasswordResetLinkSent() {
        GlobalScope.launch(Main) {
            parent_view.removeView(webView)
            webView.destroy()

            val animation = TranslateAnimation(password_reset_done_container.width.toFloat(),
            0f,
            0f,
            0f)
            animation.duration = 500
            password_reset_done_container.startAnimation(animation)
            password_reset_done_container.visibility = View.VISIBLE
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forget_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ${viewModel.hashCode()}")
        webView = view.findViewById(R.id.webview)
        loadWebView()

        return_to_launcher_fragment.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadWebView() {
        stateChangeListener.onDataStateChange(
            DataState.loading(
                isLoading = true,
                cachedData = null
            )
        )

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                stateChangeListener.onDataStateChange(
                    DataState.loading(
                        isLoading = false,
                        cachedData = null
                    )
                )
            }
        }

        webView.loadUrl(Constants.PASSWORD_RESER_URL)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(
            WebAppInterface(webInteractionCallback),
            "AndroidTextListener"
        )

    }

    class WebAppInterface constructor(private val callback: OnWebInterfaceCallback) {

        private val TAG = "WebAppInterface"

        @JavascriptInterface
        fun onSuccess(email: String) {
            callback.onSuccess(email)
        }

        @JavascriptInterface
        fun onError(errorMessage: String) {
            callback.onError(errorMessage)
        }

        @JavascriptInterface
        fun onLoading(isLoading: Boolean) {
            callback.onLoading(isLoading)
        }

        interface OnWebInterfaceCallback {
            fun onSuccess(email: String)
            fun onError(errorMessage: String)
            fun onLoading(isLoading: Boolean)
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "onAttach: $context must nt DataStateChangeListener")
        }
    }
}