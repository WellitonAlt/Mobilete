package br.com.mobilete

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_cadastro_usuario.*
import android.widget.Toast
import android.util.Log
import android.widget.EditText
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class CadastroUsuarioActivity : AppCompatActivity() {

    companion object {
        const val TAG_AUTH: String = "FirebaseLog - Cria Autenticação"
        const val TAG_CAD: String = "FirebaseLog - Salva no Banco"
        const val TAG_UP: String = "FirebaseLog - Upload"
        const val REQUEST_PHOTO: Int = 200
    }

    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var usuario : Usuario? = null
    private var fotoURI: Uri? = null
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_usuario)

        //Firebase
        mAuth = FirebaseAuth.getInstance()

        dialog = ProgressDialog(this, R.style.ProgressDialogStyle) //Inicia o Progress Dialog
        dialog.setMessage("Salvando...")

        Glide.with(this)
            .load(R.drawable.person)
            .apply(RequestOptions.circleCropTransform())
            .into(findViewById<View>(R.id.imgUsuario) as ImageView)

        btnCadastrar.setOnClickListener {
            cadastraUsuaio()
        }

        txtAlteraFoto.setOnClickListener{
            selecionaFoto()
        }
    }

    private fun cadastraUsuaio(){
        progressWheel(true)
        if (validaCampos()) {
            usuario = Usuario(edtNome.text.toString(),
                edtEmail.text.toString(),
                edtTelefone.text.toString())
            criaAutenticadorEmailSenha(usuario, edtSenha.text.toString())
        }else
            progressWheel(false)
    }

    private fun validaCampos(): Boolean {

        if (edtNome.text.isEmpty()) {
            mensagemErro("O campo nome deve conter informação!!", edtNome)
            return false
        }else if (!emailValido(edtEmail.text.toString())) {
            mensagemErro("O campo email está fora do padrão!!", edtEmail)
            return false
        }else if (edtEmail.text.isEmpty()) {
            mensagemErro("O campo email deve conter informação!!", edtEmail)
            return false
        } else if (edtTelefone.text.isEmpty()) {
            mensagemErro("O campo telefone deve conter informação!!", edtTelefone)
            return false
        } else if (edtSenha.text.isEmpty()) {
            mensagemErro("O campo senha deve conter informação!!", edtSenha)
            return false
        } else if (edtSenha.text.length < 6) {
            mensagemErro("O campo senha deve conter mais de 6 carateres!!", edtSenha)
            return false
        }
        return true
    }

    private fun emailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    private fun mensagemErro(mensagem: String, editText: EditText){
        editText.error =mensagem
        editText.requestFocus()
    }

    private fun mensagemErro(mensagem: String){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun criaAutenticadorEmailSenha(usuario: Usuario?, senha: String){
        mAuth!!.createUserWithEmailAndPassword(usuario!!.email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG_AUTH, "Sucesso")
                    user = mAuth!!.currentUser
                    if(fotoURI != null)
                        uploadFoto(user!!, fotoURI!!)
                    criaUsuario(user!!, usuario)
                } else {
                    Log.d(TAG_AUTH, "Falhou ${task.exception}")
                    mensagemErro("Email já cadastrado ou invalido!!")
                    progressWheel(false)
                }
            }
    }

    private fun criaUsuario(fireUser: FirebaseUser, usuario: Usuario){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("usuario").child(fireUser.uid)
        ref.setValue(usuario)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    Log.d(TAG_CAD, "Sucesso")
                    getUsuario()
                }else{
                    Log.d(TAG_CAD, "Falhou ${task.exception}")
                    mensagemErro("Ocorreu um erro ao salvar no banco de dados!!")
                    progressWheel(false)
                }
        }
    }


    private fun selecionaFoto(){
        val goToGaleria = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        if (goToGaleria.resolveActivity(packageManager) != null) {
           startActivityForResult(goToGaleria, REQUEST_PHOTO)
        }
    }

    private fun goToMainActivity(){
        val goToMain= Intent(this, MainActivity::class.java)
        startActivity(goToMain)
        finish()
    }

    private fun progressWheel(enabled: Boolean) {
        if (enabled)
            dialog.show()
        else
            dialog.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data != null){
                fotoURI = data.data
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, fotoURI)
                Glide.with(this)
                    .load(bitmap)
                    .apply(RequestOptions.circleCropTransform())
                    .into(findViewById<View>(R.id.imgUsuario) as ImageView)
            }
        }
    }

    private fun uploadFoto(fireUser: FirebaseUser, fotoURI: Uri) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val ref: StorageReference = storage.getReference("img_usuario").child(fireUser.uid)
        ref.putFile(fotoURI)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG_UP, "Sucesso")
                } else {
                    Log.d(TAG_UP, "Falhou ${task.exception}")
                    mensagemErro("Ocorreu um erro ao fazer upload da imagem!!")
                }
            }
    }

    private fun getUsuario(){
        val preferencias = Preferencias(this)
        if (fotoURI != null)
            usuario!!.foto = fotoURI.toString()
        preferencias.setUsuario(usuario!!)
        goToMainActivity()
    }

}
