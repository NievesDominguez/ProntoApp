package com.example.persistencia.Herramientas

object ToolsDefinition {
    val tools = listOf(
        Tool(
            function = FunctionDef(
                name = "get_carrito",
                description = "Obtiene los productos actuales en el carrito del usuario con cantidades y precios",
                parameters = Parameters(properties = emptyMap(), required = emptyList())
            )
        ),
        Tool(
            function = FunctionDef(
                name = "get_lista_compra",
                description = "Obtiene los productos en la lista de la compra del usuario",
                parameters = Parameters(properties = emptyMap(), required = emptyList())
            )
        ),
        Tool(
            function = FunctionDef(
                name = "get_cupones_usuario",
                description = "Obtiene los códigos de cupones que el usuario tiene activos",
                parameters = Parameters(properties = emptyMap(), required = emptyList())
            )
        ),
        Tool(
            function = FunctionDef(
                name = "get_ofertas",
                description = "Obtiene las ofertas actuales disponibles en la tienda",
                parameters = Parameters(properties = emptyMap(), required = emptyList())
            )
        ),
        Tool(
            function = FunctionDef(
                name = "get_producto_info",
                description = "Obtiene información detallada de un producto por su nombre o categoría",
                parameters = Parameters(
                    properties = mapOf(
                        "consulta" to Property(
                            type = "string",
                            description = "Nombre o categoría del producto a buscar"
                        )
                    ),
                    required = listOf("consulta")
                )
            )
        ),
        Tool(
            function = FunctionDef(
                name = "calcular_total_carrito",
                description = "Calcula el precio total del carrito aplicando ofertas y cupones activos",
                parameters = Parameters(properties = emptyMap(), required = emptyList())
            )
        ),
        Tool(
            function = FunctionDef(
                name = "get_historial_compras",
                description = "Obtiene el historial de compras realizadas por el usuario (tickets anteriores)",
                parameters = Parameters(properties = emptyMap(), required = emptyList())
            )
        )
    )
}