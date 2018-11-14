package br.com.mobilete

import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_anuncio.*
import java.io.IOException

class AnuncioActivity : AppCompatActivity(), OnMapReadyCallback {

    private var fotoAceita: Uri? = null
    private var anuncio : Anuncio? = null
    private lateinit var latLong : LatLng
    private lateinit var mapFragment : SupportMapFragment
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anuncio)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        anuncio = intent.getSerializableExtra(AppConstants.ANUNCIO) as Anuncio?
        if (anuncio != null)
            carregaDados()
        else
            mensagemErro("Aconteceu um erro ao carregar os dados!!")

        carregaFoto()

        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragmentAnuncio) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        colocaMarkerNoMap(latLong)

    }

    private fun colocaMarkerNoMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        mMap.addMarker(markerOptions)
    }

    private fun mensagemErro(mensagem: String, editText: EditText){
        editText.error = mensagem
        editText.requestFocus()
    }

    private fun mensagemErro(mensagem: String){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
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
        latLong = LatLng(latitude, longitude)

        txtDescricao.text = (anuncio!!.descricao)
        txtValidade.text = anuncio!!.validade
        txtValor.text = ("%.2f".format(anuncio!!.valor.toFloat()))
        fotoAceita = Uri.parse(anuncio!!.foto)

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

}
