package br.com.mobilete

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_cadastro_anuncio.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CadastroAnuncioActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA: Int = 10
        private const val RREQUEST_LOCATION: Int = 20
        private const val REQUEST_GALERIA: Int = 30
        const val TAG_CAD: String = "FirebaseLog - Salva no Banco"
        const val TAG_UP: String = "FirebaseLog - Upload"
    }

    private var fotoGaleria: Uri? = null
    private var fotoCamera: Uri? = null
    private var fotoAceita: Uri? = null
    private var anuncio : Anuncio? = null
    private lateinit var calendario: Calendar
    private lateinit var dialog: ProgressDialog

    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var loc : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_anuncio)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser
        calendario = Calendar.getInstance()

        val ano = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        dialog = ProgressDialog(this, R.style.ProgressDialogStyle) //Inicia o Progress Dialog
        dialog.setMessage("Salvando...")

        Glide.with(this)
            .load(R.drawable.food)
            .apply(RequestOptions.circleCropTransform())
            .into(findViewById<View>(R.id.imgAnuncio) as ImageView)

        txtValidade.setOnClickListener{
            val datePicker = DatePickerDialog(
                this, DatePickerDialog.OnDateSetListener {
                        view, mYear, mMoth, mDay ->
                    txtValidade.setText("$mDay/${mMoth+1}/$mYear")
            }, ano, mes, dia)
            datePicker.show()
        }

        btnFoto.setOnClickListener{
            tiraFoto()
        }

        btnGaleria.setOnClickListener{
            selecionaFoto()
        }

        btnCadastrar.setOnClickListener {
            cadastraAnuncio()
        }

        btnMapa.setOnClickListener {
            val goToMapa = Intent(this, MapaActivity::class.java)
            startActivityForResult(goToMapa, RREQUEST_LOCATION)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK){
            Glide.with(this)
                .load(fotoGaleria)
                .apply(RequestOptions.circleCropTransform())
                .into(findViewById<View>(R.id.imgAnuncio) as ImageView)
            fotoAceita = fotoGaleria
        }

        if(requestCode == RREQUEST_LOCATION && resultCode == Activity.RESULT_OK){
            val extras = data?.extras
            loc = extras?.getString(MapaActivity.EXTRA_LOCALIZACAO)
            val endereco: String? = extras?.getString(MapaActivity.EXTRA_ENDERECO)
            val lugar: String? = extras?.getString(MapaActivity.EXTRA_LUGAR)

            if (lugar != ""){
                txtMapa.setText(lugar)
            }else if (endereco  != "") {
                txtMapa.setText(endereco)
            }else if(loc != "")
                txtMapa.setText(loc)
        }

        if (requestCode == REQUEST_GALERIA && resultCode == Activity.RESULT_OK) {
            if (data != null){
                fotoCamera = data.data
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, fotoCamera)
                Glide.with(this)
                    .load(bitmap)
                    .apply(RequestOptions.circleCropTransform())
                    .into(findViewById<View>(R.id.imgAnuncio) as ImageView)
                fotoAceita = fotoCamera
            }
        }

    }

    private fun tiraFoto() {
        val tirarFoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (tirarFoto.resolveActivity(packageManager) != null) {
            val arquivoFoto = montaArquivoFoto()
            val uriFoto = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", arquivoFoto)
            tirarFoto.putExtra(MediaStore.EXTRA_OUTPUT, uriFoto)
            startActivityForResult(tirarFoto, REQUEST_CAMERA)
        } else {
            mensagemErro("Impossível tirar foto")
        }
    }

    private fun montaArquivoFoto(): File {
        val nomeArquivo = mAuth!!.uid + "_" +System.currentTimeMillis().toString()
        val diretorioArquivo = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val arquivoFoto = File.createTempFile(nomeArquivo, "jpg", diretorioArquivo)

        fotoGaleria = Uri.fromFile(arquivoFoto)

        return arquivoFoto
    }

    private fun selecionaFoto(){
        val goToGaleria = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        if (goToGaleria.resolveActivity(packageManager) != null) {
            startActivityForResult(goToGaleria, REQUEST_GALERIA)
        }
    }

    private fun cadastraAnuncio(){
        progressWheel(true)
        if (validaCampos()) {
            anuncio = Anuncio(user!!.uid,
                edtDescricao.text.toString(),
                txtValidade.text.toString(),
                edtValor.text.toString(),
                loc!!)
            criaAnuncio(anuncio!!)
        }else
            progressWheel(false)
    }

    private fun criaAnuncio(anuncio: Anuncio){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("anuncios")
        val key = ref.push().key
        ref.child(key!!).setValue(anuncio)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    Log.d(TAG_CAD, "Sucesso ${key}")
                    uploadFoto(key)
                }else{
                    Log.d(TAG_CAD, "Falhou ${task.exception}")
                    mensagemErro("Ocorreu um erro ao salvar no banco de dados!!")
                    progressWheel(false)
                }
            }
    }

    private fun uploadFoto(key: String) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val ref: StorageReference = storage.getReference("img_anuncio").child(key)
        ref.putFile(fotoAceita!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG_UP, "Sucesso")
                    mensagemErro("Salvo com sucesso")
                    limpaCampos()
                } else {
                    Log.d(TAG_UP, "Falhou ${task.exception}")
                    mensagemErro("Ocorreu um erro ao fazer upload da imagem!!")
                }
            }
    }

    private fun validaCampos(): Boolean {

        val dataAtual = java.util.Date()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
        if (fotoAceita == null){
            mensagemErro("Uma foto deve ser selecionada!!")
            return false
        }else if (edtDescricao.text.isEmpty()) {
            mensagemErro("O campo nome deve conter informação!!", edtDescricao)
            return false
        }else if (edtValor.text.isEmpty()) {
            mensagemErro("O campo valor deve conter informação!!", edtValor)
            return false
        } else if (edtValor.text.toString().toFloat() <= 0){
            mensagemErro("O valor não pode ser zero ou negativo!!", edtValor)
            return false
        } else if (txtValidade.text.isEmpty()) {
            mensagemErro("O campo validade deve conter informação!!")
            return false
        } else if (txtMapa.text.isEmpty()) {
            mensagemErro("O campo local deve conter informação!!")
            return false
        }

        val data: Date = dateFormat.parse("${txtValidade.text.toString()} 23:59")
        if(data <= dataAtual){
            mensagemErro("O validade não pode ser menor que a data atual!!")
            return false
        }
        return true
    }

    private fun mensagemErro(mensagem: String, editText: EditText){
        editText.error =mensagem
        editText.requestFocus()
    }

    private fun mensagemErro(mensagem: String){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun progressWheel(enabled: Boolean) {
        if (enabled)
            dialog.show()
        else
            dialog.dismiss()
    }

    private fun limpaCampos(){
        edtDescricao.setText("")
        txtValidade.text = ""
        edtValor.setText("")
        txtMapa.text = ""
        fotoAceita = null

        Glide.with(this)
            .load(R.drawable.food)
            .apply(RequestOptions.circleCropTransform())
            .into(findViewById<View>(R.id.imgAnuncio) as ImageView)

        progressWheel(false)
    }




}
