package com.example.comidita

class CuentaMesa {
    private val items: MutableList<ItemMesa> = mutableListOf()

    fun agregarItem(item: ItemMesa) {
        val existente = items.find { it.itemMenu == item.itemMenu }
        if (existente != null) {
            existente.agregar(item.cantidad)
        } else {
            items.add(item)
        }
    }

    fun quitarItem(nombrePlato: String, cantidad: Int = 1) {
        val existente = items.find { it.itemMenu.nombre == nombrePlato }
        existente?.let {
            it.quitar(cantidad)
            if (it.cantidad == 0) {
                items.remove(it)
            }
        }
    }

    fun totalSinPropina(): Int {
        return items.sumOf { it.subtotal() }
    }

    fun totalConPropina(porcentaje: Int = 10): Int {
        val base = totalSinPropina()
        return base + (base * porcentaje / 100)
    }

    fun mostrarResumen(): String {
        val detalle = items.joinToString("\n") {
            "${it.itemMenu.nombre} x${it.cantidad} = \$${it.subtotal()}"
        }
        return "Resumen de la cuenta:\n$detalle\nTotal: \$${totalSinPropina()}"
    }

    fun reiniciar() {
        items.clear()
    }

    fun buscarItemPorNombre(nombrePlato: String): ItemMesa? {
        return items.find { it.itemMenu.nombre == nombrePlato }
    }
}