package com.example.smartdoorapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AuditoriaAdapter(
    private val listaEventos: List<AuditoriaModel>
) : RecyclerView.Adapter<AuditoriaAdapter.AuditoriaViewHolder>() {

    class AuditoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipoBadge: TextView = view.findViewById(R.id.tvTipoBadge)
        val tvFechaEvento: TextView = view.findViewById(R.id.tvFechaEvento)
        val tvNombreEvento: TextView = view.findViewById(R.id.tvNombreEvento)
        val tvDetalleUsuario: TextView = view.findViewById(R.id.tvDetalleUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditoriaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_auditoria, parent, false)
        return AuditoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AuditoriaViewHolder, position: Int) {
        val evento = listaEventos[position]
        holder.tvFechaEvento.text = evento.fecha
        holder.tvNombreEvento.text = evento.evento
        holder.tvDetalleUsuario.text = "Usuario: ${evento.usuario} | RUT: ${evento.rut}"
        holder.tvTipoBadge.text = evento.tipo

        when (evento.tipo.uppercase()) {
            "EMERGENCIA" -> {
                holder.tvTipoBadge.setBackgroundColor(Color.parseColor("#FEF3F2"))
                holder.tvTipoBadge.setTextColor(Color.parseColor("#D92D20"))
            }
            "SEGURIDAD" -> {
                holder.tvTipoBadge.setBackgroundColor(Color.parseColor("#EFF8FF"))
                holder.tvTipoBadge.setTextColor(Color.parseColor("#175CD3"))
            }
            else -> { // INFO
                holder.tvTipoBadge.setBackgroundColor(Color.parseColor("#F2F4F7"))
                holder.tvTipoBadge.setTextColor(Color.parseColor("#344054"))
            }
        }
    }

    override fun getItemCount(): Int = listaEventos.size
}