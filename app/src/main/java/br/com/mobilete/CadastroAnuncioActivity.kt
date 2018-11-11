package br.com.mobilete

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
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
import br.com.mobilete.AppConstants.ANUNCIO
import br.com.mobilete.AppConstants.REQUEST_CAMERA
import br.com.mobilete.AppConstants.REQUEST_GALERIA
import br.com.mobilete.AppConstants.REQUEST_LOCATION
import br.com.mobilete.AppConstants.TAG_CAD
import br.com.mobilete.AppConstants.TAG_UP
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_cadastro_anuncio.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CadastroAnuncioActivity : AppCompatActivity() {

    private var fotoCamera: Uri? = null
    private var fotoAceita: Uri? = null
    private var anuncio : Anuncio? = null
    private var edicao: Boolean = false
    private var mudouFoto: Boolean = false
    private lateinit var calendario: Calendar
    private lateinit var dialog: ProgressDialog

    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseRef: DatabaseReference
    private var loc : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_anuncio)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser
        database = FirebaseDatabase.getInstance()
        databaseRef = database.getReference("anuncios").child(user!!.uid)

        calendario = Calendar.getInstance()

        dialog = ProgressDialog(this, R.style.ProgressDialogStyle) //Inicia o Progress Dialog
        dialog.setMessage("Salvando...")

        val ano = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        anuncio = intent.getSerializableExtra(ANUNCIO) as Anuncio?
        if (anuncio != null)
            carregaDados()

        carregaFoto()

        txtValidade.setOnClickListener{
            val datePicker = DatePickerDialog(
                this, DatePickerDialog.OnDateSetListener {
                        view, mYear, mMoth, mDay ->
                    txtValidade.text = "$mDay/${mMoth+1}/$mYear"
            }, ano, mes, dia)
            datePicker.show()
        }

        btnFoto.setOnClickListener{
            if (cameraPermission())
                tiraFoto()
        }

        btnGaleria.setOnClickListener{
            if (readExternalPermission())
                selecionaFoto()
        }

        btnCadastrar.setOnClickListener {
            if(edicao)
                editaAnuncio()
            else
                cadastraAnuncio()
        }

        btnMapa.setOnClickListener {
            if (locationPermission()) {
                val goToMapa = Intent(this, MapaActivity::class.java)
                startActivityForResult(goToMapa, REQUEST_LOCATION)
            }
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
            mudouFoto = true
            fotoAceita = fotoCamera
            carregaFoto()
        }

        if(requestCode == REQUEST_LOCATION && resultCode == Activity.RESULT_OK){
            val extras = data?.extras
            loc = extras?.getString(MapaActivity.EXTRA_LOCALIZACAO)
            val endereco: String? = extras?.getString(MapaActivity.EXTRA_ENDERECO)
            val lugar: String? = extras?.getString(MapaActivity.EXTRA_LUGAR)

            when {
                lugar != "" -> txtMapa.text = lugar
                endereco  != "" -> txtMapa.text = endereco
                loc != "" -> txtMapa.text = loc
            }
        }

        if (requestCode == REQUEST_GALERIA && resultCode == Activity.RESULT_OK) {
            if (data != null){
                mudouFoto = true
                fotoAceita = data.data
                carregaFoto()
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

        fotoCamera = Uri.fromFile(arquivoFoto)

        return arquivoFoto
    }

    private fun selecionaFoto(){
        val selecionaFoto = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        if (selecionaFoto.resolveActivity(packageManager) != null) {
            startActivityForResult(selecionaFoto, REQUEST_GALERIA)
        }
    }

    private fun cadastraAnuncio(){
        progressWheel(true)
        if (validaCampos()) {
            anuncio = Anuncio(
                "",
                user!!.uid,
                edtDescricao.text.toString(),
                txtValidade.text.toString(),
                edtValor.text.toString(),
                loc!!)
            getKey()
        }else
            progressWheel(false)
    }

    private fun editaAnuncio(){
        progressWheel(true)
        if (validaCampos()) {
            anuncio!!.descricao = edtDescricao.text.toString()
            anuncio!!.validade = txtValidade.text.toString()
            anuncio!!.valor = edtValor.text.toString()
            if(loc != null && loc != anuncio!!.localizacao)
                anuncio!!.localizacao = loc!!
            if(mudouFoto) {
                uploadFoto(anuncio!!.id)
                return
            }
            salvaAnuncio(anuncio!!.id)
        }else
            progressWheel(false)
    }

    private fun getKey(){
        val key = databaseRef.push().key
        anuncio!!.id = key!!
        uploadFoto(key)

    }

    private fun uploadFoto(key: String) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val ref: StorageReference = storage.getReference("img_anuncio").child(key)
        ref.putFile(fotoAceita!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG_UP, "Sucesso")
                    getStorageUrl(key, ref)
                } else {
                    Log.d(TAG_UP, "Falhou ${task.exception}")
                    mensagemErro("Ocorreu um erro ao fazer upload da imagem!!")
                }
            }
    }

    private fun getStorageUrl(key: String, storageRef: StorageReference){
       storageRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG_UP, "Sucesso")
                anuncio!!.foto = task.result.toString()
                salvaAnuncio(key)
            } else {
                Log.d(TAG_UP, "Falhou ${task.exception}")
            }
        }
    }

    private fun salvaAnuncio(key: String){
        databaseRef.child(key).setValue(anuncio)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    Log.d(TAG_CAD, "Sucesso $key")
                    mensagemErro("Anuncio salvo com sucesso")
                    limpaCampos()
                }else{
                    Log.d(TAG_CAD, "Falhou ${task.exception}")
                    mensagemErro("Ocorreu um erro ao salvar no banco de dados!!")
                    progressWheel(false)
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

        val data: Date = dateFormat.parse("${txtValidade.text} 23:59")
        if(data <= dataAtual){
            mensagemErro("O validade não pode ser menor que a data atual!!")
            return false
        }
        return true
    }

    private fun mensagemErro(mensagem: String, editText: EditText){
        editText.error = mensagem
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
        if (edicao)
            finish()

        edtDescricao.setText("")
        txtValidade.text = ""
        edtValor.setText("")
        txtMapa.text = ""
        fotoAceita = null

        carregaFoto()

        progressWheel(false)
    }

    private fun carregaFoto(){
        GlideApp.with(this)
            .load(fotoAceita)
            .placeholder(R.drawable.food)
            .dontAnimate()
            .into(findViewById<View>(R.id.imgAnuncio) as ImageView)
    }

    private fun carregaDados(){
        val latLongStr = anuncio!!.localizacao
            .replace("(","")
            .replace(")","")
            .replace("lat/lng: ","")
            .split(",")

        val latitude = latLongStr[0].toDouble()
        val longitude = latLongStr[1].toDouble()
        val latLong = LatLng(latitude, longitude)

        edtDescricao.setText(anuncio!!.descricao)
        txtValidade.text = anuncio!!.validade
        edtValor.setText(anuncio!!.valor)
        txtMapa.text = getEndereco(latLong)
        fotoAceita = Uri.parse(anuncio!!.foto)

        btnCadastrar.text = "Atualizar"
        edicao = true

    }

    private fun getEndereco(latLng: LatLng): String {
        val geocoder = Geocoder(this)
        val enderecos: List<Address>?
        val endereco: Address?

        var enderecoFinal = ""
        try {
            enderecos = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != enderecos && !enderecos.isEmpty()) {
                endereco = enderecos[0]
                for (i in 0..endereco.maxAddressLineIndex) {
                    enderecoFinal += if (i == 0) endereco.getAddressLine(i) else "\n" + endereco.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e(AppConstants.TAG_MAP, e.localizedMessage)
        }
        if (enderecoFinal == "")
            enderecoFinal = "Endereço indiponivel no momento $latLng"
        return enderecoFinal
    }

    private fun readExternalPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), AppConstants.REQUEST_EXTERNAL)
            return false
        }
        return true
    }

    private fun cameraPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), AppConstants.REQUEST_CAMERA)
            return false
        }
        return true
    }

    private fun locationPermission() : Boolean {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), AppConstants.REQUEST_LOCATION)
            return false
        }
        return true
    }

}
