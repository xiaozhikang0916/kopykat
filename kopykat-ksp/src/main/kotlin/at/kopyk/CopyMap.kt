package at.kopyk

import at.kopyk.poet.addParameter
import at.kopyk.poet.addReturn
import at.kopyk.poet.append
import at.kopyk.poet.asTransformLambda
import at.kopyk.utils.TypeCategory.Known.Data
import at.kopyk.utils.TypeCategory.Known.Sealed
import at.kopyk.utils.TypeCategory.Known.Value
import at.kopyk.utils.TypeCompileScope
import at.kopyk.utils.addGeneratedMarker
import at.kopyk.utils.baseName
import at.kopyk.utils.fullName
import at.kopyk.utils.lang.joinWithWhen
import at.kopyk.utils.lang.mapRun
import at.kopyk.utils.lang.onEachRun
import at.kopyk.utils.typeCategory
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toKModifier

internal val TypeCompileScope.copyMapFunctionKt: FileSpec
  get() = buildFile(fileName = target.append("CopyMap").reflectionName()) {
    val parameterized = target.parameterized
    addGeneratedMarker()
    addInlinedFunction(name = "copyMap", receives = parameterized, returns = parameterized) {
      visibility.toKModifier()?.let { addModifiers(it) }
      properties
        .onEachRun {
          addParameter(
            name = baseName,
            type = typeName.asTransformLambda(receiver = parameterized),
            defaultValue = "{ it }"
          )
        }
        .mapRun { "$baseName = $baseName(this, this.$baseName)" }
        .run { addReturn(repeatOnSubclasses(joinToString(), "copy")) }
    }
  }

private fun TypeCompileScope.repeatOnSubclasses(
  line: String,
  functionName: String
): String = when (typeCategory) {
  Value -> "$fullName($line)"
  Data -> "$functionName($line)"
  Sealed -> sealedTypes.joinWithWhen { "is ${it.fullName} -> $functionName($line)" }
  else -> error("Unknown type category for ${target.canonicalName}")
}
