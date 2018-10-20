package br.com.mobilete

data class Usuario (val nome: String,
                    val email: String,
                    val telefone: String,
                    val senha: String,
                    val foto: String? = null)