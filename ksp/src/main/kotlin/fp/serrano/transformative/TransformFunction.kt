package fp.serrano.transformative

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun transformFunction(
  packageName: String,
  klass: KSClassDeclaration,
  targetClassName: TypeName,
  typeVariables: List<TypeVariableName>,
  typeParamResolver: TypeParameterResolver,
) = buildFile(packageName = packageName, "${klass.simpleName.asString()}Transformative") {
  val properties = klass.getAllProperties()
  addFunction(
    name = "transform",
    receiver = targetClassName,
    returns = targetClassName,
    typeVariables = typeVariables,
  ) {
    val propertyStatements = properties.map { property ->
      val typeName = property.type.toTypeName(typeParamResolver)
      addParameter(
        ParameterSpec.builder(
          name = property.name,
          type = LambdaTypeName.get(parameters = arrayOf(typeName), returnType = typeName)
        ).defaultValue("{ it }").build()
      )
      when {
        typeName.extendsFrom<List<*>>() -> {
          addListParameter(typeName, property)
          "${property.name} = ${property.name}(this.${property.name}).map(${property.name}Each)"
        }

        typeName.extendsFrom<Map<*, *>>() -> {
          addMapParameter(typeName, property)
          "${property.name} = ${property.name}(this.${property.name}).mapValues(${property.name}Each)"
        }

        else -> "${property.name} = ${property.name}(this.${property.name})"
      }
    }
    addCode("return $targetClassName(${propertyStatements.joinToString()})")
  }
}

private fun FunSpec.Builder.addMapParameter(
  typeName: TypeName,
  property: KSPropertyDeclaration
) {
  val (keyType, valueType) = typeName.typeArguments!!
  addParameter(
    ParameterSpec.builder(
      name = property.name + "Each",
      type = LambdaTypeName.get(
        parameters = arrayOf(
          Map.Entry::class.asTypeName().parameterizedBy(keyType, valueType)
        ), returnType = valueType
      )
    ).defaultValue("{ it.value }").build()
  )
}

private fun FunSpec.Builder.addListParameter(
  typeName: TypeName,
  property: KSPropertyDeclaration
) {
  val listType = typeName.typeArguments!!.first()
  addParameter(
    ParameterSpec.builder(
      name = property.name + "Each",
      type = LambdaTypeName.get(parameters = arrayOf(listType), returnType = listType)
    ).defaultValue("{ it }").build()
  )
}

private inline fun <reified T> TypeName.extendsFrom(): Boolean =
  this is ParameterizedTypeName && rawType == T::class.asTypeName()

private val TypeName.typeArguments get() = (this as? ParameterizedTypeName)?.typeArguments

private val KSDeclaration.name get() = simpleName.asString()
