package br.com.mobilete

import java.io.Serializable
data class Anuncio (var id: String = "",
                    var usuario: String = "",
                    var descricao: String = "",
                    var validade: String = "",
                    var valor: String = "",
                    var localizacao: String = "",
                    var foto: String = "") : Serializable