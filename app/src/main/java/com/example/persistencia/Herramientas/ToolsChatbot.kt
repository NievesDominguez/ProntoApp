package com.example.persistencia.Herramientas

data class Tool(
    val type: String = "function",
    val function: FunctionDef
)

data class FunctionDef(
    val name: String,
    val description: String,
    val parameters: Parameters
)

data class Parameters(
    val type: String = "object",
    val properties: Map<String, Property>,
    val required: List<String> = emptyList()
)

data class Property(
    val type: String,
    val description: String
)