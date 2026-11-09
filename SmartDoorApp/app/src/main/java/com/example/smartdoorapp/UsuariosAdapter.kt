package com.example.smartdoorapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuariosAdapter(
    private val listaUsuarios: List<UserModel>,
    private val onToggleRol: (UserModel) -> Unit
) : RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUsuario)
        val tvEmail: TextView = view.findViewById(R.id.tvEmailUsuario)
        val tvRut: TextView = view.findViewById(R.id.tvRutUsuario)
        val tvBadgeRol: TextView = view.findViewById(R.id.tvBadgeRol)
        val btnCambiarRol: Button = view.findViewById(R.id.btnCambiarRol)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = listaUsuarios[position]

        holder.tvNombre.text = usuario.nombre.ifEmpty { "Sin nombre" }
        holder.tvEmail.text = usuario.email
        holder.tvRut.text = "RUT: ${usuario.rut}"

        if (usuario.esAdmin()) {
            holder.tvBadgeRol.text = "Administrador"
            holder.tvBadgeRol.setBackgroundColor(Color.parseColor("#FEE4E2"))
            holder.tvBadgeRol.setTextColor(Color.parseColor("#D92D20"))
            holder.btnCambiarRol.text = "Quitar Admin"
        } else {
            holder.tvBadgeRol.text = "Usuario"
            holder.tvBadgeRol.setBackgroundColor(Color.parseColor("#F2F4F7"))
            holder.tvBadgeRol.setTextColor(Color.parseColor("#344054"))
            holder.btnCambiarRol.text = "Hacer Admin"
        }

        holder.btnCambiarRol.setOnClickListener {
            onToggleRol(usuario)
        }
    }

    override fun getItemCount(): Int = listaUsuarios.size
}
