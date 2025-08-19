package com.example.comidita

data class ItemMesa(
    val itemMenu: ItemMenu,
    private var _cantidad: Int = 0
) {
    val cantidad: Int
        get() = _cantidad

    fun agregar(n: Int = 1) {
        require(n > 0) { "Solo positivos" }
        _cantidad += n
    }

    fun quitar(n: Int = 1) {
        require(n > 0) { "Solo positivos" }
        _cantidad = maxOf(0, _cantidad - n)
    }

    fun subtotal(): Int {
        return itemMenu.precio * _cantidad
    }
}