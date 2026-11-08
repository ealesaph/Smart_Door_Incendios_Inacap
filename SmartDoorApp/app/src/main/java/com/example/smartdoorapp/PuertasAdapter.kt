package com.example.smartdoorapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PuertasAdapter(
    private val listaPuertas: List<PuertaModel>,
    private val onItemClick: (PuertaModel) -> Unit
) : RecyclerView.Adapter<PuertasAdapter.PuertaViewHolder>() {

    class PuertaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigoPuerta)
        val tvUbicacion: TextView = view.findViewById(R.id.tvUbicacionPuerta)
        val tvEstadoConexion: TextView = view.findViewById(R.id.tvEstadoConexion)
        val tvBadgeEstado: TextView = view.findViewById(R.id.tvBadgeEstado)
        val tvDetalleAlerta: TextView = view.findViewById(R.id.tvDetalleAlerta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PuertaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_puerta, parent, false)
        return PuertaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PuertaViewHolder, position: Int) {
        val puerta = listaPuertas[position]
        holder.tvCodigo.text = puerta.codigo
        holder.tvUbicacion.text = puerta.ubicacion
        holder.tvEstadoConexion.text = if (puerta.conectada) "Conectada" else "Sin conexión"
        holder.tvBadgeEstado.text = puerta.estado
        holder.tvDetalleAlerta.text = puerta.detalleAlerta

        when (puerta.estado) {
            "Abierta" -> {
                holder.tvBadgeEstado.setBackgroundColor(Color.parseColor("#FEF3F2"))
                holder.tvBadgeEstado.setTextColor(Color.parseColor("#B42318"))
            }
            "Cerrada" -> {
                holder.tvBadgeEstado.setBackgroundColor(Color.parseColor("#EDFDF5"))
                holder.tvBadgeEstado.setTextColor(Color.parseColor("#027A48"))
            }
            else -> { // Sin datos
                holder.tvBadgeEstado.setBackgroundColor(Color.parseColor("#FFFAEB"))
                holder.tvBadgeEstado.setTextColor(Color.parseColor("#B54708"))
            }
        }

        holder.itemView.setOnClickListener { onItemClick(puerta) }
    }

    override fun getItemCount(): Int = listaPuertas.size
}