package com.example.comidita

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale

class MenuActivity : AppCompatActivity() {
    private val cuenta = CuentaMesa()

    private val pastel = ItemMenu("Pastel de choclo", 12_000)
    private val cazuela = ItemMenu("Cazuela", 10_000)

    // formateador CLP
    private val clp: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    }

   // Cosas del UI.
    private lateinit var tvCantidadPastel: TextView
    private lateinit var tvSubtotalPastel: TextView
    private lateinit var tvCantidadCazuela: TextView
    private lateinit var tvSubtotalCazuela: TextView
    private lateinit var tvResumen: TextView
    private lateinit var swPropina: android.widget.Switch
    private lateinit var tvTotalSinPropina: TextView
    private lateinit var tvMontoPropina: TextView
    private lateinit var tvTotalConPropina: TextView
    private lateinit var btnNuevoPedido: Button
    private lateinit var btnAceptar: Button
    private lateinit var pbProcesando: android.widget.ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        connectarViews()
        enchufarEvents()
        refrescarUI()
    }

    private fun connectarViews() {
        tvCantidadPastel = findViewById(R.id.tvCantidadPastel)
        tvSubtotalPastel = findViewById(R.id.tvSubtotalPastel)
        tvCantidadCazuela = findViewById(R.id.tvCantidadCazuela)
        tvSubtotalCazuela = findViewById(R.id.tvSubtotalCazuela)
        tvResumen = findViewById(R.id.tvResumen)
        swPropina = findViewById(R.id.swPropina)
        tvTotalSinPropina = findViewById(R.id.tvTotalSinPropina)
        tvMontoPropina = findViewById(R.id.tvMontoPropina)
        tvTotalConPropina = findViewById(R.id.tvTotalConPropina)
        btnNuevoPedido = findViewById(R.id.btnNuevoPedido)
        btnAceptar = findViewById(R.id.btnAceptar)
        pbProcesando = findViewById(R.id.pbProcesando)
    }

    // Honestamente no me gusta esto de enchufar los botones de a uno, debería
    // Recorrerlos y dinamizar los eventos pero:
    // - No se si se puede
    // - No tengo tiempo
    private fun enchufarEvents() {
        findViewById<Button>(R.id.btnMasPastel).setOnClickListener {
            cuenta.agregarItem(ItemMesa(pastel, 1))
            refrescarUI()
        }
        findViewById<Button>(R.id.btnMenosPastel).setOnClickListener {
            cuenta.quitarItem(pastel.nombre, 1)
            refrescarUI()
        }

        findViewById<Button>(R.id.btnMasCazuela).setOnClickListener {
            cuenta.agregarItem(ItemMesa(cazuela, 1))
            refrescarUI()
        }
        findViewById<Button>(R.id.btnMenosCazuela).setOnClickListener {
            cuenta.quitarItem(cazuela.nombre, 1)
            refrescarUI()
        }
        // Propina 10%
        swPropina.setOnCheckedChangeListener { _, _ ->
            refrescarUI()
        }
        // Tomar otro pedido
        btnNuevoPedido.setOnClickListener {
            cuenta.reiniciar()
            swPropina.isChecked = false
            refrescarUI()
        }
        // Aceptar pedido (simulación de envío + reset)
        btnAceptar.setOnClickListener {
            // No aceptes si no hay nada
            if (cuenta.totalSinPropina() == 0) return@setOnClickListener

            // Deshabilita controles y muestra “procesando…”
            setControlesEnabled(false)
            pbProcesando.visibility = android.view.View.VISIBLE

            // Simula envío (1.5s) y luego confirma + resetea
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                pbProcesando.visibility = android.view.View.GONE
                android.widget.Toast.makeText(this, "Pedido enviado 👍", android.widget.Toast.LENGTH_SHORT).show()
                cuenta.reiniciar()
                swPropina.isChecked = false
                setControlesEnabled(true)
                refrescarUI()
            }, 1500)
        }
    }

    private fun refrescarUI() {
        val itemPastel = cuentaItem(pastel.nombre)
        val itemCazuela = cuentaItem(cazuela.nombre)
        val cantPastel = itemPastel?.cantidad ?: 0 // Estos ifs son a la Ruby
        val cantCazuela = itemCazuela?.cantidad ?: 0
        val base = cuenta.totalSinPropina()
        val conPropina = if (swPropina.isChecked) cuenta.totalConPropina(10) else base
        val propina = conPropina - base

        // subtotales por plato
        tvCantidadPastel.text = cantPastel.toString()
        tvCantidadCazuela.text = cantCazuela.toString()
        tvSubtotalPastel.text = "Subtotal: ${clp.format(itemPastel?.subtotal() ?: 0)}"
        tvSubtotalCazuela.text = "Subtotal: ${clp.format(itemCazuela?.subtotal() ?: 0)}"

        // Resumen y propina
        tvResumen.text = "Total sin propina: ${clp.format(cuenta.totalSinPropina())}"
        tvTotalSinPropina.text = "Total sin propina: ${clp.format(base)}"
        tvMontoPropina.text = "Propina (10%): ${clp.format(propina)}"
        tvTotalConPropina.text = "Total con propina: ${clp.format(conPropina)}"

        findViewById<Button>(R.id.btnMenosPastel).isEnabled = cantPastel > 0
        findViewById<Button>(R.id.btnMenosCazuela).isEnabled = cantCazuela > 0
        btnAceptar.isEnabled = base > 0
    }

    private fun setControlesEnabled(enabled: Boolean) {
        val cantPastel = tvCantidadPastel.text.toString().toIntOrNull() ?: 0
        val cantCazuela = tvCantidadCazuela.text.toString().toIntOrNull() ?: 0

        findViewById<Button>(R.id.btnMasPastel).isEnabled = enabled
        findViewById<Button>(R.id.btnMasCazuela).isEnabled = enabled
        findViewById<Button>(R.id.btnMenosPastel).isEnabled = enabled && cantPastel > 0
        findViewById<Button>(R.id.btnMenosCazuela).isEnabled = enabled && cantCazuela > 0

        swPropina.isEnabled = enabled
        btnNuevoPedido.isEnabled = enabled

        // Sólo habilitar "Aceptar" si hay algo que cobrar
        btnAceptar.isEnabled = enabled && (cuenta.totalSinPropina() > 0)
    }

    private fun cuentaItem(nombrePlato: String): ItemMesa? =
        cuenta.buscarItemPorNombre(nombrePlato)
}