package fp.serrano.transformative

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

class Transformative(private val codegen: CodeGenerator, private val logger: KSPLogger): SymbolProcessor {
  companion object {
    const val ANNOTATION_NAME = "fp.serrano.transformative"
  }

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver
      .getSymbolsWithAnnotation(ANNOTATION_NAME)
      .filterIsInstance<KSClassDeclaration>()
      .forEach(::processClass)

    return emptyList()
  }

  private fun processClass(klass: KSClassDeclaration) {
    if (Modifier.DATA !in klass.modifiers || klass.primaryConstructor == null) {
      logger.error(klass.notDataClassErrorMessage, klass)
      return
    }

    val writer =
      codegen.createNewFile(
        Dependencies(aggregating = true, *listOfNotNull(klass.containingFile).toTypedArray()),
        klass.packageName.toString(), "${klass.simpleName}Transformative"
      ).writer()
    writer.write(generateTransform(klass))
    writer.flush()
  }

  private fun generateTransform(klass: KSClassDeclaration): String {
    val simpleTyArgs = when {
      klass.typeParameters.isEmpty() -> ""
      else -> klass.typeParameters.joinToString(prefix = "<", separator = ", ", postfix = ">") { param ->
        param.name.asString()
      }
    }
    val fullTyArgs = when {
      klass.typeParameters.isEmpty() -> ""
      else -> klass.typeParameters.joinToString(prefix = "<", separator = ", ", postfix = ">") { param ->
        when {
          param.bounds.toList().isEmpty() -> param.name.asString()
          else -> {
            val bounds = param.bounds.joinToString(separator = ", ") { bound ->
              bound.resolve().qualifiedString()
            }
            "${param.name.asString()} : $bounds"
          }
        }
      }
    }

    val originalParams = klass.primaryConstructor!!.parameters.map {
      it.name!!.asString() to it.type.resolve().qualifiedString()
    }
    val args = originalParams.joinToString(separator = ", ") { (name, type) ->
      "${name}: ($type) -> $type = { it }"
    }
    val body = originalParams.joinToString(separator = ", ") { (name, _) ->
      "$name = $name(this.$name)"
    }

    val fullType = "${klass.simpleName.asString()}$simpleTyArgs"

    return "fun $fullTyArgs $fullType.transform($args): $fullType = this.copy($body)"
  }
}

internal fun KSTypeArgument.qualifiedString(): String = when (val ty = type?.resolve()) {
  null -> toString()
  else -> ty.qualifiedString()
}

internal fun KSType.qualifiedString(): String = when (declaration) {
  is KSTypeParameter -> {
    val n = declaration.simpleName.asString()
    if (isMarkedNullable) "$n?" else n
  }
  else -> when (val qname = declaration.qualifiedName?.asString()) {
    null -> toString()
    else -> {
      val withArgs = when {
        arguments.isEmpty() -> qname
        else -> "$qname<${arguments.joinToString(separator = ", ") { it.qualifiedString() }}>"
      }
      if (isMarkedNullable) "$withArgs?" else withArgs
    }
  }
}