package br.com.mobilete

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.facebook.login.LoginManager
import com.google.firebase.auth.FacebookAuthProvider
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    companion object {
        const val TAG: String = "LoginFacebook"
        const val USUARIO: String = "Usuario"
    }

    private var mCallbackManager: CallbackManager? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        setContentView(R.layout.activity_login)


        mAuth = FirebaseAuth.getInstance() // Inicializa o Firebase Auth
        mCallbackManager = CallbackManager.Factory.create() // Inicio o CallBack

        btnFbLogin.setOnClickListener {
            progressWheel(true)
            logaFb()
        }

        btnLogin.setOnClickListener{
            //progressWheel(true)
        }

        txtCadastrar.setOnClickListener{
            progressWheel(true)
            val goToCadastro= Intent(this, CadastroUsuarioActivity::class.java)
            startActivity(goToCadastro)
            finish()
        }
    }

    public override fun onStart() {
        super.onStart()
        //Se já existir um usuario logado
        //if (mAuth?.currentUser != null)
          // goToMainActivity(mAuth?.currentUser)
    }

    private fun goToMainActivity(user: FirebaseUser?){
        val goToMain= Intent(this, MainActivity::class.java)
        goToMain.putExtra(USUARIO, user)
        startActivity(goToMain)
        finish()
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken: $token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    var user = mAuth!!.currentUser
                    goToMainActivity(user)
                } else {
                    Log.d(TAG, "signInWithCredential:failure", task.exception)
                    progressWheel(false)
                }
            }
    }

    private fun logaFb() {
        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logOut()
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"))
        LoginManager.getInstance().registerCallback(mCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "Facebook token: $loginResult.accessToken.token")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "Facebook onCancel.")
                    progressWheel(false)
                    Toast.makeText(this@LoginActivity, "Autenticação pelo Facebook falhou!!",
                        Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "Facebook onError. $error")
                    progressWheel(false)
                    Toast.makeText(this@LoginActivity, "Autenticação pelo Facebook falhou!!",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun progressWheel(habilita: Boolean){
        if (habilita) {
            linearProgress.visibility = View.VISIBLE
            linearForm.visibility = View.INVISIBLE
            btnFbLogin.visibility = View.INVISIBLE
            linearProgress.bringToFront()
        }else{
            linearProgress.visibility = View.GONE
            linearForm.visibility = View.VISIBLE
            btnFbLogin.visibility = View.VISIBLE
        }

    }
}

