package xyz.hisname.fireflyiii.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.viewmodel.AuthViewModel
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class LoginFragment: Fragment() {

    private val baseUrl by lazy { AppPref(requireContext()).getBaseUrl() }
    private lateinit var fireflyUrl: String
    private val fireflyId: String by lazy { AppPref(requireContext()).getClientId() }
    private val fireflySecretKey: String by lazy { AppPref(requireContext()).getSecretKey() }
    private val model by lazy { getViewModel(AuthViewModel::class.java) }
    private val progressOverlay by lazy { requireActivity().findViewById<View>(R.id.progress_overlay) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_login, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val argument = arguments?.getString("ACTION")
        when {
            Objects.equals(argument, "LOGIN") -> {
                firefly_url_edittext.setText(baseUrl)
                firefly_id_edittext.setText(fireflyId)
                firefly_secret_edittext.setText(fireflySecretKey)
                getAccessCode()
            }
            Objects.equals(argument, "REFRESH_TOKEN") -> {
                refreshToken()
            }
        }
    }

    private fun getAccessCode(){
        firefly_submit_button.setOnClickListener {
            RetrofitBuilder.destroyInstance()
            hideKeyboard()
            fireflyUrl = firefly_url_edittext.getString()
            val fireflyId = firefly_id_edittext.getString()
            val fireflySecretKey =  firefly_secret_edittext.getString()
            if(fireflyUrl.isEmpty() or fireflyId.isEmpty() or fireflySecretKey.isEmpty()){
                when {
                    fireflyUrl.isEmpty() -> firefly_url_edittext.error = resources.getString(R.string.required_field)
                    fireflyId.isEmpty() -> firefly_id_edittext.error = resources.getString(R.string.required_field)
                    else -> firefly_secret_edittext.error = resources.getString(R.string.required_field)
                }
            } else {
                AppPref(requireContext()).setBaseUrl(fireflyUrl)
                AppPref(requireContext()).setClientId(fireflyId)
                AppPref(requireContext()).setSecretKey(fireflySecretKey)
                if(!fireflyUrl.startsWith("http")){
                    fireflyUrl = "https://$fireflyUrl"
                }
                val builder = CustomTabsIntent.Builder()
                builder.build().launchUrl(requireContext(), ("$fireflyUrl/oauth/authorize?client_id=$fireflyId" +
                        "&redirect_uri=${Constants.REDIRECT_URI}&scope=&response_type=code&state=").toUri())
            }
        }
    }

    private fun refreshToken(){
        /* Bug: Currently there is a bug where if a user upgrades Firefly III, we have to request
            token again. Is it really a bug? Anyway, the client does not play well in this scenario.
            Currently we only checked if the refresh token is `old`
        */
        rootLayout.isVisible = false
        toastInfo("Refreshing your access token...", Toast.LENGTH_LONG)
        model.getRefreshToken(baseUrl, AppPref(requireContext()).getRefreshToken(),
                fireflySecretKey, AppPref(requireContext()).getClientId())
                .observe(this, Observer {
                    if(it.getError() == null) {
                        startHomeIntent()
                    } else {
                        ProgressBar.animateView(progressOverlay, View.GONE, 0.toFloat(), 200)
                        val error = it.getError()
                        if(error != null){
                            toastInfo(error.localizedMessage)
                        }
                    }
                })
    }

    private fun startHomeIntent(){
        if(AppPref(requireContext()).isTransactionPersistent()){
            NotificationUtils(requireContext()).showTransactionPersistentNotification()
        }
        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            delay(1234) //heh
            startActivity(Intent(requireContext(), HomeActivity::class.java))
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val uri = requireActivity().intent.data
        if(uri != null && uri.toString().startsWith(Constants.REDIRECT_URI)){
            val code = uri.getQueryParameter("code")
            if(code != null) {
                val baseUrl= AppPref(requireContext()).getBaseUrl()
                val fireflyId = AppPref(requireContext()).getClientId()
                val fireflySecretKey= AppPref(requireContext()).getSecretKey()
                ProgressBar.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
                model.getAccessToken(baseUrl, code,fireflyId,fireflySecretKey).observe(this, Observer {
                    ProgressBar.animateView(progressOverlay, View.GONE, 0f, 200)
                    if(it.getResponse() != null) {
                        toastSuccess(resources.getString(R.string.welcome))
                        AppPref(requireContext()).setAuthMethod("oauth")
                        val frameLayout = requireActivity().findViewById<FrameLayout>(R.id.bigger_fragment_container)
                        frameLayout.removeAllViews()
                        val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to it.getResponse()?.access_token)
                        requireActivity().supportFragmentManager.beginTransaction()
                                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                .add(R.id.bigger_fragment_container, OnboardingFragment().apply { arguments = bundle })
                                .commit()
                    } else {
                        val error = it.getError()
                        if(error == null){
                            toastInfo("There was an error communicating with your server")
                        } else {
                            toastError(error.localizedMessage)
                        }
                    }
                })
            } else {
                showDialog()
            }
        }
    }

    private fun showDialog(){
        ProgressBar.animateView(progressOverlay, View.GONE, 0.toFloat(), 200)
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.authentication_failed))
                .setMessage(resources.getString(R.string.authentication_failed_message, Constants.REDIRECT_URI))
                .setPositiveButton("OK") { _, _ ->
                }
                .create()
                .show()
    }
}