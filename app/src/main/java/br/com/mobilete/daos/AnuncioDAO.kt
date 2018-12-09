package br.com.mobilete.daos

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.util.Log
import br.com.mobilete.callbacks.AnuncioCallback
import br.com.mobilete.entities.Anuncio
import br.com.mobilete.entities.AppConstants
import br.com.mobilete.entities.AppConstants.PATH_ANUNCIO
import br.com.mobilete.entities.AppConstants.PATH_IMG_ANUNCIO
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AnuncioDAO(var anuncio: Anuncio? = null,
                 val fireUser: FirebaseUser,
                 var fotoUri: Uri? = null) : AppCompatActivity() {

    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseRef: DatabaseReference = database.getReference(PATH_ANUNCIO)
    private var listaAnuncios: MutableList<Anuncio> = mutableListOf()

    fun setKey(){
        val key = databaseRef.push().key
        anuncio!!.id = key!!
    }


    fun uploadFoto(callback: AnuncioCallback) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val ref: StorageReference = storage.getReference(PATH_IMG_ANUNCIO).child(anuncio!!.id)
        ref.putFile(fotoUri!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(AppConstants.TAG_UP, "Sucesso")
                    getStorageUrl(callback, ref)
                } else {
                    Log.d(AppConstants.TAG_UP, "Falhou ${task.exception}")
                    callback.onError("Ocorreu um erro ao fazer upload da imagem!!")
                }
            }
    }

    fun getStorageUrl(callback: AnuncioCallback, storageRef: StorageReference){
        storageRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(AppConstants.TAG_UP, "Sucesso")
                anuncio!!.foto = task.result.toString()
                callback.onCallbackUploadFoto(task.result.toString())
            } else {
                Log.d(AppConstants.TAG_UP, "Falhou ${task.exception}")
            }
        }
    }

    fun salvaAnuncio(callback: AnuncioCallback){
        databaseRef.child(fireUser.uid).child(anuncio!!.id).setValue(anuncio)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    Log.d(AppConstants.TAG_CAD, "Sucesso")
                    callback.onCallbackAnuncioDao()
                }else{
                    Log.d(AppConstants.TAG_CAD, "Falhou ${task.exception}")
                    callback.onError("Ocorreu um erro ao salvar no banco de dados!!")
                }
            }
    }

    fun getAnuncios(callback: AnuncioCallback) {
        databaseRef.child(fireUser.uid).addValueEventListener( //Coleta os dados do banco
            object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    listaAnuncios.clear()
                    p0.children.mapNotNullTo(listaAnuncios) {
                        it.getValue<Anuncio>(Anuncio::class.java)
                    }
                    Log.d(AppConstants.TAG_ANUNCIO, "Anuncios recuperados")
                    callback.onCallbackAnuncios(listaAnuncios)
                }

                override fun onCancelled(dataSnapshot: DatabaseError) {
                    Log.d(AppConstants.TAG_ANUNCIO, "Anuncios não recuperados")
                }
            }
        )
    }

    fun getTodosAnuncios(callback: AnuncioCallback){
        databaseRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    listaAnuncios.clear()

                    val newRef: MutableIterable<DataSnapshot> = p0.children

                    for(p in newRef) {
                        p.children.mapNotNullTo(listaAnuncios) {
                            it.getValue<Anuncio>(Anuncio::class.java)
                        }
                        Log.d(AppConstants.TAG_ANUNCIO, p.toString())
                    }
                    var aux: MutableList<Anuncio> = mutableListOf()
                    for(anuncio in listaAnuncios){
                        if(anuncio.usuario != fireUser!!.uid)
                            aux.add(anuncio)
                    }
                    listaAnuncios = aux
                    callback.onCallbackAnuncios(listaAnuncios)
                }

                override fun onCancelled(dataSnapshot: DatabaseError) {
                    Log.d(AppConstants.TAG_ANUNCIO, "Anuncios não recuperados")
                }
            }
        )
    }

    fun deletaAnuncio(callback: AnuncioCallback){
        val refDatabase: DatabaseReference = databaseRef.child(fireUser.uid).child(anuncio!!.id)
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val refDataStore: StorageReference = storage.getReference(PATH_IMG_ANUNCIO).child(anuncio!!.id)
        refDatabase.setValue(null).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(AppConstants.TAG_ANUNCIO_DELETE, "Sucesso")
                callback.onCallbackAnuncioDao()
            } else {
                Log.d(AppConstants.TAG_ANUNCIO_DELETE, "Falhou ${task.exception}")
                callback.onError("Ocorreu um erro ao excluir o anuncio!!")
            }
        }
        refDataStore.delete().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(AppConstants.TAG_ANUNCIO_FOTO, "Sucesso")
            } else {
                Log.d(AppConstants.TAG_ANUNCIO_FOTO, "Falhou ${task.exception}")
            }
        }
    }

}
